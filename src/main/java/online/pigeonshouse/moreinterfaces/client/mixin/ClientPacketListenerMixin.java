package online.pigeonshouse.moreinterfaces.client.mixin;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import online.pigeonshouse.moreinterfaces.client.ClientCommand;
import online.pigeonshouse.moreinterfaces.client.ClientSource;
import online.pigeonshouse.moreinterfaces.client.commands.SaveAllItemsCommand;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Shadow
    private CommandDispatcher<SharedSuggestionProvider> commands;

    @Shadow
    @Final
    private ClientSuggestionProvider suggestionsProvider;

    @Inject(method = "handleLogin", at = @At("RETURN"))
    private void joinGame(ClientboundLoginPacket client, CallbackInfo info) {
        final CommandDispatcher<ClientSource> dispatcher = new CommandDispatcher<>();
        ClientCommand.COMMAND_DISPATCHER.set(dispatcher);
        SaveAllItemsCommand.register(dispatcher);
    }

    @Inject(method = "handleCommands", at = @At("RETURN"))
    private void handleCommands(ClientboundCommandsPacket p, CallbackInfo info) {
        ClientCommand.addCommands((CommandDispatcher) commands, (ClientSource) suggestionsProvider);
    }

    @Inject(method = "sendUnsignedCommand", at = @At("HEAD"), cancellable = true)
    private void sendUnsignedCommand(String command, CallbackInfoReturnable<Boolean> cir) {
        if (ClientCommand.execute(command)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "sendCommand", at = @At("HEAD"), cancellable = true)
    private void sendCommand(String command, CallbackInfo info) {
        if (ClientCommand.execute(command)) {
            info.cancel();
        }
    }
}
