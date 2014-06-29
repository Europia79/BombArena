package mc.euro.demolition.timers;

import mc.euro.demolition.timers.DefuseTimer;
import java.util.Set;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.tracker.objects.WLT;
import mc.euro.demolition.BombPlugin;
import mc.euro.demolition.tracker.OUTCOME;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Example: new DetonationTimer(event, match, BOMB_LOCATION). <br/><br/>
 * 
 * plugin.detTimers.put(match.getID(), new DetonationTimer(event, match, BOMB_LOCATION)); <br/>
 * plugin.detTimers.get(match.getID()).runTaskTimer(plugin, 0L, 20L); <br/>
 * player.closeInventory(); <br/><br/>
 * 
 * This is always the 2nd Timer to be started. <br/>
 * There can only be one DetonationTimer per match. <br/>
 * 
 * getPlayer() - used to get the player that started this timer. <br/>
 * 
 */
public class DetonationTimer extends BukkitRunnable {
    
    BombPlugin plugin;
    int duration;
    Match match;
    InventoryOpenEvent event;
    Player player;
    Location BOMB_LOCATION;
    boolean cancelled;
    
    DefuseTimer counter;

    DetonationTimer(InventoryOpenEvent e, Match m, Location loc) {
        this.cancelled = false;
        this.plugin = (BombPlugin) Bukkit.getPluginManager().getPlugin("BombArena");
        this.duration = plugin.getDetonationTime() + 1;
        this.event = e;
        this.match = m;
        this.player = (Player) e.getPlayer();
        this.BOMB_LOCATION = loc;
        
    }

    @Override
    public void run() {
        duration = duration - 1;
        match.sendMessage("" + duration);
        
        if (duration <= 0) {
            ArenaTeam t = match.getArena().getTeam(player);
            t.sendMessage(ChatColor.LIGHT_PURPLE 
                    + "Congratulations, "
                    + t.getTeamChatColor() + player.getName() + ChatColor.LIGHT_PURPLE
                    + " has successfully destroyed the other teams base.");
            plugin.ti.addPlayerRecord(player.getName(), plugin.getFakeName(), "WIN");
            createExplosion(BOMB_LOCATION);
            match.setVictor(t);
            this.setCancelled(true);
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
    
    private void createExplosion(Location here) {
        here.getBlock().setType(Material.AIR);
        for (int x = -1; x < 1; x++) {
            for (int z = -1; z < 1; z++) {
                for (int y = 0; y < 1; y++) {
                    double xp = here.getX() + x;
                    double yp = here.getY() + y;
                    double zp = here.getZ() + z;
                    Location t = new Location(here.getWorld(), xp, yp, zp);
                    here.getWorld().createExplosion(
                            t.getX(), t.getY(), t.getZ(), 4F, false, false);
                }
            }
        }
        killPlayers(here);
    }
    
    private void killPlayers(Location loc) {
        Set<ArenaPlayer> players = match.getPlayers();
        for (ArenaPlayer p : players) {
            double distance = p.getLocation().distance(loc);
            if (distance <= plugin.getDamageRadius()) {
                double dmg = plugin.getMaxDamage() - (distance * plugin.getDeltaDamage());
                p.getPlayer().damage(dmg);
            }
        }
    }
    
}


