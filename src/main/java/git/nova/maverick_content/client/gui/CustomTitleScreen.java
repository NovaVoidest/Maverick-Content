package git.nova.maverick_content.client.gui;

import git.nova.maverick_content.MaverickContentMod;
import git.nova.maverick_content.init.ModSoundEvents;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.client.renderer.texture.DynamicTexture;
import com.mojang.blaze3d.platform.NativeImage;

public class CustomTitleScreen extends Screen {

    private static final Component TITLE = Component.literal("Maverick Content");

    private static final ResourceLocation BACKGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath("maverick_content", "textures/gui/maverick_menu/background.png");
    private static final int BG_TEX_WIDTH = 32;
    private static final int BG_TEX_HEIGHT = 18;

    private static final ResourceLocation SHEET_CENTER_TEX = ResourceLocation.fromNamespaceAndPath("maverick_content", "textures/gui/maverick_menu/sheet_center.png");
    private static final ResourceLocation SHEET_LEFT_TEX = ResourceLocation.fromNamespaceAndPath("maverick_content", "textures/gui/maverick_menu/sheet_left.png");
    private static final ResourceLocation SHEET_RIGHT_TEX = ResourceLocation.fromNamespaceAndPath("maverick_content", "textures/gui/maverick_menu/sheet_right.png");
    private static final int SHEET_CENTER_TEX_W = 128;
    private static final int SHEET_CENTER_TEX_H = 178;
    private static final int SHEET_SIDE_TEX_W = 128;
    private static final int SHEET_SIDE_TEX_H = 145;

    private static final int DOSSIER_ORIG_WIDTH = 800;
    private static final int DOSSIER_ORIG_HEIGHT = 1100;
    private static final float DOSSIER_ASPECT = (float) DOSSIER_ORIG_WIDTH / DOSSIER_ORIG_HEIGHT;
    private static final int DOSSIER_PADDING = 40;

    private static final long FADE_DURATION_MS = 5000;

    private static final float SHEET_W_RATIO = 0.80f;
    private static final float CENTER_SHEET_H_RATIO = 0.82f;
    private static final float SIDE_SHEET_H_RATIO = 0.66f;

    private static final float CENTER_SHEET_X = 0.50f;
    private static final float CENTER_SHEET_Y = 0.48f;
    private static final float LEFT_SHEET_X = -0.05f;
    private static final float LEFT_SHEET_Y = 0.48f;
    private static final float RIGHT_SHEET_X = 1.05f;
    private static final float RIGHT_SHEET_Y = 0.48f;

    private static final float LEFT_ROTATION_DEG = -25f;
    private static final float RIGHT_ROTATION_DEG = 25f;

    private static final float BTN_W_RATIO = 0.80f;
    private static final float BTN_H_RATIO = 0.10f;

    private static final Component SP_TEXT = Component.translatable("maverick_content.title_screen.singleplayer");
    private static final Component MP_TEXT = Component.translatable("maverick_content.title_screen.multiplayer");
    private static final Component OPT_TEXT = Component.translatable("maverick_content.title_screen.options");
    private static final Component QUIT_TEXT = Component.translatable("maverick_content.title_screen.quit");

    private static final float BG_PARALLAX_MAX = 3.0f;
    private static final float SHEET_PARALLAX_MAX = 6.0f;
    private static final float VIGNETTE_PARALLAX_MAX = 1.5f;

    private int dossierX;
    private int dossierY;
    private int dossierWidth;
    private int dossierHeight;

    private static long fadeInStart;

    private static SimpleSoundInstance musicInstance;
    private static boolean musicStarted = false;

    private DynamicTexture vignetteTexture;
    private ResourceLocation vignetteLocation;

    private float parallaxX;
    private float parallaxY;
    private float smoothParallaxX;
    private float smoothParallaxY;

    private static final float PARALLAX_LERP_SPEED = 8.0f;

    private static final int TEXT_COLOR = 0xFF888888;
    private static final int TEXT_COLOR_HOVER = 0xFF000000;

    private static final int LEFT_TEXT_COLOR = 0xFF776B27;
    private static final int LEFT_TEXT_COLOR_HOVER = 0xFF493F06;
    private static final int RIGHT_TEXT_COLOR = 0xFF8A6C84;
    private static final int RIGHT_TEXT_COLOR_HOVER = 0xFF2E0526;

    public CustomTitleScreen() {
        super(TITLE);
    }

    @Override
    protected void init() {
        super.init();
        if (this.fadeInStart == 0) {
            this.fadeInStart = System.currentTimeMillis();
        }
        int maxH = this.height - DOSSIER_PADDING * 2;
        int maxW = this.width - DOSSIER_PADDING * 2;
        int h = Math.min(maxH, (int) (maxW / DOSSIER_ASPECT));
        int w = (int) (h * DOSSIER_ASPECT);
        if (w > maxW) {
            w = maxW;
            h = (int) (w / DOSSIER_ASPECT);
        }
        this.dossierWidth = w;
        this.dossierHeight = h;
        this.dossierX = (this.width - w) / 2;
        this.dossierY = (this.height - h) / 2;

        if (this.minecraft != null && !musicStarted) {
            musicStarted = true;
            this.minecraft.getMusicManager().stopPlaying();
            musicInstance = new SimpleSoundInstance(
                    ResourceLocation.fromNamespaceAndPath(MaverickContentMod.MODID, "menu_music"),
                    SoundSource.RECORDS, 1.0F, 1.0F,
                    net.minecraft.util.RandomSource.create(), true, 0,
                    SoundInstance.Attenuation.NONE, 0.0, 0.0, 0.0, true);
            this.minecraft.getSoundManager().play(musicInstance);
        }

        createVignette();
    }

    private void createVignette() {
        if (this.vignetteTexture != null) {
            this.vignetteTexture.close();
        }
        int size = 256;
        NativeImage image = new NativeImage(size, size, true);
        float cx = size / 2.0F;
        float cy = size / 2.0F;
        float maxDist = (float) Math.sqrt(cx * cx + cy * cy);

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = x - cx;
                float dy = y - cy;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                float t = Mth.clamp(dist / maxDist, 0.0F, 1.0F);
                float alpha = t * t;
                int a = (int) (alpha * 200);
                image.setPixelRGBA(x, y, (a << 24));
            }
        }

        this.vignetteTexture = new DynamicTexture(image);
        this.vignetteLocation = this.minecraft.getTextureManager().register("maverick_content_vignette", this.vignetteTexture);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        float fadeProgress = Mth.clamp((System.currentTimeMillis() - this.fadeInStart) / (float) FADE_DURATION_MS, 0.0F, 1.0F);
        RenderSystem.setShaderColor(fadeProgress, fadeProgress, fadeProgress, 1.0F);

        float normCursorX = (mouseX - this.width / 2.0f) / (this.width / 2.0f);
        float normCursorY = (mouseY - this.height / 2.0f) / (this.height / 2.0f);
        this.parallaxX = -normCursorX;
        this.parallaxY = -normCursorY;

        float lerpFactor = 1.0f - (float) Math.exp(-PARALLAX_LERP_SPEED * partialTick);
        this.smoothParallaxX = Mth.lerp(lerpFactor, this.smoothParallaxX, this.parallaxX);
        this.smoothParallaxY = Mth.lerp(lerpFactor, this.smoothParallaxY, this.parallaxY);

        float bgOffX = this.smoothParallaxX * BG_PARALLAX_MAX;
        float bgOffY = this.smoothParallaxY * BG_PARALLAX_MAX;

        graphics.pose().pushPose();
        graphics.pose().translate(bgOffX, bgOffY, 0);
        graphics.pose().scale((float) this.width / BG_TEX_WIDTH, (float) this.height / BG_TEX_HEIGHT, 1.0F);
        graphics.blit(BACKGROUND_TEXTURE, 0, 0, 0, 0.0F, 0.0F, BG_TEX_WIDTH, BG_TEX_HEIGHT, BG_TEX_WIDTH, BG_TEX_HEIGHT);
        graphics.pose().popPose();

        float sheetOffX = this.smoothParallaxX * SHEET_PARALLAX_MAX;
        float sheetOffY = this.smoothParallaxY * SHEET_PARALLAX_MAX;

        drawSheet(graphics, RIGHT_SHEET_X, RIGHT_SHEET_Y, RIGHT_ROTATION_DEG, SIDE_SHEET_H_RATIO, sheetOffX, sheetOffY, SHEET_RIGHT_TEX, SHEET_SIDE_TEX_W, SHEET_SIDE_TEX_H);
        drawSheet(graphics, LEFT_SHEET_X, LEFT_SHEET_Y, LEFT_ROTATION_DEG, SIDE_SHEET_H_RATIO, sheetOffX, sheetOffY, SHEET_LEFT_TEX, SHEET_SIDE_TEX_W, SHEET_SIDE_TEX_H);
        drawSheetText(graphics, LEFT_SHEET_X, LEFT_SHEET_Y, LEFT_ROTATION_DEG, SIDE_SHEET_H_RATIO, QUIT_TEXT, isHovering(mouseX, mouseY, LEFT_SHEET_X, LEFT_SHEET_Y, LEFT_ROTATION_DEG, SIDE_SHEET_H_RATIO, QUIT_TEXT, 0f, 0.35f, 1.5f) ? LEFT_TEXT_COLOR_HOVER : LEFT_TEXT_COLOR, 0f, 0.35f, 1.5f);
        drawSheetText(graphics, RIGHT_SHEET_X, RIGHT_SHEET_Y, RIGHT_ROTATION_DEG, SIDE_SHEET_H_RATIO, OPT_TEXT, isHovering(mouseX, mouseY, RIGHT_SHEET_X, RIGHT_SHEET_Y, RIGHT_ROTATION_DEG, SIDE_SHEET_H_RATIO, OPT_TEXT, 0f, 0.30f, 1.3f) ? RIGHT_TEXT_COLOR_HOVER : RIGHT_TEXT_COLOR, 0f, 0.30f, 1.3f);
        drawSheet(graphics, CENTER_SHEET_X, CENTER_SHEET_Y, 0f, CENTER_SHEET_H_RATIO, sheetOffX, sheetOffY, SHEET_CENTER_TEX, SHEET_CENTER_TEX_W, SHEET_CENTER_TEX_H);

        drawSheetText(graphics, CENTER_SHEET_X, CENTER_SHEET_Y, 0f, CENTER_SHEET_H_RATIO, SP_TEXT, isHovering(mouseX, mouseY, CENTER_SHEET_X, CENTER_SHEET_Y, 0f, CENTER_SHEET_H_RATIO, SP_TEXT, -0.24f, 0.38f, 1.0f) ? TEXT_COLOR_HOVER : TEXT_COLOR, -0.24f, 0.38f);
        drawSheetText(graphics, CENTER_SHEET_X, CENTER_SHEET_Y, 0f, CENTER_SHEET_H_RATIO, MP_TEXT, isHovering(mouseX, mouseY, CENTER_SHEET_X, CENTER_SHEET_Y, 0f, CENTER_SHEET_H_RATIO, MP_TEXT, 0.22f, 0.38f, 1.0f) ? TEXT_COLOR_HOVER : TEXT_COLOR, 0.22f, 0.38f);

        if (this.vignetteTexture != null) {
            float vigOffX = this.smoothParallaxX * VIGNETTE_PARALLAX_MAX;
            float vigOffY = this.smoothParallaxY * VIGNETTE_PARALLAX_MAX;
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderTexture(0, this.vignetteLocation);
            RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionTexColorShader);
            com.mojang.blaze3d.vertex.Tesselator tesselator = com.mojang.blaze3d.vertex.Tesselator.getInstance();
            com.mojang.blaze3d.vertex.BufferBuilder buffer = tesselator.begin(com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS, com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_TEX_COLOR);
            buffer.addVertex(vigOffX, vigOffY, 0).setUv(0, 0).setColor(255, 255, 255, 255);
            buffer.addVertex(vigOffX, this.height + vigOffY, 0).setUv(0, 1).setColor(255, 255, 255, 255);
            buffer.addVertex(this.width + vigOffX, this.height + vigOffY, 0).setUv(1, 1).setColor(255, 255, 255, 255);
            buffer.addVertex(this.width + vigOffX, vigOffY, 0).setUv(1, 0).setColor(255, 255, 255, 255);
            com.mojang.blaze3d.vertex.BufferUploader.drawWithShader(buffer.buildOrThrow());
            RenderSystem.disableBlend();
            RenderSystem.setShaderColor(fadeProgress, fadeProgress, fadeProgress, 1.0F);
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void drawSheetText(GuiGraphics graphics, float sheetX, float sheetY, float angleDeg, float heightRatio, Component text, int color) {
        drawSheetText(graphics, sheetX, sheetY, angleDeg, heightRatio, text, color, 0f, 0f, 1.0f);
    }

    private void drawSheetText(GuiGraphics graphics, float sheetX, float sheetY, float angleDeg, float heightRatio, Component text, int color, float xOff, float yOff) {
        drawSheetText(graphics, sheetX, sheetY, angleDeg, heightRatio, text, color, xOff, yOff, 1.0f);
    }

    private void drawSheetText(GuiGraphics graphics, float sheetX, float sheetY, float angleDeg, float heightRatio, Component text, int color, float xOff, float yOff, float scaleMul) {
        float sheetW = this.dossierWidth * SHEET_W_RATIO;
        float sheetH = this.dossierHeight * heightRatio;
        float cx = this.dossierX + sheetX * this.dossierWidth + this.smoothParallaxX * SHEET_PARALLAX_MAX;
        float cy = this.dossierY + sheetY * this.dossierHeight + this.smoothParallaxY * SHEET_PARALLAX_MAX;
        int textWidth = this.font.width(text);
        float textScale = sheetW / (textWidth * 3.0f) * scaleMul;
        if (textScale > 1.0f) textScale = 1.0f;
        graphics.pose().pushPose();
        graphics.pose().translate(cx, cy, 0);
        graphics.pose().mulPose(com.mojang.math.Axis.ZP.rotation((float) Math.toRadians(angleDeg)));
        graphics.pose().scale(textScale, textScale, 1.0f);
        graphics.pose().translate(xOff * sheetW / textScale, yOff * sheetH / textScale, 0);
        graphics.drawString(this.font, text, (int) (-textWidth / 2f), (int) (-this.font.lineHeight / 2f), color, false);
        graphics.pose().popPose();
    }

    private void drawSheet(GuiGraphics graphics, float xRatio, float yRatio, float angleDeg, float heightRatio, float offX, float offY, ResourceLocation texture, int texW, int texH) {
        int sheetW = (int) (this.dossierWidth * SHEET_W_RATIO);
        int sheetH = (int) (this.dossierHeight * heightRatio);
        int cx = this.dossierX + (int) (xRatio * this.dossierWidth) + (int) offX;
        int cy = this.dossierY + (int) (yRatio * this.dossierHeight) + (int) offY;

        graphics.pose().pushPose();
        graphics.pose().translate(cx, cy, 0);
        graphics.pose().mulPose(com.mojang.math.Axis.ZP.rotation((float) Math.toRadians(angleDeg)));
        graphics.pose().scale((float) sheetW / texW, (float) sheetH / texH, 1.0f);
        graphics.blit(texture, -texW / 2, -texH / 2, 0, 0, texW, texH, texW, texH);
        graphics.pose().popPose();
    }

    @Override
    public void removed() {
        super.removed();
        if (this.vignetteTexture != null) {
            this.vignetteTexture.close();
            this.vignetteTexture = null;
        }
    }

    public static void ensureMusicPlaying(Minecraft mc) {
        mc.getMusicManager().stopPlaying();
        if (musicInstance == null) {
            musicInstance = new SimpleSoundInstance(
                    ResourceLocation.fromNamespaceAndPath(MaverickContentMod.MODID, "menu_music"),
                    SoundSource.RECORDS, 1.0F, 1.0F,
                    net.minecraft.util.RandomSource.create(), true, 0,
                    SoundInstance.Attenuation.NONE, 0.0, 0.0, 0.0, true);
        }
        mc.getSoundManager().play(musicInstance);
    }

    public static void stopMusic(Minecraft mc) {
        if (musicInstance != null) {
            mc.getSoundManager().stop(musicInstance);
            musicInstance = null;
        }
        musicStarted = false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (isInsideTextHitbox(mouseX, mouseY, LEFT_SHEET_X, LEFT_SHEET_Y, LEFT_ROTATION_DEG, SIDE_SHEET_H_RATIO, QUIT_TEXT, 0f, 0.35f, 1.5f)) {
                this.minecraft.stop();
                return true;
            }
            if (isInsideTextHitbox(mouseX, mouseY, RIGHT_SHEET_X, RIGHT_SHEET_Y, RIGHT_ROTATION_DEG, SIDE_SHEET_H_RATIO, OPT_TEXT, 0f, 0.30f, 1.3f)) {
                this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options));
                return true;
            }
            if (isInsideTextHitbox(mouseX, mouseY, CENTER_SHEET_X, CENTER_SHEET_Y, 0f, CENTER_SHEET_H_RATIO, SP_TEXT, -0.24f, 0.38f, 1.0f)) {
                this.minecraft.setScreen(new SelectWorldScreen(this));
                return true;
            }
            if (isInsideTextHitbox(mouseX, mouseY, CENTER_SHEET_X, CENTER_SHEET_Y, 0f, CENTER_SHEET_H_RATIO, MP_TEXT, 0.22f, 0.38f, 1.0f)) {
                this.minecraft.setScreen(new JoinMultiplayerScreen(this));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isInsideTextHitbox(double mouseX, double mouseY, float sheetX, float sheetY, float angleDeg, float heightRatio, Component text, float xOff, float yOff, float scaleMul) {
        float sheetW = this.dossierWidth * SHEET_W_RATIO;
        float sheetH = this.dossierHeight * heightRatio;
        float cx = getSheetCX(sheetX);
        float cy = getSheetCY(sheetY);
        float[] local = rotateToLocal(mouseX, mouseY, cx, cy, angleDeg);
        int textWidth = this.font.width(text);
        float textScale = sheetW / (textWidth * 3.0f) * scaleMul;
        if (textScale > 1.0f) textScale = 1.0f;
        float textCenterX = xOff * sheetW;
        float textCenterY = yOff * sheetH;
        float halfW = textWidth * textScale / 2f + 4f;
        float halfH = this.font.lineHeight * textScale / 2f + 4f;
        return local[0] >= textCenterX - halfW && local[0] <= textCenterX + halfW
                && local[1] >= textCenterY - halfH && local[1] <= textCenterY + halfH;
    }

    private float getSheetCX(float xRatio) {
        return this.dossierX + xRatio * this.dossierWidth + this.smoothParallaxX * SHEET_PARALLAX_MAX;
    }

    private float getSheetCY(float yRatio) {
        return this.dossierY + yRatio * this.dossierHeight + this.smoothParallaxY * SHEET_PARALLAX_MAX;
    }

    private float[] rotateToLocal(double mouseX, double mouseY, float cx, float cy, float angleDeg) {
        float dx = (float) mouseX - cx;
        float dy = (float) mouseY - cy;
        float rad = (float) Math.toRadians(-angleDeg);
        float localX = dx * (float) Math.cos(rad) - dy * (float) Math.sin(rad);
        float localY = dx * (float) Math.sin(rad) + dy * (float) Math.cos(rad);
        return new float[]{localX, localY};
    }

    private boolean isHovering(double mouseX, double mouseY, float xRatio, float yRatio, float angleDeg, float heightRatio, Component text, float xOff, float yOff, float scaleMul) {
        return isInsideTextHitbox(mouseX, mouseY, xRatio, yRatio, angleDeg, heightRatio, text, xOff, yOff, scaleMul);
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }
}
