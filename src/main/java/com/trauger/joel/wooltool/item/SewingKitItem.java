package com.trauger.joel.wooltool.item;

import com.trauger.joel.wooltool.ModItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;

import static com.trauger.joel.wooltool.WoolTool.LOGGER;

public class SewingKitItem extends Item {
    public SewingKitItem(Settings settings) {
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
        
        // If the sewing kit would break, return a sewing needle
        if (remainder.getDamage() >= remainder.getMaxDamage()) {
            return new ItemStack(ModItems.NEEDLE, 1);
        }
        
        return remainder;
    }
    
    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        World world = context.getWorld();
        ItemStack sewingKitStack = context.getStack();
        
        if (player == null) {
            return ActionResult.PASS;
        }
        
        // Check if the player has leather in their inventory
        if (!hasLeatherInInventory(player)) {
            if (!world.isClient()) {
                player.sendMessage(Text.of("You need leather to repair armor"), true);
            }
            return ActionResult.FAIL;
        }
        
        // Find damaged leather armor in the player's inventory
        ItemStack armorStack = findDamagedLeatherArmor(player);
        if (armorStack.isEmpty()) {
            if (!world.isClient()) {
                player.sendMessage(Text.of("You don't have any damaged leather armor"), true);
            }
            return ActionResult.FAIL;
        }
        
        // Only perform the repair on the server side
        if (!world.isClient()) {
            // Calculate repair amount (25% of max durability)
            int maxDurability = armorStack.getMaxDamage();
            int repairAmount = Math.max(1, maxDurability / 4);
            
            // Apply repair
            int newDamage = Math.max(0, armorStack.getDamage() - repairAmount);
            armorStack.setDamage(newDamage);
            
            // Consume one leather
            consumeLeather(player);
            
            // Damage the sewing kit
            sewingKitStack.setDamage(sewingKitStack.getDamage() + 1);
            
            // Play repair sound
            world.playSound(null, player.getX(), player.getY(), player.getZ(), 
                    SoundEvents.BLOCK_WOOL_PLACE, SoundCategory.PLAYERS, 0.8f, 1.0f);
            
            // Send success message
            player.sendMessage(Text.of("Repaired leather armor by 25% durability"), true);
            
            // Log the repair for debugging
            LOGGER.info("Repaired {} for player {}, restored {} durability", 
                      armorStack.getItem().getName().getString(), 
                      player.getName().getString(), 
                      repairAmount);
        }
        
        return ActionResult.SUCCESS;
    }
    
    /**
     * Check if the player has leather in their inventory
     */
    private boolean hasLeatherInInventory(PlayerEntity player) {
        return player.getInventory().contains(new ItemStack(Items.LEATHER));
    }
    
    /**
     * Consume one leather from the player's inventory
     */
    private void consumeLeather(PlayerEntity player) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() == Items.LEATHER) {
                stack.decrement(1);
                break;
            }
        }
    }
    
    /**
     * Find damaged leather armor in the player's inventory
     */
    private ItemStack findDamagedLeatherArmor(PlayerEntity player) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (canRepairLeatherArmor(stack)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
    
    /**
     * Checks if the item can be used to repair leather armor
     * @param armorStack The armor item to check
     * @return True if the item is leather armor and damaged
     */
    public boolean canRepairLeatherArmor(ItemStack armorStack) {
        if (armorStack.isEmpty()) return false;
        
        // Check if it's leather armor (helmet, chestplate, leggings, or boots)
        Item item = armorStack.getItem();
        boolean isLeatherArmor = item == Items.LEATHER_HELMET || 
                               item == Items.LEATHER_CHESTPLATE || 
                               item == Items.LEATHER_LEGGINGS || 
                               item == Items.LEATHER_BOOTS;
        
        // Check if it's damaged and is leather armor
        return isLeatherArmor && armorStack.isDamaged();
    }
}
