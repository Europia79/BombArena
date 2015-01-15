package mc.euro.demolition.arenas.factories;

import java.util.ArrayList;
import java.util.List;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.arenas.ArenaFactory;
import mc.euro.demolition.arenas.BombArena;
import mc.euro.demolition.BombPlugin;

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
    
}
