package mc.euro.demolition.tracker;

import com.enjin.officialplugin.stats.StatsPlayer;
import java.util.List;
import mc.alk.tracker.Tracker;
import mc.alk.tracker.TrackerInterface;
import mc.alk.tracker.objects.Stat;
import mc.alk.tracker.objects.StatType;
import mc.alk.tracker.objects.WLT;
import mc.euro.demolition.BombPlugin;
import mc.euro.version.Version;
import mc.euro.version.VersionFactory;
import org.bukkit.Bukkit;

/**
 *
 * @author Nikolai
 */
public class PlayerStats {
    BombPlugin plugin;
    public TrackerInterface tracker;
    boolean bt_enabled;
    Version battletracker; // BattleTracker
    Version enjin;
    
    public PlayerStats(String x) {
        plugin = (BombPlugin) Bukkit.getServer().getPluginManager().getPlugin("BombArena");
        loadTracker(x);
        loadEnjin();
    }
    
    public boolean isEnabled() {
        return bt_enabled;
    }
    
    private void loadEnjin() {
        this.enjin = VersionFactory.getPluginVersion("EnjinMinecraftPlugin");
        if (enjin.isCompatible("2.6")) {
            plugin.getLogger().info("EnjinMinecraftPlugin found & enabled.");
        } else {
            plugin.getLogger().info("EnjinMinecraftPlugin was not found or not compatible.");
        }
    }

    private void loadTracker(String i) {
        Tracker t = (mc.alk.tracker.Tracker) Bukkit.getPluginManager().getPlugin("BattleTracker");
        this.battletracker = VersionFactory.getPluginVersion("BattleTracker");
        if (t != null){
            bt_enabled = true;
            tracker = Tracker.getInterface(i);
            tracker.stopTracking(Bukkit.getServer().getOfflinePlayer(plugin.getFakeName()));
        } else {
            bt_enabled = false;
            plugin.getLogger().warning("BattleTracker turned off or not found.");
        }
    }

    public void addPlayerRecord(String name, String bombs, String wlt) {
        if (battletracker.isEnabled()) {
            tracker.addPlayerRecord(name, bombs, WLT.valueOf(wlt));
        }
        /*
        if (enjin.isCompatible("2.6.0")) {
            StatsPlayer enjinStats = new StatsPlayer(Bukkit.getOfflinePlayer(name));
            String statName = null;
            if (wlt.equalsIgnoreCase("WIN")) statName = "Bases Destroyed Successfully";
            if (wlt.equalsIgnoreCase("LOSS")) statName = "Bomb Detonation Failures";
            if (wlt.equalsIgnoreCase("TIE")) statName = "Bombs Defused";
            if (statName != null) enjinStats.addCustomStat("BombArena", statName, 1, true);
        }*/
    }

    public List<Stat> getTopXWins(int n) {
        return tracker.getTopXWins(n);
    }

    public List<Stat> getTopX(StatType statType, int n) {
        return tracker.getTopX(statType, n);
    }
    
}
