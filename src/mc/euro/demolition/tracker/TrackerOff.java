package mc.euro.demolition.tracker;

import mc.euro.demolition.Main;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import mc.alk.tracker.TrackerInterface;
import mc.alk.tracker.objects.PlayerStat;
import mc.alk.tracker.objects.Stat;
import mc.alk.tracker.objects.StatType;
import mc.alk.tracker.objects.TeamStat;
import mc.alk.tracker.objects.WLT;
import mc.alk.tracker.objects.WLTRecord;
import mc.alk.tracker.ranking.RatingCalculator;
import mc.alk.v1r7.core.Version;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * TrackerInterface ti = new TrackerOff();
 */
public class TrackerOff implements TrackerInterface {
    
    Main plugin;
    
    public TrackerOff(Main reference) {
        plugin = reference;
    }

    @Override
    public void printTopX(CommandSender cs, StatType st, int i) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
    }

    @Override
    public void printTopX(CommandSender cs, StatType st, int i, String string, String string1) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
    }

    @Override
    public void printTopX(CommandSender cs, StatType st, int i, int i1) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
    }

    @Override
    public void printTopX(CommandSender cs, StatType st, int i, int i1, String string, String string1) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
    }

    @Override
    public void addStatRecord(Stat stat, Stat stat1, WLT wlt) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
    }

    @Override
    public void addPlayerRecord(String string, String string1, WLT wlt) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
    }

    @Override
    public void addPlayerRecord(OfflinePlayer op, OfflinePlayer op1, WLT wlt) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
    }

    @Override
    public void addTeamRecord(String string, String string1, WLT wlt) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
    }

    @Override
    public void addTeamRecord(Set<String> set, Set<String> set1, WLT wlt) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
    }

    @Override
    public void addTeamRecord(Collection<Player> clctn, Collection<Player> clctn1, WLT wlt) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
    }

    @Override
    public void changePlayerElo(String string, String string1, WLT wlt) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
    }

    @Override
    public PlayerStat getPlayerRecord(String string) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
        return null;
    }

    @Override
    public PlayerStat getPlayerRecord(OfflinePlayer op) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
        return null;
    }

    @Override
    public TeamStat getTeamRecord(String string) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
        return null;
    }

    @Override
    public TeamStat getTeamRecord(Set<String> set) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
        return null;
    }

    @Override
    public boolean hidePlayer(String string, boolean bln) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
        return false;
    }

    @Override
    public void stopTracking(String string) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
    }

    @Override
    public void resumeTracking(String string) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
    }

    @Override
    public void stopMessages(String string) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
    }

    @Override
    public void resumeMessages(String string) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
    }

    @Override
    public void stopTracking(OfflinePlayer op) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
    }

    @Override
    public void resumeTracking(OfflinePlayer op) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
    }

    @Override
    public void stopMessages(OfflinePlayer op) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
    }

    @Override
    public void resumeMessages(OfflinePlayer op) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
    }

    @Override
    public void resumeMessages(Collection<Player> clctn) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
    }

    @Override
    public void resumeTracking(Collection<Player> clctn) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
    }

    @Override
    public void stopMessages(Collection<Player> clctn) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
    }

    @Override
    public void stopTracking(Collection<Player> clctn) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
    }

    @Override
    public void addRecordGroup(Collection<Player> clctn, Collection<Collection<Player>> clctn1, WLT wlt) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
    }

    @Override
    public Stat getRecord(String string) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
        return null;
    }

    @Override
    public Stat getRecord(OfflinePlayer op) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
        return null;
    }

    @Override
    public Stat loadRecord(OfflinePlayer op) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
        return null;
    }

    @Override
    public Stat loadPlayerRecord(String string) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
        return null;
    }

    @Override
    public Stat loadRecord(Set<Player> set) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
        return null;
    }

    @Override
    public Stat getRecord(Collection<Player> clctn) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
        return null;
    }

    @Override
    public void saveAll() {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
    }

    @Override
    public List<Stat> getTopX(StatType st, int i) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
        return null;
    }

    @Override
    public List<Stat> getTopXRating(int i) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
        return null;
    }

    @Override
    public List<Stat> getTopXLosses(int i) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
        return null;
    }

    @Override
    public List<Stat> getTopXWins(int i) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
        return null;
    }

    @Override
    public List<Stat> getTopXKDRatio(int i) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
        return null;
    }

    @Override
    public List<Stat> getTopX(StatType st, int i, Integer intgr) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
        return null;
    }

    @Override
    public List<Stat> getTopXRating(int i, Integer intgr) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
        return null;
    }

    @Override
    public List<Stat> getTopXLosses(int i, Integer intgr) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
        return null;
    }

    @Override
    public List<Stat> getTopXWins(int i, Integer intgr) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
        return null;
    }

    @Override
    public List<Stat> getTopXKDRatio(int i, Integer intgr) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
        return null;
    }

    @Override
    public Integer getRank(OfflinePlayer op) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
        return null;
    }

    @Override
    public Integer getRank(String string) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
        return null;
    }

    @Override
    public void resetStats() {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
    }

    @Override
    public void onlyTrackOverallStats(boolean bln) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
    }

    @Override
    public boolean setRating(OfflinePlayer op, int i) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
        return false;
    }

    @Override
    public RatingCalculator getRatingCalculator() {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
        return null;
    }

    @Override
    public RatingCalculator getRankingCalculator() {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
        return null;
    }

    @Override
    public boolean setRanking(OfflinePlayer op, int i) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
        return false;
    }

    @Override
    public List<WLTRecord> getVersusRecords(String string, String string1, int i) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
        return null;
    }

    @Override
    public String getInterfaceName() {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
        return null;
    }

    @Override
    public void save(Stat... stats) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
    }

    @Override
    public void flush() {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
    }

    @Override
    public int getRecordCount() {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
        return 0;
    }

    @Override
    public Version getVersion() {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
        return null;
    }

    @Override
    public List<WLTRecord> getWinsSince(Stat stat, Long l) {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
        return null;
    }

    @Override
    public boolean isModified() {
        plugin.getLogger().warning("BattleTracker turned off or not found.");
        return false;
    }
    
}
