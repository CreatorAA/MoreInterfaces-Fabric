package online.pigeonshouse.moreinterfaces.client.util;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

public class ClientRenderUtil {
    public static NativeImage buildStackNativeImage(Minecraft mc, int size, ItemStack stack) {
        ItemRenderer renderer = mc.getItemRenderer();
        RenderTarget renderTarget = new TextureTarget(size, size, true, Minecraft.ON_OSX);

        PoseStack viewStack = RenderSystem.getModelViewStack();
        viewStack.pushPose();
        viewStack.setIdentity();

        RenderSystem.backupProjectionMatrix();

        Matrix4f orthographic =
                new Matrix4f().ortho(0, 16, 16, 0, -150, 150);

        RenderSystem.setProjectionMatrix(orthographic);

        renderTarget.bindWrite(true);
        renderTarget.bindRead();

        BakedModel model = renderer.getModel(stack, null, null, 0);

        mc.getTextureManager()
                .getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);

        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        PoseStack viewStack1 = RenderSystem.getModelViewStack();

        viewStack1.pushPose();
        viewStack1.translate(0, 0, 100.0F + renderer.blitOffset);
        viewStack1.translate(8.0F, 8.0F, 0.0F);
        viewStack1.scale(1.0F, -1.0F, 1.0F);
        viewStack1.scale(16.0F, 16.0F, 16.0F);
        RenderSystem.applyModelViewMatrix();

        PoseStack poseStack = new PoseStack();

        MultiBufferSource.BufferSource immediate = Minecraft.getInstance()
                .renderBuffers().bufferSource();

        boolean bl = !model.usesBlockLight();

        if (bl) {
            Lighting.setupForFlatItems();
        }

        renderer.render(stack, ItemTransforms.TransformType.GUI, false, poseStack, immediate, 15728880,
                OverlayTexture.NO_OVERLAY, model);

        immediate.endBatch();
        RenderSystem.enableDepthTest();

        if (bl) {
            Lighting.setupFor3DItems();
        }

        viewStack1.popPose();
        RenderSystem.applyModelViewMatrix();

        RenderSystem.restoreProjectionMatrix();
        viewStack.popPose();

        renderTarget.unbindWrite();
        renderTarget.unbindRead();

        NativeImage img = new NativeImage(renderTarget.width, renderTarget.height, false);
        RenderSystem.bindTexture(renderTarget.getColorTextureId());
        img.downloadTexture(0, false);
        img.flipY();

        return img;
    }
}
