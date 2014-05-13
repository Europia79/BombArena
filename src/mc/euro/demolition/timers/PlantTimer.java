package mc.euro.demolition.util;

import mc.euro.demolition.BombPlugin;
import java.util.Set;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.ArenaPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Example: new PlantTimer(InventoryOpenEvent e, getMatch()). <br/><br/>
 * 
 * plugin.pTimers.put(getMatch().getID(), new PlantTimer(e, getMatch())); <br/>
 * plugin.pTimers.get(getMatch().getID()).runTaskTimer(plugin, 0L, 20L); <br/><br/>
 * 
 * This is always the 1st Timer to be started. <br/>
 * There can only be one PlantTimer per match. <br/><br/>
 * 
 * getPlayer() - used to get the player that started this Timer. <br/>
 * 
 */
public class PlantTimer extends BukkitRunnable {

    BombPlugin plugin;
    int duration;
    Match match;
    DetonationTimer dtimer;
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
        this.BOMB_LOCATION = plugin.getExactLocation(e.getPlayer().getLocation());
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
            if (player.getInventory().getHelmet().getType() == Material.TNT) {
                player.getInventory().setHelmet(new ItemStack(Material.AIR));
            }
            if (player.getInventory().contains(plugin.BombBlock)) {
                player.getInventory().remove(plugin.BombBlock);
                player.updateInventory();
            }
            plugin.detTimers.put(match.getID(), new DetonationTimer(event, match, BOMB_LOCATION));
            plugin.detTimers.get(match.getID()).runTaskTimer(plugin, 0L, 20L);
            
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
    
    public Player getPlayer() {
        return this.player;
    }
}
