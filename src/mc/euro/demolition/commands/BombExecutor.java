package mc.euro.demolition.commands;

import mc.euro.demolition.BombPlugin;
import java.util.List;
import mc.alk.arena.BattleArena;
import mc.alk.arena.executors.CustomCommandExecutor;
import mc.alk.arena.executors.MCCommand;
import mc.alk.arena.util.SerializerUtil;
import mc.alk.tracker.objects.PlayerStat;
import mc.alk.tracker.objects.Stat;
import mc.alk.tracker.objects.StatType;
import mc.euro.demolition.BombArena;
import mc.euro.demolition.debug.DebugOff;
import mc.euro.demolition.debug.DebugOn;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * All the /bomb commands and subcommands.
 * @author Nikolai
 */
public class BombExecutor extends CustomCommandExecutor {
    
    BombPlugin plugin;
    
    public BombExecutor() {
        plugin = (BombPlugin) Bukkit.getServer().getPluginManager().getPlugin("BombArena");
    }
    
    @MCCommand(cmds={"setbase"}, perm="bomb.setbase", usage="(stand in base)/bomb setbase ArenaName")
    public boolean setbase(Player sender, String a, Integer i) {
        if (i < 1 || i > 2) {
            sender.sendMessage("Bomb arenas can only have 2 teams");
            return true;
        }
        Location loc = sender.getLocation();
        BombArena arena = (BombArena) BattleArena.getArena(a);
        if (arena == null) {
            return false;
        }
        Location base_loc = arena.getExactLocation(loc);
        if (base_loc == null) {
            sender.sendMessage("setbase command failed to find a BaseBlock near your location.");
            sender.sendMessage("Please set 2 BaseBlocks in the arena (1 for each team).");
            sender.sendMessage("If you have already set BaseBlocks, then stand closer then re-run the command.");
            return true;
        }
        String path = "arenas." + a + ".bases";
        String wxyz = SerializerUtil.getLocString(base_loc);
        plugin.arenasYml.set(path + "." + i, wxyz);
        plugin.arenasYml.saveConfig();

        // Set<String> keys = plugin.getConfig("arenas").getConfigurationSection(path).getKeys(false);
        // ArenaSerializer.saveArenas(plugin);
        sender.sendMessage("Base set!");
        return true;
    }
    
    @MCCommand(cmds={"spawnbomb"}, perm="bomb.spawnbomb", usage="./bomb spawnbomb ArenaName")
    public boolean spawnbomb(Player sender, String arena) {
        if (arena == null) {
            sender.sendMessage("You must specify an ArenaName with this command.");
            sender.sendMessage("Command syntax: /bomb spawnbomb ArenaName");
            return false;
        }
        // shortcut and alias for
        // /aa select ArenaName
        // /aa addspawn BombBlock.name() fs=1 rs=300 1
        plugin.getServer().dispatchCommand(sender, "aa select " + arena);
        plugin.getServer().dispatchCommand(sender, "aa addspawn 172 fs=1 rs=300 1");
        sender.sendMessage("The bomb spawn for " + arena + " has been set!");
        // Add to documentation:
        // sender.sendMessage("Because this command was an alias for /aa, "
        //        + "please do not use the /aa command without first using /aa select");
        return true;
    }
    
    @MCCommand(cmds={"stats"}, perm="bomb.stats.self", usage="/bomb stats")
    public boolean stats(CommandSender sender) {
        if (sender instanceof ConsoleCommandSender) {
            sender.sendMessage("Invalid command syntax: Please specify a PlayerName");
            sender.sendMessage("./bomb stats PlayerName");
            return true;
        }
        stats(sender, plugin.getServer().getOfflinePlayer(sender.getName()));
        return true;
    }
    
    @MCCommand(cmds={"stats"}, perm="bomb.stats.other", usage="/bomb stats PlayerName")
    public boolean stats(CommandSender sender, OfflinePlayer p) {
        if (!plugin.ti.isEnabled()) {
            plugin.getLogger().warning("BattleTracker not found or turned off.");
            sender.sendMessage("BombArena statistics are not being tracked.");
            return true;
        }
        PlayerStat ps = plugin.ti.tracker.getPlayerRecord(p);
        int wins = ps.getWins();
        int ties = ps.getTies();
        int losses = ps.getLosses();
        int total = wins + losses;
        int percentage = (total == 0) ? 0 : (int)  (wins * 100.00) / total;
        String intro = (sender.getName().equalsIgnoreCase(p.getName())) ?
                "You have" : p.getName() + " has";
        sender.sendMessage(intro + " successfully destroyed the other teams base "
                + wins + " times out of " + total + " attempts. ("
                + percentage +"%)");
        sender.sendMessage(intro + " defused the bomb " + ties + " times.");
        return true;
    }
    
    /**
     * Shows bomb arena stats for the command sender.
     * Example Usage: /bomb stats top 5
     */
    @MCCommand(cmds={"stats"}, subCmds={"top"}, op=false, usage="./bomb stats top X")
    public boolean stats(CommandSender cs, Integer n) {
            if (!plugin.ti.isEnabled()) {
                plugin.getLogger().warning(ChatColor.AQUA + "BattleTracker not found or turned off.");
                cs.sendMessage(ChatColor.YELLOW + "Bomb Arena statistics are not being tracked.");
                return true;
            }
            
            List<Stat> planted = plugin.ti.getTopXWins(n);
            cs.sendMessage(ChatColor.AQUA  +  "Number of Bombs Planted");
            cs.sendMessage(ChatColor.YELLOW + "-----------------------");
            int i = 1;
            for (Stat w : planted) {
            if (w.getName().equalsIgnoreCase(plugin.FakeName)) {
                continue;
            }
            int total = w.getWins() + w.getLosses();
            int percentage = (total == 0) ? 0 : (int) (w.getWins() * 100.00) / total;
            cs.sendMessage("" + i + " " + w.getName() + " " 
                    + w.getWins() + " out of " + total + " (" + percentage + "%)");
            i = i + 1;
        }
            
            List<Stat> defused = plugin.ti.getTopX(StatType.TIES, n);
            cs.sendMessage(ChatColor.AQUA + "Number of Bombs Defused");
            cs.sendMessage(ChatColor.YELLOW + "-----------------------");
            i = 1;
            for (Stat d : defused) {
                if (d.getName().equalsIgnoreCase(plugin.FakeName)) continue;
                cs.sendMessage("" + i + " " + d.getName() + " " + d.getTies());
                i = i + 1;
            }
            
            return true;
    }

    /**
     * Toggles debug mode ON / OFF.
     * Usage: /bomb debug
     */
    @MCCommand(cmds={"debug"}, op=true, usage="./bomb debug")
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
