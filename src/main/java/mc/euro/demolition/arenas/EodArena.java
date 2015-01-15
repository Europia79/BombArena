package mc.euro.demolition.arenas;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
import mc.alk.arena.events.matches.MatchStartEvent;
import mc.alk.arena.events.players.ArenaPlayerLeaveEvent;
import mc.alk.arena.events.teams.TeamDeathEvent;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchResult;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.events.EventPriority;
import mc.alk.arena.objects.spawns.TimedSpawn;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.serializers.Persist;
import mc.euro.demolition.BombPlugin;
import mc.euro.demolition.objects.CompassHandler;
import mc.euro.demolition.timers.DefuseTimer;
import mc.euro.demolition.timers.DetonationTimer;
import mc.euro.demolition.timers.PlantTimer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 * EOD = Explosive Ordnance Disposal. <br/><br/>
 * <pre>
 * EodArena is the parent for BombArena & SndArena.
 * It contains all fields & methods common to both arenas.
 * 
 * default BombBlock = TNT 46
 *
 * Listen for
 * onBombSpawn()
 * onBombDespawn()
 * onBombPickup() - implementated by BombArena & SndArena.
 * onBombCarrierLeave()
 * onBombCarrierDeath()
 * onBombDrop()
 * onBombPlace() - triggers onBombPlant() if close enough.
 * onBombPlantDefuse() - InventoryOpenEvent
 * onBombPlantDefuseFailure() - InventoryCloseEvent
 *
 * </pre>
 * 
 * @author Nikolai
 */
public abstract class EodArena extends Arena {
    
    protected BombPlugin plugin;
    protected String carrier = null;
    protected PlantTimer plantTimer = null;
    protected Map<String, DefuseTimer> defuseTimers = new HashMap<String, DefuseTimer>();
    
    protected CompassHandler compass;
    
    @Persist
    private final Set<Location> savedBases = new CopyOnWriteArraySet<Location>();
    
    public EodArena(BombPlugin reference) {
        this.plugin = reference;
        this.compass = new CompassHandler(this);
    }
    
    public abstract boolean isPlayerDefusing(InventoryOpenEvent e);
    public abstract boolean isPlayerPlanting(InventoryOpenEvent e);
    public abstract boolean isValidBombPlace(BlockPlaceEvent e);
    public abstract boolean isPlayerInsideBase(Player player);
    public abstract void removeHolograms();
    protected abstract void createBaseHologram(Set<Location> locations);
    protected abstract AtomicInteger createBombHologram(Location loc);
    
    public boolean addSavedBase(Location loc) {
        return savedBases.add(loc);
    }
    
    public boolean removeSavedBase(Location loc) {
        return savedBases.remove(loc);
    }
    
    public void clearSavedBases() {
        savedBases.clear();
    }
    
    protected Set<Location> getSavedBases() {
        return savedBases;
    }
    
    public List<Location> getCopyOfSavedBases() {
        return new ArrayList<Location>(savedBases);
    }
    
    public String getBombCarrier() {
        return this.carrier;
    }
    
    public Collection<DefuseTimer> getDefuseTimers() {
        return this.defuseTimers.values();
    }
    
    /**
     * This will only cancel PlantTimer & DefuseTimer. <br/><br/>
     * 
     * Only the PlantTimer has the ability to cancel the DetonationTimer. <br/><br/>
     * 
     */
    public void cancelTimer(Player p) {
        for (String defuser : defuseTimers.keySet()) {
            if (p.getName().equalsIgnoreCase(defuser)) {
                defuseTimers.get(defuser).cancel();
                defuseTimers.remove(defuser);
            }
        }

        if (p.getName().equalsIgnoreCase(carrier) && plantTimer != null) {
            plantTimer.setCancelled(true);
        }
    }
    
    public void cancelAndClearTimers() {
        if (getDetonationTimer() != null) {
            cancelDetonationTimer();
        }
        if (plantTimer != null) {
            plantTimer.cancel();
            plantTimer = null;
        }
        for (String defuser : defuseTimers.keySet()) {
            defuseTimers.get(defuser).cancel();
        }
        defuseTimers.clear();
    }
    
    /**
     * It is possible to return a null timer.
     */
    protected DetonationTimer getDetonationTimer() {
        DetonationTimer detTimer = (plantTimer == null) ? null : plantTimer.getDetonationTimer();
        return detTimer;
    }
    
    protected boolean isDetonationTimerRunning() {
        return getDetonationTimer() != null;
    }
    
    protected void cancelDetonationTimer() {
        plantTimer.cancelDetonationTimer();
    }
    
    protected void msgAll(Set<ArenaPlayer> players, String msg) {
        for (ArenaPlayer p : players) {
            p.sendMessage("" + ChatColor.translateAlternateColorCodes('&', msg));
        }
    }
    
    /**
     * Not used: createBombHologram() + setCompass().
     */
    @ArenaEventHandler
    public void onMatchStartEvent(MatchStartEvent e) {
        Map<Long, TimedSpawn> matchSpawns = e.getMatch().getArena().getTimedSpawns();
        for (Long key : matchSpawns.keySet()) {
            plugin.debug.log("MatchStartEvent spawn key : " + key.toString());
            if (key == 1L) {
                Location loc = matchSpawns.get(key).getSpawn().getLocation();
                // createBombHologram(loc);
                // setCompass(loc);
            }
        }
    }
    
    /**
     * This method sets the compass direction when the bomb spawns. <br/><br/>
     * 
     * Without (needsPlayer=false), ItemSpawnEvent would break all other events.
     */ 
    @ArenaEventHandler(needsPlayer = false)
    public void onBombSpawn(ItemSpawnEvent e) {
        plugin.debug.log("ItemSpawnEvent called");
        plugin.debug.msgArenaPlayers(getMatch().getPlayers(), "ItemSpawnEvent called for " + e.getEntity().getItemStack().getType().name());
        Material material = e.getEntity().getItemStack().getType();
        if (material != plugin.getBombBlock()) {
            return;
        }
        if (this.carrier != null) {
            e.setCancelled(true);
            return;
        }
        msgAll(getMatch().getPlayers(), "The bomb has spawned");
        setCompass(e.getLocation());
        createBombHologram(e.getLocation());
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

        if (carrier == null) {
            Set<ArenaPlayer> allplayers = getMatch().getPlayers();
            plugin.debug.msgArenaPlayers(allplayers, "Bomb despawn cancelled.");
            e.setCancelled(true);
        } else {
            plugin.debug.msgArenaPlayers(getMatch().getPlayers(),
                    "Bomb despawn allowed because " + carrier + " has the bomb.");
        }

    } // END OF ItemDespawnEvent
    
    /**
     * This method handles the scenario where a player gets the bomb then logs out or leaves the arena.
     * 
     * @param e ArenaPlayerLeaveEvent
     */
    @ArenaEventHandler
    public void onBombCarrierLeave(ArenaPlayerLeaveEvent e) {
        plugin.debug.log("ArenaPlayerLeaveEvent has been called.");
        if (e.getPlayer().getName().equalsIgnoreCase(carrier)) {
            plugin.debug.log("onBombCarrierLeave() event detected.");
            carrier = null;
            e.getPlayer().getInventory().remove(plugin.getBombBlock());
            getTimedSpawns().get(1L).spawn();
            getMatch().sendMessage("The bomb has been respawned "
                    + "because the bomb carrier left the game.");
            // point the compass
            // delete the old hologram
            // create the new hologram
        }
        
    } // END OF ArenaPlayerLeaveEvent
    
    /**
     * This method handles the exploit where players try to break their BaseBlock
     * to prevent the other team from planting the bomb or defusing it. <br/><br/>
     * 
     * @param e BlockBreakEvent - Is it the base block ?
     */
    @ArenaEventHandler (priority=EventPriority.HIGHEST)
    public void onBaseExploit(BlockBreakEvent e) {
        // close the exploit where players can destroy the BaseBlock.
        // EXIT CONDITION:
        if (e.getBlock().getType() != plugin.getBaseBlock()) return;
        
        e.getPlayer().sendMessage("Stop trying to cheat!");
        e.setCancelled(true);
        
    } // END OF BlockBreakEvent
    
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
        if (!isValidBombPlace(e)) return;
        Player eplayer = e.getPlayer();
        if (isPlayerInsideBase(eplayer)) {
            plugin.debug.sendMessage(eplayer, "Now attempting to plant the bomb.");
            InventoryType itype = plugin.getBaseinv();
            // ANVIL, BEACON, & DROPPER are not supported by openIventory()
            if (itype == InventoryType.ANVIL) { itype = InventoryType.BREWING; }
            if (itype == InventoryType.BEACON) { itype = InventoryType.BREWING; }
            if (itype == InventoryType.DROPPER) { itype = InventoryType.HOPPER; }
            // triggers onBombPlantDefuse()
            eplayer.openInventory(Bukkit.createInventory(eplayer, itype));
        } else {
            eplayer.sendMessage("Improper bomb activation! Follow your compass to find the other Team's base.");
        }
    } // END OF BlockPlaceEvent
    
    /**
     * Is WorldGuard denying bomb plants ?
     */
    @ArenaEventHandler (priority=EventPriority.HIGHEST)
    public void onBaseInteraction(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getClickedBlock().getType() != plugin.getBaseBlock()) return;
        
        if (e.isCancelled()) {
            e.setCancelled(false);
        }
        
    } // END OF onBaseInteraction()
    
    /**
     * This event handles planting & defusing the bomb. <br/>
     * 
     * <pre>
     * Exit Conditions:
     * - Not a Base Inventory
     * - Bomb Carrier is null
     * <pre>
     * 
     * @param e InventoryOpenEvent - Is it an actual Base Inventory ? (Each team must have a base).
     */
    @ArenaEventHandler (priority=EventPriority.HIGHEST)
    public void onBombPlantDefuse(InventoryOpenEvent e) {        
        // EXIT CONDITIONS:
        if (e.getInventory().getType() != plugin.getBaseinv()) return;
        if (carrier == null) {
            e.setCancelled(true);
            return;
        }
        if (e.isCancelled()) e.setCancelled(false);
        
        Player eplayer = (Player) e.getPlayer();
        int eTeamID = getTeam(eplayer).getId();
        int cTeamID = getTeam(Bukkit.getPlayer(carrier)).getId();
        
        plugin.debug.log("onBombPlantDefuse() inventoryType = " + e.getInventory().getType());
        plugin.debug.log("planter/defuser = " + eplayer.getName());
        plugin.debug.log("bomb carrier = " + carrier);
        plugin.debug.log("event eTeamID = " + eTeamID);
        plugin.debug.log("carrier cTeamID = " + cTeamID);
        plugin.debug.log("eplayer.getLocation = " + eplayer.getLocation());
        plugin.debug.log("savedBases.toString() = " + savedBases.toString());
        plugin.debug.log("e.getPlayer().getInventory().getHelmet = " + e.getPlayer().getInventory().getHelmet());
        
        // DEFUSE CONDITIONS:
        if (isPlayerDefusing(e)) {
            plugin.debug.log("" + eplayer.getName() + " IS DEFUSING THE BOMB.");
            defuseTimers.put(eplayer.getName(), new DefuseTimer(eplayer, this).start());
        // PLANT CONDITIONS:
        } else if (isPlayerPlanting(e)) {
            plugin.debug.log("" + eplayer.getName() + " IS PLANTING THE BOMB.");
            this.plantTimer = new PlantTimer(this);
            this.plantTimer.start();
        // NOT A PLANT OR DEFUSAL ATTEMPT ?
        } else if (e.getInventory().getType() == plugin.getBaseinv()){
            plugin.debug.log("NOBODY IS PLANTING OR DEFUSING THE BOMB");
            e.setCancelled(true);
        }
    } // END OF InventoryOpenEvent
    
    /**
     * Handles the event where the player does NOT complete the time required to plant/defuse the bomb. <br/>
     * 
     * Notice that are multiple ways to trigger this event: <br/>
     * - The bomb carrier prematurely closes the Brewing Stand 
     *   thereby canceling the plant process. <br/>
     * - The bomb carrier dies. <br/>
     * - Also, when the PlantTimer is finished, it will close the player inventory.
     *   So we need to be careful that it doesn't also cancel the 30
     * seconds that it takes to destroy the base. <br/><br/>
     *
     * @param e InventoryCloseEvent
     */
    @ArenaEventHandler(priority = EventPriority.HIGHEST)
    public void onBombPlantDefuseFailure(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();
        String type = e.getInventory().getType().toString();

        // EXIT CONDITIONS:
        if (e.getInventory().getType() != plugin.getBaseinv()) return;
        if (carrier == null) return;

        plugin.debug.log("onBombPlantDefuseFailure() has been called. Type = " + type);
        plugin.debug.log("carrier = " + carrier);
        
        cancelTimer(p);
        
    } // END OF InventoryCloseEvent
    
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
        plugin.debug.log("PlayerDeathEvent called");
        plugin.debug.msgArenaPlayers(getMatch().getPlayers(), "PlayerDeathEvent called for " + e.getEntity().getName());
        List<ItemStack> items = new ArrayList<ItemStack>(e.getDrops()); 
        for (ItemStack item : items) {
            Material itemType = item.getType();
            if (itemType == Material.COMPASS || itemType == plugin.getBombBlock()) {
                e.getDrops().remove(item);
            }
        }
        
        Player p = e.getEntity().getPlayer();
        cancelTimer(p);

        // Don't drop the bomb if the DetonationTimer is running...
        // Since the player won't even have it in their inventory.
        // And since we don't want PlayerDropItemEvent to clear plugin.carriers
        if (carrier == null || isDetonationTimerRunning()) return;
        Location loc = e.getEntity().getLocation();
        if (p.getName().equals(carrier)) {
            e.setDeathMessage("" + e.getEntity().getPlayer().getName()
                    + " has died and dropped the bomb at "
                    + " " + loc.getBlockX()
                    + " " + loc.getBlockY()
                    + " " + loc.getBlockZ());
            // e.getDrops().clear();
            // Item bomb = new Bomb(e);
            Item bomb = loc.getWorld().dropItem(loc, new ItemStack(plugin.getBombBlock()));

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
     * Only ends the match if the DetonationTimer doesn't exist.
     * Otherwise, let the DetonationTimer/DefuseTimer end the match.
     * The dead team can still win via the DetonationTimer.
     */
    @ArenaEventHandler
    public void onTeamDeathEvent(TeamDeathEvent e) {
        if (getDetonationTimer() == null) {
            MatchResult result = new MatchResult();
            ArenaTeam losers = e.getTeam();
            for (ArenaTeam t : getTeams()) {
                if (t != losers) {
                    result.setVictor(t);
                }
            }
            result.addLoser(losers);
            getMatch().endMatchWithResult(result);
        }
    } // END OF TeamDeathEvent
    
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
        plugin.debug.log("PlayerDropItemEvent called");
        final Player player = e.getPlayer();
        Material type = e.getItemDrop().getItemStack().getType();
        // To-do: make sure the bomb didn't get thrown outside the map
        if (type != plugin.getBombBlock()) {
            return;
        }
        if (carrier != null
                && e.getPlayer().getName().equals(carrier)) {
            if (e.getPlayer().getInventory() != null
                    && e.getPlayer().getInventory().getHelmet() != null
                    && e.getPlayer().getInventory().getHelmet().getType() == plugin.getBombBlock()) {
                e.getPlayer().getInventory().setHelmet(new ItemStack(Material.AIR));
            }
            // sets the carrier to null
            // as long as the DetonationTimer isn't running
            DetonationTimer detTimer = getDetonationTimer();
            if (detTimer == null) {
                this.carrier = null;
                msgAll(getMatch().getPlayers(), "The bomb has been dropped! Follow your compass.");

                final Item ibomb = e.getItemDrop();
                Location loc = exact(player);
                final AtomicInteger id = createBombHologram(loc);

                BukkitTask task = new BukkitRunnable() {

                    int ticks;

                    @Override
                    public void run() {
                        int x = (ibomb.isOnGround()) ? 10 : 1;
                        ticks = ticks + x;
                        if (ticks >= 100) {
                            cancel();
                            return;
                        }
                        Location exact = exact(player);
                        plugin.debug.log("" + ticks + " y=" + ibomb.getLocation().getY());
                        compass.pointTo(exact);
                        plugin.holograms().teleport(id.get(), ibomb.getLocation());
                    }
                }.runTaskTimer(plugin, 1L, 1L);
            }
        } else {
            plugin.getLogger().warning(""
                    + e.getPlayer().getName()
                    + " has tried to drop the bomb without ever picking it up. "
                    + "Are they cheating / exploiting ? Or is this a bug ? "
                    + "Please investigate this incident and if it's a bug, then "
                    + "notify Europia79 via hotmail, Bukkit, or github.");
            e.getItemDrop().remove();
        }


    } // END OF PlayerDropItemEvent
    
    public Location exact(Entity e) {
        Location exact_loc = e.getLocation();
        List<Entity> entities = e.getNearbyEntities(20.0, 50.0, 20.0);
        for (Entity entity : entities) {
            plugin.debug.log("" + entity.getType().toString() + " : " + entity.toString());
            if (entity.getType() == EntityType.DROPPED_ITEM) {
                Item item = null;
                try {
                    item = (Item) entity;
                } catch (ClassCastException ex) {
                    continue;
                }
                if (item.getItemStack().getType() == plugin.getBombBlock()) {
                    exact_loc = item.getLocation();
                }
            }
        }
        return exact_loc;
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
    protected void setCompass(Location loc) {
        if (compass == null) compass = new CompassHandler(this);
        compass.pointTo(loc);
        /*
        Set<ArenaPlayer> players = getMatch().getPlayers();
        for (ArenaPlayer p : players) {
            if (!p.getInventory().contains(Material.COMPASS)) {
                p.getInventory().addItem(new ItemStack(Material.COMPASS));
            }
            p.getPlayer().setCompassTarget(loc);
        }
        */
    }
    
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
            plugin.getLogger().warning("BombArena & SndArena require exactly 2 teams.");
            plugin.getLogger().warning("Match is being cancelled.");
            getMatch().cancelMatch();
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
        Set<ArenaPlayer> allplayers = getMatch().getPlayers();
        for (ArenaPlayer p : allplayers) {
            if (p != null && p.getInventory() != null) {
                if (p.getInventory().contains(Material.COMPASS)) {
                    p.getInventory().remove(Material.COMPASS);
                }
                if (p.getInventory().getHelmet() != null 
                        && p.getInventory().getHelmet().getType() == plugin.getBombBlock()) {
                    p.getInventory().setHelmet(new ItemStack(Material.AIR));
                }
                if (p.getInventory().contains(plugin.getBombBlock())) {
                    p.getInventory().remove(plugin.getBombBlock());
                }
            }
        }
        compass.cancel();
        compass = null;
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
        plugin.debug.log("onFinish matchID = " + matchID);
        cancelAndClearTimers();
        removeHolograms();
        this.carrier = null;
        resetBases();
    }
    
    protected void resetBases() {
        for (Location loc : savedBases) {
            World w = loc.getWorld();
            Block block = w.getBlockAt(loc);
            block.setType(plugin.getBaseBlock());
        }
    }
    
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
}
