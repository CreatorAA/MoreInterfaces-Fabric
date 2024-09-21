package online.pigeonshouse.moreinterfaces.mixin;

import net.minecraft.server.MinecraftServer;
import online.pigeonshouse.moreinterfaces.eventhandler.ServerLifecycleEventHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(value = MinecraftServer.class)
public class MinecraftServerMixin {
    @Inject(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;buildServerStatus()Lnet/minecraft/network/protocol/status/ServerStatus;", ordinal = 0))
    private void afterSetupServer(CallbackInfo info) {
        MinecraftServer server = (MinecraftServer) (Object) this;
        ServerLifecycleEventHandler.INSTANCE.onServerStarted(server);
    }

    @Inject(method = "stopServer", at = @At("HEAD"))
    private void beforeShutdownServer(CallbackInfo info) {
        ServerLifecycleEventHandler.INSTANCE.onServerStopping();
    }

    @Inject(method = "tickServer", at = @At("TAIL"))
    private void afterServerTick(BooleanSupplier booleanSupplier, CallbackInfo info) {
        ServerLifecycleEventHandler.INSTANCE.onServerTickEnd();
    }
}
