package online.pigeonshouse.moreinterfaces.utils;

import online.pigeonshouse.moreinterfaces.config.RemoteConfig;
import online.pigeonshouse.moreinterfaces.config.RemotePower;
import online.pigeonshouse.moreinterfaces.config.RemoteToken;

import java.util.Objects;

public class PowerUtil {

    public static RemotePower getPower(String token) {
        return getPower(getToken(token));
    }

    public static RemotePower getPower(RemoteToken token) {
        if (!Objects.isNull(token))
            return getPower(token.getPower());

        return null;
    }

    public static RemotePower getPower(int power) {
        for (RemotePower remotePower : RemoteConfig.Config.INSTANCE.getPowers()) {
            if (remotePower.getPower() == power)
                return remotePower;
        }
        return null;
    }


    public static RemoteToken getToken(String token) {
        for (RemoteToken remoteToken : RemoteConfig.Config.INSTANCE.getTokens())
            if (remoteToken.getToken().equals(token))
                return remoteToken;

        return null;
    }
}
