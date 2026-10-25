package com.heeheemod.entity;

import com.heeheemod.HeeHeeMod;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HeeHeeMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEntityEvents {
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.AYUWOKI.get(), AyuwokiEntity.createAttributes().build());
    }
}
