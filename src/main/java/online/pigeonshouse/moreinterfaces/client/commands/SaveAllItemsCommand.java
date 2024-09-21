package online.pigeonshouse.moreinterfaces.client.commands;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import lombok.Setter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import online.pigeonshouse.moreinterfaces.client.ClientSource;
import online.pigeonshouse.moreinterfaces.client.MoreInterfacesClient;
import online.pigeonshouse.moreinterfaces.client.util.ClientRenderUtil;
import online.pigeonshouse.moreinterfaces.utils.ComponentUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

public class SaveAllItemsCommand {
    private static final Logger log = LogManager.getLogger(MoreInterfacesClient.class);

    public static final AtomicBoolean isRunning = new AtomicBoolean(false);
    public static final Path ICON_ROOT = Path.of("icons");

    public static final int WHITE = 0xFFFFFFFF;
    public static final int BLACK = 0xFF000000;
    public static final int RED = 0xFFFF0000;
    public static final int GREEN = 0xFF00FF00;
    public static final int BLUE = 0xFF0000FF;
    public static final int YELLOW = 0xFFFFFF00;
    public static final int CYAN = 0xFF00FFFF;
    public static final int MAGENTA = 0xFFFF00FF;

    private static int execute(CommandContext<ClientSource> context, int pixels) {
        ClientSource source = context.getSource();
        if (isRunning.get()) {
            source.getPlayer().displayClientMessage(ComponentUtil.literal("请等待上一个任务完成！")
                    .withStyle(ChatFormatting.RED), false);
            return 0;
        }
        isRunning.set(true);

        AbstractClientPlayer player = source.getPlayer();
        player.displayClientMessage(ComponentUtil.literal("开始保存所有物品！请等待任务结束！")
                .withStyle(ChatFormatting.GREEN), false);

        SaveAllItemProgressBarScreen screen = new SaveAllItemProgressBarScreen(
                ComponentUtil.literal("Loading..."),
                200,
                30,
                GREEN,
                pixels
        );
        Minecraft.getInstance().setScreen(screen);
        return 0;
    }

    private static void finish(long start, Screen screen) {
        isRunning.set(false);
        Minecraft.getInstance().setScreen(screen);
        Minecraft.getInstance().player.displayClientMessage(
                ComponentUtil.literal("任务完成！耗时：" + (System.currentTimeMillis() - start) + "ms")
                        .withStyle(ChatFormatting.GREEN), false);
        log.info(Minecraft.getInstance().screen);
    }

    public static void register(CommandDispatcher<ClientSource> dispatcher) {
        log.info("Registering command: saveallitems");

        LiteralArgumentBuilder<ClientSource> literal =
                LiteralArgumentBuilder.literal("saveallitems");

        RequiredArgumentBuilder<ClientSource, Integer> pixels =
                RequiredArgumentBuilder.argument("pixels", IntegerArgumentType.integer());

        pixels.executes(context -> execute(context, IntegerArgumentType.getInteger(context, "pixels")));
        literal.executes(context -> execute(context, 64));

        literal.then(pixels);
        dispatcher.register(literal);
    }

    @Setter
    public static class SaveAllItemProgressBarScreen extends Screen {
        private int progress;
        private final int maxProgress;
        private final int barWidth;
        private final int barHeight;
        private final int barColor;
        private final Queue<Item> queue;
        private boolean finish = false;
        private static final Logger log = LogManager.getLogger(MoreInterfacesClient.class);
        private final Path dir;
        private final long start;
        private final int imgPixels;

        private final Screen lastScreen;

        @Override
        public boolean keyPressed(int p_96552_, int p_96553_, int p_96554_) {
            return false;
        }

        public SaveAllItemProgressBarScreen(Component title, int barWidth, int barHeight, int barColor, int imgPixels) {
            super(title);
            this.imgPixels = imgPixels;
            this.queue = new LinkedList<>(BuiltInRegistries.ITEM.stream().toList());
            this.progress = 0;
            this.maxProgress = queue.size();
            this.barWidth = barWidth;
            this.barHeight = barHeight;
            this.barColor = barColor;

            this.start = System.currentTimeMillis();
            this.dir = ICON_ROOT.resolve("items-" + start);
            this.lastScreen = Minecraft.getInstance().screen;

            Minecraft.getInstance().player
                    .displayClientMessage(ComponentUtil.literal("预计保存 %s 个物品，保存路径: %s".formatted(maxProgress, dir))
                            .withStyle(ChatFormatting.GREEN), false);
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            super.render(graphics, mouseX, mouseY, delta);
            this.renderBackground(graphics);

            int leftPos = (this.width - barWidth) / 2;
            int topPos = (this.height - barHeight) / 2;

            int progressWidth = (int) (barWidth * (progress / (float) maxProgress));
            graphics.fillGradient(leftPos, topPos, leftPos + progressWidth, topPos + barHeight, barColor, darkenColor(barColor));

            String progressText = (progress * 100 / maxProgress) + "%";
            graphics.drawString(this.font, progressText, leftPos + barWidth / 2, topPos + barHeight / 2, WHITE);


            graphics.hLine(leftPos, leftPos + barWidth, topPos, WHITE);
            graphics.hLine(leftPos, leftPos + barWidth, topPos + barHeight, WHITE);
            graphics.vLine(leftPos, topPos, topPos + barHeight, WHITE);
            graphics.vLine(leftPos + barWidth, topPos, topPos + barHeight, WHITE);
        }

        private int darkenColor(int color) {
            int r = (color >> 16) & 0xFF;
            int g = (color >> 8) & 0xFF;
            int b = color & 0xFF;
            r = (int) (r * 0.8);
            g = (int) (g * 0.8);
            b = (int) (b * 0.8);
            return (r << 16) | (g << 8) | b;
        }

        @Override
        public void tick() {
            super.tick();

            if (finish) {
                finish(start, lastScreen);
                return;
            }

            long start = System.currentTimeMillis();
            int renderSize = 0;

            while ((System.currentTimeMillis() - start) <= 50) {
                if (queue.isEmpty()) {
                    finish = true;
                    break;
                }

                Item item = queue.poll();

                try {
                    ItemStack itemStack = item.getDefaultInstance();
                    Optional<ResourceKey<Item>> resourceKey = itemStack.getItemHolder().unwrapKey();

                    if (resourceKey.isEmpty()) {
                        log.error("Error item: {}", item);
                        continue;
                    }

                    ResourceKey<Item> key = resourceKey.get();
                    ResourceLocation location = key.location();
                    Path modDir = dir.resolve(location.getNamespace());
                    Files.createDirectories(modDir);

                    Minecraft minecraft = Minecraft.getInstance();

                    NativeImage nativeImage = null;
                    try {
                        nativeImage = ClientRenderUtil.buildStackNativeImage(minecraft, imgPixels, itemStack);
                        nativeImage.writeToFile(modDir.resolve(location.getPath() + ".png").toFile());
                    }finally {
                        if (nativeImage != null) {
                            nativeImage.close();
                        }
                    }

                } catch (Exception e) {
                    log.error("Error item: {}", item, e);
                } finally {
                    renderSize++;
                }
            }

            progress += renderSize;
        }
    }


}
