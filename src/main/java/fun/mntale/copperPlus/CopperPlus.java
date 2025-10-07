package fun.mntale.copperPlus;

import com.tcoded.folialib.FoliaLib;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ThreadLocalRandom;

public final class CopperPlus extends JavaPlugin implements Listener {

    private FoliaLib foliaLib;

    @Override
    public void onEnable() {
        this.foliaLib = new FoliaLib(this);
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        if (this.foliaLib != null) {
            this.foliaLib.getScheduler().cancelAllTasks();
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getHitBlock() == null) return;
        foliaLib.getScheduler().runAtEntity(event.getEntity(), (a) -> {
            Block center = event.getHitBlock();
            final int radius = 2; // 5x5x5 area radius

            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {

                        double distanceSquared = x*x + y*y + z*z;
                        if (distanceSquared > radius*radius) continue; // keep spherical shape

                        Block nearby = center.getRelative(x, y, z);
                        Material type = nearby.getType();

                            // Chance decreases with distance
                            double distance = Math.sqrt(distanceSquared);
                            double maxChance = 0.5; // 50% chance at center
                            double chance = Math.max(0, maxChance * (1 - distance / radius));
                        if (type.name().contains("COPPER") && !type.name().contains("WAXED") && !type.name().contains("OXIDIZED")) {
                            if (ThreadLocalRandom.current().nextDouble() < chance) {
                                oxidizeCopper(nearby);
                            }
                        }
                    }
                }
            }
        });
    }

    private void oxidizeCopper(Block block) {
        Material nextStage = switch (block.getType()) {
            case COPPER_BLOCK -> Material.EXPOSED_COPPER;
            case EXPOSED_COPPER -> Material.WEATHERED_COPPER;
            case WEATHERED_COPPER -> Material.OXIDIZED_COPPER;

            case COPPER_BULB -> Material.EXPOSED_COPPER_BULB;
            case EXPOSED_COPPER_BULB -> Material.WEATHERED_COPPER_BULB;
            case WEATHERED_COPPER_BULB -> Material.OXIDIZED_COPPER_BULB;

            case COPPER_DOOR -> Material.EXPOSED_COPPER_DOOR;
            case EXPOSED_COPPER_DOOR -> Material.WEATHERED_COPPER_DOOR;
            case WEATHERED_COPPER_DOOR -> Material.OXIDIZED_COPPER_DOOR;

            case COPPER_TRAPDOOR -> Material.EXPOSED_COPPER_TRAPDOOR;
            case EXPOSED_COPPER_TRAPDOOR -> Material.WEATHERED_COPPER_TRAPDOOR;
            case WEATHERED_COPPER_TRAPDOOR -> Material.OXIDIZED_COPPER_TRAPDOOR;

            case CHISELED_COPPER -> Material.EXPOSED_CHISELED_COPPER;
            case EXPOSED_CHISELED_COPPER -> Material.WEATHERED_CHISELED_COPPER;
            case WEATHERED_CHISELED_COPPER -> Material.OXIDIZED_CHISELED_COPPER;

            case CUT_COPPER -> Material.EXPOSED_CUT_COPPER;
            case EXPOSED_CUT_COPPER -> Material.WEATHERED_CUT_COPPER;
            case WEATHERED_CUT_COPPER -> Material.OXIDIZED_CUT_COPPER;

            case CUT_COPPER_SLAB -> Material.EXPOSED_CUT_COPPER_SLAB;
            case EXPOSED_CUT_COPPER_SLAB -> Material.WEATHERED_CUT_COPPER_SLAB;
            case WEATHERED_CUT_COPPER_SLAB -> Material.OXIDIZED_CUT_COPPER_SLAB;

            case COPPER_GRATE -> Material.EXPOSED_COPPER_GRATE;
            case EXPOSED_COPPER_GRATE -> Material.WEATHERED_COPPER_GRATE;
            case WEATHERED_COPPER_GRATE -> Material.OXIDIZED_COPPER_GRATE;

            case CUT_COPPER_STAIRS -> Material.EXPOSED_CUT_COPPER_STAIRS;
            case EXPOSED_CUT_COPPER_STAIRS -> Material.WEATHERED_CUT_COPPER_STAIRS;
            case WEATHERED_CUT_COPPER_STAIRS -> Material.OXIDIZED_CUT_COPPER_STAIRS;

            default -> block.getType();
        };

        if (nextStage != block.getType()) {
            foliaLib.getScheduler().runAtLocation(block.getLocation(), (t) -> {
                block.setType(nextStage, false);
            });
        }
    }
}