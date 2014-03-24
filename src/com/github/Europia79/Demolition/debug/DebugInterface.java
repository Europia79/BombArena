package com.github.Europia79.Demolition.debug;

import java.util.Set;
import mc.alk.arena.objects.ArenaPlayer;
import org.bukkit.entity.Player;

/**
 *
 * @author Nikolai
 */
public interface DebugInterface {
    
    public void log(String msg);
    public void messagePlayer(Player p, String msg);
    public void msgArenaPlayers(Set<ArenaPlayer> players, String msg);
    
}
