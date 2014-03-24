package com.github.Europia79.Demolition.debug;

import java.util.Set;
import mc.alk.arena.objects.ArenaPlayer;
import org.bukkit.entity.Player;

/**
 *
 * @author Nikolai
 */
public class DebugOff implements DebugInterface {

    @Override
    public void log(String m) {
    }

    @Override
    public void messagePlayer(Player p, String m) {
    }

    @Override
    public void msgArenaPlayers(Set<ArenaPlayer> players, String string) {
        
    }
    
}
