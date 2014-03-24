package com.github.Europia79.Demolition;

import com.github.Europia79.Demolition.objects.Bomb;
import com.github.Europia79.Demolition.util.DetonateTimer;
import com.github.Europia79.Demolition.util.PlantTimer;
import java.util.Set;
import mc.alk.arena.events.players.ArenaPlayerLeaveEvent;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.teams.ArenaTeam;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
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
 * onBombDespawn() - respawn or cancel the event. 
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
        e.getPlayer().sendMessage("onBombPickup() Listener works!");
        plugin.debug.messagePlayer(e.getPlayer(), "debug works!");

        // To-Do: sudo player hat bomb
        if (e.getItem().getItemStack().getType() == Material.HARD_CLAY) {
            if (plugin.carrier == null) {
                plugin.carrier = e.getPlayer().getName();
            } else {
                e.setCancelled(true);
                plugin.debug.messagePlayer(e.getPlayer(), 
                        "There can only be ONE bomb per Match. "
                        + plugin.carrier + " currently has the bomb.");
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
        // drop the bomb on the ground
        if (plugin.carrier == null) {
            return;
        }
        if (plugin.carrier.equalsIgnoreCase(e.getEntity().getPlayer().getName())) {
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
                /*e.getEntity().getPlayer().getWorld().dropItemNaturally(
                    e.getEntity().getPlayer().getLocation(), 
                    new ItemStack(Material.HARD_CLAY)).setPickupDelay(40);
                */
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
        // To-do: make sure the bomb didn't get thrown outside the map
        Location loc = e.getItemDrop().getLocation();
        if (e.getItemDrop().getItemStack().getType() == Material.HARD_CLAY) {
            if (plugin.carrier != null 
                    && plugin.carrier.equalsIgnoreCase(e.getPlayer().getName())) {
                plugin.carrier = null;
                // get all arena players inside this Match. 
                // set their compass direction.
                Set<ArenaPlayer> allplayers = getMatch().getPlayers();
                for (ArenaPlayer p : allplayers) {
                    p.getPlayer().setCompassTarget(loc);
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
        // temporary place holder
        // until I get time to implement this method.
        // respawn a new bomb OR cancel the event
        if (e.getEntity().getItemStack().getType() == Material.HARD_CLAY 
                && plugin.carrier != null) {
            Set<ArenaPlayer> allplayers = getMatch().getPlayers();
            for (ArenaPlayer p : allplayers) {
                plugin.debug.messagePlayer(p.getPlayer(), "Bomb despawned cancelled. ");
            }
            e.setCancelled(true);
        } else {
            plugin.debug.msgArenaPlayers(getMatch().getPlayers(), 
                    "Bomb despawn allowed because " + plugin.carrier + " has the bomb.");
        }

    } */
    
    /**
     * This method handle the scenario when players attempt 
     * to place the bomb on the ground like it's a block.
     */
    @ArenaEventHandler
    public void onBombPlace(BlockPlaceEvent e) {
        // Get the coordinates to the base
        // calculate the distance to the base
        // if the distance is small, attempt to trigger onBombPlant()
        if (e.getBlockPlaced().getType() == Material.HARD_CLAY) {
            e.getPlayer().sendMessage("Improper bomb activation!");
            // get the player
            // get his team
            // get the OTHER team
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
        Player p = (Player) e.getPlayer();
        plugin.debug.messagePlayer(p, "onBombPlant() has been called");
        plugin.debug.messagePlayer(p, 
                "carrier = " + plugin.carrier);
        // To-do: ARE THEY AT THE CORRECT BASE ?
        // start 7 second PlantTimer
        if (e.getInventory().getType() == InventoryType.BREWING 
                && plugin.carrier != null 
                && e.getPlayer().getName().equalsIgnoreCase(plugin.carrier)) {
            // converted a single Timer to one for each match.
            plugin.pTimers.put(getMatch().getID(), new PlantTimer(e, getMatch()));
            plugin.pTimers.get(getMatch().getID()).runTaskTimer(plugin, 0L, 20L);
            // plugin.ptimer = new PlantTimer(e, getMatch());
            // plugin.ptimer.runTaskTimer(plugin, 0L, 20L);
        } else {
            plugin.debug.messagePlayer(p, "e.setCancelled(true);");
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
        // Is it a brewing stand ?
        // Are they trying to plant ?
        
        if (e.getPlayer().getInventory().getType() == InventoryType.BREWING
                && plugin.carrier != null 
                && plugin.carrier.equalsIgnoreCase(e.getPlayer().getName())) {
            // if this is an actual death or drop
            // then let's let those Events handle setting 
            // the carrier to null
            // plugin.carrier = null;
            Set<ArenaPlayer> allplayers = getMatch().getPlayers();
            for (ArenaPlayer p : allplayers) {
                plugin.debug.messagePlayer(p.getPlayer(),"onBombPantFailure has been called "
                        + "due to InventoryCloseEvent");
            }
            plugin.pTimers.get(getMatch().getID()).cancel();
            plugin.ptimer.cancel();
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
        // Cancel timers and declare the winners
        if (e.getBlock().getType() == Material.HARD_CLAY) {
            ArenaTeam t = getTeam(e.getPlayer());
            getMatch().setVictor(t);
        }
        
    }
   
    
}
