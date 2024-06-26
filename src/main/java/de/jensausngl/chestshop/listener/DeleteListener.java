package de.jensausngl.chestshop.listener;

import de.jensausngl.chestshop.ChestShop;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.material.Sign;

public class DeleteListener implements Listener {

    private static final BlockFace[] FACES = new BlockFace[]{ BlockFace.NORTH, BlockFace.SOUTH,
            BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN };

    private final ChestShop chestShop;

    public DeleteListener(final ChestShop chestShop) {
        this.chestShop = chestShop;
    }

    @EventHandler
    public void onBlockBreak(final BlockBreakEvent event) {
        final Block block = event.getBlock();

        if (event.isCancelled()) {
            return;
        }

        if (block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN) {
            this.chestShop.removeShop(block);
            return;
        }

        if (block.getType() != Material.CHEST && block.getType() != Material.TRAPPED_CHEST) {
            return;
        }

        for (final BlockFace face : FACES) {
            final Block relativeBlock = block.getRelative(face);

            if (relativeBlock.getType() != Material.SIGN_POST && relativeBlock.getType() != Material.WALL_SIGN) {
                continue;
            }

            final Sign sign = (Sign) relativeBlock.getState().getData();

            if (sign.getAttachedFace().getOppositeFace() == face) {
                this.chestShop.removeShop(relativeBlock);
            }
        }
    }

}
