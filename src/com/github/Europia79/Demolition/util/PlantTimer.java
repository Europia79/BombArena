package com.github.Europia79.Demolition.util;

import com.github.Europia79.Demolition.BombArenaListener;
import com.github.Europia79.Demolition.Main;
import java.util.Set;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.tracker.objects.WLT;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Nikolai
 */
public class PlantTimer extends BukkitRunnable {

    long startTime;
    int duration;
    private boolean cancelled;
    Main plugin;
    Match match;
    BombArenaListener arena;
    DetonateTimer dtimer;
    int i;
    String msg;
    InventoryOpenEvent event;
    Player player;
    Location BOMB_LOCATION;

    public PlantTimer(InventoryOpenEvent e) {
        startTime = System.nanoTime();
        this.plugin = (Main) Bukkit.getServer().getPluginManager().getPlugin("Demolition");
        this.event = e;
        this.player = (Player) e.getPlayer();
        this.BOMB_LOCATION = e.getPlayer().getLocation();
        this.duration = 7;
        i = 37;
    }

    public PlantTimer(InventoryOpenEvent e, Match m) {
        cancelled = false;
        startTime = System.nanoTime();
        this.plugin = (Main) Bukkit.getServer().getPluginManager().getPlugin("Demolition");
        this.event = e;
        this.player = (Player) e.getPlayer();
        this.BOMB_LOCATION = e.getPlayer().getLocation();
        this.duration = 7;
        i = 37;
        match = m;
    }

    public PlantTimer(InventoryOpenEvent e, BombArenaListener b) {
        startTime = System.nanoTime();
        this.plugin = (Main) Bukkit.getServer().getPluginManager().getPlugin("Demolition");
        this.event = e;
        this.player = (Player) e.getPlayer();
        this.BOMB_LOCATION = e.getPlayer().getLocation();
        this.duration = 7;
        i = 37;
        arena = b;
    }

    public long getCurrentTime() {
        return (System.nanoTime() / 1000000000);
    }

    public long getStartTime() {
        return (this.startTime / 1000000000);
    }

    public long getTimeElapsed() {
        return ((getCurrentTime() - getStartTime()));

    }

    @Override
    public void run() {
        this.duration = this.duration - 1;
        i = i - 1;
        msg = (duration >= 0) ? "" + duration : "" + i;
        player.sendMessage(msg);
        if (this.duration == 0) {
            Set<ArenaPlayer> allplayers = match.getPlayers(); // arena.getMatch().getPlayers();
            for (ArenaPlayer p : allplayers) {
                p.getPlayer().sendMessage("The bomb will detonate in 30 seconds !!!");
            }
            setBomb(event.getPlayer().getLocation(), 10);

            // event.setCancelled(true);
            player.closeInventory();
            // dtimer.runTaskTimer(plugin, 0L, 20L);
            // this.cancel();
        }
        // if (player.getLocation().distanceSquared(BEACON_LOCATION) > 64)

        if (i <= 0) {
            player.sendMessage(ChatColor.LIGHT_PURPLE
                    + "Congratulations, you have successfully destroyed their base.");
            plugin.ti.addPlayerRecord(player.getName(), "bombs planted", WLT.WIN);
            // Player p = plugin.getServer().getPlayer(plugin.carrier);
            ArenaTeam t = match.getArena().getTeam(player); // arena.getTeam(player);
            match.setVictor(t);
            this.cancel();
        }

    }
    
    public void setCancelled(boolean x) {
        if (x) {
            this.cancel();
        } 
        cancelled = x;
    }
    
    public boolean isCancelled() {
        return cancelled;
    }
    
    
    /*
     * Block Manipulation method from 
     * http://wiki.bukkit.org/Plugin_Tutorial
     */
    public void setBomb(Location loc, int length) {
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
                        currentBlock.setType(Material.HARD_CLAY);
                    }
                }
            }
        }
    }
}
