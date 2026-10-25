package com.heeheemod.client;

import com.heeheemod.sound.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.world.entity.Entity;

/** Punto de entrada del jumpscare en el cliente (lo llama la entidad). */
public class JumpscareClient {

    /** Solo asusta a quien esta cerca (menos de 12 bloques). */
    public static void tryStart(Entity source) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || player.distanceToSqr(source) > 144.0D) return;
        if (mc.screen instanceof JumpscareScreen) return;

        mc.setScreen(new JumpscareScreen());

        // Minecraft limita el volumen de cada sonido a 1.0, asi que para que suene MAS FUERTE
        // y MAS GRAVE se apilan 3 capas del mismo grito a distintos tonos.
        // (el tono minimo que admite Minecraft es 0.5)
        var sounds = mc.getSoundManager();
        sounds.play(SimpleSoundInstance.forUI(ModSounds.JUMPSCARE.get(), 0.5F, 1.0F));  // muy grave
        sounds.play(SimpleSoundInstance.forUI(ModSounds.JUMPSCARE.get(), 0.65F, 1.0F)); // grave
        sounds.play(SimpleSoundInstance.forUI(ModSounds.JUMPSCARE.get(), 0.85F, 1.0F)); // cuerpo
    }
}
