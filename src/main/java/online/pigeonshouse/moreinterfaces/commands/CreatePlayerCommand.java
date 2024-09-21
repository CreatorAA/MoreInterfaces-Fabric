package online.pigeonshouse.moreinterfaces.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.phys.Vec3;
import online.pigeonshouse.moreinterfaces.MoreInterfaces;
import online.pigeonshouse.moreinterfaces.netty.command.CommandHandler;
import online.pigeonshouse.moreinterfaces.netty.command.CommandManage;
import online.pigeonshouse.moreinterfaces.netty.command.CommandMap;
import online.pigeonshouse.moreinterfaces.netty.message.MessageCode;
import online.pigeonshouse.moreinterfaces.utils.MapUtil;
import online.pigeonshouse.moreinterfaces.utils.WorldUtil;

import java.util.Map;

public class CreatePlayerCommand implements MICommands.MICommand {
    @Override
    public void register(Commands commands, Commands.CommandSelection selection) {
        CommandDispatcher<CommandSourceStack> dispatcher = commands.getDispatcher();
        dispatcher.register(Commands.literal("createPlayer")
                .then(Commands.argument("player", StringArgumentType.string())
                        .executes(ctx -> execute(ctx.getSource(), StringArgumentType.getString(ctx, "player")))
                        .then(Commands.argument("pos", Vec3Argument.vec3())
                                .executes(ctx -> execute(ctx.getSource(), StringArgumentType.getString(ctx, "player"), Vec3Argument.getVec3(ctx, "pos")))
                                .then(Commands.literal("in")
                                        .then(Commands.argument("in", DimensionArgument.dimension())
                                                .executes(ctx -> execute(ctx.getSource(),
                                                        StringArgumentType.getString(ctx, "player"),
                                                        Vec3Argument.getVec3(ctx, "pos"),
                                                        DimensionArgument.getDimension(ctx, "in")))))
                                .then(Commands.argument("yaw", DoubleArgumentType.doubleArg())
                                        .then(Commands.argument("pitch", DoubleArgumentType.doubleArg())
                                                .executes(ctx -> execute(ctx.getSource(), StringArgumentType.getString(ctx, "player"), Vec3Argument.getVec3(ctx, "pos"), DoubleArgumentType.getDouble(ctx, "yaw"), DoubleArgumentType.getDouble(ctx, "pitch")))
                                                .then(Commands.literal("in")
                                                        .then(Commands.argument("in", DimensionArgument.dimension())
                                                                .executes(ctx -> execute(ctx.getSource(),
                                                                        StringArgumentType.getString(ctx, "player"),
                                                                        Vec3Argument.getVec3(ctx, "pos"),
                                                                        DoubleArgumentType.getDouble(ctx, "yaw"),
                                                                        DoubleArgumentType.getDouble(ctx, "pitch"),
                                                                        DimensionArgument.getDimension(ctx, "in"))))))
                                )
                        )
                )
        );
    }

    private int execute(CommandSourceStack source, String playerName, Vec3 pos, double yaw, double pitch, ServerLevel in) {
        PlayerList playerList = source.getServer().getPlayerList();
        CommandSession session = new CommandSession(source);
        if (playerList.getPlayerByName(playerName) != null) {
            session.sendErrorMsg(MessageCode.INVALID_ARGUMENT, "玩家已存在！");
            return 0;
        }

        Map<String, Object> map = MapUtil.of(
                "player", playerName,
                "game_mode", getPlayer(source).isCreative() ? "creative" : "survival",
                "world", WorldUtil.getLevelName(in),
                "x", pos.x,
                "y", pos.y,
                "z", pos.z,
                "yaw", yaw,
                "pitch", pitch
        );

        CommandHandler createPlayer = CommandManage.getCommand(session.token(), "createPlayer");
        if (createPlayer == null) {
            session.sendErrorMsg(MessageCode.INVALID_ARGUMENT, "找不到命令！");
            return 0;
        }

        MoreInterfaces.MORE_INTERFACES.get().runThread(() -> createPlayer.onCommand(session, new CommandMap(map)));
        return 1;
    }

    private int execute(CommandSourceStack source, String player, Vec3 pos, ServerLevel in) {
        return execute(source, player, pos, getPlayer(source).yRot, getPlayer(source).xRot, in);
    }

    private int execute(CommandSourceStack source, String playerName, Vec3 pos, double yaw, double pitch) {
        return execute(source, playerName, pos, yaw, pitch, source.getLevel());
    }

    private int execute(CommandSourceStack source, String playerName, Vec3 pos) {
        return execute(source, playerName, pos, getPlayer(source).yRot, getPlayer(source).xRot, source.getLevel());
    }

    public int execute(CommandSourceStack source, String playerName) {
        return execute(source, playerName, getPlayer(source).position(), getPlayer(source).yRot,
                getPlayer(source).xRot, source.getLevel());
    }

    public ServerPlayer getPlayer(CommandSourceStack source){
        try {
            return source.getPlayerOrException();
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
