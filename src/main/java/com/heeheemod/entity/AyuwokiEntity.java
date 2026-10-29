package com.heeheemod.entity;

import com.heeheemod.client.ChaseMusicClient;
import com.heeheemod.client.JumpscareClient;
import com.heeheemod.sound.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class AyuwokiEntity extends Monster {

    // =====================================================================
    //  AJUSTES (cambia estos numeros para balancear el mob)
    //  20 ticks = 1 segundo
    // =====================================================================
    /** Dano por golpe (12 = 6 corazones). */
    public static final double ATTACK_DAMAGE = 19.0D;
    /** Duracion del efecto Oscuridad al golpear (600 ticks = 30 s). */
    public static final int DARKNESS_TICKS = 600;

    /** Teletransporte normal: espera minima y variacion aleatoria (1.5 s a 4 s). */
    private static final int TELEPORT_COOLDOWN_MIN = 30;
    private static final int TELEPORT_COOLDOWN_RANDOM = 50;
    /** Distancia al objetivo (al cuadrado) a partir de la cual se teletransporta: 7 bloques. */
    private static final double TELEPORT_MIN_DISTANCE_SQR = 49.0D;
    /** Desaparicion con jumpscare: espera entre una y otra (20 s a 40 s). */
    private static final int VANISH_COOLDOWN_MIN = 400;
    private static final int VANISH_COOLDOWN_RANDOM = 400;
    /** Segundos invisible hasta que suena el "hee hee" (2 s). */
    private static final int VANISH_TO_HEE_TICKS = 40;
    /** Tiempo desde que empieza el "hee hee" hasta que reaparece (1 s). */
    private static final int HEE_TO_APPEAR_TICKS = 20;

    /** "Te veo": espera entre una vez y otra (45 s a 90 s). */
    private static final int PEEK_COOLDOWN_MIN = 900;
    private static final int PEEK_COOLDOWN_RANDOM = 900;
    /** Ticks desde que desaparece hasta que dice "Te veo" (0.5 s). */
    private static final int PEEK_SAY_TICKS = 10;
    /** Ticks totales desaparecido hasta que vuelve a aparecer (80 = 4 s). */
    private static final int PEEK_TOTAL_TICKS = 80;

    /** Id del evento de entidad que dispara el jumpscare en el cliente. */
    public static final byte EVENT_JUMPSCARE = 80;

    private int teleportCooldown = 60;
    private int vanishCooldown = 200;
    private int peekCooldown = 400;

    public AyuwokiEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 30;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 250.0D)
                // 0.32 ~ 6.2 bloques/s: un poco mas rapido que un jugador corriendo (5.6).
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(0, new VanishGoal(this));
        this.goalSelector.addGoal(0, new TeVeoGoal(this));
        this.goalSelector.addGoal(1, new TeleportGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 32.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (this.teleportCooldown > 0) this.teleportCooldown--;
        if (this.vanishCooldown > 0) this.vanishCooldown--;
        if (this.peekCooldown > 0) this.peekCooldown--;
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.level().isClientSide) {
            // Musica tensa: mientras persigue (o esta desaparecido) avisa al cliente
            if (this.isAggressive() || this.isInvisible()) {
                ChaseMusicClient.ping(this);
            }

            if (!this.isInvisible()) {
                // Ceniza flotando alrededor (solo visual)
                if (this.random.nextInt(8) == 0) {
                    this.level().addParticle(ParticleTypes.ASH,
                            this.getRandomX(0.6D), this.getRandomY(), this.getRandomZ(0.6D), 0.0D, -0.02D, 0.0D);
                }
                // Al perseguir, ademas suelta humo negro
                if (this.isAggressive() && this.random.nextInt(3) == 0) {
                    this.level().addParticle(ParticleTypes.LARGE_SMOKE,
                            this.getRandomX(0.5D), this.getRandomY(), this.getRandomZ(0.5D), 0.0D, 0.02D, 0.0D);
                }
            }
        }
    }

    /** Cada golpe que acierta deja al jugador a oscuras durante mucho tiempo. */
    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.DARKNESS, DARKNESS_TICKS, 0), this);
            living.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 1), this);
        }
        return hit;
    }

    // ---- Jumpscare (el servidor avisa, el cliente dibuja) ----
    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_JUMPSCARE) {
            if (this.level().isClientSide) {
                JumpscareClient.tryStart(this);
            }
        } else {
            super.handleEntityEvent(id);
        }
    }

    // ---- Mientras esta desaparecido no se le puede golpear ni empujar ----
    @Override
    public boolean isPickable() {
        return !this.isInvisible() && super.isPickable();
    }

    @Override
    public boolean isPushable() {
        return !this.isInvisible() && super.isPushable();
    }

    // Si el mundo se guarda mientras estaba desaparecido, vuelve a ser visible al cargar.
    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.endVanish();
    }

    // ---- Sonidos (voz grave: 0.6 en vez de ~1.0, afecta a ambiente, dano y muerte) ----
    @Override protected SoundEvent getAmbientSound() { return SoundEvents.ENDERMAN_STARE; }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.ENDERMAN_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.ENDERMAN_DEATH; }
    @Override public int getAmbientSoundInterval() { return 240; }

    @Override
    public float getVoicePitch() {
        return 0.6F + (this.random.nextFloat() - this.random.nextFloat()) * 0.08F;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) { return false; }

    // =====================================================================
    //  Utilidades
    // =====================================================================

    /**
     * Intenta teletransportarse a una distancia [minDist, maxDist] alrededor del objetivo.
     * Primero prueba detras de el; si no hay sitio, prueba en cualquier direccion.
     */
    boolean teleportAround(LivingEntity target, double minDist, double maxDist,
                           boolean preferBehind, boolean particles) {
        Vec3 look = target.getLookAngle();
        Vec3 back = new Vec3(-look.x, 0, -look.z);
        back = back.lengthSqr() < 1.0E-4D ? new Vec3(1, 0, 0) : back.normalize();

        for (int i = 0; i < 20; i++) {
            double dist = minDist + this.random.nextDouble() * (maxDist - minDist);
            double angle = (preferBehind && i < 12)
                    ? (this.random.nextDouble() - 0.5D) * Math.toRadians(70)
                    : this.random.nextDouble() * Math.PI * 2;
            Vec3 dir = back.yRot((float) angle);

            double x = target.getX() + dir.x * dist;
            double y = target.getY() + 1.0D;
            double z = target.getZ() + dir.z * dist;

            if (this.randomTeleport(x, y, z, particles)) {
                this.getLookControl().setLookAt(target, 360.0F, 360.0F);
                return true;
            }
        }
        return false;
    }


     boolean teleportInFront(LivingEntity target, double minDist, double maxDist) {
     return teleportInFront(target, minDist, maxDist, false);
     }

     boolean teleportInFront(LivingEntity target, double minDist, double maxDist, boolean particles) {
     Vec3 look = target.getLookAngle();
     Vec3 front = new Vec3(look.x, 0, look.z);
     front = front.lengthSqr() < 1.0E-4D ? new Vec3(1, 0, 0) : front.normalize();

     for (int i = 0; i < 20; i++) {
     double dist = minDist + this.random.nextDouble() * (maxDist - minDist);
     double angle = i < 14
     ? (this.random.nextDouble() - 0.5D) * Math.toRadians(40)
     : this.random.nextDouble() * Math.PI * 2;
     Vec3 dir = front.yRot((float) angle);

     double x = target.getX() + dir.x * dist;
     double y = target.getY() + 1.0D;
     double z = target.getZ() + dir.z * dist;

     if (this.randomTeleport(x, y, z, particles)) {
     this.getLookControl().setLookAt(target, 360.0F, 360.0F);
     return true;
     }
     }
     return false;
     }

    private void smokePuff() {
        if (this.level() instanceof ServerLevel server) {
            // Mas alto que antes: el mob ahora mide ~3.8 bloques
            server.sendParticles(ParticleTypes.LARGE_SMOKE,
                    this.getX(), this.getY() + 1.9D, this.getZ(), 35, 0.3D, 1.5D, 0.3D, 0.02D);
        }
    }

    private void beginVanish() {
        this.setInvisible(true);
        this.setInvulnerable(true);
        this.setSilent(true);
    }

    private void endVanish() {
        this.setInvisible(false);
        this.setInvulnerable(false);
        this.setSilent(false);
    }

    // =====================================================================
    //  Goal: teletransporte normal (detras del jugador, cada pocos segundos)
    // =====================================================================
    static class TeleportGoal extends Goal {
        private final AyuwokiEntity mob;

        TeleportGoal(AyuwokiEntity mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = mob.getTarget();
            return target != null
                    && target.isAlive()
                    && !mob.isInvisible()
                    && mob.teleportCooldown <= 0
                    && mob.distanceToSqr(target) > TELEPORT_MIN_DISTANCE_SQR;
        }

        @Override
        public boolean canContinueToUse() { return false; }

        @Override
        public void start() {
            LivingEntity target = mob.getTarget();
            if (target == null) return;

            Level level = mob.level();
            double oldX = mob.getX(), oldY = mob.getY(), oldZ = mob.getZ();

            if (mob.teleportInFront(target, 3.0D, 5.0D, true)) {
                level.playSound(null, oldX, oldY, oldZ, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0F, 0.6F);
                level.playSound(null, mob.getX(), mob.getY(), mob.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0F, 0.6F);
            }
            mob.teleportCooldown = TELEPORT_COOLDOWN_MIN + mob.getRandom().nextInt(TELEPORT_COOLDOWN_RANDOM);
        }
    }

    // =====================================================================
    //  Goal: desaparece, suena el "hee hee" y reaparece con un jumpscare
    //
    //   t = 0 s   -> desaparece (humo) y se coloca detras del jugador
    //   t = 2 s   -> suena el "hee hee"
    //   t = 3 s   -> reaparece pegado al jugador + JUMPSCARE
    // =====================================================================
    static class VanishGoal extends Goal {
        private final AyuwokiEntity mob;
        private long startTime;
        private boolean heeHeePlayed;
        private boolean finished;

        VanishGoal(AyuwokiEntity mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean isInterruptable() { return false; }

        @Override
        public boolean canUse() {
            LivingEntity target = mob.getTarget();
            return target != null
                    && target.isAlive()
                    && !mob.isInvisible()
                    && mob.vanishCooldown <= 0
                    && mob.distanceToSqr(target) > 9.0D          // a mas de 3 bloques
                    && mob.distanceToSqr(target) < 576.0D;       // a menos de 24 bloques
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = mob.getTarget();
            return !finished && target != null && target.isAlive();
        }

        @Override
        public void start() {
            Level level = mob.level();
            startTime = level.getGameTime();
            heeHeePlayed = false;
            finished = false;

            mob.getNavigation().stop();
            mob.smokePuff();
            level.playSound(null, mob.getX(), mob.getY(), mob.getZ(),
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 0.6F, 0.4F);
            mob.beginVanish();

            // Se coloca en la espalda del jugador, listo para el susto
            LivingEntity target = mob.getTarget();
            if (target != null) {
                mob.teleportAround(target, 6.0D, 9.0D, true, false);
            }
        }

        @Override
        public void tick() {
            Level level = mob.level();
            long elapsed = level.getGameTime() - startTime;

            if (!heeHeePlayed && elapsed >= VANISH_TO_HEE_TICKS) {
                heeHeePlayed = true;
                level.playSound(null, mob.getX(), mob.getEyeY(), mob.getZ(),
                        ModSounds.HEE_HEE.get(), SoundSource.HOSTILE, 2.0F, 1.0F);
            }

            if (elapsed >= VANISH_TO_HEE_TICKS + HEE_TO_APPEAR_TICKS) {
                reappear();
            }
        }

        private void reappear() {
            finished = true;
            LivingEntity target = mob.getTarget();

            mob.endVanish();
            if (target != null) {
                // Aparece muy cerca, justo detras del jugador
                mob.teleportAround(target, 2.5D, 3.5D, true, false);
            }
            mob.smokePuff();
            mob.level().broadcastEntityEvent(mob, EVENT_JUMPSCARE);

            mob.teleportCooldown = 60;
            mob.vanishCooldown = VANISH_COOLDOWN_MIN + mob.getRandom().nextInt(VANISH_COOLDOWN_RANDOM);
            // Que no encadene un "Te veo" justo despues del susto
            mob.peekCooldown = Math.max(mob.peekCooldown, 300);
        }

        @Override
        public void stop() {
            // Si algo corto la secuencia (objetivo muerto, etc.), vuelve a ser visible
            mob.endVanish();
            if (!finished) {
                mob.vanishCooldown = 200;
            }
        }
    }

    // =====================================================================
    //  Goal: "Te veo". Desaparece, susurra, y 4 s despues reaparece a lo lejos,
    //  DELANTE del jugador y mirandolo. No hay jumpscare: es puro acoso.
    //
    //   t = 0 s    -> desaparece (humo) y se coloca detras del jugador
    //   t = 0.5 s  -> "Te veo" (sonido + texto en pantalla)
    //   t = 4 s    -> reaparece en tu campo de vision, a 9-13 bloques
    // =====================================================================
    static class TeVeoGoal extends Goal {
        private final AyuwokiEntity mob;
        private long startTime;
        private boolean said;
        private boolean finished;

        TeVeoGoal(AyuwokiEntity mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean isInterruptable() { return false; }

        @Override
        public boolean canUse() {
            LivingEntity target = mob.getTarget();
            return target != null
                    && target.isAlive()
                    && !mob.isInvisible()
                    && mob.peekCooldown <= 0
                    && mob.distanceToSqr(target) > 36.0D          // a mas de 6 bloques
                    && mob.distanceToSqr(target) < 1024.0D;       // a menos de 32 bloques
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = mob.getTarget();
            return !finished && target != null && target.isAlive();
        }

        @Override
        public void start() {
            Level level = mob.level();
            startTime = level.getGameTime();
            said = false;
            finished = false;

            mob.getNavigation().stop();
            mob.smokePuff();
            level.playSound(null, mob.getX(), mob.getY(), mob.getZ(),
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 0.8F, 0.5F);
            mob.beginVanish();

            // El susurro sale de su espalda
            LivingEntity target = mob.getTarget();
            if (target != null) {
                mob.teleportAround(target, 4.0D, 6.0D, true, false);
            }
        }

        @Override
        public void tick() {
            Level level = mob.level();
            long elapsed = level.getGameTime() - startTime;

            if (!said && elapsed >= PEEK_SAY_TICKS) {
                said = true;
                level.playSound(null, mob.getX(), mob.getEyeY(), mob.getZ(),
                        ModSounds.TE_VEO.get(), SoundSource.HOSTILE, 2.0F, 1.0F);
                // Texto sobre la barra de objetos (sirve aunque no tengas el audio puesto)
                if (mob.getTarget() instanceof Player player) {
                    player.displayClientMessage(
                            Component.literal("Te veo...").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
                            true);
                }
            }

            if (elapsed >= PEEK_TOTAL_TICKS) {
                reappear();
            }
        }

        private void reappear() {
            finished = true;
            LivingEntity target = mob.getTarget();

            mob.endVanish();
            if (target != null) {
                mob.teleportInFront(target, 9.0D, 13.0D);
            }
            mob.smokePuff();
            mob.level().playSound(null, mob.getX(), mob.getY(), mob.getZ(),
                    SoundEvents.ENDERMAN_STARE, SoundSource.HOSTILE, 1.5F, 0.5F);

            mob.teleportCooldown = 100;
            mob.peekCooldown = PEEK_COOLDOWN_MIN + mob.getRandom().nextInt(PEEK_COOLDOWN_RANDOM);
            // Que no encadene el jumpscare inmediatamente
            mob.vanishCooldown = Math.max(mob.vanishCooldown, 200);
        }

        @Override
        public void stop() {
            mob.endVanish();
            if (!finished) {
                mob.peekCooldown = 300;
            }
        }
    }
}
