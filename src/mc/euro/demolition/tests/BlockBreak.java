package mc.euro.demolition.tests;

import com.google.common.collect.Sets;
import java.text.DecimalFormat;
import mc.euro.demolition.BombPlugin;
import mc.euro.demolition.util.DefuseCounter;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * This class measures the amount of milliseconds it takes to break blocks in Minecraft.
 */
public class BlockBreak implements Listener {
    
    public static final double NINEZEROS = 1000000000.0;
    public static final double SIXZEROS = 1000000.0;

    BombPlugin plugin;
    Player player;
    DefuseCounter counter;
    long startTime;
    long endTime;
    double totalTime;
    Block block;

    /**
     * Constructor for the BlockBreak class.
     */
    public BlockBreak(Player p) {
        plugin = (BombPlugin) Bukkit.getPluginManager().getPlugin("BombArena");
        this.player = p;
        this.startTime = 0L;
        this.endTime = 0L;
        this.totalTime = 0;
    }
    
    @EventHandler
    public void onBombInteraction(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (e.getAction() == Action.LEFT_CLICK_BLOCK
                && this.player.getName().equalsIgnoreCase(p.getName()) 
                && this.startTime == 0) {
            this.block = e.getClickedBlock();
            counter = new DefuseCounter(Sets.newHashSet(p));
            this.startTime = DefuseCounter.getCurrentTime();
            plugin.debug.sendMessage(p, "Time has been started");
            PlayerInteractEvent.getHandlerList().unregister(this);
        }
        
    }
    
    @EventHandler
    public void onBombDefusal(BlockBreakEvent e) {
        Player p = e.getPlayer();

        if (e.getBlock().getType() == this.block.getType() 
                && player.getName().equalsIgnoreCase(p.getName())) 
        {
            this.endTime = DefuseCounter.getCurrentTime();
            this.totalTime = (endTime - startTime);
            
            int totalMilli = (int) (this.totalTime / SIXZEROS);
            double totalSeconds = (double) (this.totalTime / NINEZEROS);
            DecimalFormat f = new DecimalFormat("0.00");
            String t = f.format(totalSeconds);
            
            p.sendMessage("It took " + t + " seconds to destroy " + block.getType().name());
            
            plugin.getConfig().set("BreakTimes." + block.getType().name(), totalMilli);
            
            HandlerList.unregisterAll(this);
        } 
    }
    
    
 
}
