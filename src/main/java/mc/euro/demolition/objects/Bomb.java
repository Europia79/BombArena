package mc.euro.demolition.objects;

import java.util.List;
import java.util.UUID;
import mc.euro.demolition.BombPlugin;
import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

/**
 * Bomb.java: Used to trigger a new PlayerDropItemEvent for onBombCarrierDeath().
 */
@Deprecated public class Bomb implements Item {
    
    BombPlugin plugin;
    int pickupDelay;
    ItemStack bomb;
    Location loc;
    UUID uuid;
    
    public Bomb(PlayerDeathEvent e) {
        this.plugin = (BombPlugin) Bukkit.getPluginManager().getPlugin("BombArena");
        pickupDelay = 0;
        bomb = new ItemStack(plugin.getBombBlock());
        loc = e.getEntity().getPlayer().getLocation();
        uuid = UUID.randomUUID();
    }
    
    public Bomb(PlayerDeathEvent e, int i) {
        this.plugin = (BombPlugin) Bukkit.getPluginManager().getPlugin("BombArena");
        pickupDelay = i;
        bomb = new ItemStack(plugin.getBombBlock());
        loc = e.getEntity().getPlayer().getLocation();
        uuid = UUID.randomUUID();
    }
    

    @Override
    public ItemStack getItemStack() {
        return bomb;
    }

    @Override
    public void setItemStack(ItemStack is) {
        this.bomb = is;
    }

    @Override
    public int getPickupDelay() {
        return pickupDelay;
    }

    @Override
    public void setPickupDelay(int i) {
        pickupDelay = i;
    }

    @Override
    public Location getLocation() {
        return loc;
    }

    @Override
    public Location getLocation(Location lctn) {
        return loc;
    }

    @Override
    public void setVelocity(Vector vector) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Vector getVelocity() {
        return new Vector(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    @Override
    public boolean isOnGround() {
        return true;
    }

    @Override
    public World getWorld() {
        return loc.getWorld();
    }

    @Override
    public boolean teleport(Location lctn) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean teleport(Location lctn, PlayerTeleportEvent.TeleportCause tc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean teleport(Entity entity) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean teleport(Entity entity, PlayerTeleportEvent.TeleportCause tc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Entity> getNearbyEntities(double d, double d1, double d2) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getEntityId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getFireTicks() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getMaxFireTicks() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setFireTicks(int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isDead() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isValid() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Server getServer() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Entity getPassenger() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean setPassenger(Entity entity) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean eject() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public float getFallDistance() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setFallDistance(float f) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setLastDamageCause(EntityDamageEvent ede) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public EntityDamageEvent getLastDamageCause() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public UUID getUniqueId() {
        return this.uuid;
    }

    @Override
    public int getTicksLived() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setTicksLived(int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void playEffect(EntityEffect ee) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public EntityType getType() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isInsideVehicle() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean leaveVehicle() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Entity getVehicle() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setMetadata(String string, MetadataValue mv) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<MetadataValue> getMetadata(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasMetadata(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeMetadata(String string, Plugin plugin) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
