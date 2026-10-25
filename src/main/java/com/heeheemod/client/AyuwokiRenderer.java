package com.heeheemod.client;

import com.heeheemod.HeeHeeMod;
import com.heeheemod.entity.AyuwokiEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class AyuwokiRenderer extends MobRenderer<AyuwokiEntity, AyuwokiModel> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(HeeHeeMod.MOD_ID, "ayuwoki"), "main");
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(HeeHeeMod.MOD_ID, "textures/entity/ayuwoki.png");

    /** Estiramiento vertical: el modelo mide ~2.9 bloques, x1.3 = ~3.8 (igual que la hitbox). */
    private static final float STRETCH_Y = 1.3F;
    /** Ancho: algo mas fino para que se vea famelico. */
    private static final float WIDTH = 0.9F;

    public AyuwokiRenderer(EntityRendererProvider.Context context) {
        super(context, new AyuwokiModel(context.bakeLayer(LAYER)), 0.4F);
        this.addLayer(new AyuwokiEyesLayer(this));
    }

    /**
     * Estira el modelo sin tocar la textura (las cajas y el UV siguen siendo los mismos,
     * asi que tu ayuwoki.png actual sigue encajando) y lo hace "respirar" apenas.
     */
    @Override
    protected void scale(AyuwokiEntity entity, PoseStack poseStack, float partialTick) {
        float breath = Mth.sin((entity.tickCount + partialTick) * 0.08F) * 0.015F;
        poseStack.scale(WIDTH, STRETCH_Y + breath, WIDTH);
    }

    @Override
    public ResourceLocation getTextureLocation(AyuwokiEntity entity) {
        return TEXTURE;
    }
}
