package mc.euro.demolition.timers;

import java.util.Map;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.euro.demolition.BombPlugin;
import mc.euro.demolition.arenas.EodArena;
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
    EodArena arena;
    InventoryOpenEvent event;
    Player player;
    Location BOMB_LOCATION;

    public DefuseTimer(Player defuser, EodArena arena) {
        this.plugin = (BombPlugin) Bukkit.getServer().getPluginManager().getPlugin("BombArena");
        this.duration = this.plugin.getDefuseTime() + 1;
        this.arena = arena;
        this.player = defuser;
        this.BOMB_LOCATION = plugin.getExactLocation(player.getLocation());
    }
    
    @Override
    public void run() {
        duration = duration - 1;
        player.sendMessage("" + ChatColor.RED + "" + duration);
        
        if (duration <= 0) {
            ArenaTeam t = arena.getTeam(player);
            t.sendMessage(ChatColor.LIGHT_PURPLE 
                    + "Congratulations, "
                    + t.getTeamChatColor() + "" + player.getName() + ChatColor.LIGHT_PURPLE
                    + " has successfully defused the bomb. You win!");
            plugin.ti.addPlayerRecord(player.getName(), plugin.getFakeName(), "TIE");
            plugin.ti.addPlayerRecord(arena.getBombCarrier(), plugin.getFakeName(), "LOSS");
            
            arena.getMatch().setVictor(t);
            
            this.player.closeInventory();
        }
    }
    
    public DefuseTimer start() {
        String msg = "" + player.getName() + " has started to defuse the bomb!";
        plugin.debug.msgArenaPlayers(arena.getMatch().getPlayers(), msg);
        runTaskTimer(plugin, 0L, 20L);
        return this;
    }
    
    public Player getPlayer() {
        return this.player;
    }
}
