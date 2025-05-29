package com.trauger.joel.wooltool.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class WoolcardItem extends Item {
    public WoolcardItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return stack.isDamaged();
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return Math.round(13.0F - (float)stack.getDamage() * 13.0F / (float)stack.getMaxDamage());
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        return 0x55FFFF;
    }
    
    @Override
    public ItemStack getRecipeRemainder(ItemStack stack) {
        ItemStack remainder = stack.copy();
        remainder.setDamage(remainder.getDamage() + 1);
        
        // If the woolcard would break, return empty
        if (remainder.getDamage() >= remainder.getMaxDamage()) {
            return ItemStack.EMPTY;
        }
        
        return remainder;
    }
    
    // The getRecipeRemainder method is enough to make this item return a remainder
    // No need for hasRecipeRemainder in Minecraft 1.21.5
}
