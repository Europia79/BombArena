package com.github.Europia79.Demolition.debug;

import java.util.Set;
import mc.alk.arena.objects.ArenaPlayer;
import org.bukkit.entity.Player;

/**
 *
 * @author Nikolai
 */
public class DebugOn implements DebugInterface {

    @Override
    public void log(String msg) {
        
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
