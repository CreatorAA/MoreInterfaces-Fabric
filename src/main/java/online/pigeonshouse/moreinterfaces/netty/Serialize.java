package online.pigeonshouse.moreinterfaces.netty;

public interface Serialize {
    <T>T deserialize(Class<T> clazz, byte[] bytes);
    byte[] serialize(Object o);
}