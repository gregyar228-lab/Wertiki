package com.skyrotor.registry;

import com.skyrotor.SkyRotorMod;
import com.skyrotor.item.HelicopterItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, SkyRotorMod.MODID);

    public static final RegistryObject<Item> HELICOPTER = ITEMS.register("helicopter",
            () -> new HelicopterItem(new Item.Properties().stacksTo(1)));

    private ModItems() {
    }
}
