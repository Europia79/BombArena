package mc.euro.demolition.commands;

import mc.euro.demolition.Main;
import java.util.List;
import mc.alk.arena.executors.CustomCommandExecutor;
import mc.alk.arena.executors.MCCommand;
import mc.alk.tracker.objects.Stat;
import mc.alk.tracker.objects.StatType;
import mc.euro.demolition.debug.DebugOff;
import mc.euro.demolition.debug.DebugOn;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * All the commands for the Demolition plugin.
 * @author Nikolai
 */
public class Demo extends CustomCommandExecutor {
    // possible future commands:
    // /bomb setbase
    // then ask player to click the base.
    
    Main plugin;
    
    public Demo() {
        plugin = (Main) Bukkit.getServer().getPluginManager().getPlugin("Demolition");
    }
    
    /**
     * Shows bomb arena stats for the command sender.
     * Usage: /bomb stats
     */
    @MCCommand(cmds={"stats"}, op=false)
    public boolean stats(CommandSender cs) {
            int n = 3;
            if (!plugin.ti.isEnabled()) {
                plugin.getLogger().warning(ChatColor.AQUA + "BattleTracker not found or turned off.");
                cs.sendMessage(ChatColor.YELLOW + "Bomb Arena statistics are not being tracked.");
                return true;
            }
            List<Stat> planted = plugin.ti.getTopXWins(n);
            cs.sendMessage(ChatColor.AQUA + "Number of Bombs Planted Successfully");
            cs.sendMessage(ChatColor.YELLOW + "------------------------------------");
            int i = 1;
            for (Stat w : planted) {
                if (w.getName().equalsIgnoreCase("Bombs Planted Defused")) continue;
                cs.sendMessage("" + i + " " + w.getName() + " " + w.getWins());
                i = i + 1;
            }
            
            List<Stat> defused = plugin.ti.getTopX(StatType.TIES, n);
            cs.sendMessage(ChatColor.AQUA + "Number of Bombs Defused");
            cs.sendMessage(ChatColor.YELLOW + "-----------------------");
            i = 1;
            for (Stat d : defused) {
                if (d.getName().equalsIgnoreCase("Bombs Planted Defused")) continue;
                cs.sendMessage("" + i + " " + d.getName() + " " + d.getTies());
                i = i + 1;
            }
            
            return true;
    }

    /**
     * Toggles debug mode ON / OFF.
     * Usage: /bomb debug
     */
    @MCCommand(cmds={"debug"}, op=true)
    public boolean debug(CommandSender cs) {
        if (plugin.debug instanceof DebugOn) {
            plugin.debug = new DebugOff(plugin);
            cs.sendMessage("Debugging mode for the BombArena has been turned off.");
            return true;
        } else if (plugin.debug instanceof DebugOff) {
            plugin.debug = new DebugOn(plugin);
            cs.sendMessage("Debugging mode for the BombArena has been turned on.");
            return true;
        }
        return false;
    }

    
}
