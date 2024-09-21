package online.pigeonshouse.moreinterfaces.commands;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands;
import online.pigeonshouse.moreinterfaces.trendsobject.TrendsObject;
import online.pigeonshouse.moreinterfaces.trendsobject.TrendsObjectFactory;

import java.util.ArrayList;
import java.util.List;

public class MICommands {
    public static final TrendsObject<Commands> COMMANDS = TrendsObjectFactory.buildObject(null);

    public static final List<MICommand> COMMANDS_LIST = new ArrayList<>();

    static {
        COMMANDS_LIST.add(new CreatePlayerCommand());
        COMMANDS_LIST.add(new PlayerControlCommand());
        COMMANDS_LIST.add(new TestCommand());
        COMMANDS_LIST.add(new MoreInterfacesCommand());
    }

    public static void initCommand(Commands commands, Commands.CommandSelection commandSelection,
                                   CommandBuildContext commandBuildContext) {
        if (COMMANDS.isEmpty()) {
            COMMANDS.set(commands);
        }

        COMMANDS_LIST.forEach(command -> command.register(commands, commandSelection, commandBuildContext));
    }

    public interface MICommand {
        void register(Commands commands, Commands.CommandSelection selection, CommandBuildContext context);
    }
}
