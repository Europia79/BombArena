package mc.euro.demolition.debug;

import java.util.Set;
import mc.alk.arena.objects.ArenaPlayer;
import mc.euro.demolition.BombPlugin;
import org.bukkit.entity.Player;

/**
 * debug = new DebugOff(); will toggle debugging mode OFF.
 */
public class DebugOff implements DebugInterface {

    public DebugOff() {
    }

    public DebugOff(BombPlugin m) {
    }
    
    @Override
    public void log(String m) {
        // Doesn't log because Debugging is OFF.
    }

    @Override
    public void sendMessage(Player p, String m) {
    }

    @Override
    public void msgArenaPlayers(Set<ArenaPlayer> players, String string) {
        
    }
    
}
