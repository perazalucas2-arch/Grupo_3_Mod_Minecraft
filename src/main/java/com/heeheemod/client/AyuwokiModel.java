package com.heeheemod.client;

import com.heeheemod.entity.AyuwokiEntity;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class AyuwokiModel extends net.minecraft.client.model.EntityModel<AyuwokiEntity> {
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart hat;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart jaw;
    private final ModelPart rightFingers;
    private final ModelPart leftFingers;

    public AyuwokiModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.hat = root.getChild("hat");
        this.body = root.getChild("body");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
        this.jaw = this.head.getChild("jaw");
        this.rightFingers = this.rightArm.getChild("fingers");
        this.leftFingers = this.leftArm.getChild("fingers");
    }

    @Override
    public void renderToBuffer(com.mojang.blaze3d.vertex.PoseStack poseStack, com.mojang.blaze3d.vertex.VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // ---- Cabeza + mandíbula articulada ----
        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -8.0F, -3.5F, 7, 8, 7),
                PartPose.offset(0.0F, -14.0F, 0.0F));
        head.addOrReplaceChild("jaw",
                CubeListBuilder.create().texOffs(28, 28).addBox(-3.0F, 0.0F, -6.0F, 6, 3, 6),
                PartPose.offset(0.0F, -1.0F, 3.0F));

        // ---- Pelo (casquete + melena trasera) ----
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

        // ---- Brazos largos (mapeados en la mitad inferior de la textura 64x128) ----
        PartDefinition rightArm = root.addOrReplaceChild("right_arm",
                CubeListBuilder.create().texOffs(28, 64).addBox(-1.0F, -2.0F, -1.0F, 2, 24, 2),
                PartPose.offset(-4.0F, -12.0F, 0.0F));
        PartDefinition leftArm = root.addOrReplaceChild("left_arm",
                CubeListBuilder.create().texOffs(36, 64).addBox(-1.0F, -2.0F, -1.0F, 2, 24, 2),
                PartPose.offset(4.0F, -12.0F, 0.0F));
        CubeListBuilder fingers = CubeListBuilder.create()
                .texOffs(44, 90).addBox(-2.0F, -1.0F, -0.5F, 1, 6, 1)
                .texOffs(44, 90).addBox(-0.5F, -1.0F, -0.5F, 1, 6, 1)
                .texOffs(44, 90).addBox(1.0F, -1.0F, -0.5F, 1, 6, 1);
        rightArm.addOrReplaceChild("fingers", fingers, PartPose.offset(0.0F, 22.0F, 0.0F));
        leftArm.addOrReplaceChild("fingers", fingers, PartPose.offset(0.0F, 22.0F, 0.0F));

        // ---- Piernas finas (mapeadas en la mitad inferior de la textura 64x128) ----
        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(0, 72).addBox(-1.5F, 0.0F, -1.5F, 3, 22, 3),
                PartPose.offset(-1.6F, 2.0F, 0.0F));
        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(16, 72).addBox(-1.5F, 0.0F, -1.5F, 3, 22, 3),
                PartPose.offset(1.6F, 2.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 128);
    }

    @Override
    public void setupAnim(AyuwokiEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {

        float jerkySwing = Mth.floor(limbSwing / 1.6F) * 1.6F;

        // Movimiento de piernas al caminar
        this.rightLeg.xRot = Mth.sin(jerkySwing * 0.5F) * 1.2F * limbSwingAmount;
        this.leftLeg.xRot = Mth.sin(jerkySwing * 0.5F + (float)Math.PI) * 1.2F * limbSwingAmount;

        // Definir estado y reloj antes de usarlos en los brazos
        boolean chasing = entity.isAggressive();
        float t = ageInTicks + entity.getId() * 13.0F;

        // ---- Brazos ----
        if (chasing) {
            AnimationUtils.animateZombieArms(this.leftArm, this.rightArm, true, this.attackTime, ageInTicks);
            this.rightArm.yRot = -0.18F + Mth.sin(t * 0.7F) * 0.04F;
            this.leftArm.yRot = 0.18F + Mth.sin(t * 0.7F + 1.7F) * 0.04F;
            this.rightArm.xRot = Mth.sin(t * 1.3F) * 0.05F;
            this.leftArm.xRot = Mth.sin(t * 1.3F + 2.1F) * 0.05F;
        } else {
            this.rightArm.zRot = 0.05F + Mth.cos(t * 0.09F) * 0.05F;
            this.leftArm.zRot = -0.05F + Mth.cos(t * 0.09F) * 0.05F;
            this.rightArm.xRot = 0.0F;
            this.leftArm.xRot = 0.0F;
        }

        // ---- Dedos ----
        float twitch = Mth.sin(t * (chasing ? 0.9F : 0.45F)) * (chasing ? 0.45F : 0.25F);
        float curl = chasing ? 1.0F : 0.5F;
        this.rightFingers.xRot = curl + twitch;
        this.leftFingers.xRot = curl - twitch;

        // ---- Cabeza ----
        float tic = Mth.sin(t * 2.3F) * Mth.sin(t * 0.31F);
        this.head.zRot = 0.45F + (tic * 0.08F);
        this.head.xRot = (headPitch * ((float)Math.PI / 180F)) + (chasing ? 0.35F : 0.22F);
        this.head.yRot = (netHeadYaw * ((float)Math.PI / 180F)) + (tic * 0.06F);
        this.head.z = chasing ? -3.0F : -1.5F;

        // Glitch de cabeza
        boolean snap = (t % 90.0F) < 3.0F;
        if (snap) {
            this.head.zRot = -0.55F;
            this.head.yRot += 0.7F;
            this.head.xRot += 0.2F;
        }

        // ---- Mandibula ----
        float open = chasing
                ? 0.95F + Mth.sin(t * 1.1F) * 0.15F
                : 0.35F + Mth.sin(t * 0.12F) * 0.08F + Math.max(0.0F, Mth.sin(t * 0.8F)) * 0.05F;
        if (snap) open += 0.4F;
        this.jaw.xRot = open;

        // El pelo sigue a la cabeza
        this.hat.copyFrom(this.head);
    }
}