package online.pigeonshouse.moreinterfaces.gui;

import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import online.pigeonshouse.moreinterfaces.eventhandler.PlayerEventHandler;
import online.pigeonshouse.moreinterfaces.handlers.MIServerPlayer;
import online.pigeonshouse.moreinterfaces.utils.ComponentUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Getter
public class MIServerPlayerBackPack extends SimpleContainer {
    private static final Logger log = LogManager.getLogger(MIServerPlayerBackPack.class);

    public static void openMIPlayerMenu(MIServerPlayer player, ServerPlayer openFor) {
        if (player.hasOpenBackpack()) return;

        MIServerPlayerBackPack container = new MIServerPlayerBackPack(player, openFor, 45);
        Inventory inventory = player.getInventory();

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            container.set(i, inventory.getItem(i));
        }

        SimpleMenuProvider provider = new SimpleMenuProvider((p_53124_, p_53125_, p_53126_) ->
                new PlayerBackPackMenu(p_53124_, p_53125_, container),
                ComponentUtil.literal("假人背包，但是不要一键整理！！！"));

        PlayerEventHandler.MIPlayerItem.EventHandler pickup = (player1) -> {
            if (player1 instanceof MIServerPlayer && player.equals(player1)) {
                container.update();
            }
        };

        PlayerEventHandler.PlayerContainer.eventHandlers.add(new PlayerEventHandler.PlayerContainer.EventHandler() {
            @Override
            public void onPlayerCloseContainer(ServerPlayer serverPlayer, AbstractContainerMenu menu) {
                if (openFor.equals(serverPlayer) && menu instanceof PlayerBackPackMenu) {
                    PlayerEventHandler.PlayerContainer.eventHandlers.remove(this);
                    PlayerEventHandler.MIPlayerItem.eventHandlers.remove(pickup);
                    player.setBackpack(null);
                }
            }
        });

        PlayerEventHandler.MIPlayerItem.eventHandlers.add(pickup);
        openFor.openMenu(provider);
    }

    final MIServerPlayer player;
    final ServerPlayer openFor;

    public MIServerPlayerBackPack(MIServerPlayer player, ServerPlayer openFor, int p_19150_) {
        super(p_19150_);
        this.player = player;
        this.openFor = openFor;
        player.setBackpack(this);
    }

    /**
     * 同步玩家背包
     */
    public void update() {
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            setItem(i, inventory.getItem(i));
        }
    }


    public void set(int p_19162_, ItemStack p_19163_) {
        super.setItem(p_19162_, p_19163_);
    }

    @Override
    public void setItem(int p_19162_, ItemStack p_19163_) {
        super.setItem(p_19162_, p_19163_);
        player.getInventory().setItem(p_19162_, p_19163_);
    }

    public static class PlayerBackPackMenu extends ChestMenu {
        public PlayerBackPackMenu(int p_39238_, Inventory p_39239_, Container p_39240_) {
            super(MenuType.GENERIC_9x5, p_39238_, p_39239_, p_39240_, 5);
        }

        @Override
        public void clicked(int index, int p_150401_, ClickType type, Player player) {
            if (index > 40 && index < 45) {
                return;
            }
            super.clicked(index, p_150401_, type, player);
        }
    }
}
