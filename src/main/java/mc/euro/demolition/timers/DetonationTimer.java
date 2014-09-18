package mc.euro.demolition.timers;

import java.util.Set;
import java.util.logging.Level;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.euro.demolition.BombPlugin;
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
    Player planter;
    Location BOMB_LOCATION;
    boolean cancelled;
    
    DefuseTimer counter;

    DetonationTimer(InventoryOpenEvent e, Match m, Location loc) {
        this.cancelled = false;
        this.plugin = (BombPlugin) Bukkit.getPluginManager().getPlugin("BombArena");
        this.duration = plugin.getDetonationTime() + 1;
        this.event = e;
        this.match = m;
        this.planter = (Player) e.getPlayer();
        this.BOMB_LOCATION = loc;
        
    }

    @Override
    public void run() {
        duration = duration - 1;
        match.sendMessage("" + duration);
        
        if (duration <= 0) {
            createExplosion(BOMB_LOCATION);
            plugin.ti.addPlayerRecord(planter.getName(), plugin.getFakeName(), "WIN");
            try {
                ArenaTeam t = match.getArena().getTeam(planter);
                t.sendMessage(ChatColor.LIGHT_PURPLE
                        + "Congratulations, "
                        + t.getTeamChatColor() + planter.getName() + ChatColor.LIGHT_PURPLE
                        + " has successfully destroyed the other teams base.");
                match.setVictor(t);
            } catch (NullPointerException ex) {
                // Should be fixed in v1.1.4
                plugin.getLogger().severe("NPE caused by using the wrong victoryCondition.");
                plugin.getLogger().severe("Please change BombArenaConfig.yml victoryCondition node to");
                plugin.getLogger().severe("victoryCondition: NoTeamsLeft");
                plugin.getLogger().log(Level.SEVERE, null, ex);
            }
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
    
    public Player getBombPlanter() {
        return this.planter;
    }
    
    public Location getLocation() {
        return this.BOMB_LOCATION;
    }

    private void createExplosion(Location here) {
        here.getBlock().setType(Material.AIR);
        double xp = here.getX();
        double yp = here.getY();
        double zp = here.getZ();
        Location t = new Location(here.getWorld(), xp, yp, zp);
        here.getWorld().createExplosion(
                t.getX(), t.getY(), t.getZ(), 4F, false, false);
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


