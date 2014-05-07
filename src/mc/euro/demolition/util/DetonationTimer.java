package mc.euro.demolition.util;

import java.util.Set;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.tracker.objects.WLT;
import mc.euro.demolition.BombPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Possible future use for being responsible for the Detonation timer (instead of PlantTimer). <br/><br/>
 * 
 * Right now, the PlantTimer handles the 8 seconds plant timer AND the 30 second detonation timer. <br/>
 * The 30 sec timer might be refactored here in the future. <br/>
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
        this.duration = plugin.DetonationTime + 1;
        this.event = e;
        this.match = m;
        this.player = (Player) e.getPlayer();
        this.BOMB_LOCATION = loc;
        
        this.counter = new DefuseTimer(m.getPlayers());
        
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
            plugin.ti.addPlayerRecord(player.getName(), plugin.FakeName, WLT.WIN);
            createExplosion(BOMB_LOCATION);
            match.setVictor(t);
            this.setCancelled(true);
        }
        
    }
    
    public DefuseTimer getCounter() {
        return this.counter;
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
            if (distance <= plugin.DamageRadius) {
                double dmg = plugin.MaxDamage - (distance * plugin.DeltaDamage);
                p.getPlayer().damage(dmg);
            }
        }
    }
    
}


