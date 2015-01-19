package mc.euro.demolition.arenas.factories;

import java.util.ArrayList;
import java.util.List;
import mc.alk.arena.BattleArena;
import mc.alk.arena.executors.CustomCommandExecutor;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.arenas.ArenaFactory;
import mc.euro.demolition.arenas.BombArena;
import mc.euro.demolition.BombPlugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Creates new instances of BombArena. <br/><br/>
 * 
 * Registration using ArenaFactory requires BattleArena v3.9.8+
 * 
 * @author Nikolai
 */
public class BombArenaFactory implements ArenaFactory {

    private static final List<BombArena> arenas = new ArrayList<BombArena>();
    
    private BombPlugin plugin;
    
    public BombArenaFactory(BombPlugin reference) {
        this.plugin = reference;
    }

    @Override
    public Arena newArena() {
        BombArena bomb = new BombArena(plugin);
        arenas.add(bomb);
        return bomb;
    }
    
    public List<BombArena> getArenas() {
        return arenas;
    }
    
    /**
     * Wrapper method to allow servers to use older versions of BattleArena.
     * Works by shielding other classes from the ArenaFactory import.
     * Any classes that have this import would break on old BA versions.
     * This class is invoked at runtime only if a newer version of BA is installed.
     */
    public static void registerCompetition(JavaPlugin jplugin, String name, String cmd, Class<? extends BombArena> clazz, CustomCommandExecutor executor) {
        BombArenaFactory factory = new BombArenaFactory((BombPlugin) jplugin);
        BattleArena.registerCompetition(jplugin, name, cmd, factory, executor);
    }
    
}
