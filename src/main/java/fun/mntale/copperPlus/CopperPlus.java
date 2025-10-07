package fun.mntale.copperPlus;

import com.tcoded.folialib.FoliaLib;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
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
            final Block center = event.getHitBlock();
            final int radius = 2; // 5x5x5 area radius

            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {

                        double distanceSquared = x*x + y*y + z*z;
                        if (distanceSquared > radius*radius) continue; // keep spherical shape

                        final Block nearby = center.getRelative(x, y, z);
                        Material type = nearby.getType();

                        if (type == Material.COPPER_BLOCK) {
                            // Chance decreases with distance
                            double distance = Math.sqrt(distanceSquared);
                            double maxChance = 0.5; // 50% chance at center
                            double chance = Math.max(0, maxChance * (1 - distance / radius));

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
            default -> block.getType();
        };

        if (nextStage != block.getType()) {
            foliaLib.getScheduler().runAtLocation(block.getLocation(), (t) -> {
                block.setType(nextStage, false);
            });
        }
    }
}