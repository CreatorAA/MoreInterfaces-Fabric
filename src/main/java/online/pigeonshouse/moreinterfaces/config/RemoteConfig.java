package online.pigeonshouse.moreinterfaces.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;
import net.fabricmc.loader.api.FabricLoader;
import online.pigeonshouse.moreinterfaces.MoreInterfaces;
import online.pigeonshouse.moreinterfaces.netty.SerializeFactory;
import online.pigeonshouse.moreinterfaces.trendsobject.TrendsObject;
import online.pigeonshouse.moreinterfaces.trendsobject.TrendsObjectFactory;
import online.pigeonshouse.moreinterfaces.utils.FileUtil;
import online.pigeonshouse.moreinterfaces.utils.GsonUtil;
import online.pigeonshouse.moreinterfaces.utils.StringUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class RemoteConfig {
    public static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir()
            .resolve("moreinterfaces.json");

    private static final Logger log = LogManager.getLogger(MoreInterfaces.class);

    public final TrendsObject<Boolean> enable = TrendsObjectFactory.build(false);
    public final TrendsObject<Integer> port = TrendsObjectFactory.build(6666);

    public final TrendsObject<HashSet<RemoteToken>> tokens =
            TrendsObjectFactory.build(new HashSet<>());

    public final TrendsObject<HashSet<RemotePower>> powers =
            TrendsObjectFactory.build(new HashSet<>(List.of(RemotePower.DEFAULT)));

    public Integer getPort() {
        return port.get();
    }

    public Boolean getEnable() {
        return enable.get();
    }

    public HashSet<RemoteToken> getTokens() {
        return tokens.get();
    }

    public HashSet<RemotePower> getPowers() {
        return powers.get();
    }

    public RemoteToken getRootToken(String dif) {
        for (RemoteToken remoteToken : tokens.get()) {
            if (remoteToken.getPower() == 0) {
                return remoteToken;
            }
        }

        return new RemoteToken(dif, 0);
    }

    public RemotePower getRootPower() {
        for (RemotePower remotePower : powers.get()) {
            if (remotePower.getPower() == 0) {
                return remotePower;
            }
        }

        return RemotePower.DEFAULT;
    }

    public static Gson getGson() {
        return GsonUtil.FORMATTER_GSON;
    }


    public RemoteConfig load() throws Exception {
        if (!Files.exists(CONFIG_PATH)) {
            log.warn("Config file not found, creating default config file.");
            RemoteToken remoteToken = new RemoteToken(StringUtil.generateKey(24), 0);
            tokens.get().add(remoteToken);

            save();
            return this;
        }

        log.info("Loading config file[{}]", CONFIG_PATH);
        Config config = getGson().fromJson(Objects.requireNonNull(FileUtil.getReader(CONFIG_PATH)),
                Config.class);

        if (config != null) {
            enable.set(config.enable);
            port.set(config.port);
            tokens.set(config.tokens);
            if (!config.powers.isEmpty()) {
                powers.set(config.powers);
            }

            SerializeFactory.AES_KEY.set(config.aesKey);
        }

        return this;
    }

    public RemoteConfig save() {
        Config config = new Config();
        config.enable = enable.get();
        config.port = port.get();
        config.tokens = tokens.get();
        config.powers = powers.get();
        config.aesKey = SerializeFactory.AES_KEY.get();

        FileUtil.writeString(CONFIG_PATH, getGson().toJson(config));
        log.info("Save config file[{}]", CONFIG_PATH);
        return this;
    }

    @Data
    public static class Config {
        public static final RemoteConfig INSTANCE = new RemoteConfig();

        Boolean enable;
        Integer port;
        String aesKey;
        HashSet<RemoteToken> tokens;
        HashSet<RemotePower> powers;

        public boolean isEmpty() {
            return enable == null && port == null;
        }
    }
}
