package mc.euro.demolition.util;

import java.util.Set;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.tracker.objects.WLT;
import mc.euro.demolition.Main;
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
public class DetonateTimer extends BukkitRunnable {
    
    int i;
    Main plugin;
    Match match;
    InventoryOpenEvent event;
    Player player;
    Location BOMB_LOCATION;
    boolean cancelled;

    DetonateTimer(InventoryOpenEvent e, Match m, Location loc) {
        this.cancelled = false;
        i = 31;
        this.plugin = (Main) Bukkit.getPluginManager().getPlugin("Demolition");
        this.event = e;
        this.match = m;
        this.player = (Player) e.getPlayer();
        this.BOMB_LOCATION = loc;
        
    }

    @Override
    public void run() {
        i = i - 1;
        match.sendMessage("" + i);
        
        if (i <= 0) {
            ArenaTeam t = match.getArena().getTeam(player);
            t.sendMessage(ChatColor.LIGHT_PURPLE 
                    + "Congratulations, "
                    + t.getTeamChatColor() + player.getName() + ChatColor.LIGHT_PURPLE
                    + " has successfully destroyed the other teams base.");
            plugin.ti.addPlayerRecord(player.getName(), "Bombs Planted Defused", WLT.WIN);
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
    
    private void createExplosion(Location here) {
        here.getBlock().setType(Material.AIR);
        for (int x = -1; x < 1; x++) {
            for (int z = -1; z < 1; z++) {
                for (int y = 0; y < 1; y++) {
                    double xp = here.getX() + x;
                    double yp = here.getY() + y;
                    double zp = here.getZ() + z;
                    Location temp = new Location(here.getWorld(), xp, yp, zp);
                    here.getWorld().createExplosion(temp, 0L);
                }
            }
        }
        killPlayers(here);
    }
    
    private void killPlayers(Location loc) {
        Set<ArenaPlayer> players = match.getPlayers();
        for (ArenaPlayer p : players) {
            double distance = p.getLocation().distance(loc);
            if (distance <= 9) {
                double dmg = 50 - (distance * 5);
                p.getPlayer().damage(dmg);
            }
        }
    }
    
}


