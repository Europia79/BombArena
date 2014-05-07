package mc.euro.demolition.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.ArenaPlayer;
import mc.euro.demolition.BombPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 *
 * @author Nikolai
 */
public class DefuseCounter {
    
    private Map<String, Integer> total;
    private Map<String, Integer> timesBroken;
    private Map<String, Long> lastTime;
    
    private long tolerance;
    BombPlugin plugin;
    Match match;

    public DefuseCounter(Set players) {
        plugin = (BombPlugin) Bukkit.getServer().getPluginManager().getPlugin("BombArena");
        // Tolerance = BlockBreakTime + tolerance;
        this.tolerance = setTolerance(plugin.Tolerance);
        
        total = new HashMap<String, Integer>();
        timesBroken = new HashMap<String, Integer>();
        lastTime = new HashMap<String, Long>();
        
        initializeMaps(convert(players));
    }

    public int getTimesBroken(Player p) {
        return this.timesBroken.get(p.getName());
    }
    
    public int addBlockBreak(Player p) {
        String name = p.getName();
        
        int newTotal = total.get(name) + 1;
        total.put(name, newTotal);
        
        String msgTotal = " (" + total.get(name) + ")";
        plugin.debug.log("DefuseCounter.addBlockBreak() called for " + name + msgTotal);
        
        if (true) {
            return total.get(name);
        }
        
        plugin.debug.log("lastTime = " + this.lastTime);
        plugin.debug.log("elapsedTime = " + getMilliElapsed(name) + " milliseconds");
        
        if (getLastTime(name) >= this.tolerance) {
            String msg2 = "Player " + name + " has had his total block broken reset"
                    + " because they weren't defusing fast enough.";
            plugin.debug.log(msg2);
            this.timesBroken.put(name, 0);
        }
        
        int t = this.timesBroken.get(name);
        t = t + 1;
        this.timesBroken.put(name, t);
        
        setLastTime(name);
        return this.timesBroken.get(name);
    }
    

    private long getSecondsElapsed(String name) {
        return (getMilliElapsed(name) / 1000);
    }

    private long getMilliElapsed(String name) {
        return (getNanoElapsed(name) / 1000000);
    }

    private long getNanoElapsed(String name) {
        return ((getCurrentTime() - getLastTime(name)));
    }

    private long getLastTime(String name) {
        return this.lastTime.get(name);
    }
    
    private void setLastTime(String name) {
        this.lastTime.put(name, getCurrentTime());
    }
    
    public static long getCurrentTime() {
        return System.nanoTime();
    }

    private long setTolerance(int milli) {
        // Tolerance = BlockBreakTime + tolerance;
        Material m = plugin.BombBlock;
        int breakTime = plugin.getConfig().getInt("BreakTimes." + m.name());
        return breakTime + milli;
    }
    
    private Set<Player> convert(Set players) {
        Set<Player> temp = new HashSet<Player>();
        if (players.toArray()[0] instanceof Player) {
            temp.addAll(players);
        } else if (players.toArray()[0] instanceof ArenaPlayer) {
            for (Object p : players) {
                ArenaPlayer ap = (ArenaPlayer) p;
                temp.add(ap.getPlayer());
            }
        }
        return temp;
    }

    private void initializeMaps(Set<Player> allplayers) {
        for (Player p : allplayers) {
            this.total.put(p.getName(), 0);
            this.timesBroken.put(p.getName(), 0);
            this.lastTime.put(p.getName(), getCurrentTime());
        }
    }
}
