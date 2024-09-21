package online.pigeonshouse.moreinterfaces.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.BuiltInExceptionProvider;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import online.pigeonshouse.moreinterfaces.trendsobject.TrendsObject;
import online.pigeonshouse.moreinterfaces.trendsobject.TrendsObjectFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class ClientCommand {
    public static final TrendsObject<CommandDispatcher<ClientSource>> COMMAND_DISPATCHER = TrendsObjectFactory
            .buildObject(null);
    private static final Logger log = LogManager.getLogger(MoreInterfacesClient.class);

    public static boolean execute(String command) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getProfiler().push(command);
        ClientSource commandSource = (ClientSource) minecraft.getConnection().getSuggestionsProvider();

        try {
            COMMAND_DISPATCHER.get().execute(command, commandSource);
            return true;
        } catch (CommandSyntaxException e) {
            boolean ignored = isIgnoredException(e.getType());

            if (ignored) {
                log.debug("Syntax exception for client-sided command '{}'", command, e);
                return false;
            }

            log.warn("Syntax exception for client-sided command '{}'", command, e);
            commandSource.sendError(getErrorMessage(e));
            return true;
        } catch (Exception e) {
            log.warn("Error while executing client-sided command '{}'", command, e);
            commandSource.sendError(Component.literal(e.getMessage()));
            return true;
        } finally {
            minecraft.getProfiler().pop();
        }
    }

    private static Component getErrorMessage(CommandSyntaxException e) {
        Component message = ComponentUtils.fromMessage(e.getRawMessage());
        String context = e.getContext();

        return context != null ? Component.translatable("command.context.parse_error", message, e.getCursor(), context) : message;
    }

    private static boolean isIgnoredException(CommandExceptionType type) {
        BuiltInExceptionProvider builtins = CommandSyntaxException.BUILT_IN_EXCEPTIONS;
        return type == builtins.dispatcherUnknownCommand() || type == builtins.dispatcherParseException();
    }

    public static void addCommands(CommandDispatcher<ClientSource> target, ClientSource source) {
        Map<CommandNode<ClientSource>, CommandNode<ClientSource>> originalToCopy = new HashMap<>();
        CommandDispatcher<ClientSource> dispatcher = COMMAND_DISPATCHER.get();
        originalToCopy.put(dispatcher.getRoot(), target.getRoot());
        copyChildren(dispatcher.getRoot(), target.getRoot(), source, originalToCopy);
    }

    private static void copyChildren(
            CommandNode<ClientSource> origin,
            CommandNode<ClientSource> target,
            ClientSource source,
            Map<CommandNode<ClientSource>, CommandNode<ClientSource>> originalToCopy
    ) {
        for (CommandNode<ClientSource> child : origin.getChildren()) {
            if (!child.canUse(source)) continue;

            ArgumentBuilder<ClientSource, ?> builder = child.createBuilder();
            builder.requires(s -> true);

            if (builder.getCommand() != null) {
                builder.executes(context -> 0);
            }

            if (builder.getRedirect() != null) {
                builder.redirect(originalToCopy.get(builder.getRedirect()));
            }

            CommandNode<ClientSource> result = builder.build();
            originalToCopy.put(child, result);
            target.addChild(result);

            if (!child.getChildren().isEmpty()) {
                copyChildren(child, result, source, originalToCopy);
            }
        }
    }
}
