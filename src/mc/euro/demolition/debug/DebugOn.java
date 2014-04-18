package mc.euro.demolition.debug;

import mc.euro.demolition.Main;
import java.util.Set;
import mc.alk.arena.objects.ArenaPlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * debug = new DebugOn(); will toggle debugging mode ON.
 */
public class DebugOn implements DebugInterface {
    
    Main plugin;
    
    public DebugOn(Main reference) {
        this.plugin = reference;
    }

    @Override
    public void log(String msg, ChatColor... c) {
        ChatColor color = (ChatColor) ((c.length < 1) ? ChatColor.WHITE : c[0]);
        plugin.getLogger().info(color + msg);
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
