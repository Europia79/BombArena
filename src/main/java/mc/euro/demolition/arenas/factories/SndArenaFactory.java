package mc.euro.demolition.arenas.factories;

import java.util.ArrayList;
import java.util.List;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.arenas.ArenaFactory;
import mc.euro.demolition.BombPlugin;
import mc.euro.demolition.arenas.SndArena;

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
    
}
