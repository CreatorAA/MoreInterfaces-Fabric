package online.pigeonshouse.moreinterfaces.netty.message;

public interface SessionMessage {
    void setSessionId(String sessionId);
    String getSessionId();
}