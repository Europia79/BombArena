package mc.euro.demolition.tracker;

import java.util.List;
import mc.alk.tracker.Tracker;
import mc.alk.tracker.TrackerInterface;
import mc.alk.tracker.objects.Stat;
import mc.alk.tracker.objects.StatType;
import mc.alk.tracker.objects.WLT;
import mc.euro.demolition.BombPlugin;
import org.bukkit.Bukkit;

/**
 *
 * @author Nikolai
 */
public class PlayerStats {
    BombPlugin plugin;
    TrackerInterface tracker;
    boolean enabled;
    
    public PlayerStats(String x) {
        plugin = (BombPlugin) Bukkit.getServer().getPluginManager().getPlugin("Demolition");
        loadTracker(x);
    }
    
    public boolean isEnabled() {
        return enabled;
    }

    private void loadTracker(String i) {
        Tracker t = (mc.alk.tracker.Tracker) Bukkit.getPluginManager().getPlugin("BattleTracker");
        if (t != null){
            enabled = true;
            tracker = Tracker.getInterface(i);
        } else {
            enabled = false;
            plugin.getLogger().warning("BattleTracker turned off or not found.");
        }
    }

    public void addPlayerRecord(String name, String bombs, WLT wlt) {
        if (this.isEnabled()) {
            tracker.addPlayerRecord(name, bombs, wlt);
        }
    }

    public List<Stat> getTopXWins(int n) {
        return tracker.getTopXWins(n);
    }

    public List<Stat> getTopX(StatType statType, int n) {
        return tracker.getTopX(statType, n);
    }
    
}
