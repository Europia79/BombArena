package mc.euro.demolition.util;

import mc.euro.demolition.BombPlugin;
import java.util.Set;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.ArenaPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Example: new PlantTimer(InventoryOpenEvent e, getMatch()). <br/><br/>
 * 
 * plugin.pTimers.put(getMatch().getID(), new PlantTimer(e, getMatch())); <br/>
 * plugin.pTimers.get(getMatch().getID()).runTaskTimer(plugin, 0L, 20L); <br/>
 */
public class PlantTimer extends BukkitRunnable {

    BombPlugin plugin;
    int duration;
    Match match;
    DetonateTimer dtimer;
    InventoryOpenEvent event;
    Player player;
    Location BOMB_LOCATION;
    Long startTime;
    private boolean cancelled;

    public PlantTimer(InventoryOpenEvent e, Match m) {
        cancelled = false;
        this.plugin = (BombPlugin) Bukkit.getServer().getPluginManager().getPlugin("BombArena");
        this.duration = this.plugin.PlantTime;
        this.event = e;
        this.match = m;
        this.player = (Player) e.getPlayer();
        this.BOMB_LOCATION = e.getPlayer().getLocation();
    }

    @Override
    public void run() {
        duration = duration - 1;
        match.sendMessage("" + duration);
        
        if (duration == 0) {
            Set<ArenaPlayer> allplayers = match.getPlayers();
            for (ArenaPlayer p : allplayers) {
                p.getPlayer().sendMessage("The bomb will detonate in " 
                        + plugin.DetonationTime + " seconds !!!");
            }
            setBomb(event.getPlayer().getLocation(), 10);
            
            plugin.dTimers.put(match.getID(), new DetonateTimer(event, match, BOMB_LOCATION));
            plugin.dTimers.get(match.getID()).runTaskTimer(plugin, 0L, 20L);
            player.closeInventory();
        }

    }
    
    public void setCancelled(boolean x) {
        this.cancelled = x;
        if (x) {
            this.cancel();
        } 
    }
    
    public boolean isCancelled() {
        return this.cancelled;
    }
    
    /**
     * Block Manipulation method from
     * http://wiki.bukkit.org/Plugin_Tutorial <br/><br/>
     * 
     * This method takes an approximate location and finds the exact location of a base.
     * 
     * @param loc The approximate area that needs to be checked for a base.
     * @param length This is the radius around the location that you want to search for a base.
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
                    // Set the block to type 172 (HARD_CLAY) or plugin.BombBlock
                    if (currentBlock.getType() == plugin.BaseBlock) {
                        currentBlock.setType(plugin.BombBlock);
                        this.BOMB_LOCATION = currentBlock.getLocation();
                        /*
                        Set<ArenaPlayer> players = match.getPlayers();
                        for (ArenaPlayer p : players) {
                            p.getPlayer().sendBlockChange(BOMB_LOCATION, Material.TNT, (byte) 0);
                        } */
                    }
                }
            }
        }
    }
    

}
