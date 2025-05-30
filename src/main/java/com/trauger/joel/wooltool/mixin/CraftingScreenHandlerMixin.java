package com.trauger.joel.wooltool.mixin;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.CraftingScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.trauger.joel.wooltool.WoolTool.LOGGER;

/**
 * Mixin to intercept crafting results for leather armor repair recipes
 */
@Mixin(CraftingScreenHandler.class)
public class CraftingScreenHandlerMixin {
    
    /**
     * Inject at the method that handles when a player takes a crafted item
     */
    @Inject(method = "onContentChanged", at = @At("RETURN"))
    private void onContentChanged(CallbackInfo ci) {
        // Get the crafting result
        CraftingScreenHandler handler = (CraftingScreenHandler)(Object)this;
        ItemStack resultStack = handler.getSlot(0).getStack(); // Slot 0 is the result slot
        
        // Only process leather armor items
        if (!isLeatherArmor(resultStack.getItem()) || resultStack.isEmpty()) {
            return;
        }
        
        // Check if this is a leather armor repair recipe (has a damaged leather armor in the grid)
        // Access the crafting inventory through the handler's slots
        ItemStack damagedArmor = findDamagedLeatherArmor(handler);
        if (damagedArmor.isEmpty()) {
            return; // No damaged armor found in the crafting grid
        }
        
        // Check if there's a sewing kit in the recipe (to make sure this is a repair recipe)
        boolean hasSewingKit = false;
        for (int i = 1; i <= 9; i++) {
            ItemStack stack = handler.getSlot(i).getStack();
            if (stack.getItem() == com.trauger.joel.wooltool.ModItems.SEWING_KIT) {
                hasSewingKit = true;
                break;
            }
        }
        
        if (!hasSewingKit) {
            return; // Not a leather armor repair recipe with sewing kit
        }
        
        // Calculate repair amount (25% of max durability)
        int maxDurability = resultStack.getMaxDamage();
        int repairAmount = Math.max(1, maxDurability / 4);
        
        // Calculate new damage value (original damage - repair amount)
        int originalDamage = damagedArmor.getDamage();
        int newDamage = Math.max(0, originalDamage - repairAmount);
        
        // Apply the new damage value to the output item
        resultStack.setDamage(newDamage);
        
        LOGGER.info("Repaired leather armor {} from {} to {} durability (restored {})", 
                   resultStack.getItem().getName().getString(), 
                   maxDurability - originalDamage,
                   maxDurability - newDamage,
                   repairAmount);
    }
    
    /**
     * Find damaged leather armor in the crafting inventory
     * 
     * @param handler The crafting screen handler
     * @return The damaged leather armor ItemStack, or ItemStack.EMPTY if none is found
     */
    private ItemStack findDamagedLeatherArmor(CraftingScreenHandler handler) {
        // In a crafting table, slots 1-9 are the crafting grid
        for (int i = 1; i <= 9; i++) {
            ItemStack stack = handler.getSlot(i).getStack();
            if (isLeatherArmor(stack.getItem()) && stack.isDamaged()) {
                return stack;
            }
        }
        
        return ItemStack.EMPTY;
    }
    
    /**
     * Check if an item is leather armor
     */
    private boolean isLeatherArmor(Item item) {
        return item == Items.LEATHER_HELMET || 
               item == Items.LEATHER_CHESTPLATE || 
               item == Items.LEATHER_LEGGINGS || 
               item == Items.LEATHER_BOOTS;
    }
}
