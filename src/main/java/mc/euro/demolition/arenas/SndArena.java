package mc.euro.demolition.arenas;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import mc.alk.arena.events.matches.MatchResultEvent;
import mc.alk.arena.objects.CompetitionResult;
import mc.alk.arena.objects.MatchResult;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.spawns.TimedSpawn;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.util.SerializerUtil;
import mc.euro.demolition.BombPlugin;
import mc.euro.demolition.objects.CompassHandler;
import mc.euro.demolition.timers.DetonationTimer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

/**
 * SndArena = Search N Destroy game-mode from Call of Duty & Counterstrike.
 * 
 * One team guards their bases, while the other teams attempts to destroy a base to win.
 *
 * <pre>
 * SndArena events & logic:
 * onBombSpawn() - THIS BREAKS ALL OTHER EVENTS.
 * onBombPickup() - set HAT & compass.
 * onBombCarrierLeave() - if they log out or leave the arena.
 * onBombCarrierDeath() - drop it on the ground.
 * onBombDrop() - is it outside the map ?
 * onBombDespawn() - THIS BREAKS ALL OTHER EVENTS.
 * onBombPlace() - trigger onBombPlant() if close enough.
 * onBombPlant() - takes 7 sec to plant + 30 sec to blow up.
 * onPlantFailure() - Self cancelled or caused by death ?
 * onBombDefuse() - takes 7 sec, declare winners.
 *
 * </pre>
 */
public class SndArena extends EodArena {
    
    AtomicInteger bombHologram = new AtomicInteger(-1);
    Set<Integer> holograms = new HashSet<Integer>(4);
    // String carrier = null;
    // PlantTimer plantTimer;
    // Map<String, DefuseTimer> defuseTimers = new HashMap<String, DefuseTimer>();
    
    ArenaTeam attackers;
    ArenaTeam defenders;
    
    /**
     * Pre-BattleArena v3.9.8 constructor to support backwards compatibility.
     */
    public SndArena() {
        super((BombPlugin) Bukkit.getPluginManager().getPlugin("BombArena"));
    }
    
    /**
     * This constructor requires BattleArena v3.9.8+.
     */
    public SndArena(BombPlugin plugin) {
        super(plugin);
    }
    
    @ArenaEventHandler
    public void onMatchResult(MatchResultEvent e) {
        CompetitionResult result = e.getMatchResult();
        if (result.isDraw()) {
            CompetitionResult newResult = new MatchResult();
            newResult.addLoser(attackers);
            newResult.setVictor(defenders);
            e.setMatchResult(newResult);
        }
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
     *
     * @param e PlayerPickupItemEvent: checks to see if they picked up the bomb
     * item, or a different item.
     */
    @ArenaEventHandler
    public void onBombPickup(PlayerPickupItemEvent e) {
        int teamID = getTeam(e.getPlayer()).getId();
        plugin.debug.sendMessage(e.getPlayer(), "debug works!");
        plugin.debug.sendMessage(e.getPlayer(), "onBombPickup() Listener works!");
        
        if (e.getItem().getItemStack().getType() != plugin.getBombBlock()) return;
        
        if (defenders.getId() == teamID) {
            e.setCancelled(true);
            return;
        }
        
        if (carrier == null) {
            this.carrier = e.getPlayer().getName();
            e.getPlayer().getInventory().setHelmet(new ItemStack(plugin.getBombBlock()));
            ArenaTeam team2 = getOtherTeam(e.getPlayer());
            createBaseHologram(getSavedBases());
            msgAll(team2.getPlayers(), "Hurry back to defend the bases from being destroyed!");
            msgAll(getMatch().getArena().getTeam(e.getPlayer()).getPlayers(),
                    "Your team has the bomb! Follow your compass to find the target bases.");
            compass.pointTo(getCopyOfSavedBases());
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
        Player eplayer = e.getPlayer();
        ArenaTeam eventTeam = getTeam(eplayer);
        if (e.getBlockPlaced().getType() == plugin.getBombBlock()) {
            e.setCancelled(true);
            if (eventTeam.getId() == defenders.getId()) {
                eplayer.getInventory().remove(plugin.getBombBlock());
                return false;
            } else if (eventTeam.getId() == attackers.getId()) {
                eplayer.updateInventory();
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean isPlayerInsideBase(Player player) {
        for (Location loc : getCopyOfSavedBases()) {
            if (loc.distance(player.getLocation()) <= plugin.getBaseRadius()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Defuse Conditions:.
     * <pre>
     * Defuse Conditions:
     * - Is a DetonationTimer running ?
     * - Is the player that's attempting to defuse on the defending team ?
     * - Are they at the correct base location ? (where the bomb was originally planted)
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
        boolean isPlayerAtBombLocation = (detTimer == null) ? false : 
                eplayer.getLocation().distance(detTimer.getBombLocation()) < 6;
        plugin.debug.log("Are they at the correct base ? (where the bomb was planted) : " + isPlayerAtBombLocation);
        
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
     * Plant Conditions:
     * - Does the player have the bomb ? (i.e. Are they the bomb carrier ?)
     * - Make sure a DetonationTimer isn't already running.
     * - No need to check if they're on the attacking team 
     *   because this was done in the PlayerPickupItemEvent.
     * </pre>
     */
    @Override
    public boolean isPlayerPlanting(InventoryOpenEvent e) {
        
        Player eplayer = (Player) e.getPlayer();
        int eTeamID = getTeam(eplayer).getId();
        DetonationTimer detTimer = (plantTimer == null) ? null : plantTimer.getDetonationTimer();
        
        plugin.debug.log("*** PLANT CONDITIONS: ***");
        plugin.debug.log("carrier != null : " + (carrier != null));
        plugin.debug.log("EventPlayer.getName().equals(carrier) : " + eplayer.getName().equalsIgnoreCase(carrier));
        plugin.debug.log("Are they on the attacking team ?" + (attackers.getId() == eTeamID));
        plugin.debug.log("(We don't need to do a base check in Snd because the attackers do not have a base to protect)");
        plugin.debug.log("The detonation timer is NOT already running : " + (detTimer == null));
        
        if (carrier != null 
                && eplayer.getName().equalsIgnoreCase(carrier) 
                && attackers.getId() == eTeamID 
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
        // loadLocations();
        int matchID = getMatch().getID();
        plugin.debug.log("onStart matchID = " + matchID);
        this.carrier = null;
        resetBases();
        plugin.giveCompass(getMatch().getPlayers());
        // loadLocations();
        assignTeams(getMatch().getTeams());
        compass = new CompassHandler(this);
        compass.runTaskTimer(plugin, 0L, 120L);
    }
    
    private void loadLocations() {
        if (getSavedBases().isEmpty()) {
            String arenaName = getName();
            Collection<Location> list = getOldBases(arenaName);
            for (Location loc : list) {
                addSavedBase(loc);
            }
        }
    }
    
    /**
     * Used by BombPlugin->updateBasesYml() to transfer data from bases.yml to arenas.yml.
     * @param arenaName
     * @return Just a set of locations (no duplicates).
     * @deprecated Labeled as deprecated to prevent accidental usage.
     */
    @Deprecated
    public Collection<Location> getOldBases(String arenaName) {
        // bases.yml
        // PATH = "{arenaName}"
        String path = arenaName;
        Collection<Location> temp = new HashSet<Location>();
        plugin.debug.log("size = " + plugin.basesYml.getStringList(path).size());
        if (plugin.basesYml.getStringList(path) != null) {
            List<String> stringList = plugin.basesYml.getStringList(path);
            for (String s : stringList) {
                Location loc = SerializerUtil.getLocation(s);
                temp.add(loc);
            }
            plugin.debug.log("SndArena:getBases(String arenaName) size of returning List = " + temp.size());
            return temp;
        }
        plugin.getLogger().severe("SndArena:getBases(String ArenaName) has failed to return a List of Locations.");
        return new ArrayList();
    }
    
    /**
     * This is called from onStart() and assigns both Teams to a base.
     * <br/><br/>
     *
     * Since teams are assigned to a base, we can use this information to
     * prevent them from trying to destroy their own base. <br/><br/>
     *
     * And force them to destroy the other teams base. <br/><br/>
     *
     * @param bothTeams - Assign bases for what teams ?
     */
    public void assignTeams(List<ArenaTeam> bothTeams) {
        
        Map<Long, TimedSpawn> spawns = getTimedSpawns();
        Location bombSpawnLoc = spawns.get(1L).getSpawn().getLocation();
        
        ArenaTeam team1 = null;
        ArenaTeam team2;
        for (ArenaTeam t : bothTeams) {
            team1 = t;
            break;
        }
        team2 = getOtherTeam(team1);
        
        double distance1 = getMatch().getTeamSpawn(team1, false).getLocation().distance(bombSpawnLoc);
        double distance2 = getMatch().getTeamSpawn(team2, false).getLocation().distance(bombSpawnLoc);

        if (distance1 < distance2) {
            this.attackers = team1; 
            this.defenders = team2; 
        } else {
            this.attackers = team2; 
            this.defenders = team1; 
        }
        this.attackers.sendMessage("You are the attacking team! ");
        this.defenders.sendMessage("You are the defending team! ");

        List<Location> locations = getCopyOfSavedBases();

        plugin.debug.log("SndArena.java:assignBases()");
        plugin.debug.log("arena name = " + getName());
        
        if (locations == null) {
            msgAll(getMatch().getPlayers(), "[SndArena]" + getName()
                    + " has stopped because no bases were configured.");
            msgAll(getMatch().getPlayers(), "[SndArena] "
                    + "please use the command (/bomb addbase <ArenaName>)"
                    + " to properly setup arenas.");
            plugin.getLogger().warning("[SndArena] No bases found inside bases.yml: "
                    + "Please use the cmd (/bomb addbase <ArenaName>)"
                    + "to properly setup arenas.");
            getMatch().cancelMatch();
        }

    }  // END OF assignTeams()
    
    @Override
    public void removeHolograms() {
        if (bombHologram.get() != -1) {
            plugin.holograms().removeHologram(bombHologram.get());
            bombHologram.set(-1);
        }
        for (Integer id : holograms) {
            plugin.holograms().removeHologram(id);
        }
        holograms.clear();
    }
    
    @Override
    protected void createBaseHologram(Set<Location> locations) {
        removeHolograms();
        for (Location loc : locations) {
            int id = plugin.holograms().createBaseHologram(loc);
            holograms.add(id);
        }
    }
    
    @Override
    protected AtomicInteger createBombHologram(Location loc) {
        removeHolograms();
        int id = plugin.holograms().createBombHologram(loc);
        bombHologram.set(id);

        return bombHologram;
    }
    
}
