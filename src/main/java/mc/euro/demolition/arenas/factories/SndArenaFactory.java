package mc.euro.demolition.arenas.factories;

import java.util.ArrayList;
import java.util.List;
import mc.alk.arena.BattleArena;
import mc.alk.arena.executors.CustomCommandExecutor;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.arenas.ArenaFactory;
import mc.euro.demolition.BombPlugin;
import mc.euro.demolition.arenas.SndArena;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Creates new instances of SndArena. <br/><br/>
 * 
 * Registration using ArenaFactory requires BattleArena v3.9.8+
 * 
 * @author Nikolai
 */
public class SndArenaFactory implements ArenaFactory {
    
    private static final List<SndArena> arenas = new ArrayList<SndArena>();
    
    private BombPlugin plugin;
    
    public SndArenaFactory(BombPlugin reference) {
        this.plugin = reference;
    }

    @Override
    public Arena newArena() {
        SndArena snd = new SndArena(plugin);
        arenas.add(snd);
        return snd;
    }
    
    public List<SndArena> getArenas() {
        return arenas;
    }
    
    /**
     * Wrapper method to allow servers to use older versions of BattleArena.
     * Works by shielding other classes from the ArenaFactory import.
     * Any classes that have this import would break on old BA versions.
     * This class is invoked at runtime only if a newer version of BA is installed.
     */
    public static void registerCompetition(JavaPlugin jplugin, String name, String cmd, Class<? extends SndArena> clazz, CustomCommandExecutor executor) {
        SndArenaFactory factory = new SndArenaFactory((BombPlugin) jplugin);
        BattleArena.registerCompetition(jplugin, name, cmd, factory, executor);
    }
    
}
