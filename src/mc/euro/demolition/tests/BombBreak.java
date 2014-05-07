package mc.euro.demolition.tests;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;
import mc.euro.demolition.BombPlugin;
import mc.euro.demolition.util.DefuseCounter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * This class simulates the Bomb Defusal Event outside of Arenas for testing purposes.
 */
public class BombBreak implements Listener {
    
    public static final double NINEZEROS = 1000000000.0;

    BombPlugin plugin;
    Player player;
    DefuseCounter counter;
    long startTime;
    long endTime;
    double totalTime;

    /**
     * Constructor for the TESTER class.
     */
    public BombBreak(Player p) {
        plugin = (BombPlugin) Bukkit.getPluginManager().getPlugin("BombArena");
        this.player = p;
        this.startTime = 0L;
        this.endTime = 0L;
        this.totalTime = 0;
    }
    
    @EventHandler (priority=EventPriority.HIGHEST, ignoreCancelled = false)
    public void onBombInteraction(PlayerInteractEvent e) {
        if (e.getAction() == Action.LEFT_CLICK_BLOCK
                && e.getClickedBlock().getType() == plugin.getBombBlock() 
                && this.startTime == 0) {
            Player p = e.getPlayer();
            Set<Player> pset = new HashSet<Player>();
            pset.add(p);
            counter = new DefuseCounter(pset);
            this.startTime = DefuseCounter.getCurrentTime();
            plugin.debug.sendMessage(p, "Time has been started");
            PlayerInteractEvent.getHandlerList().unregister(this);
        }
        
    }
    
    @EventHandler (priority=EventPriority.HIGHEST)
    public void onBombDefusal(BlockBreakEvent e) {
        Player p = e.getPlayer();

        if (e.getBlock().getType() == plugin.BombBlock 
                && player.getName().equalsIgnoreCase(p.getName())) 
        {
            int timesBroken = counter.addBlockBreak(p);
            
            
            if (timesBroken >= plugin.getDefuseTime()) {
                this.endTime = DefuseCounter.getCurrentTime();
                this.totalTime = (double) (endTime - startTime) / NINEZEROS;
                DecimalFormat f = new DecimalFormat("0.00");
                String t = f.format(totalTime);
                p.sendMessage("You have simulated the bomb defusal outside of an Arena: ");
                p.sendMessage("You broke it " + timesBroken + " times.");
                p.sendMessage("It took you " + t + " seconds to defuse the fake bomb.");
                HandlerList.unregisterAll(this);
            } else if (timesBroken < plugin.getDefuseTime()) {
                e.setCancelled(true);
            }
        } 
    }
    
    
 
}
