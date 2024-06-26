package de.jensausngl.chestshop.model;

import de.jensausngl.chestshop.util.NumberUtil;
import javafx.scene.paint.Color;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;

public class Shop {

    // ToDo: add company id
    private final String world;
    private final int x;
    private final int y;
    private final int z;
    private final ShopType type;
    private final ShopItem item;
    private final double price;

    public Shop(final Block block, final ShopType type, final ShopItem item, final double price) {
        this.world = block.getWorld().getName();
        this.x = block.getX();
        this.y = block.getY();
        this.z = block.getZ();
        this.type = type;
        this.item = item;
        this.price = price;
    }

    public String getWorld() {
        return this.world;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getZ() {
        return this.z;
    }

    public ShopType getType() {
        return this.type;
    }

    public ShopItem getItem() {
        return this.item;
    }

    public double getPrice() {
        return this.price;
    }

    public void updateSign(final Block block) {
        final WorldServer world = ((CraftWorld) block.getWorld()).getHandle();
        final TileEntitySign sign = (TileEntitySign) world.getTileEntity(new BlockPosition(block.getX(), block.getY(), block.getZ()));

        if (sign == null) {
            return;
        }

        final IChatBaseComponent header = new ChatComponentText("Firma")
                .setChatModifier(new ChatModifier().setColor(EnumChatFormat.DARK_BLUE));

        final IChatBaseComponent type = new ChatComponentText(this.type.getDisplayName())
                .setChatModifier(new ChatModifier().setBold(true));

        final IChatBaseComponent price = new ChatComponentText(NumberUtil.format(this.price) + " Euro");

        final IChatBaseComponent item = (this.item.hasDisplayName() ? new ChatComponentText(this.item.getDisplayName()) : new ChatMessage(this.item.getTranslation()))
                .setChatModifier(new ChatModifier().setColor(EnumChatFormat.DARK_GRAY));

        sign.lines[0] = header;
        sign.lines[1] = type;
        sign.lines[2] = price;
        sign.lines[3] = item;
        sign.update();
    }

}
