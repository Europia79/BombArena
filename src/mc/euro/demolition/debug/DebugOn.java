package mc.euro.demolition.debug;

import java.util.Set;
import mc.alk.arena.objects.ArenaPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * debug = new DebugOn(); will toggle debugging mode ON.
 */
public class DebugOn implements DebugInterface {
    
    Plugin plugin;
    
    public DebugOn(Plugin reference) {
        this.plugin = reference;
    }

    @Override
    public void log(String msg) {
        plugin.getLogger().info(msg);
    }

    @Override
    public void sendMessage(Player p, String msg) {
        p.sendMessage(msg);
    }

    @Override
    public void msgArenaPlayers(Set<ArenaPlayer> players, String msg) {
        for (ArenaPlayer p : players) {
            p.sendMessage(msg);
        }
    }


}
