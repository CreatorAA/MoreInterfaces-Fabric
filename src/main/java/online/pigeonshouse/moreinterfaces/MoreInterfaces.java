package online.pigeonshouse.moreinterfaces;

import net.fabricmc.api.ModInitializer;
import net.minecraft.server.MinecraftServer;
import online.pigeonshouse.moreinterfaces.config.RemoteConfig;
import online.pigeonshouse.moreinterfaces.netty.MoreInterfacesNetty;
import online.pigeonshouse.moreinterfaces.netty.command.CommandManage;
import online.pigeonshouse.moreinterfaces.trendsobject.TrendsObject;
import online.pigeonshouse.moreinterfaces.trendsobject.TrendsObjectFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MoreInterfaces implements ModInitializer {
    private static final Logger log = LogManager.getLogger(MoreInterfaces.class);
    public static final String MOD_ID = "moreinterfaces";
    public static final TrendsObject<MinecraftServer> MINECRAFT_SERVER = TrendsObjectFactory.buildObject(null);
    public static final TrendsObject<MoreInterfaces> MORE_INTERFACES = TrendsObjectFactory.buildObject(null);
    public static final String VERSION = "1.0.2";

    private MoreInterfacesNetty NETTY;
    private RemoteConfig config;
    public ExecutorService EXECUTOR_SERVICE;

    @Override
    public void onInitialize() {
        MORE_INTERFACES.set(this);

        try {
            config = RemoteConfig.Config.INSTANCE
                    .load();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        CommandManage.init();

        if (!config.getEnable()) {
            return;
        }

        NETTY = new MoreInterfacesNetty(config);
        log.info("More Interfaces version: {}", VERSION);
    }

    public void onServerStarted(MinecraftServer server) {
        MINECRAFT_SERVER.set(server);
        if (NETTY == null) return;

        int availabled = Runtime.getRuntime().availableProcessors();
        EXECUTOR_SERVICE = new ThreadPoolExecutor(
                availabled, availabled * 2,
                60, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(availabled * 2)
        );

        NETTY.start();
        log.info("More Interfaces Netty started! Remote port: {}", config.getPort());
    }

    public void onServerStopping() {
        MINECRAFT_SERVER.set(null);
        if (NETTY == null) return;

        EXECUTOR_SERVICE.shutdownNow();
        EXECUTOR_SERVICE = null;
        NETTY.close();
        config.save();
        log.info("More Interfaces Netty stopped!");
    }

    public void runThread(Runnable runnable) {
        if (EXECUTOR_SERVICE == null || EXECUTOR_SERVICE.isShutdown()) {
            new Thread(runnable).start();
        }else {
            EXECUTOR_SERVICE.execute(runnable);
        }
    }
}
