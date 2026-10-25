package com.heeheemod.entity;

import com.heeheemod.HeeHeeMod;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, HeeHeeMod.MOD_ID);

    public static final RegistryObject<EntityType<AyuwokiEntity>> AYUWOKI = ENTITY_TYPES.register("ayuwoki",
            () -> EntityType.Builder.<AyuwokiEntity>of(AyuwokiEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 3.8F)      // 3.8 bloques: mas alto que un Enderman (2.9) y que un Iron Golem (2.7)
                    .eyeHeight(3.4F)
                    .clientTrackingRange(10)
                    .build("ayuwoki"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
