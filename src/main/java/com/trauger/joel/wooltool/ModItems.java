package com.trauger.joel.wooltool;

import com.trauger.joel.wooltool.item.SewingKitItem;
import com.trauger.joel.wooltool.item.WoolcardItem;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.Function;

import static com.trauger.joel.wooltool.WoolTool.*;

public class ModItems {
    public static Item register(String name, Function<Item.Settings, Item> itemFactory, Item.Settings settings) {
        // Create the item key.
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, name));

        // Create the item instance.
        Item item = itemFactory.apply(settings.registryKey(itemKey));

        // Register the item.
        Registry.register(Registries.ITEM, itemKey, item);

        return item;
    }

    public static Function<Item.Settings, Item> func = Item::new;
    public static final Item NEEDLE = register("needle", func, new Item.Settings());
    public static final Item SPOOL_THREAD = register("spool_thread", func, new Item.Settings());
    public static final Item SEWING_KIT = register("sewing_kit", SewingKitItem::new, new Item.Settings().maxDamage(8));
    public static final Item WOOLCARD = register("woolcard", WoolcardItem::new, new Item.Settings().maxDamage(64));

    public static void initialize(){
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register((itemGroup) -> {
            itemGroup.add(ModItems.NEEDLE);
            itemGroup.add(ModItems.SPOOL_THREAD);
            itemGroup.add(ModItems.SEWING_KIT);
            itemGroup.add(ModItems.WOOLCARD);
        });
    }

}