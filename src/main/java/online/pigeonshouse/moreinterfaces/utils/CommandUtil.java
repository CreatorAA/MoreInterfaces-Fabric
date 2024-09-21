package online.pigeonshouse.moreinterfaces.utils;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import online.pigeonshouse.moreinterfaces.MoreInterfaces;
import online.pigeonshouse.moreinterfaces.handlers.message.CommandResultMessage;
import online.pigeonshouse.moreinterfaces.netty.ChatSession;

public class CommandUtil {
    private static volatile CommandSourceStack USER, SERVER;
    private static CommandBuildContext commandRegistryAccess;

    public static CommandSourceStack getUserCommandSourceStack() {
        if (USER == null) {
            synchronized (CommandUtil.class) {
                if (USER == null) {
                    MinecraftServer server = MoreInterfaces.MINECRAFT_SERVER.get();
                    ServerLevel overworld = server.overworld();

                    USER = new CommandSourceStack(
                            CommandSource.NULL,
                            Vec3.atLowerCornerOf(overworld.getSharedSpawnPos()),
                            Vec2.ZERO,
                            overworld,
                            0,
                            "MoreInterfaces.CommandUtil",
                            ComponentUtil.literal("CommandUtil"),
                            server,
                            null
                    );
                }
            }
        }

        return USER;
    }

    public static CommandSourceStack getUserCommandSourceStack(ChatSession session) {
        MinecraftServer server = MoreInterfaces.MINECRAFT_SERVER.get();
        ServerLevel overworld = server.overworld();

        return new CommandSourceStack(
                new CommandUtilSource(session),
                Vec3.atLowerCornerOf(overworld.getSharedSpawnPos()),
                Vec2.ZERO,
                overworld,
                0,
                "MoreInterfaces.CommandUtil",
                ComponentUtil.literal("CommandUtil"),
                server,
                null
        );
    }

    public static CommandSourceStack getServerCommandSourceStack() {
        if (SERVER == null) {
            synchronized (CommandUtil.class) {
                if (SERVER == null) {
                    SERVER = MoreInterfaces.MINECRAFT_SERVER.get()
                            .createCommandSourceStack();
                }
            }
        }

        return SERVER;
    }

    public static CommandSourceStack getServerCommandSourceStack(ChatSession session) {
        MinecraftServer server = MoreInterfaces.MINECRAFT_SERVER.get();
        ServerLevel overworld = server.overworld();

        return new CommandSourceStack(
                new CommandUtilSource(session),
                Vec3.atLowerCornerOf(overworld.getSharedSpawnPos()),
                Vec2.ZERO,
                overworld,
                4,
                "MoreInterfaces.CommandUtil",
                ComponentUtil.literal("CommandUtil"),
                server,
                null
        );
    }

    public static CommandBuildContext getRegistryAccess() {
        if (commandRegistryAccess == null) {
            synchronized (CommandUtil.class) {
                if (commandRegistryAccess == null) {
                    commandRegistryAccess = Commands.createValidationContext(VanillaRegistries.createLookup());
                }
            }
        }

        return commandRegistryAccess;
    }

    public static class CommandUtilSource implements CommandSource {
        private final ChatSession session;

        public CommandUtilSource(ChatSession session) {
            this.session = session;
        }

        @Override
        public void sendSystemMessage(Component component) {
            session.sendMsg(new CommandResultMessage(session.sessionId(), component.getString()));
        }

        @Override
        public boolean acceptsSuccess() {
            return true;
        }

        @Override
        public boolean acceptsFailure() {
            return true;
        }

        @Override
        public boolean shouldInformAdmins() {
            return true;
        }
    }
}
