package com.heeheemod.client;

import com.heeheemod.entity.AyuwokiEntity;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

/**
 * Ayuwoki: ~3.8 bloques de alto (el renderer lo estira x1.3), extremidades larguisimas,
 * torso estrecho, cabeza ladeada con melena larga, mandibula articulada y dedos de garra.
 *
 * Las cajas y el UV NO cambian respecto a la version anterior, asi que la textura
 * actual (64x64) sigue encajando. Lo nuevo esta en setupAnim.
 *
 * Mapa de la textura (64x64):
 *   cabeza (0,0)  | brazo der (28,0) | brazo izq (36,0) | pelo trasero (44,0)
 *   torso (0,16)  | dedos (44,20)    | mandibula (28,28)
 *   piernas (0,36) y (12,36)         | pelo/capucha (24,38)
 */
public class AyuwokiModel extends HumanoidModel<AyuwokiEntity> {
    private final ModelPart jaw;
    private final ModelPart rightFingers;
    private final ModelPart leftFingers;

    public AyuwokiModel(ModelPart root) {
        super(root);
        this.jaw = root.getChild("head").getChild("jaw");
        this.rightFingers = root.getChild("right_arm").getChild("fingers");
        this.leftFingers = root.getChild("left_arm").getChild("fingers");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // ---- Cabeza + mandibula articulada ----
        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -8.0F, -3.5F, 7, 8, 7),
                PartPose.offset(0.0F, -14.0F, 0.0F));
        head.addOrReplaceChild("jaw",
                CubeListBuilder.create().texOffs(28, 28).addBox(-3.0F, 0.0F, -6.0F, 6, 3, 6),
                PartPose.offset(0.0F, -1.0F, 3.0F));

        // ---- Pelo (usa la parte "hat"): casquete + melena trasera ----
        PartDefinition hat = root.addOrReplaceChild("hat",
                CubeListBuilder.create().texOffs(24, 38)
                        .addBox(-3.5F, -8.0F, -3.5F, 7, 8, 7, new CubeDeformation(0.55F)),
                PartPose.offset(0.0F, -14.0F, 0.0F));
        hat.addOrReplaceChild("hair_back",
                CubeListBuilder.create().texOffs(44, 0).addBox(-4.0F, -6.0F, 3.9F, 8, 16, 2),
                PartPose.ZERO);

        // ---- Torso estrecho ----
        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 16).addBox(-3.0F, 0.0F, -1.5F, 6, 16, 3),
                PartPose.offset(0.0F, -14.0F, 0.0F));

        // ---- Brazos larguisimos con dedos de garra ----
        PartDefinition rightArm = root.addOrReplaceChild("right_arm",
                CubeListBuilder.create().texOffs(28, 0).addBox(-1.0F, -2.0F, -1.0F, 2, 24, 2),
                PartPose.offset(-4.0F, -12.0F, 0.0F));
        PartDefinition leftArm = root.addOrReplaceChild("left_arm",
                CubeListBuilder.create().texOffs(36, 0).addBox(-1.0F, -2.0F, -1.0F, 2, 24, 2),
                PartPose.offset(4.0F, -12.0F, 0.0F));
        CubeListBuilder fingers = CubeListBuilder.create()
                .texOffs(44, 20).addBox(-2.0F, -1.0F, -0.5F, 1, 6, 1)
                .texOffs(44, 20).addBox(-0.5F, -1.0F, -0.5F, 1, 6, 1)
                .texOffs(44, 20).addBox(1.0F, -1.0F, -0.5F, 1, 6, 1);
        rightArm.addOrReplaceChild("fingers", fingers, PartPose.offset(0.0F, 22.0F, 0.0F));
        leftArm.addOrReplaceChild("fingers", fingers, PartPose.offset(0.0F, 22.0F, 0.0F));

        // ---- Piernas finas ----
        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(0, 36).addBox(-1.5F, 0.0F, -1.5F, 3, 22, 3),
                PartPose.offset(-1.6F, 2.0F, 0.0F));
        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(12, 36).addBox(-1.5F, 0.0F, -1.5F, 3, 22, 3),
                PartPose.offset(1.6F, 2.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(AyuwokiEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // Camina "a tirones" (como stop-motion): la fase de las piernas salta en escalones.
        float jerkySwing = Mth.floor(limbSwing / 1.6F) * 1.6F;
        super.setupAnim(entity, jerkySwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        boolean chasing = entity.isAggressive();
        // Cada ayuwoki tiene su propio "reloj" para que dos juntos no se muevan sincronizados
        float t = ageInTicks + entity.getId() * 13.0F;

        // ---- Brazos ----
        if (chasing) {
            AnimationUtils.animateZombieArms(this.leftArm, this.rightArm, true, this.attackTime, ageInTicks);
            // Garras abiertas y temblando, como si fuera a agarrarte
            this.rightArm.yRot -= 0.18F + Mth.sin(t * 0.7F) * 0.04F;
            this.leftArm.yRot += 0.18F + Mth.sin(t * 0.7F + 1.7F) * 0.04F;
            this.rightArm.xRot += Mth.sin(t * 1.3F) * 0.05F;
            this.leftArm.xRot += Mth.sin(t * 1.3F + 2.1F) * 0.05F;
        } else {
            this.rightArm.zRot += 0.05F + Mth.cos(t * 0.09F) * 0.05F;
            this.leftArm.zRot -= 0.05F + Mth.cos(t * 0.09F) * 0.05F;
            // Espasmos: de vez en cuando un brazo se sacude solo
            if (Mth.sin(t * 0.13F) > 0.96F) this.rightArm.xRot += Mth.sin(t * 9.0F) * 0.18F;
            if (Mth.sin(t * 0.11F + 2.0F) > 0.96F) this.leftArm.xRot += Mth.sin(t * 9.0F) * 0.18F;
        }

        // ---- Dedos: se retuercen sin parar (mas rapido y cerrados al perseguir) ----
        float twitch = Mth.sin(t * (chasing ? 0.9F : 0.45F)) * (chasing ? 0.45F : 0.25F);
        float curl = chasing ? 1.0F : 0.5F;
        this.rightFingers.xRot = curl + twitch;
        this.leftFingers.xRot = curl - twitch;

        // ---- Cabeza: muy ladeada, agachada, adelantada y con tics nerviosos ----
        float tic = Mth.sin(t * 2.3F) * Mth.sin(t * 0.31F);
        this.head.zRot = 0.45F + tic * 0.08F;
        this.head.xRot += chasing ? 0.35F : 0.22F;
        this.head.yRot += tic * 0.06F;
        this.head.z = chasing ? -3.0F : -1.5F;

        // Cada ~4.5 s la cabeza "chasquea" al otro lado durante 3 ticks (glitch)
        boolean snap = (t % 90.0F) < 3.0F;
        if (snap) {
            this.head.zRot = -0.55F;
            this.head.yRot += 0.7F;
            this.head.xRot += 0.2F;
        }

        // ---- Mandibula: cuelga abierta, tiembla, y se desencaja al perseguir ----
        float open = chasing
                ? 0.95F + Mth.sin(t * 1.1F) * 0.15F
                : 0.35F + Mth.sin(t * 0.12F) * 0.08F + Math.max(0.0F, Mth.sin(t * 0.8F)) * 0.05F;
        if (snap) open += 0.4F;
        this.jaw.xRot = open;

        // El pelo sigue a la cabeza
        this.hat.copyFrom(this.head);
    }
}
