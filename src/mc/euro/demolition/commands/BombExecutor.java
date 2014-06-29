package mc.euro.demolition.commands;

import mc.euro.demolition.BombPlugin;
import java.util.List;
import java.util.Set;
import mc.alk.arena.BattleArena;
import mc.alk.arena.executors.CustomCommandExecutor;
import mc.alk.arena.executors.MCCommand;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.util.SerializerUtil;
import mc.alk.tracker.objects.PlayerStat;
import mc.alk.tracker.objects.Stat;
import mc.alk.tracker.objects.StatType;
import mc.euro.demolition.BombArena;
import mc.euro.demolition.debug.DebugOff;
import mc.euro.demolition.debug.DebugOn;
import mc.euro.demolition.util.BaseType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * All the /bomb commands and subcommands.
 * @author Nikolai
 */
public class BombExecutor extends CustomCommandExecutor {
    
    BombPlugin plugin;
    
    public BombExecutor() {
        plugin = (BombPlugin) Bukkit.getServer().getPluginManager().getPlugin("BombArena");
    }
    
    @MCCommand(cmds={"setbase"}, perm="bombarena.setbase", usage="setbase <arena> <teamID>")
    public boolean setbase(Player sender, Arena arena, Integer i) {
        if (i < 1 || i > 2) {
            sender.sendMessage("Bomb arenas can only have 2 teams: 1 or 2");
            return true;
        }
        // path {arenaName}.{index}
        String path = arena.getName() + "." + i.toString();
        Location loc = sender.getLocation();
        Location base_loc = plugin.getExactLocation(loc);
        if (base_loc == null) {
            sender.sendMessage("setbase command failed to find a BaseBlock near your location.");
            sender.sendMessage("Please set 2 BaseBlocks in the arena (1 for each team).");
            sender.sendMessage("If you have already set BaseBlocks, then stand closer and re-run the command.");
            return true;
        }
        String sloc = SerializerUtil.getLocString(base_loc);
        plugin.basesYml.set(path, sloc);
        plugin.basesYml.saveConfig();
        return true;

    }
    
    @MCCommand(cmds={"spawnbomb"}, perm="bombarena.spawnbomb", usage="spawnbomb <arena>")
    public boolean spawnbomb(Player sender, String a) {
        plugin.debug.log("arena = " + a);
        Arena arena =(BombArena) BattleArena.getArena(a);
        if (arena == null) return false;
        Material bomb = plugin.getBombBlock();
        int matchTime = arena.getParams().getMatchTime();
        
        plugin.debug.log("spawnbomb() MatchTime = " + matchTime);
        
        String selectArena = "aa select " + arena.getName();
        
        if (plugin.getServer().dispatchCommand(sender, selectArena) 
                && plugin.getServer().dispatchCommand(sender, 
                Command.addspawn(bomb.name(), matchTime))) {
            sender.sendMessage("The bomb spawn for " + arena.getName() + " has been set!");
            return true;
        }
        sender.sendMessage("The spawnbomb command has failed.");
        return false;
    }
    
    @MCCommand(cmds={"stats"}, perm="bomb.stats", usage="stats")
    public boolean stats(CommandSender sender) {
        if (sender instanceof ConsoleCommandSender) {
            sender.sendMessage("Invalid command syntax: Please specify a player name");
            sender.sendMessage("./bomb stats <player>");
            sender.sendMessage("or /bomb stats top X");
            return true;
        }
        stats(sender, plugin.getServer().getOfflinePlayer(sender.getName()));
        return true;
    }
    
    @MCCommand(cmds={"stats"}, perm="bomb.stats.other", usage="stats <player>")
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
    @MCCommand(cmds={"stats"}, subCmds={"top"}, perm="bomb.stats.top", usage="stats top X")
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
            if (w.getName().equalsIgnoreCase(plugin.getFakeName())) {
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
                if (d.getName().equalsIgnoreCase(plugin.getFakeName())) continue;
                cs.sendMessage("" + i + " " + d.getName() + " " + d.getTies());
                i = i + 1;
            }
            
            return true;
    }
    
    @MCCommand(cmds={"setconfig"}, subCmds={"bombblock"}, 
            perm="bombarena.setconfig", usage="setconfig BombBlock <handItem>")
    public boolean setBombBlock(Player p) {
        ItemStack hand = p.getInventory().getItemInHand();
        if (hand == null) {
            p.sendMessage("There is nothing in your hand.");
            return false;
        }
        plugin.setBombBlock(hand.getType());
        p.sendMessage("BombBlock has been set to " + hand.getType());
        p.sendMessage("All of your arenas have been automatically "
                + "updated with the new BombBlock.");
        return true;
    }

    @MCCommand(cmds={"setconfig"}, subCmds={"baseblock"}, perm="bombarena.setconfig",
            usage="setconfig BaseBlock <handItem>")
    public boolean setBaseBlock(Player p) {
        ItemStack hand = p.getInventory().getItemInHand();
        if (hand == null) {
            p.sendMessage("There is nothing in your hand.");
            return false;
        }
        if (!BaseType.containsKey(hand.getType().name())) {
            p.sendMessage("That is not a valid BaseBlock in your hand!");
            return true;
        }
        p.sendMessage("BaseBlock has been set to " + hand.getType().name());
        plugin.setBaseBlock(hand.getType());
        return true;
    }
    
    @MCCommand(cmds={"setconfig"}, subCmds={"databasetable"}, 
            perm="bombarena.setconfig", usage="setconfig DatabaseTable <name>")
    public boolean setDatabaseTable(CommandSender sender, String table) {
        plugin.setDatabaseTable(table);
        sender.sendMessage("DatabaseTable has been set to " + table);
        return true;
    }
    
    @MCCommand(cmds={"setconfig"}, subCmds={"fakename"}, 
            perm="bombarena.setconfig", usage="setconfig FakeName <new name>")
    public boolean setFakeName(CommandSender sender, String[] name) {
        // name[] = "setconfig fakename args[2] args[3]"
        if (name.length <= 3) {
            sender.sendMessage("FakeName must have at least one space in order "
                    + "to distinguish it from a real player in the database.");
            return false;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 2; i < name.length; i++) {
            sb.append(name[i]).append(" ");
        }
        String newName = sb.toString().trim();
        plugin.setFakeName(newName);
        sender.sendMessage("FakeName has been set to " + newName);
        return true;
    }
    
    @MCCommand(cmds={"setconfig"}, subCmds={"changefakename"},
            perm="bombarena.setconfig", usage="setconfig ChangeFakeName <name>")
    public boolean setChangeFakeName(CommandSender sender, String name) {
        sender.sendMessage("This option has not been implemented.");
        return true;
    }
    @MCCommand(cmds={"setconfig"}, perm="bombarena.setconfig", usage="setconfig <option> <integer>")
    public boolean setconfig(CommandSender sender, String option, Integer value) {
        Set<String> keys = plugin.getConfig().getKeys(false);
        for (String key : keys) {
            if (option.equalsIgnoreCase(key)) {
                plugin.getConfig().set(key, value);
                sender.sendMessage("" + key + " has been set to " + value);
                plugin.saveConfig();
                plugin.loadDefaultConfig();
                return true;
            }
        }
        sender.sendMessage("Valid options: " + keys.toString());
        return false;
    }
    
    @MCCommand(cmds={"listconfig"}, perm="bombarena.setconfig", usage="listconfig")
    public boolean listconfig(CommandSender sender) {
        sender.sendMessage("Config options: " + plugin.getConfig().getKeys(false).toString());
        return true;
    }
    
    @MCCommand(cmds={"setconfig"}, subCmds={"debug"}, 
            perm="bombarena.setconfig", usage="setconfig debug <true/false>")
    public boolean setDebug(CommandSender sender, boolean b) {
        plugin.getConfig().set("Debug", (boolean) b);
        plugin.saveConfig();
        sender.sendMessage("config.yml option 'Debug' has been set to " + b);
        plugin.loadDefaultConfig();
        return true;
    }

    /**
     * Toggles debug mode ON / OFF.
     * Usage: /bomb debug
     */
    @MCCommand(cmds={"debug"}, perm="bombarena.debug", usage="debug")
    public boolean toggleDebug(CommandSender sender) {
        if (plugin.debug instanceof DebugOn) {
            plugin.debug = new DebugOff(plugin);
            plugin.getConfig().set("Debug", false);
            plugin.saveConfig();
            sender.sendMessage("Debugging mode for the BombArena has been turned off.");
            return true;
        } else if (plugin.debug instanceof DebugOff) {
            plugin.debug = new DebugOn(plugin);
            plugin.getConfig().set("Debug", true);
            plugin.saveConfig();
            sender.sendMessage("Debugging mode for the BombArena has been turned on.");
            return true;
        }
        return false;
    }
    
    @MCCommand(cmds={"getname"}, perm="bombarena.setconfig", usage="getname <handItem>")
    public boolean getBlockName(Player p) {
        String name = p.getItemInHand().getType().name();
        p.sendMessage("You are holding " + name);
        return true;
    }

    
}
