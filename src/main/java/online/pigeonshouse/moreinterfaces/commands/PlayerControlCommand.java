package online.pigeonshouse.moreinterfaces.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import online.pigeonshouse.moreinterfaces.MoreInterfaces;
import online.pigeonshouse.moreinterfaces.netty.command.CommandHandler;
import online.pigeonshouse.moreinterfaces.netty.command.CommandManage;
import online.pigeonshouse.moreinterfaces.netty.command.CommandMap;

import java.util.HashMap;
import java.util.Map;

public class PlayerControlCommand implements MICommands.MICommand {
    @Override
    public void register(Commands commands, Commands.CommandSelection selection, CommandBuildContext context) {
        CommandDispatcher<CommandSourceStack> dispatcher = commands.getDispatcher();
        dispatcher.register(Commands.literal("playerControl")
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.literal("stop")
                                .executes(source -> executeStop(source.getSource(), EntityArgument.getPlayer(source, "player"))))
                        .then(Commands.literal("kill")
                                .executes(source -> executeKill(source.getSource(), EntityArgument.getPlayer(source, "player"))))
                        .then(literal("use"))
                        .then(literal("attack"))
                        .then(literal("jump"))
                )
        );
    }


    private static LiteralArgumentBuilder<CommandSourceStack> literal(String name) {
        return Commands.literal(name)
                .executes(s -> execute(s.getSource(), EntityArgument.getPlayer(s, "player"), name, "once", 0, 0))
                .then(Commands.literal("continue")
                        .executes(s -> execute(s.getSource(), EntityArgument.getPlayer(s, "player"), name, "continue", 0, 0))
                        .then(Commands.literal("count")
                                .then(Commands.argument("count", IntegerArgumentType.integer())
                                        .executes(s -> execute(s.getSource(), EntityArgument.getPlayer(s, "player"), name, "continue", IntegerArgumentType.getInteger(s, "count"), 0))
                                        .then(Commands.literal("delay")
                                                .then(Commands.argument("delay", IntegerArgumentType.integer())
                                                        .executes(s -> execute(s.getSource(), EntityArgument.getPlayer(s, "player"), name, "continue", IntegerArgumentType.getInteger(s, "count"), IntegerArgumentType.getInteger(s, "delay")))
                                                )
                                        )
                                )
                        )
                        .then(Commands.literal("delay")
                                .then(Commands.argument("delay", IntegerArgumentType.integer())
                                        .executes(s -> execute(s.getSource(), EntityArgument.getPlayer(s, "player"), name, "continue", 0, IntegerArgumentType.getInteger(s, "delay")))
                                )
                        )
                )
                .then(Commands.literal("interval")
                        .executes(s -> execute(s.getSource(), EntityArgument.getPlayer(s, "player"), name, "interval", 0, 0))
                        .then(Commands.literal("count")
                                .then(Commands.argument("count", IntegerArgumentType.integer())
                                        .executes(s -> execute(s.getSource(), EntityArgument.getPlayer(s, "player"), name, "interval", IntegerArgumentType.getInteger(s, "count"), 0))
                                        .then(Commands.literal("delay")
                                                .then(Commands.argument("delay", IntegerArgumentType.integer())
                                                        .executes(s -> execute(s.getSource(), EntityArgument.getPlayer(s, "player"), name, "interval", IntegerArgumentType.getInteger(s, "count"), IntegerArgumentType.getInteger(s, "delay")))
                                                )
                                        )
                                )
                        )
                        .then(Commands.literal("delay")
                                .then(Commands.argument("delay", IntegerArgumentType.integer())
                                        .executes(s -> execute(s.getSource(), EntityArgument.getPlayer(s, "player"), name, "interval", 0, IntegerArgumentType.getInteger(s, "delay")))
                                )
                        )
                );
    }

    private static int executeStop(CommandSourceStack source, ServerPlayer player) {
        return execute(source, player, "stop", "once", 0, 0);
    }

    private static int executeKill(CommandSourceStack source, ServerPlayer player) {
        return execute(source, player, "kill", "once", 0, 0);
    }

    private static int execute(CommandSourceStack source, ServerPlayer player, String action, String type, int count, int delay) {
        CommandSession session = new CommandSession(source);
        Map<String, Object> data = new HashMap<>();
        data.put("player", player.getDisplayName().getString());
        data.put("isDummy", false);
        data.put("actionType", action);
        data.put("executeType", type);

        if (delay > 0 || delay == -1) {
            data.put("tick", delay);
        }

        if (count > 0 || count == -1) {
            data.put("count", count);
        }

        CommandHandler controlPlayer = CommandManage.getCommand(session.token(), "controlPlayer");

        MoreInterfaces.MORE_INTERFACES.get().runThread(() -> controlPlayer.onCommand(session, new CommandMap(data)));

        return 1;
    }
}
