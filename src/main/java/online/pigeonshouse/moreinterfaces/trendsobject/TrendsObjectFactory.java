package online.pigeonshouse.moreinterfaces.trendsobject;

import online.pigeonshouse.moreinterfaces.MoreInterfaces;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings({"rawtypes", "unchecked"})
public class TrendsObjectFactory {
    private static final Logger log = LogManager.getLogger(MoreInterfaces.class);
    private static final Map<Class<?>, Class<? extends TrendsObject>> REGISTER = new ConcurrentHashMap<>();

    static {
        register(new BooleanTrendsObject());
        register(new DoubleTrendsObject());
        register(new IntegerTrendsObject());
        register(new StringTrendsObject());
    }

    public static void register(TrendsObject<?> trendsObject) {
        if (trendsObject.compareClass(Object.class) || trendsObject.equals(NullTrendsObject.NULL)){
            return;
        }
        REGISTER.put(trendsObject.getWrapperClass(), trendsObject.getClass());
    }

    public static <T> TrendsObject<T> buildObject(T defValue) {
        return (TrendsObject<T>) new ObjectTrendsObject(defValue);
    }

    public static <T> TrendsObject<T> build(T defValue) {
        if (defValue == null) return (TrendsObject<T>) NullTrendsObject.NULL;
        Class<? extends TrendsObject> aClass = REGISTER.get(defValue.getClass());
        if (aClass == null) return (TrendsObject<T>) new ObjectTrendsObject(defValue);
        return newInstance(aClass, defValue);
    }

    private static <T> TrendsObject<T> newInstance(Class<? extends TrendsObject> aClass, T defValue) {
        try {
            Constructor<? extends TrendsObject> constructor = aClass.getDeclaredConstructor(defValue.getClass());
            constructor.setAccessible(true);
            return (TrendsObject<T>) constructor.newInstance(defValue);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            log.error(e);
        } catch (NoSuchMethodException e) {
            log.error("TrendsObjectFactory: {} must have a constructor with one parameter", aClass, e);
            return (TrendsObject<T>) new ObjectTrendsObject(defValue);
        }
        return (TrendsObject<T>) NullTrendsObject.NULL;
    }
}
