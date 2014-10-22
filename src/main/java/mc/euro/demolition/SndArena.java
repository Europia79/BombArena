package mc.euro.demolition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import mc.alk.arena.events.matches.MatchCompletedEvent;
import mc.alk.arena.events.matches.MatchStartEvent;
import mc.alk.arena.events.players.ArenaPlayerLeaveEvent;
import mc.alk.arena.events.teams.TeamDeathEvent;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchResult;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.events.EventPriority;
import mc.alk.arena.objects.spawns.SpawnLocation;
import mc.alk.arena.objects.spawns.TimedSpawn;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.util.SerializerUtil;
import mc.euro.demolition.debug.DebugOff;
import mc.euro.demolition.debug.DebugOn;
import mc.euro.demolition.objects.Bomb;
import mc.euro.demolition.timers.DefuseTimer;
import mc.euro.demolition.timers.DetonationTimer;
import mc.euro.demolition.timers.PlantTimer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

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
public class SndArena extends Arena {

    BombPlugin plugin;
    /**
     * {@literal <matchID, teamID> }
     */
    Map<Integer, Integer> attackers;
    /**
     * <matchID, teamID>
     */
    Map<Integer, Integer> defenders;
    Map<Integer, List<Location>> bases;
    Map<Integer, Integer> holograms;

    /**
     * Constructor: gets a reference to Main.java and stores it in the plugin
     * field.
     */
    public SndArena() {
        plugin = (BombPlugin) Bukkit.getPluginManager().getPlugin("BombArena");
        attackers = new ConcurrentHashMap<Integer, Integer>();
        defenders = new ConcurrentHashMap<Integer, Integer>();
        bases = new ConcurrentHashMap<Integer, List<Location>>();
        holograms = new HashMap<Integer, Integer>();
    }
    
    @ArenaEventHandler
    public void onMatchStartEvent(MatchStartEvent e) {
        int matchID = e.getMatch().getID();
        Map<Long, TimedSpawn> matchSpawns = e.getMatch().getArena().getTimedSpawns();
        for (Long key : matchSpawns.keySet()) {
            plugin.debug.log("MatchStartEvent spawn key : " + key.toString());
            if (key == 1L) {
                Location loc = matchSpawns.get(key).getSpawn().getLocation();
                int hologramID = plugin.holograms().createBombHologram(loc);
                holograms.put(matchID, hologramID);

                setCompass(loc);
            }
        }
    }
    
    /**
     * This method sets the compass direction when the bomb spawns. <br/><br/>
     * 
     * Without (needsPlayer=false), ItemSpawnEvent would break all other events.
     */ 
    @ArenaEventHandler (needsPlayer=false)
    public void onBombSpawn(ItemSpawnEvent e) {
        plugin.debug.log("ItemSpawnEvent called");
        if (e.getEntity().getItemStack().getType() != plugin.getBombBlock()) return;
        int matchID = getMatch().getID();
        if (plugin.carriers.containsKey(matchID) 
                && plugin.carriers.get(matchID) != null) {
            e.setCancelled(true);
            return;
        }
        if (e.getEntity().getItemStack().getType() == plugin.getBombBlock()) {
            setCompass(e.getLocation());
            // int hologramID = plugin.holograms().createBombHologram(e.getLocation());
            // holograms.put(matchID, hologramID);
            msgAll(getMatch().getPlayers(), "The bomb has spawned");
        }
    } // END of ItemSpawnEvent
    
    /**
     * This method makes sure that the bomb doesn't despawn during a match. <br/><br/>
     * 
     * <pre>
     * - is the item a bomb ?
     * - ok, does someone already have the bomb in their inventory ?
     * - if not, cancel the ItemDespawnEvent.
     * </pre>
     * @param e ItemDespawnEvent - Was it the bomb ? Or another item ?
     */
    @ArenaEventHandler(needsPlayer = false)
    public void onBombDespawn(ItemDespawnEvent e) {
        Material mat = e.getEntity().getItemStack().getType();
        if (mat != plugin.getBombBlock()) {
            return;
        }
        int matchID = getMatch().getID();
        String c = (plugin.carriers.get(matchID) == null) ? null : plugin.carriers.get(matchID);

        if (c == null) {
            Set<ArenaPlayer> allplayers = getMatch().getPlayers();
            plugin.debug.msgArenaPlayers(allplayers, "Bomb despawn cancelled.");
            e.setCancelled(true);
        } else {
            plugin.debug.msgArenaPlayers(getMatch().getPlayers(),
                    "Bomb despawn allowed because " + c + " has the bomb.");
        }

    } // END OF ItemDespawnEvent
    
    /**
     * This method sets plugin.carriers, compass direction, and gives a hat
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
        int matchID = getMatch().getID();
        int teamID = getTeam(e.getPlayer()).getId();
        String c = (plugin.carriers.get(matchID) == null) ? null : plugin.carriers.get(matchID);
        plugin.debug.sendMessage(e.getPlayer(), "debug works!");
        plugin.debug.sendMessage(e.getPlayer(), "onBombPickup() Listener works!");
        
        if (e.getItem().getItemStack().getType() != plugin.getBombBlock()) return;
        
        if (defenders.get(matchID) == teamID) {
            e.setCancelled(true);
            return;
        }
        
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
            teamID = team2.getId();
            // getMatch().getArena().getTeam(e.getPlayer()).getCurrentParams().
            List<Location> base_locs = this.bases.get(matchID);
            Location min = getMinLocation(base_locs);
            setCompass(min);
            int hologramID = holograms.get(matchID);
            plugin.holograms().removeHologram(hologramID);
            hologramID = plugin.holograms().createBaseHologram(min);
            holograms.put(matchID, hologramID);
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



    } // END OF PlayerPickupItemEvent

    /**
     * This method handles the scenario where a player gets the bomb then logs
     * out or leaves the arena.
     *
     * @param e ArenaPlayerLeaveEvent
     */
    @ArenaEventHandler
    public void onBombCarrierLeave(ArenaPlayerLeaveEvent e) {
        int matchID = getMatch().getID();
        String c = (plugin.carriers.get(matchID) == null) ? null : plugin.carriers.get(matchID);
        plugin.debug.log("ArenaPlayerLeaveEvent has been called.");
        if (e.getPlayer().getName().equalsIgnoreCase(c)) {
            plugin.debug.log("onBombCarrierLeave() event detected.");
            plugin.carriers.remove(matchID);
            e.getPlayer().getInventory().remove(plugin.getBombBlock());
            getTimedSpawns().get(1L).spawn();
        }

    } // END OF ArenaPlayerLeaveEvent

    @ArenaEventHandler
    public void onTeamDeathEvent(TeamDeathEvent e) {
        int matchID = e.getCompetition().getID();
        if (!plugin.detTimers.containsKey(e.getCompetition().getID())) {
            MatchResult result = new MatchResult();
            ArenaTeam losers = e.getTeam();
            for (ArenaTeam t : getTeams()) {
                if (!t.isDead() || t != losers) {
                    result.setVictor(t);
                }
            }
            result.addLoser(losers);
            getMatch().endMatchWithResult(result);
        }
    } // END OF TeamDeathEvent

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
        cancelTimer(p);

        // Don't drop the bomb if the DetonationTimer is running...
        // Since the player won't even have it in their inventory.
        // And since we don't want PlayerDropItemEvent to clear plugin.carriers
        if (c == null || plugin.detTimers.containsKey(matchID)) {
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
     * @param e PlayerDropItemEvent - Did they drop the bomb item ? Or another
     * item ?
     */
    @ArenaEventHandler
    public void onBombDrop(PlayerDropItemEvent e) {
        final int matchID = getMatch().getID();
        final Player player = e.getPlayer();
        String c = (plugin.carriers.get(matchID) == null) ? null : plugin.carriers.get(matchID);
        Material type = e.getItemDrop().getItemStack().getType();
        // To-do: make sure the bomb didn't get thrown outside the map
        if (type == plugin.getBombBlock()) {
            if (c != null
                    && e.getPlayer().getName().equals(c)) {
                if (e.getPlayer().getInventory() != null
                        && e.getPlayer().getInventory().getHelmet() != null
                        && e.getPlayer().getInventory().getHelmet().getType() == Material.TNT) {
                    e.getPlayer().getInventory().setHelmet(new ItemStack(Material.AIR));
                }
                // sets the carrier to null
                // as long as the DetonationTimer isn't running
                if (!plugin.detTimers.containsKey(matchID)) {
                    plugin.carriers.remove(matchID);
                    // setCompass(loc);
                    msgAll(getMatch().getPlayers(), "The bomb has been dropped! Follow your compass.");
                    
                    final Item ibomb = e.getItemDrop();
                    Location loc = exact(player);
                    plugin.holograms().removeHologram(holograms.get(matchID));
                    final int hologramID = plugin.holograms().createBombHologram(loc);
                    holograms.put(matchID, hologramID);
                    
                    BukkitTask task = new BukkitRunnable() {
                        
                        int ticks;
                        @Override
                        public void run() {
                            ticks = ticks + 1;
                            if (ticks >= 100) {
                                cancel();
                                return;
                            }
                            Location exact = exact(player);
                            System.out.println("" + ticks + " y=" + ibomb.getLocation().getY());
                            plugin.holograms().teleport(hologramID, ibomb.getLocation());
                            setCompass(exact);
                            if (ibomb.isOnGround()) {
                                ticks = ticks + 10;
                            }
                        }
                    }.runTaskTimer(plugin, 1L, 1L);
                }

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
    
    public Location exact(Entity e) {
        Location exact_loc = e.getLocation();
        List<Entity> entities = e.getNearbyEntities(20.0, 50.0, 20.0);
        for (Entity entity : entities) {
            plugin.debug.log("" + entity.getType().toString() + " : " + entity.toString());
            if (entity.getType() == EntityType.DROPPED_ITEM) {
                plugin.debug.log("Dropped Item found!");
                Item item = null;
                try {
                    item = (Item) entity;
                } catch (ClassCastException ex) {
                    plugin.debug.log("ClassCastException");
                    continue;
                }
                if (item.getItemStack().getType() == plugin.getBombBlock()) {
                    plugin.debug.log("Exact Bomb Location found!");
                    exact_loc = item.getLocation();
                }
            }
        }
        return exact_loc;
    }

    /**
     * This event breaks ALL other events.
     *
     * <pre>
     * respawn a new bomb OR cancel the despawn event.
     * </pre>
     *
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
     * Handles the scenario when players attempt to place the bomb on the ground
     * like it's a block. <br/><br/>
     *
     * This method is going to help out new players by checking the distance to
     * the base:<br/>
     *
     * - if the distance is small, then trigger onBombPlant(InventoryOpenEvent
     * e). <br/>
     * - if they're too far away, then give the player helpful hints about the
     * distance and compass direction to the enemy base. <br/>
     *
     * @param e BlockPlaceEvent - Is it the bomb block ?
     */
    @ArenaEventHandler
    public void onBombPlace(BlockPlaceEvent e) {
        if (e.getBlockPlaced().getType() != plugin.getBombBlock()) {
            return;
        }
        e.setCancelled(true);
        e.getPlayer().updateInventory();
        int matchID = getMatch().getID();
        Player eplayer = e.getPlayer();
        int teamID = getOtherTeam(eplayer).getId();
        // Get the coordinates to the base
        // calculate the distance to the base
        // if the distance is small, attempt to trigger onBombPlant()
        if (bases.get(matchID).get(teamID).distance(eplayer.getLocation()) <= plugin.getBaseRadius()) {
            plugin.debug.sendMessage(eplayer, "Now attempting to plant the bomb.");
            InventoryType itype = plugin.getBaseinv();
            // ANVIL, BEACON, & DROPPER are not supported by openIventory()
            if (itype == InventoryType.ANVIL) {
                itype = InventoryType.BREWING;
            }
            if (itype == InventoryType.BEACON) {
                itype = InventoryType.BREWING;
            }
            if (itype == InventoryType.DROPPER) {
                itype = InventoryType.HOPPER;
            }
            // triggers onBombPlantDefuse()
            eplayer.openInventory(Bukkit.createInventory(eplayer, itype));
        } else {
            eplayer.sendMessage("Improper bomb activation! Follow your compass to find the other Team's base.");
        }

        if (e.getBlockPlaced().getType() == plugin.getBombBlock()) {
            e.getPlayer().sendMessage("Improper bomb activation!");
            e.setCancelled(true);
            // updateInventory() is deprecated
            // Must eventually find another solution.
            e.getPlayer().updateInventory();
        }
    } // END OF BlockPlaceEvent

    /**
     * Is WorldGuard denying bomb plants ?
     */
    @ArenaEventHandler(priority = EventPriority.HIGHEST)
    public void onBaseInteraction(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (e.getClickedBlock().getType() != plugin.getBaseBlock()) {
            return;
        }

        if (e.isCancelled()) {
            e.setCancelled(false);
        }

    } // END OF onBaseInteraction()

    /**
     * This event handles players who attempt to plant or defuse the bomb. <br/><br/>
     *
     * <pre>
     * Exit Conditions:
     * - Did they open a Base Inventory ?
     * - Did anyone pickup the bomb ? if not, exit.
     * 
     * Defuse Conditions:
     * - Is a DetonationTimer running ?
     * - Is the player that's attempting to defuse on the defending team ?
     * - Are they at the correct base location ? (where the bomb was originally planted)
     * 
     * Plant Conditions:
     * - Does the player have the bomb ? (i.e. Are they the bomb carrier ?)
     * - Make sure a DetonationTimer isn't already running.
     * - No need to check if they're on the attacking team 
     *   because this was done in the PlayerPickupItemEvent.
     * 
     * </pre>
     * 
     * @param e InventoryOpenEvent
     */
    @ArenaEventHandler(priority = EventPriority.HIGHEST)
    public void onBombPlantDefuse(InventoryOpenEvent e) {
        int matchID = getMatch().getID();
        String c = (plugin.carriers.get(matchID) == null) ? null : plugin.carriers.get(matchID);

        // EXIT CONDITIONS:
        if (e.getInventory().getType() != plugin.getBaseinv()) {
            return;
        }
        if (c == null) {
            e.setCancelled(true);
            return;
        }
        if (e.isCancelled()) {
            e.setCancelled(false);
        }

        Player eplayer = (Player) e.getPlayer();
        int eTeamID = getTeam(eplayer).getId();
        int cTeamID = getTeam(Bukkit.getPlayer(c)).getId();

        plugin.debug.log("onBombPlant() has been called.");
        plugin.debug.log("matchID = " + matchID);
        plugin.debug.log("plugin.carriers.get(matchID) = " + plugin.carriers.get(matchID));
        plugin.debug.log("planter/defuser = " + eplayer.getName());
        plugin.debug.log("teamID = " + eTeamID);
        plugin.debug.sendMessage(eplayer, "onBombPlantDefuse() has been called");
        plugin.debug.log("e.getInventory().getType() = " + e.getInventory().getType());
        plugin.debug.log("carrier, c = " + c);
        plugin.debug.log("e.getPlayer().getName() = " + e.getPlayer().getName());
        plugin.debug.log("planter.getLocation = " + eplayer.getLocation());
        plugin.debug.log("bases.get(matchID) = " + bases.get(matchID).toString());
        plugin.debug.log("e.getPlayer().getInventory().getHelmet = " + e.getPlayer().getInventory().getHelmet());

        // DEFUSE CONDITIONS:
        if (plugin.detTimers.containsKey(matchID)
                && eTeamID != cTeamID) {
            Location bomb_location = plugin.detTimers.get(matchID).getLocation();
            if (eplayer.getLocation().distance(bomb_location) < 5) {
                plugin.defTimers.get(matchID).put(eplayer.getName(), new DefuseTimer(e, getMatch()));
                plugin.defTimers.get(matchID).get(eplayer.getName()).runTaskTimer(plugin, 0L, 20L);
            } else {
                eplayer.sendMessage("Wrong base: Please follow your compass!");
                e.setCancelled(true);
            }
            // PLANT CONDITIONS:
        } else if (eplayer.getName().equalsIgnoreCase(c)
                && !plugin.detTimers.containsKey(matchID)) {
            plugin.pTimers.put(matchID, new PlantTimer(e, getMatch()));
            plugin.pTimers.get(matchID).runTaskTimer(plugin, 0L, 20L);
        } else {
            // NOT A PLANT OR DEFUSAL ATTEMPT ?
            e.setCancelled(true);
        }
    } // END OF InventoryOpenEvent

    /**
     * Handles the event where the Bomb planter or defuser 
     * does NOT complete the time required to plant the bomb. <br/>
     *
     * Notice that are multiple ways to trigger this event: <br/>
     * - The player prematurely closes the Base Inventory thereby canceling
     * the plant/defuse process. <br/>
     * - The player dies while attempting to plant or defuse. <br/>
     * - Also, when the PlantTimer is finished, it will close the player
     * inventory. So we need to be careful that it doesn't also cancel the 30
     * seconds that it takes to destroy the base. <br/><br/>
     *
     * @param e InventoryCloseEvent
     */
    @ArenaEventHandler(priority = EventPriority.HIGHEST)
    public void onBombPlantFailure(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();
        String type = e.getInventory().getType().toString();
        int matchID = getMatch().getID();
        String c = (plugin.carriers.get(matchID) == null) ? null : plugin.carriers.get(matchID);

        // EXIT CONDITIONS:
        if (e.getInventory().getType() != plugin.getBaseinv()) {
            return;
        }
        if (c == null) {
            return;
        }

        plugin.debug.sendMessage(p, "onBombPlantFailure has been called.");
        plugin.debug.sendMessage(p, "matchID = " + matchID);
        plugin.debug.log("type = " + type);
        plugin.debug.sendMessage(p, "carrier = " + c);

        cancelTimer(p);

    } // END OF InventoryCloseEvent

    /**
     * This will only cancel PlantTimer & DefuseTimer. <br/><br/>
     * 
     * Only the DefuseTimer has the ability to cancel the DetonationTimer. <br/><br/>
     * 
     */
    public void cancelTimer(Player p) {
        int matchID = getMatch().getID();
        String c = (plugin.carriers.get(matchID) == null) ? null : plugin.carriers.get(matchID);

        Map<String, DefuseTimer> temp = plugin.defTimers.get(matchID);
        for (String defuser : temp.keySet()) {
            if (p.getName().equalsIgnoreCase(defuser)) {
                temp.get(defuser).setCancelled(true);
                return;
            }
        }

        if (p.getName().equalsIgnoreCase(c)
                && plugin.pTimers.containsKey(matchID)) {
            // if this is an actual death or drop then those Events 
            // will handle setting the carrier to null
            plugin.pTimers.get(matchID).setCancelled(true);
            plugin.pTimers.remove(matchID);
        }
    }

    /**
     * This method handles the exploit where players try to break their
     * BaseBlock to prevent the other team from planting the bomb or defusing
     * it. <br/><br/>
     *
     * @param e BlockBreakEvent - Is it the base block ?
     */
    @ArenaEventHandler(priority = EventPriority.HIGHEST)
    public void onBaseExploit(BlockBreakEvent e) {
        // close the exploit where players can destroy the BaseBlock.
        // EXIT CONDITION:
        if (e.getBlock().getType() != plugin.getBaseBlock()) {
            return;
        }

        e.getPlayer().sendMessage("Stop trying to cheat!");
        e.setCancelled(true);

    } // END OF onBaseExploit()

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
        plugin.debug.log("onBegin() has been called.");
        validateTeams();
    }

    private void validateTeams() {
        int i = 0;
        for (ArenaTeam t : getTeams()) {
            i = i + 1;
        }
        if (i != 2) {
            plugin.getLogger().warning("SndArena requires exactly 2 teams.");
            plugin.getLogger().warning("Match is being cancelled.");
            getMatch().cancelMatch();
        }
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
        loadLocations();
        plugin.debug.msgArenaPlayers(getMatch().getPlayers(), "onStart matchID = " + matchID);
        setBases(getName());
        Set<ArenaPlayer> allplayers = getMatch().getPlayers();
        for (ArenaPlayer p : allplayers) {
            if (!p.getInventory().contains(Material.COMPASS)) {
                p.getInventory().addItem(new ItemStack(Material.COMPASS));
            }
        }
        assignTeams(getMatch().getTeams());
        plugin.defTimers.put(matchID, new HashMap<String, DefuseTimer>());
    }
    
    private void loadLocations() {
        int matchID = getMatch().getID();
        String arenaName = getName();
        this.bases.put(matchID, getBases(arenaName));
    }
    
    public List<Location> getBases(String arenaName) {
        // bases.yml
        // PATH = "{arenaName}"
        String path = arenaName;
        List<Location> temp = new ArrayList<Location>();
        System.out.println("size = " + plugin.basesYml.getStringList(path).size());
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

    public void setBases(String arenaName) {
        List<Location> locations = getBases(arenaName);
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
    
    @ArenaEventHandler
    public void onMatchCompletedEvent(MatchCompletedEvent e) {
        int matchID = e.getMatch().getID();
        if (holograms.containsKey(matchID)) {
            int hologramID = holograms.get(matchID);
            plugin.holograms().removeHologram(hologramID);
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
        bases.remove(matchID);

        if (plugin.pTimers.containsKey(matchID)) {
            plugin.pTimers.get(matchID).cancel();
            plugin.pTimers.remove(matchID);
        }
        if (plugin.detTimers.containsKey(matchID)) {
            plugin.detTimers.get(matchID).cancel();
            plugin.detTimers.remove(matchID);
        }
        plugin.defTimers.remove(matchID);
    }

    private void resetBases() {
        int matchID = getMatch().getID();
        List<Location> locations = bases.get(matchID);
        if (locations == null || locations.isEmpty()) {
            String msg = "resetBases() for arena: " + getMatch().getArena().getName() + " has failed.";
            plugin.getLogger().warning(msg);
            return;
        }
        for (Location loc : locations) {
            World world = loc.getWorld();
            Block block = world.getBlockAt(loc);
            block.setType(plugin.getBaseBlock());
        }
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
        
        int matchID = getMatch().getID();
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
            this.attackers.put(matchID, team1.getId());
            this.defenders.put(matchID, team2.getId());
            team1.sendMessage("You are the attacking team! " + team1.getId());
            team2.sendMessage("You are the defending team! " + team2.getId());
        } else {
            this.attackers.put(matchID, team2.getId());
            this.defenders.put(matchID, team1.getId());
            team2.sendMessage("You are the attacking team! " + team1.getId());
            team1.sendMessage("You are the defending team! " + team2.getId());
        }

        List<Location> locations = getBases(getMatch().getArena().getName());
        // this.bases.put(matchID, locations);

        plugin.debug.log("SndArena.java:assignBases()");
        plugin.debug.log("arena name = " + getMatch().getArena().getName());
        
        if (locations == null) {
            msgAll(getMatch().getPlayers(), "[SndArena]" + getName()
                    + " has stopped because no bases were found"
                    + " inside bases.yml");
            msgAll(getMatch().getPlayers(), "[SndArena] "
                    + "please use the command (/bomb addbase ArenaName)"
                    + " to properly setup arenas.");
            plugin.getLogger().warning("[SndArena] No bases found inside bases.yml: "
                    + "Please use the cmd (/bomb setbase ArenaName Index)"
                    + "to properly setup arenas.");
            getMatch().cancelMatch();
        }

    }  // END OF assignTeams()

    /**
     * This method uses a Player input parameter to get the other team.
     *
     * @param p Use this player to get his opponents Team.
     * @return Returns an ArenaTeam object of the other team.
     * @throws NullPointerException No longer throws NPE because Match
     * validation happens onBegin().
     */
    public ArenaTeam getOtherTeam(Player p) {
        ArenaTeam team1 = getTeam(p);
        return getOtherTeam(team1);
    }

    public ArenaTeam getOtherTeam(ArenaTeam team1) {
        List<ArenaTeam> bothTeams = getTeams();
        ArenaTeam team2 = null;
        for (ArenaTeam t : bothTeams) {
            if (team1 != t) {
                team2 = t;
            }
        }
        return team2;
    }

    /**
     * Points to a base to destroy, or a dropped/spawned bomb. <br/><br/>
     *
     * When the bomb spawns, we need to set the compass direction. <br/>
     * When the bomb is picked up, we need to set the compass direction. <br/>
     * When the bomb is dropped, we need to set the compass direction.
     * <br/><br/>
     *
     * This is to help out BOTH attackers and defenders find where they need to
     * go if they're unfamiliar with the map. <br/><br/>
     */
    private void setCompass(Location loc) {
        Set<ArenaPlayer> players = getMatch().getPlayers();
        for (ArenaPlayer p : players) {
            if (!p.getInventory().contains(Material.COMPASS)) {
                p.getInventory().addItem(new ItemStack(Material.COMPASS));
            }
            p.getPlayer().setCompassTarget(loc);
        }
    }
    
    private Location getMinLocation(List<Location> locations) {
        int matchID = getMatch().getID();
        String c = plugin.carriers.get(matchID);
        Player carrier = Bukkit.getPlayer(c);
        
        Location minloc = locations.get(0);
        double min = Double.MAX_VALUE;
        for (Location location : locations) {
            double d = location.distanceSquared(carrier.getLocation());
            if (d < min) {
                min = d;
                minloc = location;
            }
        }
        return minloc;
    }

    private void msgAll(Set<ArenaPlayer> players, String msg, ChatColor... color) {
        for (ArenaPlayer p : players) {
            ChatColor colour = (ChatColor) ((color.length < 1)
                    ? p.getTeam().getTeamChatColor() : color[0]);
            p.sendMessage(colour + msg);
        }
    }
    
}
