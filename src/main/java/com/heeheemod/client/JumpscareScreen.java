package com.heeheemod.client;

import com.heeheemod.HeeHeeMod;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Pantalla del jumpscare: cara a pantalla completa que crece y tiembla, con destellos rojos.
 * Se cierra sola a los 1.5 segundos. No pausa el juego.
 */
public class JumpscareScreen extends Screen {
    private static final ResourceLocation FACE =
            ResourceLocation.fromNamespaceAndPath(HeeHeeMod.MOD_ID, "textures/gui/jumpscare.png");

    /** Duracion total en ticks (30 = 1.5 s). */
    private static final int DURATION = 30;
    private int ticksLeft = DURATION;

    public JumpscareScreen() {
        super(Component.empty());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void tick() {
        if (--this.ticksLeft <= 0) {
            this.onClose();
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        int w = this.width;
        int h = this.height;

        float left = Math.max(0.0F, this.ticksLeft - partialTick);
        float progress = 1.0F - left / DURATION;           // 0 -> 1
        float alpha = left > 10.0F ? 1.0F : left / 10.0F;  // se desvanece al final

        // Fondo negro
        g.fill(0, 0, w, h, ((int) (alpha * 235) << 24));
        // Destellos rojos
        if (left > 10.0F && this.ticksLeft % 4 < 2) {
            g.fill(0, 0, w, h, 0x40FF0000);
        }

        // La cara crece hacia el jugador y tiembla
        float scale = 1.0F + progress * 0.35F;
        int size = (int) (h * scale);
        int shakeX = (int) ((Math.random() - 0.5D) * 24.0D * alpha);
        int shakeY = (int) ((Math.random() - 0.5D) * 24.0D * alpha);
        int x = (w - size) / 2 + shakeX;
        int y = (h - size) / 2 + shakeY;

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        g.blit(FACE, x, y, size, size, 0.0F, 0.0F, 256, 256, 256, 256);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }
}
