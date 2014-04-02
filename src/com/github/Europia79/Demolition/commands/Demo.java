package com.github.Europia79.Demolition.commands;

import com.github.Europia79.Demolition.Main;
import java.util.List;
import mc.alk.arena.executors.CustomCommandExecutor;
import mc.alk.arena.executors.MCCommand;
import mc.alk.tracker.objects.Stat;
import mc.alk.tracker.objects.StatType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 *
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
    
    // /bomb stats
    @MCCommand(cmds={"stats"}, op=false)
    public boolean stats(CommandSender cs) {
            int n = 10;
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


    
}
