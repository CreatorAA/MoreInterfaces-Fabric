package online.pigeonshouse.moreinterfaces.client.util;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.opengl.GL11;

public class ClientRenderUtil {
    public static NativeImage buildStackNativeImage(Minecraft mc, int size, ItemStack stack) {
        ItemRenderer renderer = mc.getItemRenderer();
        RenderTarget renderTarget = new RenderTarget(size, size, true, Minecraft.ON_OSX);

        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();
        RenderSystem.matrixMode(GL11.GL_PROJECTION);
        RenderSystem.pushMatrix();
        RenderSystem.ortho(0, 16, 16, 0, -150, 150);
        renderTarget.bindWrite(true);
        renderTarget.bindRead();

        RenderSystem.matrixMode(GL11.GL_MODELVIEW);
        renderer.renderGuiItem(stack, 0, 0);

        RenderSystem.matrixMode(GL11.GL_PROJECTION);
        RenderSystem.popMatrix();
        RenderSystem.popMatrix();

        renderTarget.unbindWrite();
        renderTarget.unbindRead();

        NativeImage img = new NativeImage(renderTarget.width, renderTarget.height, false);
        RenderSystem.bindTexture(renderTarget.getColorTextureId());
        img.downloadTexture(0, false);
        img.flipY();

        return img;
    }
}
