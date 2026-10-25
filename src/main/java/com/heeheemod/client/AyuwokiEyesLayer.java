package com.heeheemod.client;

import com.heeheemod.HeeHeeMod;
import com.heeheemod.entity.AyuwokiEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

/** Ojos rojos que brillan en la oscuridad (como los de la Arana o el Enderman). */
public class AyuwokiEyesLayer extends EyesLayer<AyuwokiEntity, AyuwokiModel> {
    private static final RenderType EYES = RenderType.eyes(
            ResourceLocation.fromNamespaceAndPath(HeeHeeMod.MOD_ID, "textures/entity/ayuwoki_eyes.png"));

    public AyuwokiEyesLayer(RenderLayerParent<AyuwokiEntity, AyuwokiModel> parent) {
        super(parent);
    }

    @Override
    public RenderType renderType() {
        return EYES;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AyuwokiEntity entity,
                       float limbSwing, float limbSwingAmount, float partialTicks,
                       float ageInTicks, float netHeadYaw, float headPitch) {
        // Mientras esta desaparecido, los ojos tampoco se ven
        if (entity.isInvisible()) return;
        super.render(poseStack, buffer, packedLight, entity, limbSwing, limbSwingAmount,
                partialTicks, ageInTicks, netHeadYaw, headPitch);
    }
}
