package com.github.Europia79.Demolition;

import com.github.Europia79.Demolition.objects.Bomb;
import com.github.Europia79.Demolition.util.DetonateTimer;
import com.github.Europia79.Demolition.util.PlantTimer;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mc.alk.arena.events.players.ArenaPlayerLeaveEvent;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.util.WorldGuardUtil;
import mc.alk.tracker.objects.WLT;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Nikolai
 *
 * Bomb = Hardened Clay 172
 *
 * Listen for 
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
 * multiple listeners are currently not possible
 * because addArenaListener() is not working: 
 * from constructor, init(), or onBegin().
 *
 */
public class BombArenaListener extends Arena {

    Main plugin;

    // constructor
    public BombArenaListener() {
        plugin = (Main) Bukkit.getPluginManager().getPlugin("Demolition");

    }

    /**
     * Give the bomb carrier a hat so that other players know WHO has the bomb.
     */
    @ArenaEventHandler
    public void onBombPickup(PlayerPickupItemEvent e) {
        int matchID = getMatch().getID();
        String c = (plugin.carriers.get(matchID) == null) ? null : plugin.carriers.get(matchID);
        e.getPlayer().sendMessage("onBombPickup() Listener works!");
        plugin.debug.messagePlayer(e.getPlayer(), "debug works!");

        // To-Do: sudo player hat bomb
        if (e.getItem().getItemStack().getType() == Material.HARD_CLAY) {
            if (c == null) {
                c = e.getPlayer().getName();
                plugin.carriers.put(matchID, c);
                ArenaTeam team2 = null;
                try {
                    team2 = getOtherTeam(e.getPlayer());
                } catch (NullPointerException ex) {
                    plugin.getLogger().severe("Stopping match because getOtherTeam() method failed");
                    getMatch().cancelMatch();
                }
                setCompass(getMatch().getPlayers(), team2);
            } else {
                e.setCancelled(true);
                plugin.debug.messagePlayer(e.getPlayer(), 
                        "There can only be ONE bomb per Match. "
                        + c + " currently has the bomb.");
                e.getItem().remove();
            }


        }
    }
    
    /**
     * Handle the scenario where a player
     * gets the bomb then logs out or leaves the arena.
     */
    @ArenaEventHandler
    public void onBombCarrierLeave(ArenaPlayerLeaveEvent e) {
        int id = getMatch().getID();
        String c = (plugin.carriers.get(id) == null) ? null : plugin.carriers.get(id);
        
    }
    
    /**
     * Two ways to implement this: 
     * 1. Get the location and spawn a new bomb or
     * 2. Make sure the player drops the bomb. 
     * During testing, make sure that the
     * PlayerDropItemEvent is triggered.
     */
    @ArenaEventHandler
    public void onBombCarrierDeath(PlayerDeathEvent e) {
        int id = getMatch().getID();
        String c = (plugin.carriers.get(id) == null) ? null : plugin.carriers.get(id);
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
        
    }
    
    /**
     * 1. Make sure the bomb didn't get thrown outside of the map 2. Point the
     * compass to the direction of the bomb 3. and give a visual aid so that
     * players know the location of bombs when they're on the ground.
     */
    @ArenaEventHandler
    public void onBombDrop(PlayerDropItemEvent e) {
        int matchID = getMatch().getID();
        String c = (plugin.carriers.get(matchID) == null) ? null : plugin.carriers.get(matchID);
        Material type = e.getItemDrop().getItemStack().getType();
        // To-do: make sure the bomb didn't get thrown outside the map
        Location loc = e.getItemDrop().getLocation();
        if (type == Material.HARD_CLAY) {
            if (c != null 
                    && e.getPlayer().getName().equals(c)) {
                // sets the carrier to null
                plugin.carriers.remove(matchID);
                // get all arena players inside this Match. 
                // set their compass direction.
                Set<ArenaPlayer> allplayers = getMatch().getPlayers();
                for (ArenaPlayer p : allplayers) {
                    p.getPlayer().setCompassTarget(loc);
                    p.sendMessage("The bomb has been dropped! Follow your compass.");
                }
            } else {
                plugin.getLogger().warning(""
                        + e.getPlayer().getName()
                        + "has tried to drop the bomb without ever picking it up. "
                        + "Are they cheating / exploiting ? Or is this a bug ? "
                        + "Please investigate this incident and if it's a bug, then "
                        + "notify Europia79@hotmail.com OR on the Bukkit forums.");
            }

        }
    }
    
    /**
     * respawn a new bomb OR cancel the event.
     * This event breaks ALL other events.
     */
    /* @ArenaEventHandler
    public void onBombDespawn(ItemDespawnEvent e) {
        int id = getMatch().getID();
        String c = (plugin.carriers.get(id) == null) ? null : plugin.carriers.get(id);
        // temporary place holder
        // until I get time to implement this method.
        // respawn a new bomb OR cancel the event
        if (e.getEntity().getItemStack().getType() == Material.HARD_CLAY 
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

    } */
    
    /**
     * This method handle the scenario when players attempt 
     * to place the bomb on the ground like it's a block.
     * This method is going to help out new players 
     * by checking the distance to the base: 
     * if the distance is small, then trigger onBombPlant(InventoryOpenEven e).
     * if the distance is too large, then give the player helpful hints 
     * about the distance and compass direction to the enemy base.
     */
    @ArenaEventHandler
    public void onBombPlace(BlockPlaceEvent e) {
        // Get the coordinates to the base
        // calculate the distance to the base
        // if the distance is small, attempt to trigger onBombPlant()
        if (e.getBlockPlaced().getType() == Material.HARD_CLAY) {
            e.getPlayer().sendMessage("Improper bomb activation!");
            
            // get the other team's base location
            // set the player compass
            // and msg him to follow the compass
            // e.getPlayer().getInventory().addItem(new ItemStack(Material.HARD_CLAY));
            e.setCancelled(true);
            // updateInventory() is deprecated
            // Must eventually find another solution.
            e.getPlayer().updateInventory();
        }
    }
    
    /**
     * 1. Use a brewing stand for each base. 2. event cancels (inventory closes)
     * after 5 to 8 seconds.
     */
    @ArenaEventHandler
    public void onBombPlant(InventoryOpenEvent e) {
        int matchID = getMatch().getID();
        String c = (plugin.carriers.get(matchID) == null) ? null : plugin.carriers.get(matchID);
        // Player carrier = (c == null) ? null : Bukkit.getPlayer(c);
        Player planter = (Player) e.getPlayer();
        int teamID = getTeam(planter).getId();
        plugin.debug.messagePlayer(planter, "onBombPlant() has been called");
        plugin.debug.messagePlayer(planter, 
                "carrier = " + c );
        // ARE THEY AT THE CORRECT BASE ?
        // Use the Match ID to get all the player bases, then
        // get the bomb carriers base:
        // Compare his current position with the position of HIS OWN BASE
        // to make sure he's not trying to plant the bomb at his own base.
        if (e.getInventory().getType() == InventoryType.BREWING 
                && c != null 
                && e.getPlayer().getName().equalsIgnoreCase(c) 
                && plugin.bases.get(matchID).get(teamID).distance(planter.getLocation()) > 30) {
            // converted a single Timer to one for each match.
            plugin.pTimers.put(getMatch().getID(), new PlantTimer(e, getMatch()));
            plugin.pTimers.get(getMatch().getID()).runTaskTimer(plugin, 0L, 20L);
            // plugin.ptimer = new PlantTimer(e, getMatch());
            // plugin.ptimer.runTaskTimer(plugin, 0L, 20L);
        } else {
            plugin.debug.messagePlayer(planter, "event.setCancelled(true);");
            e.setCancelled(true);
        }
    }
    
    /**
     * 
     * 
     * 
     */
    @ArenaEventHandler
    public void onBombPlantFailure(InventoryCloseEvent e) {
        int matchID = getMatch().getID();
        String c = (plugin.carriers.get(matchID) == null) ? null : plugin.carriers.get(matchID);
        // Is it a brewing stand ?
        // Are they trying to plant ?
        if (e.getPlayer().getInventory().getType() == InventoryType.BREWING 
                && c != null 
                && e.getPlayer().getName().equalsIgnoreCase(c)) {
            // if this is an actual death or drop then those Events 
            // will handle setting the carrier to null
            plugin.debug.msgArenaPlayers(getMatch().getPlayers(), 
                    "onBombPlantFailure has been called " 
                    + "due to InventoryCloseEvent");
            plugin.pTimers.get(matchID).cancel();
        }

    }
    
    /**
     * onBombDefusal(BlockBreakEvent event)
     * 
     * Make sure that the player (or his teammates) who just planted the bomb
     * cannot turn around and defuse it for the win.
     */
    @ArenaEventHandler
    public void onBombDefusal(BlockBreakEvent e) {
        int matchID = getMatch().getID();
        String c = (plugin.carriers.get(matchID) == null) ? null : plugin.carriers.get(matchID);
        // Cancel timers and declare the winners
        if (e.getBlock().getType() == Material.HARD_CLAY 
                && getTeam(e.getPlayer()) != getTeam(Bukkit.getServer().getPlayer(c))) {
            Set<ArenaPlayer> allplayers = getMatch().getPlayers();
            for (ArenaPlayer p : allplayers) {
                p.sendMessage("" + e.getPlayer().getName() 
                        + " has defused the bomb.");
                if (getTeam(p) == getTeam(e.getPlayer())) {
                    p.sendMessage("You win!");
                } else {
                    p.sendMessage("You lost!");
                }
            }
            plugin.ti.addPlayerRecord(e.getPlayer().getName(), "bombs defused", WLT.WIN);
            plugin.ti.addPlayerRecord(c, "bombs planted", WLT.LOSS);
            ArenaTeam t = getTeam(e.getPlayer());
            getMatch().setVictor(t);
            plugin.pTimers.get(matchID).cancel();
        } else if (e.getBlock().getType() == Material.HARD_CLAY 
                && getTeam(e.getPlayer()) == getTeam(Bukkit.getServer().getPlayer(c))) {
            e.getPlayer().sendMessage("If you defuse the bomb, then the other team will win.");
            e.setCancelled(true);
        }
        
    }
    
    // This is the Order in which they're called by BattleArena:
    // onBegin() called first.
    @Override
    public void onBegin() {
        super.onBegin(); 
        plugin.getLogger().info("onBegin() has been called by Demolition plugin: BombArenaListener.java");
    }
    
    // onStart() called at the start of a Match.
    @Override
    public void onStart() {
        super.onStart();
        plugin.debug.msgArenaPlayers(getMatch().getPlayers(), "onStart");
        // temporary:
        plugin.carriers.clear();
        // World w = getWorldGuardRegion().getWorld();
        assignBases(getMatch().getTeams());
        // plugin.tbases.get(getMatch().getID()).
    }
    
    // onComplete() is called before money is given.
    @Override
    public void onComplete() {
        super.onComplete();
        plugin.debug.msgArenaPlayers(getMatch().getPlayers(), "onComplete");
    }
    
    // onFinish() is called after money is given.
    @Override
    public void onFinish() {
        super.onFinish();
        plugin.debug.msgArenaPlayers(getMatch().getPlayers(), "onFinish");
    }
    
    /*
     * For what teams ?
     */
    public void assignBases(List<ArenaTeam> bothTeams) {
        
        WorldGuardPlugin wg = WGBukkit.getPlugin();
        
        for (ArenaTeam t : bothTeams) {
            Set<Player> playerzSet = t.getBukkitPlayers();
            Player playerOne = null;
            // Use the 1st player on the Team to assign the base
            // for the whole team.
            for (Player first : playerzSet) {
                playerOne = first;
                break;
            }
            World w = playerOne.getWorld();
            RegionManager manager = wg.getRegionManager(w);
            Location loc = playerOne.getLocation();
            ApplicableRegionSet set = manager.getApplicableRegions(loc);
            Vector teleportV = null;
            Vector spawnV = null;            
            for (ProtectedRegion region : set) {
                if (region.getFlag(DefaultFlag.TELE_LOC) != null) {
                    teleportV = region.getFlag(DefaultFlag.TELE_LOC).getPosition();
                }
                if (region.getFlag(DefaultFlag.SPAWN_LOC) != null) {
                    spawnV = region.getFlag(DefaultFlag.SPAWN_LOC).getPosition();
                }
            }
            // COMPARE THESE TWO POINTS WITH THE PLAYER TO DETERMINE
            //        WHICH BASE IS CLOSER.
            Location teleportXYZ = new Location(w,
                    teleportV.getBlockX(), teleportV.getBlockY(), teleportV.getBlockZ());
            Location spawnXYZ = new Location(w, 
                    spawnV.getBlockX(), spawnV.getBlockY(), spawnV.getBlockZ());
            
            double tdistance = playerOne.getLocation().distance(teleportXYZ);
            double sdistance = playerOne.getLocation().distance(spawnXYZ);
            
            if (tdistance < sdistance) {
                assignBase(t.getId(), teleportXYZ);
            } else if ( tdistance > sdistance) {
                assignBase(t.getId(), spawnXYZ);
            } else if (tdistance == sdistance) {
                plugin.getLogger().warning("Could NOT assign bases because " 
                        + "the player's spawn is equi-distance to both.");
                plugin.getLogger().info("Please change the spawn locations " 
                        + "for the teams in the bomb arena.");
            }
            
            
        }
    }
    
    /*
     * For what team ?
     * At what location ?
     * 
     */
    public void assignBase(int teamID, Location loc) {
        int length = 5;
        int matchID = getMatch().getID();
        Map<Integer, Location> temp = new HashMap<Integer, Location>();
        Map<String, Location> ptemp = new HashMap<String, Location>();

        // Set one corner of the cube to the given location.
        // Uses getBlockN() instead of getN() to avoid casting to an int later.
        int x1 = loc.getBlockX() - length;
        int y1 = loc.getBlockY() - length;
        int z1 = loc.getBlockZ() - length;

        // Figure out the opposite corner of the cube by taking the corner and adding length to all coordinates.
        int x2 = loc.getBlockX() + length;
        int y2 = loc.getBlockY() + length;
        int z2 = loc.getBlockZ() + length;

        World world = loc.getWorld();

        // Loop over the cube in the x dimension.
        for (int xPoint = x1; xPoint <= x2; xPoint++) {
            // Loop over the cube in the y dimension.
            for (int yPoint = y1; yPoint <= y2; yPoint++) {
                // Loop over the cube in the z dimension.
                for (int zPoint = z1; zPoint <= z2; zPoint++) {
                    // Get the block that we are currently looping over.
                    Block currentBlock = world.getBlockAt(xPoint, yPoint, zPoint);
                    // Set the block to type 57 (Diamond block!)
                    if (currentBlock.getType() == Material.BREWING_STAND) {
                        // currentBlock.setType(Material.HARD_CLAY);
                        Location base_loc = new Location(world, xPoint, yPoint, zPoint);
                        temp.put(teamID, base_loc);
                        // ptemp.put(p.getName(), base_loc);
                        plugin.bases.put(matchID, temp);
                        // Checking for the correct base below:

                        // plugin.bases.get(matchID).get(teamID).distance(player.getLocation);
                        // plugin.pbases.get(id).get(p.getName()).distance(p.getLocation());
                        // if the distance is less than 20, then don't allow the bomb to be planted.
                    }
                }
            }
        }
    }
    
    /**
     *
     * @param p = Player
     * @return ArenaTeam
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

    private void setCompass(Set<ArenaPlayer> players, ArenaTeam team2) {
        plugin.debug.log("setCompass(), team2 = " + team2);
        int matchID = getMatch().getID();
        int teamID = team2.getId();
        plugin.debug.log("getMatch().getID() = " + matchID);
        plugin.debug.log("plugin.bases.get(matchID).get(teamID) = "
                + plugin.bases.get(matchID).get(teamID));
        
        // NullPointerException if the player doesn't have a compass
        // in their inventory
        for (ArenaPlayer p : players) {
            p.getPlayer().setCompassTarget(plugin.bases.get(matchID).get(teamID)); // 519
            if (p.getTeam().getId() == teamID) {
                
            } else {
                
            }
        }
    }
    
    
}
