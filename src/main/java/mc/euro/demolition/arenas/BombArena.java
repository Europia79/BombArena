package mc.euro.demolition.arenas;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.util.SerializerUtil;
import mc.euro.demolition.BombPlugin;
import mc.euro.demolition.objects.CompassHandler;
import mc.euro.demolition.timers.DetonationTimer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

/**
 * BombArena = Sabotage game-mode from Call of Duty; Also known as Demolition in SOCOM: US Navy Seals.
 * 
 * <pre>
 * default BombBlock = TNT 46
 *
 * Listen for 
 * onMatchStartEvent() - move functionality to onBombSpawn() ?
 * onBombSpawn() - create a hologram and point the compass to the bomb.
 * onBombPickup() - set HAT & point compass to the base. 
 * onBombCarrierLeave() - Did they log out or leave the arena with the bomb ?
 * onBombCarrierDeath() - Were they defusing ? If so, cancel their timer.
 * onBombDrop() - Make sure that other events don't cancel the bombDropEvent.
 * onBombDespawn() - Make sure that a bomb is always available: via cancel or respawn.
 * onBombPlace() - trigger onBombPlant() if close enough. 
 * onBombPlantDefuse() - InventoryOpenEvent: Are they trying to plant or defuse ?
 * 
 * </pre>
 */
public class BombArena extends EodArena {
    
    private final AtomicInteger hologramID = new AtomicInteger();
    
    Map<Integer, Location> teamBases = new HashMap<Integer, Location>(); // key = teamID
    
    /**
     * Pre-BattleArena v3.9.8 constructor to support backwards compatibility.
     */
    public BombArena() {
        super((BombPlugin) Bukkit.getPluginManager().getPlugin("BombArena"));
    }
    
    /**
     * This constructor requires BattleArena v3.9.8+.
     */
    public BombArena(BombPlugin plugin) {
        super(plugin); // I love this line of code :P
    }
    
    /**
     * This method sets plugin.carriers, compass direction, and gives a hat. <br/><br/>
     * 
     * <pre>
     * 1. Give the bomb carrier a hat so that other players know WHO has the bomb.
     * 2. Set the compass direction for all players to point to the objective:
     *    - This helps attackers find the opponents base.
     *    - This helps defenders find their own base.
     * </pre>
     * @param e PlayerPickupItemEvent: checks to see if they picked up the bomb item, or a different item.
     */
    @ArenaEventHandler
    public void onBombPickup(PlayerPickupItemEvent e) {
        plugin.debug.log("onBombPickup() called");
        
        if (e.getItem().getItemStack().getType() != plugin.getBombBlock()) return;

        if (carrier == null) {
            carrier = e.getPlayer().getName();
            e.getPlayer().getInventory().setHelmet(new ItemStack(plugin.getBombBlock()));
            ArenaTeam team2 = getOtherTeam(e.getPlayer());
            int teamID = team2.getId();
            Location base_loc = teamBases.get(teamID);
            setCompass(base_loc);
            createBaseHologram(base_loc);
            msgAll(team2.getPlayers(), "Hurry back to defend your base from being destroyed!");
            msgAll(getMatch().getArena().getTeam(e.getPlayer()).getPlayers(),
                    "Your team has the bomb! Follow your compass to find the other teams base.");
        } else {
            e.setCancelled(true);
            e.getPlayer().sendMessage(
                    "There can only be ONE bomb per Match. "
                    + carrier + " currently has the bomb.");
            e.getItem().remove();
        }

    } // END OF PlayerPickupItemEvent
    
    @Override
    public boolean isValidBombPlace(BlockPlaceEvent e) {
        if (e.getBlockPlaced().getType() == plugin.getBombBlock()) {
            e.setCancelled(true);
            e.getPlayer().updateInventory();
            return true;
        }
        return false;
    }
    
    @Override
    public boolean isPlayerInsideBase(Player player) {
        int teamID = getOtherTeam(player).getId();
        if (teamBases.get(teamID).distance(player.getLocation()) <= plugin.getBaseRadius()) {
            return true;
        }
        return false;
    }
    
    /**
     * Defuse Conditions:.
     * <pre>
     * 1. A DetonationTimer exists = running
     * 2. The Defusers team is different than the carrier's team.
     * 3. The Defuser is at his own base.
     * - If so, start a DefuseTimer for this player.
     * - If not, check the plant conditions.
     * </pre>
     */
    @Override 
    public boolean isPlayerDefusing(InventoryOpenEvent e) {
        
        Player eplayer = (Player) e.getPlayer();
        int eTeamID = getTeam(eplayer).getId();
        int cTeamID = getTeam(Bukkit.getPlayer(carrier)).getId();
        
        DetonationTimer detTimer = getDetonationTimer();
        plugin.debug.log("*** DEFUSE CONDITIONS: ***");
        plugin.debug.log("defuseTimer is running = " + (detTimer != null));
        plugin.debug.log("eTeamID != cTeamID : " + (eTeamID != cTeamID));
        // Two different ways to check the same thing:
        boolean isPlayerAtTheirOwnBase = teamBases.get(eTeamID).distance(eplayer.getLocation()) < 6;
        boolean isPlayerAtBombLocation = (detTimer == null) ? false : 
                eplayer.getLocation().distance(detTimer.getBombLocation()) < 6;
        plugin.debug.log("Are they at the correct base ? (where the bomb was planted) : " + isPlayerAtBombLocation);
        plugin.debug.log("Are they at their own base ?" + isPlayerAtTheirOwnBase);
        
        if (detTimer != null
                && eTeamID != cTeamID
                && isPlayerAtBombLocation) {
            return true;
        }
        return false;
    }
    
    /**
     * Plant Conditions:.
     * <pre>
     * 1. The player that triggered this event is in fact the bomb carrier.
     * 2. The player is NOT at his own base.
     * 3. The DetonationTimer does not exist yet.
     * - If so, start the Plant Timer: It takes about 5 to 10 seconds to plant the bomb.
     * - If not, cancel the InventoryOpenEvent.
     * </pre>
     */
    @Override
    public boolean isPlayerPlanting(InventoryOpenEvent e) {
        
        Player eplayer = (Player) e.getPlayer();
        int eTeamID = getTeam(eplayer).getId();
        DetonationTimer detTimer = getDetonationTimer();
        
        plugin.debug.log("*** PLANT CONDITIONS: ***");
        plugin.debug.log("carrier != null : " + (carrier != null));
        plugin.debug.log("EventPlayer.getName().equals(carrier) : " + eplayer.getName().equalsIgnoreCase(carrier));
        boolean areTheyAwayFromHomeBase = teamBases.get(eTeamID).distance(eplayer.getLocation()) > 9;
        plugin.debug.log("Are they sufficiently away from their own base ?" + areTheyAwayFromHomeBase);
        plugin.debug.log("The detonation timer is NOT already running : " + (detTimer == null));
        if (carrier != null 
                && eplayer.getName().equalsIgnoreCase(carrier) 
                && teamBases.get(eTeamID).distance(eplayer.getLocation()) > 9
                && detTimer == null) {
            return true;
        }
        return false;
    }
    
    /**
     * onStart() is called at the start of a Match. <br/><br/>
     * 
     * Order: onStart(), onComplete(), onFinish() 
     */
    @Override
    public void onStart() {
        super.onStart();
        int matchID = getMatch().getID();
        plugin.debug.log("onStart matchID = " + matchID);
        plugin.debug.log("Map<> bases.values() = " + teamBases.values());
        this.carrier = null;
        resetBases();
        Set<ArenaPlayer> allplayers = getMatch().getPlayers();
        for (ArenaPlayer p : allplayers) {
            if (!p.getInventory().contains(Material.COMPASS)) {
                p.getInventory().addItem(new ItemStack(Material.COMPASS));
            }
        }
        // loadLocations();
        assignBases(getMatch().getTeams());
        compass = new CompassHandler(this);
        compass.runTaskTimer(plugin, 0L, 120L);
        plugin.debug.log("Map<> bases.values() = " + teamBases.values());
    }
    
    private void loadLocations() {
        if (getSavedBases().isEmpty()) {
            String arenaName = getName();
            Collection<Location> list = getOldBases(arenaName).values();
            for (Location loc : list) {
                addSavedBase(loc);
            }
        }
    }
    
    /**
     * Used by BombPlugin->updateBasesYml() to transfer data from bases.yml to arenas.yml.
     * @param arenaName 
     * @return Map of key-value pairs, where the key is an incremental ordinal number starting at 1.
     * @deprecated Labeled as deprecated to prevent accidental usage.
     */
    @Deprecated
    public Map<Integer, Location> getOldBases(String arenaName) {
        // bases.yml
        // PATH = "{arenaName}.{index}"
        String path = arenaName;
        Map<Integer, Location> temp = new HashMap<Integer, Location>();
        if (plugin.basesYml.getConfigurationSection(path) != null
                && plugin.basesYml.getConfigurationSection(path).getKeys(false) != null
                && plugin.basesYml.getConfigurationSection(path).getKeys(false).size() >= 2) {
            ConfigurationSection cs = plugin.basesYml.getConfigurationSection(path);
            for (String key : cs.getKeys(false)) {
                String sloc = cs.getString(key);
                Location loc = SerializerUtil.getLocation(sloc);
                Location base_loc = plugin.getExactLocation(loc);
                temp.put(Integer.valueOf(key), base_loc);
            }
            plugin.debug.log("getBases(String arenaName) size of returning List = " + temp.size());
            return temp;
        }
        plugin.getLogger().severe("BombArena:getBases(String ArenaName) has failed to return a List of Locations.");
        return temp;
    }
    
    /**
     * This is called from onStart() and assigns both Teams to a base. <br/><br/>
     * 
     * Since teams are assigned to a base, we can use this information to prevent them from 
     * trying to destroy their own base. <br/><br/>
     * 
     * And force them to destroy the other teams base. <br/><br/>
     * 
     * @param bothTeams - Assign bases for what teams ?
     */
    public void assignBases(List<ArenaTeam> bothTeams) {
        for (ArenaTeam t : bothTeams) {
            plugin.debug.log("onStart()::assignBases() TEAM ID = " + t.getId());
        }
        plugin.debug.log("BombArena::assignBases()");
        plugin.debug.log("arena name = " + getMatch().getArena().getName());
        teamBases.clear();
        List<Location> locations = getCopyOfSavedBases();
        if (locations.isEmpty() || locations.size() < 2) {
            msgAll(getMatch().getPlayers(), "[BombArena]" + getName()
                    + " has stopped because no bases were found" 
                    + " inside arenas.yml");
            msgAll(getMatch().getPlayers(), "[BombArena] "
                    + "To properly setup arenas, " 
                    + "please use the command: /bomb addbase <ArenaName>");
            plugin.getLogger().warning("[BombArena] No bases found inside arena.yml: "
                    + "Please use the cmd: /bomb addbase <ArenaName>");
            getMatch().cancelMatch();
            return;
        }
        Location ONE = locations.get(0);
        Location TWO = locations.get(1);
        
        for (ArenaTeam t : bothTeams) {
            plugin.debug.log("teamOne = " + t.getName());
            Set<Player> playerzSet = t.getBukkitPlayers();
            Player playerOne = null;
            // Use the 1st player on the Team to assign the base
            // for the whole team.
            for (Player first : playerzSet) {
                playerOne = first;
                break;
            }
            
            
            // COMPARE THESE TWO POINTS WITH THE PLAYER TO DETERMINE
            //        WHICH BASE IS CLOSER.
            double onedistance;
            double twodistance;
            // try & catch VirtualPlayers 
            // which are used to make testing arenas easier.
            try {
                onedistance = playerOne.getLocation().distance(ONE);
                twodistance = playerOne.getLocation().distance(TWO);
            } catch (NullPointerException ex) {
                plugin.getLogger().warning("Possible VirtualPlayer found inside BombArena.");
                continue;
            }
            
            int teamID = t.getId();
            if (onedistance < twodistance) {
                teamBases.put(teamID, plugin.getExactLocation(ONE));
                teamID = getOtherTeam(playerOne).getId();
                teamBases.put(teamID, plugin.getExactLocation(TWO));
                break;
            } else if (onedistance > twodistance) {
                teamBases.put(teamID, plugin.getExactLocation(TWO));
                teamID = getOtherTeam(playerOne).getId();
                teamBases.put(teamID, plugin.getExactLocation(ONE));
                break;
            } else if (onedistance == twodistance) {
                plugin.getLogger().warning("Could NOT assign bases because " 
                        + "the player's spawn is equi-distance to both.");
                plugin.getLogger().info("Please change the spawn locations " 
                        + "for the teams in the bomb arena.");
            }
        }
        plugin.debug.log("Number of Team bases: bases.size() = " + teamBases.size());
        if (teamBases.size() != 2) {
            plugin.debug.log("BombArena must have 2 teams & 2 bases (1 base per team)!");
        }
    }  // END OF assignBases()    
    
    @Override
    public void removeHolograms() {
        plugin.holograms().removeHologram(hologramID.get());
        this.hologramID.set(0);
    }
    
    protected void createBaseHologram(Location loc) {
        Set<Location> set = new HashSet<Location>();
        set.add(loc);
        createBaseHologram(set);
    }
    
    @Override
    protected void createBaseHologram(Set<Location> locations) {
        for (Location loc : locations) {
            removeHolograms();
            int id = plugin.holograms().createBaseHologram(loc);
            this.hologramID.set(id);
        }
    }

    @Override
    protected AtomicInteger createBombHologram(Location loc) {
        removeHolograms();
        int id = plugin.holograms().createBombHologram(loc);
        hologramID.set(id);
        return hologramID;
    }
}