package online.pigeonshouse.moreinterfaces.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.*;
import online.pigeonshouse.moreinterfaces.netty.SerializeFactory;
import online.pigeonshouse.moreinterfaces.trendsobject.TrendsObject;

public class MoreInterfacesCommand implements MICommands.MICommand {
    @Override
    public void register(Commands commands, Commands.CommandSelection selection, CommandBuildContext context) {
        CommandDispatcher<CommandSourceStack> dispatcher = commands.getDispatcher();

        dispatcher.register(Commands.literal("moreinterfaces")
                .then(Commands.literal("AES")
                        .then(Commands.literal("printKey")
                                .executes(MoreInterfacesCommand::printAESKey)
                        )
                        .then(Commands.literal("generate")
                                .executes(MoreInterfacesCommand::generateAESKey)
                        )
                )
        );
    }

    private static int generateAESKey(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        if (!source.hasPermission(4)) {
            source.sendFailure(Component
                    .literal("你没有权限执行此指令！"));
        }

        String key;

        try {
            key = SerializeFactory.AESJsonSerialize.generateAESKey(true);
            SerializeFactory.AES_KEY.set(key);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        MutableComponent component = Component.literal(key)
                .withStyle(ChatFormatting.YELLOW)
                .withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("点击复制"))))
                .withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, key)));

        source.sendSuccess(() -> component, false);

        return 1;
    }

    private static int printAESKey(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        if (!source.hasPermission(4)) {
            source.sendFailure(Component
                    .literal("你没有权限执行此指令！"));
        }

        TrendsObject<String> aesKey = SerializeFactory.AES_KEY;
        String key = aesKey.get();

        if (aesKey.isEmpty()) {
            try {
                key = SerializeFactory.AESJsonSerialize.generateAESKey(false);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        MutableComponent component = Component.literal(key)
                .withStyle(ChatFormatting.YELLOW)
                .withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("点击复制"))))
                .withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, key)));

        source.sendSuccess(() -> component, false);
        return 1;
    }
}
