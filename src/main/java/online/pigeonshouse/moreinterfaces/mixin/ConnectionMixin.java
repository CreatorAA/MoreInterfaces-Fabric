package online.pigeonshouse.moreinterfaces.mixin;

import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import online.pigeonshouse.moreinterfaces.eventhandler.PlayerConnectionEventHandler;
import online.pigeonshouse.moreinterfaces.handlers.commands.PlayerControlCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Connection.class)
public class ConnectionMixin {
    @Shadow
    private volatile PacketListener packetListener;

    @Inject(method = "handleDisconnection", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketListener;onDisconnect(Lnet/minecraft/network/DisconnectionDetails;)V"))
    private void disconnectAddon(CallbackInfo ci) {
        if (packetListener instanceof ServerGamePacketListenerImpl gamePacketListener && PlayerConnectionEventHandler.INSTANCE != null) {
            ServerPlayer player = gamePacketListener.getPlayer();
            PlayerControlCommand.remove(player.getName().getString());

            if (PlayerConnectionEventHandler.INSTANCE != null)
                PlayerConnectionEventHandler.getInstance().onPlayDisconnect(player, player.getServer());
        }
    }
}
