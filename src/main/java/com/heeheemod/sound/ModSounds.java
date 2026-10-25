package com.heeheemod.sound;

import com.heeheemod.HeeHeeMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, HeeHeeMod.MOD_ID);

    /** El "hee hee" caracteristico. Archivo: assets/heeheemod/sounds/hee_hee.ogg */
    public static final RegistryObject<SoundEvent> HEE_HEE = registerSound("hee_hee");
    /** Grito del jumpscare. Archivo: assets/heeheemod/sounds/jumpscare.ogg */
    public static final RegistryObject<SoundEvent> JUMPSCARE = registerSound("jumpscare");
    /** Susurro "Te veo". Archivo: assets/heeheemod/sounds/te_veo.ogg (lo pones tu) */
    public static final RegistryObject<SoundEvent> TE_VEO = registerSound("te_veo");
    /** Musica tensa en bucle mientras te persigue. Archivo: assets/heeheemod/sounds/chase_music.ogg */
    public static final RegistryObject<SoundEvent> CHASE_MUSIC = registerSound("chase_music");

    private static RegistryObject<SoundEvent> registerSound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(HeeHeeMod.MOD_ID, name)));
    }

    public static void register(IEventBus eventBus) {
        SOUNDS.register(eventBus);
    }
}
