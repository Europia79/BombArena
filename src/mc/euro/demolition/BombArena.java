package mc.euro.demolition;

import mc.euro.demolition.objects.Bomb;
import mc.euro.demolition.util.PlantTimer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mc.alk.arena.BattleArena;
import mc.alk.arena.controllers.BattleArenaController;
import mc.alk.arena.events.players.ArenaPlayerLeaveEvent;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.arenas.ArenaType;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.events.EventPriority;
import mc.alk.arena.objects.spawns.ItemSpawn;
import mc.alk.arena.objects.spawns.SpawnInstance;
import mc.alk.arena.objects.spawns.TimedSpawn;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.serializers.ArenaSerializer;
import mc.euro.demolition.debug.DebugOff;
import mc.euro.demolition.debug.DebugOn;
import mc.euro.demolition.tracker.OUTCOME;
import mc.euro.demolition.util.DefuseTimer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * This class listens for all the bomb events and acts accordingly.
 * 
 * <pre>
 * Bomb = Hardened Clay 172
 *
 * Listen for 
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
public class BombArena extends Arena {

    BombPlugin plugin;

    /**
     * Constructor: gets a reference to Main.java and stores it in the plugin field.
     */
    public BombArena() {
        plugin = (BombPlugin) Bukkit.getPluginManager().getPlugin("BombArena");
    }
    
    /**
     * This method sets the compass direction when the bomb spawns. <br/><br/>
     * 
     * ItemSpawnEvent breaks all other events.
     */
    /*@ArenaEventHandler
    public void onBombSpawn(ItemSpawnEvent e) {
        msgAll(getMatch().getPlayers(), "ItemSpawnEven called");
        int matchID = getMatch().getID();
        if (plugin.carriers.containsKey(matchID) 
                && plugin.carriers.get(matchID) != null) {
            return;
        }
        if (e.getEntity().getItemStack().getType() == plugin.BombBlock) {
            setCompass(e.getLocation());
            msgAll(getMatch().getPlayers(), "The bomb has spawned");
        }
    } */

    /**
     * This method sets plugin.carriers, compass direction, and gives a hat
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
        int matchID = getMatch().getID();
        String c = (plugin.carriers.get(matchID) == null) ? null : plugin.carriers.get(matchID);
        plugin.debug.sendMessage(e.getPlayer(), "debug works!");
        plugin.debug.sendMessage(e.getPlayer(), "onBombPickup() Listener works!");
        
        if (e.getItem().getItemStack().getType() == plugin.BombBlock) {
            if (c == null) {
                c = e.getPlayer().getName();
                plugin.carriers.put(matchID, c);
                e.getPlayer().getInventory().setHelmet(new ItemStack(Material.TNT));
                ArenaTeam team2 = null;
                try {
                    team2 = getOtherTeam(e.getPlayer());
                } catch (NullPointerException ex) {
                    if (plugin.debug instanceof DebugOff) {
                        plugin.getLogger().severe("Stopping match because getOtherTeam() method failed");
                        getMatch().cancelMatch();
                    } else if (plugin.debug instanceof DebugOn) {
                        plugin.getLogger().severe(ChatColor.LIGHT_PURPLE 
                                + "getOtherTeam() failed, but match is being allowed to continue "
                                + "because debugging mode is ON.");
                    }
                }
                int teamID = team2.getId();
                // getMatch().getArena().getTeam(e.getPlayer()).getCurrentParams().
                Location base_loc = plugin.bases.get(matchID).get(teamID);
                setCompass(base_loc);
                msgAll(team2.getPlayers(), "Hurry back to defend your base from being destroyed!");
                msgAll(getMatch().getArena().getTeam(e.getPlayer()).getPlayers(), 
                        "Your team has the bomb! Follow your compass to find the other teams base.");
            } else {
                e.setCancelled(true);
                e.getPlayer().sendMessage(
                        "There can only be ONE bomb per Match. "
                        + c + " currently has the bomb.");
                e.getItem().remove();
            }


        }
    } // END OF PlayerPickupItemEvent
    
    /**
     * This method handles the scenario where a player gets the bomb then logs out or leaves the arena.
     * 
     * @param e ArenaPlayerLeaveEvent
     */
    @ArenaEventHandler
    public void onBombCarrierLeave(ArenaPlayerLeaveEvent e) {
        int id = getMatch().getID();
        String c = (plugin.carriers.get(id) == null) ? null : plugin.carriers.get(id);
        
    } // END OF ArenaPlayerLeaveEvent
    
    /**
     * This method triggers a new bombDropEvent if a player dies with the bomb.
     * 
     * <pre>
     * It also lets all the players know the location of the bomb.
     *   - But only if it's on the ground, never if a player has it.
     * </pre>
     * 
     * @param e PlayerDeathEvent - Do they have the bomb ?
     */
    @ArenaEventHandler
    public void onBombCarrierDeath(PlayerDeathEvent e) {
        int matchID = getMatch().getID();
        String c = (plugin.carriers.get(matchID) == null) ? null : plugin.carriers.get(matchID);
        Player p = e.getEntity().getPlayer();
        // drop the bomb on the ground
        if (c == null) {
            return;
        }
        if (p.getName().equals(c)) {
            e.setDeathMessage("" + e.getEntity().getPlayer().getName()
                    + " has died and dropped the bomb at "
                    + " " + (int) e.getEntity().getPlayer().getLocation().getX()
                    + " " + (int) e.getEntity().getPlayer().getLocation().getY()
                    + " " + (int) e.getEntity().getPlayer().getLocation().getZ());
            e.getDrops().clear();
            // (Item) new ItemStack(Material.HARD_CLAY causes ClassCastException
            Item bomb = new Bomb(e);
            bomb.setPickupDelay(40);
            PlayerDropItemEvent bombDropEvent = new PlayerDropItemEvent(e.getEntity().getPlayer(), bomb);
            Bukkit.getServer().getPluginManager().callEvent(bombDropEvent);
            if (bombDropEvent.isCancelled()) {
                plugin.getLogger().warning("Something has attempted to cancel the bombDropEvent. "
                        + "Is this intended ? Or is it a bug ?");
            }
            bombDropEvent.getPlayer().getWorld().dropItem(
                        bombDropEvent.getPlayer().getLocation(),
                        bombDropEvent.getItemDrop().getItemStack());
            
        }
        
    } // END OF PlayerDeathEvent
    
    /**
     * This event handles the scenario when the bomb is thrown on the ground.
     * 
     * <pre>
     * 1. Point the compass to the direction of the bomb.
     * 2. Give a visual aid for the bomb location (not implemented yet).
     * 3. Make sure the bomb didn't get thrown outside the map (not implemented).
     * </pre>
     * 
     * @param e PlayerDropItemEvent - Did they drop the bomb item ? Or another item ?
     */
    @ArenaEventHandler
    public void onBombDrop(PlayerDropItemEvent e) {
        int matchID = getMatch().getID();
        String c = (plugin.carriers.get(matchID) == null) ? null : plugin.carriers.get(matchID);
        Material type = e.getItemDrop().getItemStack().getType();
        // To-do: make sure the bomb didn't get thrown outside the map
        Location loc = e.getItemDrop().getLocation();
        if (type == plugin.BombBlock) {
            if (c != null 
                    && e.getPlayer().getName().equals(c)) {
                if (e.getPlayer().getInventory().getHelmet().getType() == Material.TNT) {
                    e.getPlayer().getInventory().setHelmet(new ItemStack(Material.AIR));
                }
                // sets the carrier to null
                plugin.carriers.remove(matchID);
                // get all arena players inside this Match. 
                // set their compass direction.
                setCompass(loc);
                msgAll(getMatch().getPlayers(), "The bomb has been dropped! Follow your compass.");
            } else {
                plugin.getLogger().warning(""
                        + e.getPlayer().getName()
                        + "has tried to drop the bomb without ever picking it up. "
                        + "Are they cheating / exploiting ? Or is this a bug ? "
                        + "Please investigate this incident and if it's a bug, then "
                        + "notify Europia79 via hotmail, Bukkit, or github.");
                e.getItemDrop().remove();
            }

        }
    } // END OF PlayerDropItemEvent
    
    /**
     * This event breaks ALL other events.
     * 
     * <pre>
     * respawn a new bomb OR cancel the despawn event.
     * </pre>
     * @param e ItemDespawnEvent - Was it the bomb ? Or another item ?
     */
    /*@ArenaEventHandler
    public void onBombDespawn(ItemDespawnEvent e) {
        int id = getMatch().getID();
        String c = (plugin.carriers.get(id) == null) ? null : plugin.carriers.get(id);
        // temporary place holder
        // until I get time to implement this method.
        // respawn a new bomb OR cancel the event
        if (e.getEntity().getItemStack().getType() == plugin.BombBlock 
                && c != null) {
            Set<ArenaPlayer> allplayers = getMatch().getPlayers();
            for (ArenaPlayer p : allplayers) {
                plugin.debug.messagePlayer(p.getPlayer(), "Bomb despawned cancelled. ");
            }
            e.setCancelled(true);
        } else {
            plugin.debug.msgArenaPlayers(getMatch().getPlayers(), 
                    "Bomb despawn allowed because " + c + " has the bomb.");
        }

    }*/
    
    /**
     * Handles the scenario when players attempt to place the bomb on the ground like it's a block. <br/><br/>
     * 
     * This method is going to help out new players by checking the distance to the base:<br/>
     * 
     *   - if the distance is small, then trigger onBombPlant(InventoryOpenEvent e). <br/>
     *   - if they're too far away, then give the player helpful hints
     *     about the distance and compass direction to the enemy base. <br/>
     * 
     * @param e BlockPlaceEvent - Is it the bomb block ?
     */
    @ArenaEventHandler
    public void onBombPlace(BlockPlaceEvent e) {
        // Get the coordinates to the base
        // calculate the distance to the base
        // if the distance is small, attempt to trigger onBombPlant()
        if (e.getBlockPlaced().getType() == plugin.BombBlock) {
            e.getPlayer().sendMessage("Improper bomb activation!");
            
            // get the other team's base location
            // set the player compass
            // and msg him to follow the compass
            // e.getPlayer().getInventory().addItem(new ItemStack(plugin.BombBlock));
            e.setCancelled(true);
            // updateInventory() is deprecated
            // Must eventually find another solution.
            e.getPlayer().updateInventory();
        }
    } // END OF BlockPlaceEvent
    
    /**
     * This event handles players who access Brewing Stands. <br/>
     * 
     * Main if-statement checks: <br/>
     * - Did they open a Brewing Stand Inventory ? <br/>
     * - Does the player have the bomb ? <br/>
     * - Are they at the enemy base ? <br/><br/>
     * 
     * If so, start the Plant Timer: It takes about 5 to 10 seconds to plant the bomb. <br/><br/>
     * 
     * If not, cancel the InventoryOpenEvent. <br/>
     * 
     * @param e InventoryOpenEvent - Is it a Brewing Stand Inventory ? (Each base must have a brewing stand).
     */
    @ArenaEventHandler (priority=EventPriority.HIGHEST)
    public void onBombPlantDefuse(InventoryOpenEvent e) {
        int matchID = getMatch().getID();
        String c = (plugin.carriers.get(matchID) == null) ? null : plugin.carriers.get(matchID);
        
        // EXIT CONDITIONS:
        if (e.getInventory().getType() != plugin.Baseinv) return;
        if (c == null) {
            e.setCancelled(true);
            return;
        }
        
        Player eplayer = (Player) e.getPlayer();
        int teamID = getTeam(eplayer).getId();
        int cTeamID = getTeam(Bukkit.getPlayer(c)).getId();
        
        plugin.debug.log("onBombPlant() has been called.");
        plugin.debug.log("matchID = " + matchID);
        plugin.debug.log("plugin.carriers.get(matchID) = " + plugin.carriers.get(matchID));
        plugin.debug.log("planter/defuser = " + eplayer.getName());
        plugin.debug.log("teamID = " + teamID);
        plugin.debug.sendMessage(eplayer, "onBombPlant() has been called");
        plugin.debug.log("e.getInventory().getType() = " + e.getInventory().getType());
        plugin.debug.log("carrier, c = " + c);
        plugin.debug.log("e.getPlayer().getName() = " + e.getPlayer().getName());
        plugin.debug.log("planter.getLocation = " + eplayer.getLocation());
        plugin.debug.log("plugin.bases.get(matchID) = " + plugin.bases.get(matchID).toString());
        plugin.debug.log("plugin.bases.get(matchID).get(teamID) = " + plugin.bases.get(matchID).get(teamID).toString());
        plugin.debug.log("e.getPlayer().getInventory().getHelmet = " + e.getPlayer().getInventory().getHelmet());
        
        // DEFUSE CONDITIONS:
        if (plugin.detTimers.containsKey(matchID)
                && teamID != cTeamID
                && plugin.bases.get(matchID).get(teamID).distance(eplayer.getLocation()) < 6) {
            plugin.defTimers.get(matchID).put(eplayer.getName(), new DefuseTimer(e, getMatch()));
            plugin.defTimers.get(matchID).get(eplayer.getName()).runTaskTimer(plugin, 0L, 20L);
            // PLANT CONDITIONS:
        } else if (eplayer.getName().equalsIgnoreCase(c) 
                && plugin.bases.get(matchID).get(teamID).distance(eplayer.getLocation()) > 30
                && !plugin.detTimers.containsKey(matchID)) {
            plugin.pTimers.put(matchID, new PlantTimer(e, getMatch()));
            plugin.pTimers.get(matchID).runTaskTimer(plugin, 0L, 20L);
        } else if (e.getInventory().getType() == plugin.Baseinv){
            // NOT A PLANT OR DEFUSAL ATTEMPT ?
            plugin.debug.sendMessage(eplayer, "event.setCancelled(true);");
            e.setCancelled(true);
        }
    } // END OF InventoryOpenEvent
    
    /**
     * Handles the event where the Bomb Carrier does NOT complete the time required to plant the bomb. <br/>
     * 
     * Notice that are multiple ways to trigger this event: <br/>
     * - The bomb carrier prematurely closes the Brewing Stand 
     *   thereby canceling the plant process. <br/>
     * - The bomb carrier dies. <br/>
     * - Also, when the PlantTimer is finished, it will close the player inventory.
     *   So we need to be careful that it doesn't also cancel the 30 seconds that
     *   it takes to destroy the base. <br/><br/>
     * 
     * @param e InventoryCloseEvent
     */
    @ArenaEventHandler (priority=EventPriority.HIGHEST)
    public void onBombPlantFailure(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();
        String type = e.getInventory().getType().toString();
        int matchID = getMatch().getID();
        String c = (plugin.carriers.get(matchID) == null) ? null : plugin.carriers.get(matchID);
        
        // EXIT CONDITIONS:
        if (e.getInventory().getType() != plugin.Baseinv) return;
        if (c == null) return;
        
        plugin.debug.sendMessage(p, "onBombPlantFailure has been called.");
        plugin.debug.sendMessage(p, "matchID = " + matchID);
        plugin.debug.log("type = " + type);
        plugin.debug.sendMessage(p, "carrier = " + c);

        Map<String, DefuseTimer> temp = plugin.defTimers.get(matchID);
        for (String defuser : temp.keySet()) {
            if (p.getName().equalsIgnoreCase(defuser)) {
                temp.get(defuser).setCancelled(true);
            }
        }

        if (p.getName().equalsIgnoreCase(c)) {
            // if this is an actual death or drop then those Events 
            // will handle setting the carrier to null
            plugin.debug.msgArenaPlayers(getMatch().getPlayers(), 
                    "onBombPlantFailure has been called " 
                    + "due to InventoryCloseEvent");
            plugin.pTimers.get(matchID).setCancelled(true);
        }

    } // END OF InventoryCloseEvent
    
    /**
     * This method handles the event when players try to defuse the bomb (by breaking the bomb block). <br/><br/>
     * 
     * Make sure that player (or his teammates) who just planted the bomb
     * cannot turn around and break it: <br/><br/>
     * Such a scenario would produce 2 possible scenarios: <br/>
     * 1. The plant/defuse for the win exploit. <br/>
     * 2. The plant/break exploit (prevents the other team from defusing). <br/><br/>
     * 
     * This version currently contains bug #2.
     * 
     * @param e BlockBreakEvent - Is it the bomb block ?
     */
    @ArenaEventHandler (priority=EventPriority.HIGHEST)
    public void onBaseBreakExploit(BlockBreakEvent e) {
        // close the exploit where players can destroy the BaseBlock.
        int matchID = getMatch().getID();
        int otherTeam = getOtherTeam(e.getPlayer()).getId();
        String c = (plugin.carriers.get(matchID) == null) ? null : plugin.carriers.get(matchID);
        
        // add location check
        if (e.getBlock().getType() == plugin.BombBlock 
                && getTeam(e.getPlayer()) != getTeam(Bukkit.getServer().getPlayer(c))) 
        {
                plugin.ti.addPlayerRecord(e.getPlayer().getName(), "Bombs Planted Defused", OUTCOME.getDefuseSuccess());
                plugin.ti.addPlayerRecord(c, "Bombs Planted Defused", OUTCOME.getPlantFailure());
                ArenaTeam t = getTeam(e.getPlayer());
                getMatch().setVictor(t);
                plugin.detTimers.get(matchID).cancel();

        } else if (e.getBlock().getType() == plugin.BombBlock 
                && getTeam(e.getPlayer()) == getTeam(Bukkit.getServer().getPlayer(c))) {
            e.getPlayer().sendMessage("If you defuse the bomb, then the other team will win.");
            e.setCancelled(true);
        }
        
    } // END OF onBombDefusal
    
    /**
     * First method called by BattleArena. <br/><br/>
     * 
     * Do NOT handle anything specific to matches here. <br/><br/>
     * 
     * BattleArena method order (during matches): <br/>
     * 1. onStart() <br/>
     * 2. onComplete() <br/>
     * 3. onFinish() <br/>
     * 
     */
    @Override
    public void onBegin() {
        super.onBegin(); 
        plugin.getLogger().info("onBegin() has been called by Demolition plugin: BombArenaListener.java");
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
        plugin.debug.msgArenaPlayers(getMatch().getPlayers(), "onStart matchID = " + matchID);
        setBases(getMatch().getArena().getName());
        Set<ArenaPlayer> allplayers = getMatch().getPlayers();
        for (ArenaPlayer p : allplayers) {
            if (!p.getInventory().contains(Material.COMPASS)) {
                p.getInventory().addItem(new ItemStack(Material.COMPASS));
            }
        }
        assignBases(getMatch().getTeams());
        plugin.defTimers.put(matchID, new HashMap<String, DefuseTimer>());
    }
    
    public void setBases(String arena) {
        // PATH = "arenas.{arena}.spawns.{n}.spawn"
        ArrayList<Location> locations = plugin.getBases(getMatch().getArena().getName());
        for (Location loc : locations) {
            World w = loc.getWorld();
            Block block = w.getBlockAt(loc);
            block.setType(plugin.getBaseBlock());
        }
    }
    
    /**
     * onComplete() is called before money is given. <br/><br/>
     * 
     * Order: onStart(), onComplete(), onFinish()
     */
    @Override
    public void onComplete() {
        super.onComplete();
        int matchID = getMatch().getID();
        plugin.debug.msgArenaPlayers(getMatch().getPlayers(), "onComplete matchID = " + matchID);
        Set<ArenaPlayer> players = getMatch().getPlayers();
        for (ArenaPlayer p : players) {
            if (p != null 
                    && p.getInventory() != null 
                    && p.getInventory().getHelmet() != null 
                    && p.getInventory().getHelmet().getType() == Material.TNT) {
                p.getInventory().setHelmet(new ItemStack(Material.AIR));
            }
        }
    }
    
    /**
     * onFinish() is called after money is given.
     * 
     * Order: onStart(), onComplete(), onFinish()
     */
    @Override
    public void onFinish() {
        super.onFinish();
        int matchID = getMatch().getID();
        resetBases();
        plugin.debug.msgArenaPlayers(getMatch().getPlayers(), "onFinish matchID = " + matchID);
        plugin.carriers.remove(matchID);
        plugin.bases.remove(matchID);
        
        if (plugin.pTimers.containsKey(matchID)) {
            plugin.pTimers.remove(matchID);
        }
        if (plugin.detTimers.containsKey(matchID)) {
            plugin.detTimers.remove(matchID);
        }
        plugin.defTimers.remove(matchID);
    }
    
    private void resetBases() {
        int matchID = getMatch().getID();
        Map bases = plugin.bases.get(matchID);
        List<ArenaTeam> teams = getMatch().getTeams();
        for (ArenaTeam t : teams) {
            if (bases == null || bases.isEmpty()) {
                break;
            }
            Location loc = (Location) bases.get(t.getId());
            World world = loc.getWorld();
            Block block = world.getBlockAt(loc);
            block.setType(plugin.BaseBlock);
        }
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
        Map<Integer, Location> temp = new HashMap<Integer, Location>();
        plugin.debug.log("BombArena.java:assignBases()");
        plugin.debug.log("arena name = " + getMatch().getArena().getName());
        ArrayList<Location> locations = plugin.getBases(getMatch().getArena().getName());
        if (locations == null) {
            msgAll(getMatch().getPlayers(), "[BombArena]" + getName()
                    + " has stopped because no bases were found" 
                    + " inside arenas.yml");
            msgAll(getMatch().getPlayers(), "[BombArena] "
                    + "please use the command (/bomb setbase ArenaName Index)"
                    + " to properly setup arenas.");
            plugin.getLogger().warning("[BombArena] No bases found inside arena.yml: "
                    + "Please use the cmd (/bomb setbase ArenaName Index)"
                    + "to properly setup arenas.");
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
            } catch (Exception ex) {
                plugin.getLogger().warning("Possible VirtualPlayer found inside BombArena.");
                continue;
            }
            
            int teamID = t.getId();
            if (onedistance < twodistance) {
                temp.put(teamID, plugin.getExactLocation(ONE));
                teamID = getOtherTeam(playerOne).getId();
                temp.put(teamID, plugin.getExactLocation(TWO));
                break;
            } else if (onedistance > twodistance) {
                temp.put(teamID, plugin.getExactLocation(TWO));
                teamID = getOtherTeam(playerOne).getId();
                temp.put(teamID, plugin.getExactLocation(TWO));
                break;
            } else if (onedistance == twodistance) {
                plugin.getLogger().warning("Could NOT assign bases because " 
                        + "the player's spawn is equi-distance to both.");
                plugin.getLogger().info("Please change the spawn locations " 
                        + "for the teams in the bomb arena.");
            }
        }
        int matchID = getMatch().getID();
        plugin.bases.put(matchID, temp);
        plugin.debug.log("Number of Team bases: temp.size() = " + temp.size());
        plugin.debug.log("Number of Team bases: plugin.bases.get(matchID).size() = " + plugin.bases.get(matchID).size());
        if (temp.size() != 2) {
            plugin.getLogger().warning("The bomb game type must have 2 teams !!!");
        }
    }  // END OF assignBases()
    
    /**
     * This method uses a Player input parameter to get the other team.
     * 
     * @param p Use this player to get his opponents Team.
     * @return Returns an ArenaTeam object of the other team.
     * @throws NullPointerException Handles the scenario where a server owner mis-configured a Bomb Arena with only ONE Team.
     */
    public ArenaTeam getOtherTeam(Player p) throws NullPointerException {
        // get the player
        // get his team
        ArenaTeam team1 = getTeam(p);
        // get the OTHER team
        List<ArenaTeam> bothTeams = getTeams();
        for (ArenaTeam t : bothTeams) {
            if (team1 != t) {
                return t;
            }
        }
        plugin.getLogger().warning("getOtherTeam() method failed: The Bomb Arena Type must have two teams!");
        
        throw new NullPointerException();

    }
    
    /**
     * Points to a base to destroy, or a dropped/spawned bomb. <br/><br/>
     * 
     * When the bomb spawns, we need to set the compass direction. <br/>
     * When the bomb is picked up, we need to set the compass direction. <br/>
     * When the bomb is dropped, we need to set the compass direction. <br/><br/>
     * 
     * This is to help out BOTH attackers and defenders find where they need to go 
     * if they're unfamiliar with the map. <br/><br/>
     */
    private void setCompass(Location loc) {
        Set<ArenaPlayer> players = getMatch().getPlayers();
        for (ArenaPlayer p : players) {
            if (!p.getInventory().contains(Material.COMPASS)) {
                p.getInventory().addItem(new ItemStack(Material.COMPASS));
            }
            if (p.getInventory().contains(Material.COMPASS)) {
                p.getPlayer().setCompassTarget(loc);
                // p.sendMessage(ChatColor.GREEN + "Compass set.");
            } else {
                plugin.getLogger().warning(
                        "Players in the bomb Arena type should have a compass so they know " 
                        + "where bombs and bases are located.");
            }
        }
    }

    private void msgAll(Set<ArenaPlayer> players, String msg, ChatColor... color) {
        ChatColor colour = (ChatColor) ((color.length < 1) ? ChatColor.GREEN : color[0]);
        for (ArenaPlayer p : players) {
            p.sendMessage(colour + msg);
        }
    }
    
}
