package com.heeheemod.client;

import com.heeheemod.sound.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;

/**
 * Musica tensa de fondo mientras un ayuwoki te persigue.
 *
 * Funcionamiento: cada ayuwoki que persigue (o esta desaparecido) llama a {@link #ping(Entity)}
 * en cada tick de cliente. Mientras haya pings recientes la musica suena (con fade-in);
 * cuando dejan de llegar, hace fade-out y se apaga sola.
 */
public class ChaseMusicClient {
    /** Distancia maxima (en bloques) a la que un ayuwoki activa la musica. */
    private static final double RANGE = 40.0D;
    /** Ticks sin recibir ping tras los cuales la musica empieza a apagarse (2 s). */
    private static final int PING_TIMEOUT = 40;
    /** Volumen maximo de la musica (0.0 a 1.0). */
    private static final float MAX_VOLUME = 0.8F;
    /** Cuanto sube/baja el volumen por tick (0.025 = ~1.6 s de fade). */
    private static final float FADE_STEP = 0.025F;

    private static ChaseMusicInstance current;
    private static int lastPingTick = Integer.MIN_VALUE / 2;

    /** Lo llama la entidad (solo en el cliente) mientras persigue o esta desaparecida. */
    public static void ping(Entity source) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || !player.isAlive()) return;
        if (player.distanceToSqr(source) > RANGE * RANGE) return;

        lastPingTick = player.tickCount;

        if (current == null || current.isStopped()) {
            // Callamos la musica ambiente de Minecraft para que no se pise con la nuestra
            mc.getMusicManager().stopPlaying();
            current = new ChaseMusicInstance();
            mc.getSoundManager().play(current);
        }
    }

    private static class ChaseMusicInstance extends AbstractTickableSoundInstance {
        ChaseMusicInstance() {
            super(ModSounds.CHASE_MUSIC.get(), SoundSource.MASTER, RandomSource.create());
            this.looping = true;
            this.delay = 0;
            this.volume = 0.01F;
            // Suena "dentro de la cabeza" del jugador, sin posicion ni atenuacion
            this.relative = true;
            this.attenuation = SoundInstance.Attenuation.NONE;
        }

        @Override
        public void tick() {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null || !player.isAlive()) {
                this.stop();
                return;
            }

            boolean active = player.tickCount - lastPingTick <= PING_TIMEOUT;
            if (active) {
                this.volume = Math.min(MAX_VOLUME, this.volume + FADE_STEP);
            } else {
                this.volume -= FADE_STEP;
                if (this.volume <= 0.0F) {
                    this.stop();
                }
            }
        }
    }
}
