package com.github.Europia79.Demolition.debug;

import com.github.Europia79.Demolition.Main;
import java.util.Set;
import mc.alk.arena.objects.ArenaPlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 *
 * @author Nikolai
 */
public class DebugOn implements DebugInterface {
    
    Main plugin;
    
    public DebugOn(Main reference) {
        this.plugin = reference;
    }

    @Override
    public void log(String msg) {
        plugin.getLogger().info(ChatColor.LIGHT_PURPLE + msg);
    }

    @Override
    public void messagePlayer(Player p, String msg) {
        p.sendMessage(msg);
    }

    @Override
    public void msgArenaPlayers(Set<ArenaPlayer> players, String msg) {
        for (ArenaPlayer p : players) {
            p.sendMessage(msg);
        }
    }


}
