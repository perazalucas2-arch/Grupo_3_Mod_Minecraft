package com.heeheemod;

import com.heeheemod.item.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HeeHeeMod.MOD_ID)
public class ModEvents {
    private static final String OJO_ENTREGADO = "heeheemod_ojo_entregado";

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        CompoundTag data = player.getPersistentData();
        if (data.getBoolean(OJO_ENTREGADO)) return;

        ItemStack ojo = new ItemStack(ModItems.OJO_AYUWOKI.get());
        if (!player.addItem(ojo)) {
            player.drop(ojo, false);
        }
        data.putBoolean(OJO_ENTREGADO, true);
    }
}