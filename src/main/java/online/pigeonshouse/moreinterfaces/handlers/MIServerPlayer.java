package online.pigeonshouse.moreinterfaces.handlers;

import com.mojang.authlib.GameProfile;
import io.netty.channel.Channel;
import io.netty.channel.embedded.EmbeddedChannel;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import online.pigeonshouse.moreinterfaces.eventhandler.PlayerConnectionEventHandler;
import online.pigeonshouse.moreinterfaces.eventhandler.PlayerEventHandler;
import online.pigeonshouse.moreinterfaces.gui.MIServerPlayerBackPack;
import online.pigeonshouse.moreinterfaces.handlers.commands.PlayerControlCommand;
import online.pigeonshouse.moreinterfaces.utils.ComponentUtil;
import online.pigeonshouse.moreinterfaces.utils.MIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * 此代码借鉴于：
 * <a href="https://github.com/gnembon/fabric-carpet/blob/1.20.6">Carpet</a>
 */
@Getter
@Setter
public class MIServerPlayer extends ServerPlayer {
    public boolean isAShadow;
    MIServerPlayerBackPack backpack = null;
    public Runnable resetSpawnPosition = () -> {};

    public static MIPlayer createFakePlayer(String name,
                                            MinecraftServer server, ServerLevel world,
                                            GameType gamemode, boolean flying,
                                            double x, double y, double z, double yaw, double pitch) {
        GameProfileCache.setUsesAuthentication(false);
        GameProfile gameprofile;
        try {
            gameprofile = server.getProfileCache().get(name).orElse(null);
        } finally {
            GameProfileCache.setUsesAuthentication(server.isDedicatedServer() && server.usesAuthentication());
        }

        if (gameprofile == null) {
            gameprofile = new GameProfile(UUIDUtil.createOfflinePlayerUUID(name), name);
        }

        if (gamemode == GameType.SPECTATOR) {
            flying = true;
        } else if (gamemode.isSurvival()) {
            flying = false;
        }

        Optional<GameProfile> p = SkullBlockEntity.fetchGameProfile(name).join();

        if (p.isPresent()) {
            gameprofile = p.get();
        }

        MIServerPlayer instance = new MIServerPlayer(server, world, gameprofile, ClientInformation.createDefault(), false);
        instance.resetSpawnPosition = () -> instance.moveTo(x, y, z, (float) yaw, (float) pitch);
        MIClientConnection clientConnection = new MIClientConnection(PacketFlow.SERVERBOUND);
        ClientInformation clientInformation = instance.clientInformation();
        CommonListenerCookie listenerCookie = new CommonListenerCookie(gameprofile, 0, clientInformation);
        server.getPlayerList().placeNewPlayer(clientConnection, instance, listenerCookie);
        instance.setHealth(20.0F);
        instance.getFoodData().setSaturation(10.0F);
        instance.unsetRemoved();
        instance.setMaxUpStep(0.6F);
        instance.gameMode.changeGameModeForPlayer(gamemode);
        server.getPlayerList().broadcastAll(new ClientboundRotateHeadPacket(instance, (byte) (instance.yHeadRot * 256 / 360)), world.dimension());
        server.getPlayerList().broadcastAll(new ClientboundTeleportEntityPacket(instance), world.dimension());
        instance.entityData.set(DATA_PLAYER_MODE_CUSTOMISATION, (byte) 0x7f);
        instance.getAbilities().flying = flying;

        instance.teleportTo(world, x, y, z, (float) yaw, (float) pitch);
        return MIUtil.buildMIPlayer(instance);
    }

    public MIServerPlayer(MinecraftServer server, ServerLevel worldIn, GameProfile profile, ClientInformation clientInformation, boolean shadow) {
        super(server, worldIn, profile, clientInformation);
        isAShadow = shadow;
    }

    @Override
    public void onEquipItem(EquipmentSlot equipmentSlot, ItemStack itemStack, ItemStack itemStack2) {
        if (!isUsingItem())
            super.onEquipItem(equipmentSlot, itemStack, itemStack2);
    }

    @Override
    public void kill() {
        kill(ComponentUtil.literal("Killed"));
    }

    public void kill(Component reason) {
        shakeOff();

        if (reason.getContents() instanceof TranslatableContents && ((TranslatableContents) reason.getContents()).getKey().equals("multiplayer.disconnect.duplicate_login")) {
            this.connection.onDisconnect(reason);
        } else {
            this.server.tell(new TickTask(this.server.getTickCount(), () -> {
                this.connection.onDisconnect(reason);
            }));
        }

        if (hasOpenBackpack()) {
            backpack.getOpenFor().closeContainer();
        }

        PlayerControlCommand.remove(getDisplayName().getString());

        if (PlayerConnectionEventHandler.INSTANCE != null) {
            PlayerConnectionEventHandler.getInstance().onPlayDisconnect(this, getServer());
        }
    }

    @Override
    public void tick() {
        if (this.getServer().getTickCount() % 10 == 0) {
            this.connection.resetPosition();
            this.serverLevel().getChunkSource().move(this);
        }
        try {
            super.tick();
            this.doTick();
        } catch (NullPointerException ignored) {

        }
    }

    private void shakeOff() {
        if (getVehicle() instanceof Player) stopRiding();

        for (Entity passenger : getIndirectPassengers()) {
            if (passenger instanceof Player) passenger.stopRiding();
        }
    }

    @Override
    public void die(DamageSource cause) {
        shakeOff();
        super.die(cause);
        setHealth(20);
        this.foodData = new FoodData();
        kill(this.getCombatTracker().getDeathMessage());
    }

    @Override
    public @NotNull String getIpAddress() {
        return "127.0.0.1";
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
        doCheckFallDamage(0.0, y, 0.0, onGround);
    }

    @Nullable
    @Override
    public Entity changeDimension(ServerLevel level) {
        super.changeDimension(level);
        if (wonGame) {
            ServerboundClientCommandPacket p = new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.PERFORM_RESPAWN);
            connection.handleClientCommand(p);
        }

        if (connection.player.isChangingDimension()) {
            connection.player.hasChangedDimension();
        }
        return connection.player;
    }

    public static final List<StatType<?>> STATS = List.of(
            Stats.ITEM_CRAFTED,
            Stats.ITEM_BROKEN,
            Stats.ITEM_USED,
            Stats.ITEM_CRAFTED
    );

    @Override
    public void awardStat(@NotNull Stat<?> stat, int p_9027_) {
        if (STATS.contains(stat.getType())) {
            for (PlayerEventHandler.MIPlayerItem.EventHandler handler : PlayerEventHandler.MIPlayerItem.eventHandlers) {
                handler.onPlayerItem(this);
            }
        }

        super.awardStat(stat, p_9027_);
    }

    public boolean hasOpenBackpack() {
        return backpack != null;
    }

    public static class MIClientConnection extends Connection {
        public MIClientConnection(PacketFlow packetFlow) {
            super(packetFlow);
            ((ClientConnectionInterface) this).setChannel(new EmbeddedChannel());
        }

        @Override
        public void setReadOnly() {
        }

        @Override
        public void handleDisconnection() {
        }

        @Override
        public void setListener(PacketListener packetListener) {

        }
    }

    public interface ClientConnectionInterface {
        void setChannel(Channel channel);
    }
}
