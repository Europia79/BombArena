package mc.euro.demolition.timers;

import java.util.Set;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.ArenaPlayer;
import mc.euro.demolition.BombPlugin;
import mc.euro.demolition.arenas.EodArena;
import mc.euro.demolition.util.MatchUtil;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
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
    EodArena arena;
    Player player;
    final Location BOMB_LOCATION;
    DetonationTimer detTimer;
    boolean cancelled = true;
    
    public PlantTimer(EodArena arena) {
        this.plugin = (BombPlugin) Bukkit.getPluginManager().getPlugin("BombArena");
        this.duration = this.plugin.getPlantTime() + 1;
        this.arena = arena;
        this.player = Bukkit.getPlayer(arena.getBombCarrier());
        this.BOMB_LOCATION = plugin.getExactLocation(player.getLocation());
    }

    @Override
    public void run() {
        plugin.debug.log("PlantTimer (" + getTaskId() + ") is running: " + duration);
        if (cancelled) return;
        duration = duration - 1;
        player.playSound(player.getLocation(), Sound.ARROW_HIT, 1F, 1F);
        player.sendMessage("" + duration);
        
        if (duration == 0) {
            Set<ArenaPlayer> allplayers = arena.getMatch().getPlayers();
            for (ArenaPlayer p : allplayers) {
                p.getPlayer().sendMessage("The bomb will detonate in " 
                        + plugin.getDetonationTime() + " seconds !!!");
            }
            if (player.getInventory() != null
                    && player.getInventory().getHelmet() != null
                    && player.getInventory().getHelmet().getType() == Material.TNT) {
                player.getInventory().setHelmet(new ItemStack(Material.AIR));
            }
            if (player.getInventory() != null 
                    && player.getInventory().contains(plugin.getBombBlock())) {
                player.getInventory().remove(plugin.getBombBlock());
                player.updateInventory();
            }
            this.detTimer = new DetonationTimer(arena, BOMB_LOCATION).start();
            
            player.closeInventory();
            // MatchUtil.continueMatchOnExpiration(arena.getMatch());
            MatchUtil.setTime(arena.getMatch(), plugin.getDetonationTime());
        }
        
    }
    
    public PlantTimer start() {
        this.cancelled = false;
        runTaskTimer(plugin, 0L, 20L);
        return this;
    }
    
    public void setCancelled(boolean stop) {
        plugin.debug.log("PlantTimer->setCancelled(" + stop + ")");
        this.cancelled = stop;
        cancel();
    }
    
    public boolean isDetonationTimerRunning() {
        return duration <= 0 && detTimer != null;
    }
    
    public DetonationTimer getDetonationTimer() {
        return this.detTimer;
    }

    public void cancelDetonationTimer() {
        this.detTimer.cancel();
        this.detTimer = null;
    }
}
