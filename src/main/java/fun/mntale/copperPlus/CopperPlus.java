package fun.mntale.copperPlus;

import com.tcoded.folialib.FoliaLib;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.entity.Player;
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
            Block center = event.getHitBlock();
            final int radius = 2; // 5x5x5 sphere

            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {

                        double distanceSquared = x * x + y * y + z * z;
                        if (distanceSquared > radius * radius) continue;

                        Block nearby = center.getRelative(x, y, z);
                        Material type = nearby.getType();

                        // Chance decreases with distance
                        double distance = Math.sqrt(distanceSquared);
                        double maxChance = 0.6; // 60% at center
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
        Material current = block.getType();
        Material nextStage = switch (current) {
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

            default -> current;
        };

        if (nextStage == current) return;

        foliaLib.getScheduler().runAtLocation(block.getLocation(), (t) -> {

            // Preserve stairs
            if (block.getBlockData() instanceof Stairs stairs) {
                Stairs newStairs = (Stairs) Bukkit.createBlockData(nextStage);
                newStairs.setFacing(stairs.getFacing());
                newStairs.setHalf(stairs.getHalf());
                newStairs.setShape(stairs.getShape());
                block.setBlockData(newStairs, false);
                return;
            }

            // Preserve slabs
            if (block.getBlockData() instanceof Slab slab) {
                Slab newSlab = (Slab) Bukkit.createBlockData(nextStage);
                newSlab.setType(slab.getType());
                block.setBlockData(newSlab, false);
                return;
            }

            // Preserve doors
            if (block.getBlockData() instanceof Door door) {
                Block top = door.getHalf() == Door.Half.TOP ? block : block.getRelative(0, 1, 0);
                Block bottom = door.getHalf() == Door.Half.BOTTOM ? block : block.getRelative(0, -1, 0);

                Door newTop = (Door) Bukkit.createBlockData(nextStage);
                Door newBottom = (Door) Bukkit.createBlockData(nextStage);

                // Copy states
                newTop.setFacing(door.getFacing());
                newTop.setHalf(Door.Half.TOP);
                newTop.setOpen(door.isOpen());

                newBottom.setFacing(door.getFacing());
                newBottom.setHalf(Door.Half.BOTTOM);
                newBottom.setOpen(door.isOpen());

                top.setBlockData(newTop, false);
                bottom.setBlockData(newBottom, false);
                return;
            }

            // Preserve trapdoors
            if (block.getBlockData() instanceof TrapDoor trap) {
                TrapDoor newTrap = (TrapDoor) Bukkit.createBlockData(nextStage);
                newTrap.setFacing(trap.getFacing());
                newTrap.setOpen(trap.isOpen());
                newTrap.setHalf(trap.getHalf());
                block.setBlockData(newTrap, false);
                return;
            }

            // Default for other blocks (chiseled, cut copper, etc.)
            block.setType(nextStage, false);
        });
    }
}
