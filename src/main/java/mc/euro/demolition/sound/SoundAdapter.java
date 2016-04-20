package mc.euro.demolition.sound;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Sound;

/**
 * Handles the conversion from old sounds to new sounds and vice-versa. Allows
 * easy filtering of user-input... to make it easy on server admins to be able
 * to specify 1.8 Sound and have it "just work" on a 1.9 server and vice-versa.
 *
 * https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Sound.html
 * http://rainbowcraft.sytes.net/javadocs/bukkit/1.8/org/bukkit/Sound.html
 * http://minecraft.gamepedia.com/Sounds.json#Sound_events
 */
public class SoundAdapter {

    private static final Map<String, String> smap = initMap();

    private static Map<String, String> initMap() {
        Map<String, String> temp = new HashMap<String, String>();
        
        // Key = v1.8 org.bukkit.Sound
        // Value = v1.9 org.bukkit.Sound
        // Names have changed, so let's match the old 1.8 name with the new 1.9 name.
        // https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Sound.html
        // http://rainbowcraft.sytes.net/javadocs/bukkit/1.8/org/bukkit/Sound.html
        // http://minecraft.gamepedia.com/Sounds.json#Sound_events
        // XXXXXXXXXXXXX = No matching equivalent
        temp.put("AMBIENCE_CAVE", "AMBIENT_CAVE");
        temp.put("AMBIENCE_RAIN", "WEATHER_RAIN_ABOVE");
        temp.put("AMBIENCE_THUNDER", "ENTITY_LIGHTNING_THUNDER");
        temp.put("ANVIL_BREAK", "BLOCK_ANVIL_BREAK");
        temp.put("ANVIL_LAND", "BLOCK_ANVIL_LAND");
        temp.put("ANVIL_USE", "BLOCK_ANVIL_USE");
        temp.put("ARROW_HIT", "ENTITY_ARROW_HIT");
        temp.put("BAT_DEATH", "ENTITY_BAT_DEATH");
        temp.put("BAT_HURT", "ENTITY_BAT_HURT");
        temp.put("BAT_IDLE", "ENTITY_BAT_AMBIENT");
        temp.put("BAT_LOOP", "ENTITY_BAT_LOOP");
        temp.put("BAT_TAKEOFF", "ENTITY_BAT_TAKEOFF");
        temp.put("BLAZE_BREATH", "ENTITY_BLAZE_AMBIENT");
        temp.put("BLAZE_DEATH", "ENTITY_BLAZE_DEATH");
        temp.put("BLAZE_HIT", "ENTITY_BLAZE_HURT");
        temp.put("BURP", "ENTITY_PLAYER_BURP");
        temp.put("CAT_HISS", "ENTITY_CAT_HISS");
        temp.put("CAT_HIT", "ENTITY_CAT_HURT");
        temp.put("CAT_MEOW", "ENTITY_CAT_AMBIENT");
        temp.put("CAT_PURR", "ENTITY_CAT_PURR");
        temp.put("CAT_PURREOW", "ENTITY_CAT_PURREOW");
        temp.put("CHEST_CLOSE", "BLOCK_CHEST_CLOSE");
        temp.put("CHEST_OPEN", "BLOCK_CHEST_OPEN");
        temp.put("CHICKEN_EGG_POP", "ENTITY_CHICKEN_EGG");
        temp.put("CHICKEN_HURT", "ENTITY_CHICKEN_HURT");
        temp.put("CHICKEN_IDLE", "ENTITY_CHICKEN_AMBIENT");
        temp.put("CHICKEN_WALK", "ENTITY_CHICKEN_STEP");
        temp.put("CLICK", "UI_BUTTON_CLICK");
        temp.put("COW_HURT", "ENTITY_COW_HURT");
        temp.put("COW_IDLE", "ENTITY_COW_AMBIENT");
        temp.put("COW_WALK", "ENTITY_COW_STEP");
        temp.put("CREEPER_DEATH", "ENTITY_CREEPER_DEATH");
        temp.put("CREEPER_HISS", "ENTITY_CREEPER_PRIMED");
        temp.put("DIG_GRASS", "BLOCK_GRASS_HIT");
        temp.put("DIG_GRAVEL", "BLOCK_GRAVEL_HIT");
        temp.put("DIG_SAND", "BLOCK_SAND_HIT");
        temp.put("DIG_SNOW", "BLOCK_SNOW_HIT");
        temp.put("DIG_STONE", "BLOCK_STONE_HIT");
        temp.put("DIG_WOOD", "BLOCK_WOOD_HIT");
        temp.put("DIG_WOOL", "BLOCK_CLOTH_HIT");
        temp.put("DONKEY_ANGRY", "ENTITY_DONKEY_ANGRY");
        temp.put("DONKEY_DEATH", "ENTITY_DONKEY_DEATH");
        temp.put("DONKEY_HIT", "ENTITY_DONKEY_HURT");
        temp.put("DONKEY_IDLE", "ENTITY_DONKEY_AMBIENT");
        temp.put("DOOR_CLOSE", "BLOCK_WOODEN_DOOR_CLOSE");
        temp.put("DOOR_OPEN", "BLOCK_WOODEN_DOOR_OPEN");
        temp.put("DRINK", "ENTITY_GENERIC_DRINK");
        temp.put("EAT", "ENTITY_GENERIC_EAT");
        temp.put("ENDERDRAGON_DEATH", "ENTITY_ENDERDRAGON_DEATH");
        temp.put("ENDERDRAGON_GROWL", "ENTITY_ENDERDRAGON_GROWL");
        temp.put("ENDERDRAGON_HIT", "ENTITY_ENDERDRAGON_HURT");
        temp.put("ENDERDRAGON_WINGS", "ENTITY_ENDERDRAGON_FLAP");
        temp.put("ENDERMAN_DEATH", "ENTITY_ENDERMEN_DEATH");
        temp.put("ENDERMAN_HIT", "ENTITY_ENDERMEN_HURT");
        temp.put("ENDERMAN_IDLE", "ENTITY_ENDERMEN_AMBIENT");
        temp.put("ENDERMAN_SCREAM", "ENTITY_ENDERMEN_SCREAM");
        temp.put("ENDERMAN_STARE", "ENTITY_ENDERMEN_STARE");
        temp.put("ENDERMAN_TELEPORT", "ENTITY_ENDERMEN_TELEPORT");
        temp.put("EXPLODE", "ENTITY_GENERIC_EXPLODE");
        temp.put("FALL_BIG", "ENTITY_GENERIC_BIG_FALL");
        temp.put("FALL_SMALL", "ENTITY_GENERIC_SMALL_FALL");
        temp.put("FIRE", "BLOCK_FIRE_AMBIENT");
        temp.put("FIRE_IGNITE", "ITEM_FLINTANDSTEEL_USE");
        temp.put("FIREWORK_BLAST", "ENTITY_FIREWORK_BLAST");
        temp.put("FIREWORK_BLAST2", "ENTITY_FIREWORK_BLAST_FAR");
        temp.put("FIREWORK_LARGE_BLAST", "ENTITY_FIREWORK_LARGE_BLAST");
        temp.put("FIREWORK_LARGE_BLAST2", "ENTITY_FIREWORK_LARGE_BLAST_FAR");
        temp.put("FIREWORK_LAUNCH", "ENTITY_FIREWORK_LAUNCH");
        temp.put("FIREWORK_TWINKLE", "ENTITY_FIREWORK_TWINKLE");
        temp.put("FIREWORK_TWINKLE2", "ENTITY_FIREWORK_TWINKLE_FAR");
        temp.put("FIZZ", "BLOCK_FIRE_EXTINGUISH");
        temp.put("FUSE", "ENTITY_TNT_PRIMED");
        temp.put("GHAST_CHARGE", "ENTITY_GHAST_WARN");
        temp.put("GHAST_DEATH", "ENTITY_GHAST_DEATH");
        temp.put("GHAST_FIREBALL", "ENTITY_GHAST_SHOOT");
        temp.put("GHAST_MOAN", "ENTITY_GHAST_AMBIENT");
        temp.put("GHAST_SCREAM", "ENTITY_GHAST_HURT");
        temp.put("GHAST_SCREAM2", "ENTITY_GHAST_SCREAM");
        temp.put("GLASS", "BLOCK_GLASS_BREAK");
        temp.put("HORSE_ANGRY", "ENTITY_HORSE_ANGRY");
        temp.put("HORSE_ARMOR", "ENTITY_HORSE_ARMOR");
        temp.put("HORSE_BREATHE", "ENTITY_HORSE_BREATHE");
        temp.put("HORSE_DEATH", "ENTITY_HORSE_DEATH");
        temp.put("HORSE_GALLOP", "ENTITY_HORSE_GALLOP");
        temp.put("HORSE_HIT", "ENTITY_HORSE_HURT");
        temp.put("HORSE_IDLE", "ENTITY_HORSE_AMBIENT");
        temp.put("HORSE_JUMP", "ENTITY_HORSE_JUMP");
        temp.put("HORSE_LAND", "ENTITY_HORSE_LAND");
        temp.put("HORSE_SADDLE", "ENTITY_HORSE_SADDLE");
        temp.put("HORSE_SKELETON_DEATH", "ENTITY_SKELETON_HORSE_DEATH");
        temp.put("HORSE_SKELETON_HIT", "ENTITY_SKELETON_HORSE_HURT");
        temp.put("HORSE_SKELETON_IDLE", "ENTITY_SKELETON_HORSE_AMBIENT");
        temp.put("HORSE_SOFT", "ENTITY_HORSE_STEP");
        temp.put("HORSE_WOOD", "ENTITY_HORSE_STEP_WOOD");
        temp.put("HORSE_ZOMBIE_DEATH", "ENTITY_ZOMBIE_HORSE_DEATH");
        temp.put("HORSE_ZOMBIE_HIT", "ENTITY_ZOMBIE_HORSE_HURT");
        temp.put("HORSE_ZOMBIE_IDLE", "ENTITY_ZOMBIE_HORSE_AMBIENT");
        temp.put("HURT_FLESH", "ENTITY_PLAYER_HURT");
        temp.put("IRONGOLEM_DEATH", "ENTITY_IRONGOLEM_DEATH");
        temp.put("IRONGOLEM_HIT", "ENTITY_IRONGOLEM_HURT");
        temp.put("IRONGOLEM_THROW", "ENTITY_IRONGOLEM_ATTACK");
        temp.put("IRONGOLEM_WALK", "ENTITY_IRONGOLEM_STEP");
        temp.put("ITEM_BREAK", "ENTITY_ITEM_BREAK");
        temp.put("ITEM_PICKUP", "ENTITY_ITEM_PICKUP");
        temp.put("LAVA", "BLOCK_LAVA_AMBIENT");
        temp.put("LAVA_POP", "BLOCK_LAVA_POP");
        temp.put("LEVEL_UP", "ENTITY_PLAYER_LEVELUP");
        temp.put("MAGMACUBE_JUMP", "ENTITY_MAGMACUBE_JUMP");
        temp.put("MAGMACUBE_WALK", "ENTITY_MAGMACUBE_SQUISH");
        temp.put("MAGMACUBE_WALK2", "ENTITY_MAGMACUBE_HURT");
        temp.put("MINECART_BASE", "ENTITY_MINECART_RIDING");
        temp.put("MINECART_INSIDE", "ENTITY_MINECART_INSIDE");
        temp.put("NOTE_BASS", "BLOCK_NOTE_BASS");
        temp.put("NOTE_BASS_DRUM", "BLOCK_NOTE_BASEDRUM");
        temp.put("NOTE_BASS_GUITAR", "BLOCK_NOTE_BASS"); // XXXXXXXXXXXXX
        temp.put("NOTE_PIANO", "BLOCK_NOTE_HARP");
        temp.put("NOTE_PLING", "BLOCK_NOTE_PLING");
        temp.put("NOTE_SNARE_DRUM", "BLOCK_NOTE_SNARE");
        temp.put("NOTE_STICKS", "BLOCK_NOTE_HAT");
        temp.put("ORB_PICKUP", "ENTITY_EXPERIENCE_ORB_PICKUP");
        temp.put("PIG_DEATH", "ENTITY_PIG_DEATH");
        temp.put("PIG_IDLE", "ENTITY_PIG_AMBIENT");
        temp.put("PIG_WALK", "ENTITY_PIG_STEP");
        temp.put("PISTON_EXTEND", "BLOCK_PISTON_EXTEND");
        temp.put("PISTON_RETRACT", "BLOCK_PISTON_CONTRACT");
        temp.put("PORTAL", "BLOCK_PORTAL_AMBIENT");
        temp.put("PORTAL_TRAVEL", "BLOCK_PORTAL_TRAVEL");
        temp.put("PORTAL_TRIGGER", "BLOCK_PORTAL_TRIGGER");
        temp.put("SHEEP_IDLE", "ENTITY_SHEEP_AMBIENT");
        temp.put("SHEEP_SHEAR", "ENTITY_SHEEP_SHEAR");
        temp.put("SHEEP_WALK", "ENTITY_SHEEP_STEP");
        temp.put("SHOOT_ARROW", "ENTITY_ARROW_SHOOT");
        temp.put("SILVERFISH_HIT", "ENTITY_SILVERFISH_HURT");
        temp.put("SILVERFISH_IDLE", "ENTITY_SILVERFISH_AMBIENT");
        temp.put("SILVERFISH_KILL", "ENTITY_SILVERFISH_DEATH");
        temp.put("SILVERFISH_WALK", "ENTITY_SILVERFISH_STEP");
        temp.put("SKELETON_DEATH", "ENTITY_SKELETON_DEATH");
        temp.put("SKELETON_HURT", "ENTITY_SKELETON_HURT");
        temp.put("SKELETON_IDLE", "ENTITY_SKELETON_AMBIENT");
        temp.put("SKELETON_WALK", "ENTITY_SKELETON_STEP");
        temp.put("SLIME_ATTACK", "ENTITY_SLIME_ATTACK");
        temp.put("SLIME_WALK", "ENTITY_SLIME_HURT");
        temp.put("SLIME_WALK2", "ENTITY_SLIME_JUMP");
        temp.put("SPIDER_DEATH", "ENTITY_SPIDER_DEATH");
        temp.put("SPIDER_IDLE", "ENTITY_SPIDER_AMBIENT");
        temp.put("SPIDER_WALK", "ENTITY_SPIDER_STEP");
        temp.put("SPLASH", "ENTITY_PLAYER_SWIM");
        temp.put("SPLASH2", "ENTITY_PLAYER_SPLASH");
        temp.put("STEP_GRASS", "BLOCK_GRASS_STEP");
        temp.put("STEP_GRAVEL", "BLOCK_GRAVEL_STEP");
        temp.put("STEP_LADDER", "BLOCK_LADDER_STEP");
        temp.put("STEP_SAND", "BLOCK_SAND_STEP");
        temp.put("STEP_SNOW", "BLOCK_SNOW_STEP");
        temp.put("STEP_STONE", "BLOCK_STONE_STEP");
        temp.put("STEP_WOOD", "BLOCK_WOOD_STEP");
        temp.put("STEP_WOOL", "BLOCK_CLOTH_STEP");
        temp.put("SUCCESSFUL_HIT", "ENTITY_ARROW_HIT_PLAYER");
        temp.put("SWIM", "ENTITY_PLAYER_SWIM");
        temp.put("VILLAGER_DEATH", "ENTITY_VILLAGER_DEATH");
        temp.put("VILLAGER_HAGGLE", "ENTITY_VILLAGER_TRADING");
        temp.put("VILLAGER_HIT", "ENTITY_VILLAGER_HURT");
        temp.put("VILLAGER_IDLE", "ENTITY_VILLAGER_AMBIENT");
        temp.put("VILLAGER_NO", "ENTITY_VILLAGER_NO");
        temp.put("VILLAGER_YES", "ENTITY_VILLAGER_YES");
        temp.put("WATER", "BLOCK_WATER_AMBIENT");
        temp.put("WITHER_DEATH", "ENTITY_WITHER_DEATH");
        temp.put("WITHER_HURT", "ENTITY_WITHER_HURT");
        temp.put("WITHER_IDLE", "ENTITY_WITHER_AMBIENT");
        temp.put("WITHER_SHOOT", "ENTITY_WITHER_SHOOT");
        temp.put("WITHER_SPAWN", "ENTITY_WITHER_SPAWN");
        temp.put("WOLF_BARK", "ENTITY_WOLF_AMBIENT");
        temp.put("WOLF_DEATH", "ENTITY_WOLF_DEATH");
        temp.put("WOLF_GROWL", "ENTITY_WOLF_GROWL");
        temp.put("WOLF_HOWL", "ENTITY_WOLF_HOWL");
        temp.put("WOLF_HURT", "ENTITY_WOLF_HURT");
        temp.put("WOLF_PANT", "ENTITY_WOLF_PANT");
        temp.put("WOLF_SHAKE", "ENTITY_WOLF_SHAKE");
        temp.put("WOLF_WALK", "ENTITY_WOLF_STEP");
        temp.put("WOLF_WHINE", "ENTITY_WOLF_WHINE");
        temp.put("WOOD_CLICK", "BLOCK_WOOD_BUTTON_CLICK_ON");
        temp.put("ZOMBIE_DEATH", "ENTITY_ZOMBIE_DEATH");
        temp.put("ZOMBIE_HURT", "ENTITY_ZOMBIE_HURT");
        temp.put("ZOMBIE_IDLE", "ENTITY_ZOMBIE_AMBIENT");
        temp.put("ZOMBIE_INFECT", "ENTITY_ZOMBIE_INFECT");
        temp.put("ZOMBIE_METAL", "ENTITY_ZOMBIE_ATTACK_IRON_DOOR");
        temp.put("ZOMBIE_PIG_ANGRY", "ENTITY_ZOMBIE_PIG_ANGRY");
        temp.put("ZOMBIE_PIG_DEATH", "ENTITY_ZOMBIE_PIG_DEATH");
        temp.put("ZOMBIE_PIG_HURT", "ENTITY_ZOMBIE_PIG_HURT");
        temp.put("ZOMBIE_PIG_IDLE", "ENTITY_ZOMBIE_PIG_AMBIENT");
        temp.put("ZOMBIE_REMEDY", "ENTITY_ZOMBIE_VILLAGER_CURE");
        temp.put("ZOMBIE_UNFECT", "ENTITY_ZOMBIE_VILLAGER_CONVERTED");
        temp.put("ZOMBIE_WALK", "ENTITY_ZOMBIE_STEP");
        temp.put("ZOMBIE_WOOD", "ENTITY_ZOMBIE_ATTACK_DOOR_WOOD");
        temp.put("ZOMBIE_WOODBREAK", "ENTITY_ZOMBIE_BREAK_DOOR_WOOD");

        // Key = v1.9 org.bukkit.Sound
        // Value = v1.8 org.bukkit.Sound
        // Let's make sure that every 1.9 Sound has a 1.8 non-null equivalent.
        // If no 1.8 value matches exactly, we'll just pick the closest one.
        // https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Sound.html
        // http://rainbowcraft.sytes.net/javadocs/bukkit/1.8/org/bukkit/Sound.html
        // http://minecraft.gamepedia.com/Sounds.json#Sound_events
        // XXXXXXXXXXXXX = No matching equivalent
        temp.put("AMBIENT_CAVE", "AMBIENCE_CAVE");
        temp.put("BLOCK_ANVIL_BREAK", "ANVIL_BREAK");
        temp.put("BLOCK_ANVIL_DESTROY", "ANVIL_BREAK");
        temp.put("BLOCK_ANVIL_FALL", "ANVIL_LAND");
        temp.put("BLOCK_ANVIL_HIT", "ANVIL_USE");
        temp.put("BLOCK_ANVIL_LAND", "ANVIL_LAND");
        temp.put("BLOCK_ANVIL_PLACE", "ANVIL_LAND");
        temp.put("BLOCK_ANVIL_STEP", "ANVIL_LAND");
        temp.put("BLOCK_ANVIL_USE", "ANVIL_USE");
        temp.put("BLOCK_BREWING_STAND_BREW", "LAVA");
        temp.put("BLOCK_CHEST_CLOSE", "CHEST_CLOSE");
        temp.put("BLOCK_CHEST_LOCKED", "CHEST_CLOSE");
        temp.put("BLOCK_CHEST_OPEN", "CHEST_OPEN");
        temp.put("BLOCK_CHORUS_FLOWER_DEATH", "CREEPER_DEATH"); // XXXXXXXXXXXXX
        temp.put("BLOCK_CHORUS_FLOWER_GROW", "CAT_PURREOW"); // XXXXXXXXXXXXX
        temp.put("BLOCK_CLOTH_BREAK", "DIG_WOOL");
        temp.put("BLOCK_CLOTH_FALL", "DIG_WOOL");
        temp.put("BLOCK_CLOTH_HIT", "DIG_WOOL");
        temp.put("BLOCK_CLOTH_PLACE", "DIG_WOOL");
        temp.put("BLOCK_CLOTH_STEP", "STEP_WOOL");
        temp.put("BLOCK_COMPARATOR_CLICK", "CLICK");
        temp.put("BLOCK_DISPENSER_DISPENSE", "CLICK");
        temp.put("BLOCK_DISPENSER_FAIL", "CLICK");
        temp.put("BLOCK_DISPENSER_LAUNCH", "CLICK");
        temp.put("BLOCK_END_GATEWAY_SPAWN", "EXPLODE");
        temp.put("BLOCK_ENDERCHEST_CLOSE", "CHEST_CLOSE");
        temp.put("BLOCK_ENDERCHEST_OPEN", "CHEST_OPEN");
        temp.put("BLOCK_FENCE_GATE_CLOSE", "DOOR_CLOSE");
        temp.put("BLOCK_FENCE_GATE_OPEN", "DOOR_OPEN");
        temp.put("BLOCK_FIRE_AMBIENT", "FIRE");
        temp.put("BLOCK_FIRE_EXTINGUISH", "FIZZ");
        temp.put("BLOCK_FURNACE_FIRE_CRACKLE", "FIRE");
        temp.put("BLOCK_GLASS_BREAK", "GLASS");
        temp.put("BLOCK_GLASS_FALL", "DIG_STONE");
        temp.put("BLOCK_GLASS_HIT", "GLASS");
        temp.put("BLOCK_GLASS_PLACE", "DIG_STONE");
        temp.put("BLOCK_GLASS_STEP", "STEP_STONE");
        temp.put("BLOCK_GRASS_BREAK", "DIG_GRASS");
        temp.put("BLOCK_GRASS_FALL", "DIG_GRASS");
        temp.put("BLOCK_GRASS_HIT", "DIG_GRASS");
        temp.put("BLOCK_GRASS_PLACE", "DIG_GRASS");
        temp.put("BLOCK_GRASS_STEP", "STEP_GRASS");
        temp.put("BLOCK_GRAVEL_BREAK", "DIG_GRAVEL");
        temp.put("BLOCK_GRAVEL_FALL", "DIG_GRASS");
        temp.put("BLOCK_GRAVEL_HIT", "DIG_GRAVEL");
        temp.put("BLOCK_GRAVEL_PLACE", "DIG_GRASS");
        temp.put("BLOCK_GRAVEL_STEP", "STEP_GRAVEL");
        temp.put("BLOCK_IRON_DOOR_CLOSE", "DOOR_CLOSE");
        temp.put("BLOCK_IRON_DOOR_OPEN", "DOOR_OPEN");
        temp.put("BLOCK_IRON_TRAPDOOR_CLOSE", "DOOR_CLOSE");
        temp.put("BLOCK_IRON_TRAPDOOR_OPEN", "DOOR_OPEN");
        temp.put("BLOCK_LADDER_BREAK", "DIG_WOOD");
        temp.put("BLOCK_LADDER_FALL", "DIG_WOOD");
        temp.put("BLOCK_LADDER_HIT", "DIG_WOOD");
        temp.put("BLOCK_LADDER_PLACE", "DIG_WOOD");
        temp.put("BLOCK_LADDER_STEP", "STEP_LADDER");
        temp.put("BLOCK_LAVA_AMBIENT", "LAVA");
        temp.put("BLOCK_LAVA_EXTINGUISH", "FIZZ");
        temp.put("BLOCK_LAVA_POP", "LAVA_POP");
        temp.put("BLOCK_LEVER_CLICK", "WOOD_CLICK");
        temp.put("BLOCK_METAL_BREAK", "DIG_STONE");
        temp.put("BLOCK_METAL_FALL", "DIG_STONE");
        temp.put("BLOCK_METAL_HIT", "DIG_STONE");
        temp.put("BLOCK_METAL_PLACE", "DIG_STONE");
        temp.put("BLOCK_METAL_PRESSUREPLATE_CLICK_OFF", "CLICK");
        temp.put("BLOCK_METAL_PRESSUREPLATE_CLICK_ON", "CLICK");
        temp.put("BLOCK_METAL_STEP", "STEP_STONE");
        temp.put("BLOCK_NOTE_BASEDRUM", "NOTE_BASS_DRUM");
        temp.put("BLOCK_NOTE_BASS", "NOTE_BASS");
        temp.put("BLOCK_NOTE_HARP", "NOTE_PIANO");
        temp.put("BLOCK_NOTE_HAT", "NOTE_STICKS");
        temp.put("BLOCK_NOTE_PLING", "NOTE_PLING");
        temp.put("BLOCK_NOTE_SNARE", "NOTE_SNARE_DRUM");
        temp.put("BLOCK_PISTON_CONTRACT", "PISTON_RETRACT");
        temp.put("BLOCK_PISTON_EXTEND", "PISTON_EXTEND");
        temp.put("BLOCK_PORTAL_AMBIENT", "PORTAL");
        temp.put("BLOCK_PORTAL_TRAVEL", "PORTAL_TRAVEL");
        temp.put("BLOCK_PORTAL_TRIGGER", "PORTAL_TRIGGER");
        temp.put("BLOCK_REDSTONE_TORCH_BURNOUT", "DIG_WOOD");
        temp.put("BLOCK_SAND_BREAK", "DIG_SAND");
        temp.put("BLOCK_SAND_FALL", "DIG_SAND");
        temp.put("BLOCK_SAND_HIT", "DIG_SAND");
        temp.put("BLOCK_SAND_PLACE", "DIG_SAND");
        temp.put("BLOCK_SAND_STEP", "STEP_SAND");
        temp.put("BLOCK_SLIME_BREAK", "SLIME_WALK2");
        temp.put("BLOCK_SLIME_FALL", "SLIME_WALK2");
        temp.put("BLOCK_SLIME_HIT", "SLIME_WALK2");
        temp.put("BLOCK_SLIME_PLACE", "SLIME_WALK2");
        temp.put("BLOCK_SLIME_STEP", "STEP_SAND"); // XXXXXXXXXXXXX
        temp.put("BLOCK_SNOW_BREAK", "DIG_SNOW");
        temp.put("BLOCK_SNOW_FALL", "DIG_SNOW");
        temp.put("BLOCK_SNOW_HIT", "DIG_SNOW");
        temp.put("BLOCK_SNOW_PLACE", "DIG_SNOW");
        temp.put("BLOCK_SNOW_STEP", "STEP_SNOW");
        temp.put("BLOCK_STONE_BREAK", "DIG_STONE");
        temp.put("BLOCK_STONE_BUTTON_CLICK_OFF", "CLICK");
        temp.put("BLOCK_STONE_BUTTON_CLICK_ON", "CLICK");
        temp.put("BLOCK_STONE_FALL", "DIG_STONE");
        temp.put("BLOCK_STONE_HIT", "DIG_STONE");
        temp.put("BLOCK_STONE_PLACE", "DIG_STONE");
        temp.put("BLOCK_STONE_PRESSUREPLATE_CLICK_OFF", "CLICK");
        temp.put("BLOCK_STONE_PRESSUREPLATE_CLICK_ON", "CLICK");
        temp.put("BLOCK_STONE_STEP", "STEP_STONE");
        temp.put("BLOCK_TRIPWIRE_ATTACH", "DIG_WOOD");
        temp.put("BLOCK_TRIPWIRE_CLICK_OFF", "CLICK");
        temp.put("BLOCK_TRIPWIRE_CLICK_ON", "CLICK");
        temp.put("BLOCK_TRIPWIRE_DETACH", "SHOOT_ARROW"); // ARROW_HIT
        temp.put("BLOCK_WATER_AMBIENT", "WATER");
        temp.put("BLOCK_WATERLILY_PLACE", "DIG_WOOL"); // XXXXXXXXXXXXX
        temp.put("BLOCK_WOOD_BREAK", "DIG_WOOD");
        temp.put("BLOCK_WOOD_BUTTON_CLICK_OFF", "WOOD_CLICK");
        temp.put("BLOCK_WOOD_BUTTON_CLICK_ON", "WOOD_CLICK");
        temp.put("BLOCK_WOOD_FALL", "DIG_WOOD");
        temp.put("BLOCK_WOOD_HIT", "DIG_WOOD");
        temp.put("BLOCK_WOOD_PLACE", "DIG_WOOD");
        temp.put("BLOCK_WOOD_PRESSUREPLATE_CLICK_OFF", "WOOD_CLICK");
        temp.put("BLOCK_WOOD_PRESSUREPLATE_CLICK_ON", "WOOD_CLICK");
        temp.put("BLOCK_WOOD_STEP", "STEP_WOOD");
        temp.put("BLOCK_WOODEN_DOOR_CLOSE", "DOOR_CLOSE");
        temp.put("BLOCK_WOODEN_DOOR_OPEN", "DOOR_OPEN");
        temp.put("BLOCK_WOODEN_TRAPDOOR_CLOSE", "DOOR_CLOSE");
        temp.put("BLOCK_WOODEN_TRAPDOOR_OPEN", "DOOR_OPEN");
        temp.put("ENCHANT_THORNS_HIT", "ARROW_HIT"); // XXXXXXXXXXXXX
        temp.put("ENTITY_ARMORSTAND_BREAK", "ITEM_BREAK"); // XXXXXXXXXXXXX
        temp.put("ENTITY_ARMORSTAND_FALL", "DIG_SNOW"); // XXXXXXXXXXXXX
        temp.put("ENTITY_ARMORSTAND_HIT", "DIG_SNOW"); // XXXXXXXXXXXXX
        temp.put("ENTITY_ARMORSTAND_PLACE", "DIG_SNOW"); // XXXXXXXXXXXXX
        temp.put("ENTITY_ARROW_HIT", "ARROW_HIT");
        temp.put("ENTITY_ARROW_HIT_PLAYER", "SUCCESSFUL_HIT");
        temp.put("ENTITY_ARROW_SHOOT", "SHOOT_ARROW");
        temp.put("ENTITY_BAT_AMBIENT", "BAT_IDLE");
        temp.put("ENTITY_BAT_DEATH", "BAT_DEATH");
        temp.put("ENTITY_BAT_HURT", "BAT_HURT");
        temp.put("ENTITY_BAT_LOOP", "BAT_LOOP");
        temp.put("ENTITY_BAT_TAKEOFF", "BAT_TAKEOFF");
        temp.put("ENTITY_BLAZE_AMBIENT", "BLAZE_BREATH");
        temp.put("ENTITY_BLAZE_BURN", "BLAZE_HIT");
        temp.put("ENTITY_BLAZE_DEATH", "BLAZE_DEATH");
        temp.put("ENTITY_BLAZE_HURT", "BLAZE_HIT");
        temp.put("ENTITY_BLAZE_SHOOT", "BLAZE_HIT");
        temp.put("ENTITY_BOBBER_SPLASH", "WATER"); // XXXXXXXXXXXXX
        temp.put("ENTITY_BOBBER_THROW", "IRONGOLEM_THROW"); // XXXXXXXXXXXXX
        temp.put("ENTITY_CAT_AMBIENT", "CAT_MEOW");
        temp.put("ENTITY_CAT_DEATH", "CAT_HIT");
        temp.put("ENTITY_CAT_HISS", "CAT_HISS");
        temp.put("ENTITY_CAT_HURT", "CAT_HIT");
        temp.put("ENTITY_CAT_PURR", "CAT_PURR");
        temp.put("ENTITY_CAT_PURREOW", "CAT_MEOW");
        temp.put("ENTITY_CHICKEN_AMBIENT", "CHICKEN_IDLE");
        temp.put("ENTITY_CHICKEN_DEATH", "CHICKEN_HURT");
        temp.put("ENTITY_CHICKEN_EGG", "CHICKEN_EGG_POP");
        temp.put("ENTITY_CHICKEN_HURT", "CHICKEN_HURT");
        temp.put("ENTITY_CHICKEN_STEP", "CHICKEN_WALK");
        temp.put("ENTITY_COW_AMBIENT", "COW_IDLE");
        temp.put("ENTITY_COW_DEATH", "COW_HURT");
        temp.put("ENTITY_COW_HURT", "COW_HURT");
        temp.put("ENTITY_COW_MILK", "COW_IDLE");
        temp.put("ENTITY_COW_STEP", "COW_WALK");
        temp.put("ENTITY_CREEPER_DEATH", "CREEPER_DEATH");
        temp.put("ENTITY_CREEPER_HURT", "CREEPER_HISS");
        temp.put("ENTITY_CREEPER_PRIMED", "FUSE");
        temp.put("ENTITY_DONKEY_AMBIENT", "DONKEY_IDLE");
        temp.put("ENTITY_DONKEY_ANGRY", "DONKEY_ANGRY");
        temp.put("ENTITY_DONKEY_CHEST", "DONKEY_IDLE"); // XXXXXXXXXXXXX
        temp.put("ENTITY_DONKEY_DEATH", "DONKEY_DEATH");
        temp.put("ENTITY_DONKEY_HURT", "DONKEY_HIT");
        temp.put("ENTITY_EGG_THROW", "CHICKEN_EGG_POP");
        temp.put("ENTITY_ELDER_GUARDIAN_AMBIENT", "SILVERFISH_IDLE"); // XXXXXXXXXXXXX
        temp.put("ENTITY_ELDER_GUARDIAN_AMBIENT_LAND", "SILVERFISH_IDLE"); // XXXXXXXXXXXXX
        temp.put("ENTITY_ELDER_GUARDIAN_CURSE", "SILVERFISH_IDLE"); // XXXXXXXXXXXXX
        temp.put("ENTITY_ELDER_GUARDIAN_DEATH", "SILVERFISH_KILL"); // XXXXXXXXXXXXX
        temp.put("ENTITY_ELDER_GUARDIAN_DEATH_LAND", "SILVERFISH_KILL"); // XXXXXXXXXXXXX
        temp.put("ENTITY_ELDER_GUARDIAN_HURT", "SILVERFISH_HIT "); // XXXXXXXXXXXXX
        temp.put("ENTITY_ELDER_GUARDIAN_HURT_LAND", "SILVERFISH_HIT"); // XXXXXXXXXXXXX
        temp.put("ENTITY_ENDERDRAGON_AMBIENT", "ENDERDRAGON_WINGS");
        temp.put("ENTITY_ENDERDRAGON_DEATH", "ENDERDRAGON_DEATH");
        temp.put("ENTITY_ENDERDRAGON_FIREBALL_EXPLODE", "EXPLODE");
        temp.put("ENTITY_ENDERDRAGON_FLAP", "ENDERDRAGON_WINGS");
        temp.put("ENTITY_ENDERDRAGON_GROWL", "ENDERDRAGON_GROWL");
        temp.put("ENTITY_ENDERDRAGON_HURT", "ENDERDRAGON_HIT");
        temp.put("ENTITY_ENDERDRAGON_SHOOT", "ENDERDRAGON_HIT");
        temp.put("ENTITY_ENDEREYE_LAUNCH", "CHICKEN_EGG_POP"); // XXXXXXXXXXXXX
        temp.put("ENTITY_ENDERMEN_AMBIENT", "ENDERMAN_IDLE");
        temp.put("ENTITY_ENDERMEN_DEATH", "ENDERMAN_DEATH");
        temp.put("ENTITY_ENDERMEN_HURT", "ENDERMAN_HIT");
        temp.put("ENTITY_ENDERMEN_SCREAM", "ENDERMAN_SCREAM");
        temp.put("ENTITY_ENDERMEN_STARE", "ENDERMAN_STARE");
        temp.put("ENTITY_ENDERMEN_TELEPORT", "ENDERMAN_TELEPORT");
        temp.put("ENTITY_ENDERMITE_AMBIENT", "SILVERFISH_IDLE"); // XXXXXXXXXXXXX
        temp.put("ENTITY_ENDERMITE_DEATH", "SILVERFISH_KILL"); // XXXXXXXXXXXXX
        temp.put("ENTITY_ENDERMITE_HURT", "SILVERFISH_HIT"); // XXXXXXXXXXXXX
        temp.put("ENTITY_ENDERMITE_STEP", "SILVERFISH_WALK"); // XXXXXXXXXXXXX
        temp.put("ENTITY_ENDERPEARL_THROW", "CHICKEN_EGG_POP"); // XXXXXXXXXXXXX
        temp.put("ENTITY_EXPERIENCE_BOTTLE_THROW", "CHICKEN_EGG_POP"); // XXXXXXXXXXXXX
        temp.put("ENTITY_EXPERIENCE_ORB_PICKUP", "ORB_PICKUP");
        temp.put("ENTITY_EXPERIENCE_ORB_TOUCH", "ORB_PICKUP");
        temp.put("ENTITY_FIREWORK_BLAST", "FIREWORK_BLAST");
        temp.put("ENTITY_FIREWORK_BLAST_FAR", "FIREWORK_BLAST2");
        temp.put("ENTITY_FIREWORK_LARGE_BLAST", "FIREWORK_LARGE_BLAST");
        temp.put("ENTITY_FIREWORK_LARGE_BLAST_FAR", "FIREWORK_LARGE_BLAST2");
        temp.put("ENTITY_FIREWORK_LAUNCH", "FIREWORK_LAUNCH");
        temp.put("ENTITY_FIREWORK_SHOOT", "FIREWORK_LAUNCH");
        temp.put("ENTITY_FIREWORK_TWINKLE", "FIREWORK_TWINKLE");
        temp.put("ENTITY_FIREWORK_TWINKLE_FAR", "FIREWORK_TWINKLE2");
        temp.put("ENTITY_GENERIC_BIG_FALL", "FALL_BIG");
        temp.put("ENTITY_GENERIC_BURN", "FIRE");
        temp.put("ENTITY_GENERIC_DEATH", "CREEPER_DEATH"); // XXXXXXXXXXXXX
        temp.put("ENTITY_GENERIC_DRINK", "DRINK");
        temp.put("ENTITY_GENERIC_EAT", "EAT");
        temp.put("ENTITY_GENERIC_EXPLODE", "EXPLODE");
        temp.put("ENTITY_GENERIC_EXTINGUISH_FIRE", "FIZZ");
        temp.put("ENTITY_GENERIC_HURT", "HURT_FLESH");
        temp.put("ENTITY_GENERIC_SMALL_FALL", "FALL_SMALL");
        temp.put("ENTITY_GENERIC_SPLASH", "SPLASH");
        temp.put("ENTITY_GENERIC_SWIM", "SWIM");
        temp.put("ENTITY_GHAST_AMBIENT", "GHAST_MOAN");
        temp.put("ENTITY_GHAST_DEATH", "GHAST_DEATH");
        temp.put("ENTITY_GHAST_HURT", "GHAST_SCREAM");
        temp.put("ENTITY_GHAST_SCREAM", "GHAST_SCREAM");
        temp.put("ENTITY_GHAST_SHOOT", "GHAST_FIREBALL");
        temp.put("ENTITY_GHAST_WARN", "GHAST_CHARGE");
        temp.put("ENTITY_GUARDIAN_AMBIENT", "SILVERFISH_IDLE"); // XXXXXXXXXXXXX
        temp.put("ENTITY_GUARDIAN_AMBIENT_LAND", "SILVERFISH_IDLE"); // XXXXXXXXXXXXX
        temp.put("ENTITY_GUARDIAN_ATTACK", "SILVERFISH_HIT"); // XXXXXXXXXXXXX
        temp.put("ENTITY_GUARDIAN_DEATH", "SILVERFISH_KILL"); // XXXXXXXXXXXXX
        temp.put("ENTITY_GUARDIAN_DEATH_LAND", "SILVERFISH_KILL"); // XXXXXXXXXXXXX
        temp.put("ENTITY_GUARDIAN_FLOP", "SILVERFISH_HIT"); // XXXXXXXXXXXXX
        temp.put("ENTITY_GUARDIAN_HURT", "SILVERFISH_HIT"); // XXXXXXXXXXXXX
        temp.put("ENTITY_GUARDIAN_HURT_LAND", "SILVERFISH_HIT"); // XXXXXXXXXXXXX
        temp.put("ENTITY_HORSE_AMBIENT", "HORSE_IDLE");
        temp.put("ENTITY_HORSE_ANGRY", "HORSE_ANGRY");
        temp.put("ENTITY_HORSE_ARMOR", "HORSE_ARMOR");
        temp.put("ENTITY_HORSE_BREATHE", "HORSE_BREATHE");
        temp.put("ENTITY_HORSE_DEATH", "HORSE_DEATH");
        temp.put("ENTITY_HORSE_EAT", "EAT");
        temp.put("ENTITY_HORSE_GALLOP", "HORSE_GALLOP");
        temp.put("ENTITY_HORSE_HURT", "HORSE_HIT");
        temp.put("ENTITY_HORSE_JUMP", "HORSE_JUMP");
        temp.put("ENTITY_HORSE_LAND", "HORSE_LAND");
        temp.put("ENTITY_HORSE_SADDLE", "HORSE_SADDLE");
        temp.put("ENTITY_HORSE_STEP", "HORSE_SOFT");
        temp.put("ENTITY_HORSE_STEP_WOOD", "STEP_WOOD");
        temp.put("ENTITY_HOSTILE_BIG_FALL", "FALL_BIG");
        temp.put("ENTITY_HOSTILE_DEATH", "VILLAGER_DEATH"); // XXXXXXXXXXXXX
        temp.put("ENTITY_HOSTILE_HURT", "HURT_FLESH");
        temp.put("ENTITY_HOSTILE_SMALL_FALL", "FALL_SMALL");
        temp.put("ENTITY_HOSTILE_SPLASH", "SPLASH");
        temp.put("ENTITY_HOSTILE_SWIM", "SWIM");
        temp.put("ENTITY_IRONGOLEM_ATTACK", "IRONGOLEM_THROW");
        temp.put("ENTITY_IRONGOLEM_DEATH", "IRONGOLEM_DEATH");
        temp.put("ENTITY_IRONGOLEM_HURT", "IRONGOLEM_HIT");
        temp.put("ENTITY_IRONGOLEM_STEP", "IRONGOLEM_WALK");
        temp.put("ENTITY_ITEM_BREAK", "ITEM_BREAK");
        temp.put("ENTITY_ITEM_PICKUP", "ITEM_PICKUP");
        temp.put("ENTITY_ITEMFRAME_ADD_ITEM", "SILENT"); // XXXXXXXXXXXXX
        temp.put("ENTITY_ITEMFRAME_BREAK", "SILENT"); // XXXXXXXXXXXXX
        temp.put("ENTITY_ITEMFRAME_PLACE", "SILENT"); // XXXXXXXXXXXXX
        temp.put("ENTITY_ITEMFRAME_REMOVE_ITEM", "SILENT"); // XXXXXXXXXXXXX
        temp.put("ENTITY_ITEMFRAME_ROTATE_ITEM", "SILENT"); // XXXXXXXXXXXXX
        temp.put("ENTITY_LEASHKNOT_BREAK", "SILENT"); // XXXXXXXXXXXXX
        temp.put("ENTITY_LEASHKNOT_PLACE", "SILENT"); // XXXXXXXXXXXXX
        temp.put("ENTITY_LIGHTNING_IMPACT", "EXPLODE");
        temp.put("ENTITY_LIGHTNING_THUNDER", "AMBIENCE_THUNDER");
        temp.put("ENTITY_LINGERINGPOTION_THROW", "CHICKEN_EGG_POP");
        temp.put("ENTITY_MAGMACUBE_DEATH", "MAGMACUBE_WALK2");
        temp.put("ENTITY_MAGMACUBE_HURT", "MAGMACUBE_WALK2");
        temp.put("ENTITY_MAGMACUBE_JUMP", "MAGMACUBE_JUMP");
        temp.put("ENTITY_MAGMACUBE_SQUISH", "MAGMACUBE_WALK");
        temp.put("ENTITY_MINECART_INSIDE", "MINECART_INSIDE");
        temp.put("ENTITY_MINECART_RIDING", "MINECART_BASE");
        temp.put("ENTITY_MOOSHROOM_SHEAR", "SHEEP_SHEAR");
        temp.put("ENTITY_MULE_AMBIENT", "DONKEY_IDLE"); // HORSE_IDLE 
        temp.put("ENTITY_MULE_DEATH", "DONKEY_DEATH"); // HORSE_DEATH
        temp.put("ENTITY_MULE_HURT", "DONKEY_HIT"); // HORSE_HIT
        temp.put("ENTITY_PAINTING_BREAK", "SILENT"); // XXXXXXXXXXXXX
        temp.put("ENTITY_PAINTING_PLACE", "SILENT"); // XXXXXXXXXXXXX
        temp.put("ENTITY_PIG_AMBIENT", "PIG_IDLE");
        temp.put("ENTITY_PIG_DEATH", "PIG_DEATH");
        temp.put("ENTITY_PIG_HURT", "HURT_FLESH"); // XXXXXXXXXXXXX
        temp.put("ENTITY_PIG_SADDLE", "HORSE_SADDLE"); // XXXXXXXXXXXXX
        temp.put("ENTITY_PIG_STEP", "PIG_WALK");
        temp.put("ENTITY_PLAYER_ATTACK_CRIT", "SUCCESSFUL_HIT"); // XXXXXXXXXXXXX
        temp.put("ENTITY_PLAYER_ATTACK_KNOCKBACK", "CREEPER_HISS"); // XXXXXXXXXXXXX
        temp.put("ENTITY_PLAYER_ATTACK_NODAMAGE", "CREEPER_DEATH"); // XXXXXXXXXXXXX
        temp.put("ENTITY_PLAYER_ATTACK_STRONG", "CREEPER_HISS"); // XXXXXXXXXXXXX
        temp.put("ENTITY_PLAYER_ATTACK_SWEEP", "CREEPER_HISS"); // XXXXXXXXXXXXX
        temp.put("ENTITY_PLAYER_ATTACK_WEAK", "CREEPER_DEATH"); // XXXXXXXXXXXXX
        temp.put("ENTITY_PLAYER_BIG_FALL", "FALL_BIG");
        temp.put("ENTITY_PLAYER_BREATH", "HORSE_BREATHE"); // XXXXXXXXXXXXX
        temp.put("ENTITY_PLAYER_BURP", "BURP");
        temp.put("ENTITY_PLAYER_DEATH", "VILLAGER_DEATH"); // XXXXXXXXXXXXX
        temp.put("ENTITY_PLAYER_HURT", "HURT_FLESH");
        temp.put("ENTITY_PLAYER_LEVELUP", "LEVEL_UP");
        temp.put("ENTITY_PLAYER_SMALL_FALL", "FALL_SMALL");
        temp.put("ENTITY_PLAYER_SPLASH", "SPLASH2");
        temp.put("ENTITY_PLAYER_SWIM", "SPLASH");
        temp.put("ENTITY_RABBIT_AMBIENT", "BAT_IDLE"); // XXXXXXXXXXXXX
        temp.put("ENTITY_RABBIT_ATTACK", "BAT_HURT"); // XXXXXXXXXXXXX
        temp.put("ENTITY_RABBIT_DEATH", "BAT_DEATH"); // XXXXXXXXXXXXX
        temp.put("ENTITY_RABBIT_HURT", "BAT_HURT"); // XXXXXXXXXXXXX
        temp.put("ENTITY_RABBIT_JUMP", "BAT_TAKEOFF"); // XXXXXXXXXXXXX
        temp.put("ENTITY_SHEEP_AMBIENT", "SHEEP_IDLE");
        temp.put("ENTITY_SHEEP_DEATH", "SHEEP_IDLE");
        temp.put("ENTITY_SHEEP_HURT", "SHEEP_IDLE");
        temp.put("ENTITY_SHEEP_SHEAR", "SHEEP_SHEAR");
        temp.put("ENTITY_SHEEP_STEP", "SHEEP_WALK");
        temp.put("ENTITY_SHULKER_AMBIENT", "ENDERMAN_IDLE"); // XXXXXXXXXXXXX
        temp.put("ENTITY_SHULKER_BULLET_HIT", "SUCCESSFUL_HIT"); // XXXXXXXXXXXXX
        temp.put("ENTITY_SHULKER_BULLET_HURT", "SUCCESSFUL_HIT"); // XXXXXXXXXXXXX
        temp.put("ENTITY_SHULKER_CLOSE", "CHEST_CLOSE"); // XXXXXXXXXXXXX
        temp.put("ENTITY_SHULKER_DEATH", "ENDERMAN_DEATH"); // XXXXXXXXXXXXX
        temp.put("ENTITY_SHULKER_HURT", "ENDERMAN_HIT"); // XXXXXXXXXXXXX
        temp.put("ENTITY_SHULKER_HURT_CLOSED", "ENDERMAN_SCREAM"); // XXXXXXXXXXXXX
        temp.put("ENTITY_SHULKER_OPEN", "CHEST_OPEN"); // XXXXXXXXXXXXX
        temp.put("ENTITY_SHULKER_SHOOT", "SHOOT_ARROW"); // XXXXXXXXXXXXX
        temp.put("ENTITY_SHULKER_TELEPORT", "ENDERMAN_TELEPORT"); // XXXXXXXXXXXXX
        temp.put("ENTITY_SILVERFISH_AMBIENT", "SILVERFISH_IDLE");
        temp.put("ENTITY_SILVERFISH_DEATH", "SILVERFISH_KILL");
        temp.put("ENTITY_SILVERFISH_HURT", "SILVERFISH_HIT");
        temp.put("ENTITY_SILVERFISH_STEP", "SILVERFISH_WALK");
        temp.put("ENTITY_SKELETON_AMBIENT", "SKELETON_IDLE");
        temp.put("ENTITY_SKELETON_DEATH", "SKELETON_DEATH");
        temp.put("ENTITY_SKELETON_HORSE_AMBIENT", "HORSE_SKELETON_IDLE");
        temp.put("ENTITY_SKELETON_HORSE_DEATH", "HORSE_SKELETON_DEATH");
        temp.put("ENTITY_SKELETON_HORSE_HURT", "HORSE_SKELETON_HIT");
        temp.put("ENTITY_SKELETON_HURT", "SKELETON_HURT");
        temp.put("ENTITY_SKELETON_SHOOT", "SHOOT_ARROW");
        temp.put("ENTITY_SKELETON_STEP", "SKELETON_WALK");
        temp.put("ENTITY_SLIME_ATTACK", "SLIME_ATTACK");
        temp.put("ENTITY_SLIME_DEATH", "SPIDER_DEATH"); // XXXXXXXXXXXXX
        temp.put("ENTITY_SLIME_HURT", "SLIME_WALK");
        temp.put("ENTITY_SLIME_JUMP", "SLIME_WALK2");
        temp.put("ENTITY_SLIME_SQUISH", "SLIME_WALK2");
        temp.put("ENTITY_SMALL_MAGMACUBE_DEATH", "SPIDER_DEATH"); // XXXXXXXXXXXXX
        temp.put("ENTITY_SMALL_MAGMACUBE_HURT", "SLIME_WALK");
        temp.put("ENTITY_SMALL_MAGMACUBE_SQUISH", "SLIME_WALK2");
        temp.put("ENTITY_SMALL_SLIME_DEATH", "SLIME_WALK");
        temp.put("ENTITY_SMALL_SLIME_HURT", "SLIME_WALK");
        temp.put("ENTITY_SMALL_SLIME_JUMP", "SLIME_WALK2");
        temp.put("ENTITY_SMALL_SLIME_SQUISH", "SLIME_WALK2");
        temp.put("ENTITY_SNOWBALL_THROW", "SHOOT_ARROW"); // XXXXXXXXXXXXX
        temp.put("ENTITY_SNOWMAN_AMBIENT", "STEP_SNOW"); // XXXXXXXXXXXXX
        temp.put("ENTITY_SNOWMAN_DEATH", "DIG_SNOW"); // XXXXXXXXXXXXX
        temp.put("ENTITY_SNOWMAN_HURT", "DIG_SNOW"); // XXXXXXXXXXXXX
        temp.put("ENTITY_SNOWMAN_SHOOT", "CHICKEN_EGG_POP"); // XXXXXXXXXXXXX
        temp.put("ENTITY_SPIDER_AMBIENT", "SPIDER_IDLE");
        temp.put("ENTITY_SPIDER_DEATH", "SPIDER_DEATH");
        temp.put("ENTITY_SPIDER_HURT", "HURT_FLESH"); // XXXXXXXXXXXXX
        temp.put("ENTITY_SPIDER_STEP", "SPIDER_WALK");
        temp.put("ENTITY_SPLASH_POTION_BREAK", "GLASS");
        temp.put("ENTITY_SPLASH_POTION_THROW", "SHOOT_ARROW");
        temp.put("ENTITY_SQUID_AMBIENT", "SWIM"); // XXXXXXXXXXXXX
        temp.put("ENTITY_SQUID_DEATH", "SPIDER_DEATH"); // XXXXXXXXXXXXX
        temp.put("ENTITY_SQUID_HURT", "SPIDER_IDLE"); // XXXXXXXXXXXXX
        temp.put("ENTITY_TNT_PRIMED", "FUSE");
        temp.put("ENTITY_VILLAGER_AMBIENT", "VILLAGER_IDLE");
        temp.put("ENTITY_VILLAGER_DEATH", "VILLAGER_DEATH");
        temp.put("ENTITY_VILLAGER_HURT", "VILLAGER_HIT");
        temp.put("ENTITY_VILLAGER_NO", "VILLAGER_NO");
        temp.put("ENTITY_VILLAGER_TRADING", "VILLAGER_HAGGLE");
        temp.put("ENTITY_VILLAGER_YES", "VILLAGER_YES");
        temp.put("ENTITY_WITCH_AMBIENT", "DIG_SAND"); // XXXXXXXXXXXXX
        temp.put("ENTITY_WITCH_DEATH", "DIG_SAND"); // XXXXXXXXXXXXX
        temp.put("ENTITY_WITCH_DRINK", "DRINK"); // XXXXXXXXXXXXX
        temp.put("ENTITY_WITCH_HURT", "DIG_SAND"); // XXXXXXXXXXXXX
        temp.put("ENTITY_WITCH_THROW", "DIG_SAND"); // XXXXXXXXXXXXX
        temp.put("ENTITY_WITHER_AMBIENT", "WITHER_IDLE");
        temp.put("ENTITY_WITHER_BREAK_BLOCK", "DIG_STONE"); // XXXXXXXXXXXXX
        temp.put("ENTITY_WITHER_DEATH", "WITHER_DEATH");
        temp.put("ENTITY_WITHER_HURT", "WITHER_HURT");
        temp.put("ENTITY_WITHER_SHOOT", "WITHER_SHOOT");
        temp.put("ENTITY_WITHER_SPAWN", "WITHER_SPAWN");
        temp.put("ENTITY_WOLF_AMBIENT", "WOLF_BARK");
        temp.put("ENTITY_WOLF_DEATH", "WOLF_DEATH");
        temp.put("ENTITY_WOLF_GROWL", "WOLF_GROWL");
        temp.put("ENTITY_WOLF_HOWL", "WOLF_HOWL");
        temp.put("ENTITY_WOLF_HURT", "WOLF_HURT");
        temp.put("ENTITY_WOLF_PANT", "WOLF_PANT");
        temp.put("ENTITY_WOLF_SHAKE", "WOLF_SHAKE");
        temp.put("ENTITY_WOLF_STEP", "WOLF_WALK");
        temp.put("ENTITY_WOLF_WHINE", "WOLF_WHINE");
        temp.put("ENTITY_ZOMBIE_AMBIENT", "ZOMBIE_IDLE");
        temp.put("ENTITY_ZOMBIE_ATTACK_DOOR_WOOD", "ZOMBIE_WOOD");
        temp.put("ENTITY_ZOMBIE_ATTACK_IRON_DOOR", "ZOMBIE_METAL");
        temp.put("ENTITY_ZOMBIE_BREAK_DOOR_WOOD", "ZOMBIE_WOODBREAK");
        temp.put("ENTITY_ZOMBIE_DEATH", "ZOMBIE_DEATH");
        temp.put("ENTITY_ZOMBIE_HORSE_AMBIENT", "HORSE_ZOMBIE_IDLE");
        temp.put("ENTITY_ZOMBIE_HORSE_DEATH", "HORSE_ZOMBIE_DEATH");
        temp.put("ENTITY_ZOMBIE_HORSE_HURT", "HORSE_ZOMBIE_HIT");
        temp.put("ENTITY_ZOMBIE_HURT", "ZOMBIE_HURT");
        temp.put("ENTITY_ZOMBIE_INFECT", "ZOMBIE_INFECT");
        temp.put("ENTITY_ZOMBIE_PIG_AMBIENT", "ZOMBIE_PIG_IDLE");
        temp.put("ENTITY_ZOMBIE_PIG_ANGRY", "ZOMBIE_PIG_ANGRY");
        temp.put("ENTITY_ZOMBIE_PIG_DEATH", "ZOMBIE_PIG_DEATH");
        temp.put("ENTITY_ZOMBIE_PIG_HURT", "ZOMBIE_PIG_HURT");
        temp.put("ENTITY_ZOMBIE_STEP", "ZOMBIE_WALK");
        temp.put("ENTITY_ZOMBIE_VILLAGER_AMBIENT", "ZOMBIE_PIG_IDLE"); // XXXXXXXXXXXXX
        temp.put("ENTITY_ZOMBIE_VILLAGER_CONVERTED", "ZOMBIE_UNFECT");
        temp.put("ENTITY_ZOMBIE_VILLAGER_CURE", "ZOMBIE_REMEDY");
        temp.put("ENTITY_ZOMBIE_VILLAGER_DEATH", "VILLAGER_DEATH"); // XXXXXXXXXXXXX
        temp.put("ENTITY_ZOMBIE_VILLAGER_HURT", "VILLAGER_HIT"); // XXXXXXXXXXXXX
        temp.put("ENTITY_ZOMBIE_VILLAGER_STEP", "ZOMBIE_WALK"); // XXXXXXXXXXXXX
        temp.put("ITEM_ARMOR_EQUIP_CHAIN", "NOTE_PIANO"); // XXXXXXXXXXXXX
        temp.put("ITEM_ARMOR_EQUIP_DIAMOND", "ORB_PICKUP"); // XXXXXXXXXXXXX
        temp.put("ITEM_ARMOR_EQUIP_GENERIC", "NOTE_STICKS"); // XXXXXXXXXXXXX
        temp.put("ITEM_ARMOR_EQUIP_GOLD", "NOTE_PLING"); // XXXXXXXXXXXXX
        temp.put("ITEM_ARMOR_EQUIP_IRON", "NOTE_PLING"); // XXXXXXXXXXXXX
        temp.put("ITEM_ARMOR_EQUIP_LEATHER", "NOTE_STICKS"); // XXXXXXXXXXXXX
        temp.put("ITEM_BOTTLE_FILL", "SWIM"); // XXXXXXXXXXXXX
        temp.put("ITEM_BOTTLE_FILL_DRAGONBREATH", "FIRE_IGNITE"); // XXXXXXXXXXXXX
        temp.put("ITEM_BUCKET_EMPTY", "WATER");
        temp.put("ITEM_BUCKET_EMPTY_LAVA", "LAVA");
        temp.put("ITEM_BUCKET_FILL", "SWIM");
        temp.put("ITEM_BUCKET_FILL_LAVA", "LAVA_POP");
        temp.put("ITEM_CHORUS_FRUIT_TELEPORT", "ENDERMAN_TELEPORT"); // XXXXXXXXXXXXX
        temp.put("ITEM_ELYTRA_FLYING", "BAT_LOOP"); // ENDERDRAGON_WINGS
        temp.put("ITEM_FIRECHARGE_USE", "FIRE_IGNITE");
        temp.put("ITEM_FLINTANDSTEEL_USE", "FIRE_IGNITE");
        temp.put("ITEM_HOE_TILL", "DIG_GRASS");
        temp.put("ITEM_SHIELD_BLOCK", "NOTE_BASS_DRUM"); // XXXXXXXXXXXXX
        temp.put("ITEM_SHIELD_BREAK", "ITEM_BREAK"); // XXXXXXXXXXXXX
        temp.put("ITEM_SHOVEL_FLATTEN", "DIG_GRASS"); // XXXXXXXXXXXXX
        temp.put("MUSIC_CREATIVE", "NOTE_PIANO"); // XXXXXXXXXXXXX
        temp.put("MUSIC_CREDITS", "NOTE_PIANO"); // XXXXXXXXXXXXX
        temp.put("MUSIC_DRAGON", "NOTE_PIANO"); // XXXXXXXXXXXXX
        temp.put("MUSIC_END", "NOTE_PIANO"); // XXXXXXXXXXXXX
        temp.put("MUSIC_GAME", "NOTE_PIANO"); // XXXXXXXXXXXXX
        temp.put("MUSIC_MENU", "NOTE_PIANO"); // XXXXXXXXXXXXX
        temp.put("MUSIC_NETHER", "NOTE_PIANO"); // XXXXXXXXXXXXX
        temp.put("RECORD_11", "NOTE_PIANO"); // XXXXXXXXXXXXX
        temp.put("RECORD_13", "NOTE_PIANO"); // XXXXXXXXXXXXX
        temp.put("RECORD_BLOCKS", "NOTE_PIANO"); // XXXXXXXXXXXXX
        temp.put("RECORD_CAT", "NOTE_PIANO"); // XXXXXXXXXXXXX
        temp.put("RECORD_CHIRP", "NOTE_PIANO"); // XXXXXXXXXXXXX
        temp.put("RECORD_FAR", "NOTE_PIANO"); // XXXXXXXXXXXXX
        temp.put("RECORD_MALL", "NOTE_PIANO"); // XXXXXXXXXXXXX
        temp.put("RECORD_MELLOHI", "NOTE_PIANO"); // XXXXXXXXXXXXX
        temp.put("RECORD_STAL", "NOTE_PIANO"); // XXXXXXXXXXXXX
        temp.put("RECORD_STRAD", "NOTE_PIANO"); // XXXXXXXXXXXXX
        temp.put("RECORD_WAIT", "NOTE_PIANO"); // XXXXXXXXXXXXX
        temp.put("RECORD_WARD", "NOTE_PIANO"); // XXXXXXXXXXXXX
        temp.put("UI_BUTTON_CLICK", "CLICK");
        temp.put("WEATHER_RAIN", "AMBIENCE_RAIN");
        temp.put("WEATHER_RAIN_ABOVE", "AMBIENCE_RAIN");

        for (Sound sound : Sound.values()) {
            if (!temp.containsKey(sound.toString())) {
                System.out.println("----------------------------------------------");
                System.out.println("Missing Sound = " + sound);
                System.out.println("mc.euro.sound.SoundAdapter needs to be updated");
                System.out.println("Please report this issue to ");
                System.out.println("https://github.com/Europia79/Demolition/issues");

                temp.put(sound.toString().toUpperCase(), "SILENT");
            }
        }

        return Collections.unmodifiableMap(temp);

    } // END OF initMap();

    /**
     * This method can return null, which is fine as long as you don't dispatch
     * any methods, like toString(). Otherwise, it's safe to pass null to
     * playSound()
     */
    public static Sound getSound(String x) throws IllegalArgumentException {
        String name = (x == null) ? null : x.replace(".", "_").toUpperCase().trim();
        Sound sound = null;
        try {
            sound = Sound.valueOf(name);
        } catch (IllegalArgumentException handled) {
            try {
                sound = Sound.valueOf(smap.get(name));
            } catch (Exception ignored) {

            }
        } catch (NullPointerException ignored) {
            
        }
        return sound;
    }

}
