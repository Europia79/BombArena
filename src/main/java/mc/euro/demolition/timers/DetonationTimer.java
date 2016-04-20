package mc.euro.demolition.timers;

import java.util.Set;
import java.util.logging.Level;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.euro.demolition.BombPlugin;
import mc.euro.demolition.arenas.EodArena;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
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
    EodArena arena;
    Player planter;
    Location BOMB_LOCATION;
    boolean cancelled;
    
    DefuseTimer counter;

    DetonationTimer(EodArena a, Location loc) {
        this.plugin = (BombPlugin) Bukkit.getPluginManager().getPlugin("BombArena");
        this.duration = plugin.getDetonationTime() + 1;
        this.arena = a;
        this.planter = Bukkit.getPlayer(arena.getBombCarrier());
        this.BOMB_LOCATION = loc;
        
    }

    @Override
    public void run() {
        duration = duration - 1;
        // This makes the Chat Countdown optional:
        plugin.debug.msgArenaPlayers(arena.getMatch().getPlayers(), "" + duration);
        plugin.playTimerSound(BOMB_LOCATION, arena.getMatch().getPlayers());
        
        if (duration == 0) {
            cancel();
            plugin.ti.addPlayerRecord(planter.getName(), plugin.getFakeName(), "WIN");
            try {
                ArenaTeam t = arena.getTeam(planter);
                t.sendMessage(ChatColor.LIGHT_PURPLE
                        + "Congratulations, "
                        + t.getTeamChatColor() + planter.getName() + ChatColor.LIGHT_PURPLE
                        + " has successfully destroyed the other teams base.");
                arena.getMatch().setVictor(t);
            } catch (NullPointerException ex) {
                // Should be fixed in v1.1.4
                plugin.getLogger().severe("NPE caused by using the wrong victoryCondition.");
                plugin.getLogger().severe("Please change BombArenaConfig.yml victoryCondition node to");
                plugin.getLogger().severe("victoryCondition: NoTeamsLeft");
                plugin.getLogger().log(Level.SEVERE, null, ex);
            }
            createExplosion(BOMB_LOCATION);
        }
        
    }
    
    public DetonationTimer start() {
        runTaskTimer(plugin, 0L, 20L);
        return this;
    }
    
    public Player getBombPlanter() {
        return this.planter;
    }
    
    public Location getBombLocation() {
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
        Set<ArenaPlayer> players = arena.getMatch().getPlayers();
        for (ArenaPlayer p : players) {
            if (!p.getLocation().getWorld().equals(loc.getWorld())) continue;
            double distance = p.getLocation().distance(loc);
            if (distance <= plugin.getDamageRadius()) {
                double dmg = plugin.getMaxDamage() - (distance * plugin.getDeltaDamage());
                p.getPlayer().damage(dmg);
            }
        }
    }    
}


