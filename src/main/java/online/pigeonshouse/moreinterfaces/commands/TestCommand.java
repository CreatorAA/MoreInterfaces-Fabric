package online.pigeonshouse.moreinterfaces.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import online.pigeonshouse.moreinterfaces.commands.MICommands.MICommand;

public class TestCommand implements MICommand {
    @Override
    public void register(Commands commands, Commands.CommandSelection selection, CommandBuildContext context) {
        CommandDispatcher<CommandSourceStack> dispatcher = commands.getDispatcher();
    }
}
