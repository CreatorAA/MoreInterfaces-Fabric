package online.pigeonshouse.moreinterfaces.mixin;

import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import online.pigeonshouse.moreinterfaces.eventhandler.PlayerConnectionEventHandler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Connection.class)
public abstract class ConnectionMixin {
    @Shadow
    private volatile PacketListener packetListener;

    @Shadow @Nullable
    public abstract Component getDisconnectedReason();

    @Shadow public abstract PacketListener getPacketListener();

    @Inject(method = "handleDisconnection", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;getDisconnectedReason()Lnet/minecraft/network/chat/Component;", ordinal = 0))
    private void disconnectAddon(CallbackInfo ci) {
        if (getDisconnectedReason() == null && getPacketListener() == null) return;

        if (packetListener instanceof ServerGamePacketListenerImpl && PlayerConnectionEventHandler.INSTANCE != null) {
            ServerGamePacketListenerImpl gamePacketListener = (ServerGamePacketListenerImpl) packetListener;
            ServerPlayer player = gamePacketListener.player;
            PlayerConnectionEventHandler.getInstance().onPlayDisconnect(player, player.getServer());
        }
    }
}
