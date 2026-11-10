package com.heeheemod;

import com.heeheemod.entity.ModEntities;
import com.heeheemod.item.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
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

    // Drop del Ayuwoki al morir
    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        Level level = entity.level();
        if (level.isClientSide()) return;

        // Solo para el Ayuwoki
        if (entity.getType() != ModEntities.AYUWOKI.get()) return;

        // Probabilidad de drop (1.0f = siempre, 0.5f = 50%)
        float probabilidad = 1.0f;
        if (entity.getRandom().nextFloat() > probabilidad) return;

        // Cantidad entre 1 y 2
        int cantidad = 1 + entity.getRandom().nextInt(2);

        ItemStack drop = new ItemStack(ModItems.OJO_AYUWOKI.get(), cantidad);
        event.getDrops().add(new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(), drop));
    }
}
