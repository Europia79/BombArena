package com.github.Europia79.Demolition.debug;

import org.bukkit.entity.Player;

/**
 *
 * @author Nikolai
 */
public class DebugOn implements DebugInterface {

    @Override
    public void log(String m) {
        
    }

    @Override
    public void messagePlayer(Player p, String m) {
        p.sendMessage(m);
    }


}
