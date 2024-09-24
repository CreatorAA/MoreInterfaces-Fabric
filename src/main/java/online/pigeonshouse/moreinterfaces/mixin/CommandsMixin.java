package online.pigeonshouse.moreinterfaces.mixin;
import net.minecraft.commands.Commands;
import online.pigeonshouse.moreinterfaces.commands.MICommands;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Commands.class)
public class CommandsMixin {
    @Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/CommandDispatcher;setConsumer(Lcom/mojang/brigadier/ResultConsumer;)V"), method = "<init>")
    private void initCommand(Commands.CommandSelection commandSelection, CallbackInfo info) {
        Commands commands = (Commands) (Object) this;
        MICommands.initCommand(commands, commandSelection);
    }
}
