package mc.euro.demolition.sound;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * 
 * @author Nikolai
 */
public class SoundPlayer extends BukkitRunnable {
    
    Player player;
    Location location;
    Sound sound;
    float volume;
    float pitch;
    
    public SoundPlayer(Player player, Sound sound, float volume, float pitch) {
        this.player = player;
        this.location = player.getLocation();
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    @Override
    public void run() {
        player.sendMessage("Playing: " + sound.toString() + ". Pitch: " + pitch);
        player.playSound(location, sound, volume, pitch);
    }

}
