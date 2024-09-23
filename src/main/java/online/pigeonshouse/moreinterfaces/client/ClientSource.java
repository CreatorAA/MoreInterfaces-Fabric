package online.pigeonshouse.moreinterfaces.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.commands.CommandSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

public interface ClientSource extends CommandSource {
    Minecraft getClient();

    AbstractClientPlayer getPlayer();

    default Entity getEntity() {
        return getPlayer();
    }

    ClientLevel getWorld();

    void sendError(Component errorMessage);
}
