package online.pigeonshouse.moreinterfaces.mixin;

import io.netty.channel.Channel;
import net.minecraft.network.Connection;
import online.pigeonshouse.moreinterfaces.handlers.MIServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Connection.class)
public abstract class MIClientConnection implements MIServerPlayer.ClientConnectionInterface {
    @Override
    @Accessor
    public abstract void setChannel(Channel channel);
}
