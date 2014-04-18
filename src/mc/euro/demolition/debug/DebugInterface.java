package mc.euro.demolition.debug;

import java.util.Set;
import mc.alk.arena.objects.ArenaPlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * This class is the interface between DebugOn() and DebugOff(). <br/><br/>
 * 
 * It is used to print information about the state of objects and fields. <br/>
 * To see if they're within expected ranges. <br/><br/>
 * 
 * As you can see, Debug can be toggled on or off at runtime.
 * 
 * Examples: <br/>
 * <pre>
 * DebugInteraface debug = new DebugOn();
 * debug = new DebugOff();
 * debug = new DebugOn();
 * 
 * debug.log("The value of x = " + x);
 * debug.messagePlayer("Debug msg here.");
 * deubg.messagePlayers(getMatch().getPlayers(), "Sends msg to each player");
 * </pre>
 * 
 * @author Nikolai
 * 
 */
public interface DebugInterface {
    
    public void log(String msg, ChatColor... c);
    public void messagePlayer(Player p, String msg);
    public void msgArenaPlayers(Set<ArenaPlayer> players, String msg);
    
}
