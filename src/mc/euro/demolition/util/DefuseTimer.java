package mc.euro.demolition.util;

import java.util.Map;
import mc.euro.demolition.BombPlugin;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.euro.demolition.tracker.OUTCOME;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Example: new DefuseTimer(InventoryOpenEvent e, getMatch()). <br/><br/>
 * 
 * plugin.defTimers.put(getMatch().getID(), new PlantTimer(e, getMatch())); <br/>
 * plugin.defTimers.get(getMatch().getID()).runTaskTimer(plugin, 0L, 20L); <br/><br/>
 * 
 * There can be a DefuseTimer for each player attempting to defuse the bomb. <br/><br/>
 * 
 */
public class DefuseTimer extends BukkitRunnable {

    BombPlugin plugin;
    int duration;
    Match match;
    InventoryOpenEvent event;
    Player player;
    Location BOMB_LOCATION;
    Long startTime;
    private boolean cancelled;

    public DefuseTimer(InventoryOpenEvent e, Match m) {
        cancelled = false;
        this.plugin = (BombPlugin) Bukkit.getServer().getPluginManager().getPlugin("BombArena");
        this.duration = this.plugin.PlantTime;
        this.event = e;
        this.match = m;
        this.player = (Player) e.getPlayer();
        this.BOMB_LOCATION = plugin.getExactLocation(e.getPlayer().getLocation());
        match.sendMessage("" + player.getName() + " has started to defuse the bomb!");
    }

    @Override
    public void run() {
        duration = duration - 1;
        player.sendMessage("" + ChatColor.RED + "" + duration);
        
        if (duration <= 0) {
            int matchID = match.getID();
            ArenaTeam t = match.getArena().getTeam(player);
            t.sendMessage(ChatColor.LIGHT_PURPLE 
                    + "Congratulations, "
                    + t.getTeamChatColor() + "" + player.getName() + ChatColor.LIGHT_PURPLE
                    + " has successfully defused the bomb. You win!");
            plugin.ti.addPlayerRecord(player.getName(), plugin.FakeName, OUTCOME.getDefuseSuccess());
            plugin.ti.addPlayerRecord(plugin.carriers.get(matchID), plugin.FakeName, OUTCOME.getPlantFailure());
            match.setVictor(t);
            Map<String, DefuseTimer> temp = plugin.defTimers.get(matchID);
            for (DefuseTimer d : temp.values()) {
                d.setCancelled(true);
            }
            plugin.detTimers.get(matchID).setCancelled(true);
            plugin.defTimers.get(matchID).clear();
        }
    }
    
    public void setCancelled(boolean x) {
        this.cancelled = x;
        if (x) {
            this.player.closeInventory();
            this.cancel();
        } 
    }
    
    public boolean isCancelled() {
        return this.cancelled;
    }
    
    public Player getPlayer() {
        return this.player;
    }
}
