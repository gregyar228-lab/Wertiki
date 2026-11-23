package com.skyrotor.registry;

import com.skyrotor.SkyRotorMod;
import com.skyrotor.entity.HelicopterEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, SkyRotorMod.MODID);

    public static final RegistryObject<EntityType<HelicopterEntity>> HELICOPTER = ENTITIES.register("helicopter",
            () -> EntityType.Builder.<HelicopterEntity>of(HelicopterEntity::new, MobCategory.MISC)
                    .sized(2.6F, 1.9F)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .fireImmune()
                    .build(SkyRotorMod.MODID + ":helicopter"));

    private ModEntities() {
    }
}
