package com.heeheemod;

import com.heeheemod.item.ModItems;
import com.mojang.logging.LogUtils;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import com.heeheemod.entity.ModEntities;
import com.heeheemod.sound.ModSounds;

@Mod(HeeHeeMod.MOD_ID)
public class HeeHeeMod {
    public static final String MOD_ID = "heeheemod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public HeeHeeMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        ModEntities.register(modEventBus);
        ModSounds.register(modEventBus);
        ModItems.register(modEventBus);
        modEventBus.addListener(this::addCreative);
        LOGGER.info("HeeHeeMod cargado correctamente");
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.OJO_AYUWOKI);
        }
    }
}