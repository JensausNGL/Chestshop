package de.jensausngl.chestshop;

import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

public class Utils {

    public static ItemStack getCurrentItemOnSlotInChest(Chest chest, int slot) {
     System.out.println(chest.getBlockInventory().getItem(slot));

     return chest.getBlockInventory().getItem(slot);
    }
}
