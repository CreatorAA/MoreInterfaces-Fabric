package online.pigeonshouse.moreinterfaces.trendsobject;

import lombok.Setter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

@SuppressWarnings({"unused", "unchecked"})
public abstract class TrendsObject<T> {
    private transient final List<TrendsObjectEvent<T>> LISTENERS = new CopyOnWriteArrayList<>();
    // 一个用于强制刷新储存值的回调函数，要求返回T类型的值
    @Setter
    private Function<T, T> refreshFunction;

    private T value;

    public TrendsObject(T value){
        this.value = value;
    }

    public T get() {
        return value;
    }

    public <R>R get(Class<R> clazz) {
        if (compareClass(clazz) || getWrapperClass().isAssignableFrom(clazz)) {
            return (R) value;
        }
        throw new RuntimeException();
    }

    public void set(T value) {
        T oldValue = this.value;
        synchronized(this) {
            this.value = value;
        }
        set(oldValue, this.value);
    }

    public void refresh() {
        if (refreshFunction != null) {
            synchronized (this) {
                this.value = refreshFunction.apply(this.value);
            }
        }
    }

    public boolean isEmpty() {
        return value == null || this == NullTrendsObject.NULL;
    }

    public abstract Class<T> getWrapperClass();

    public boolean compareClass(Class<?> clazz) {
        return getWrapperClass() == clazz;
    }
    public boolean compare(Object value) {
        return compareClass(value.getClass());
    }

    public void addListener(TrendsObjectEvent<T> listener) {
        LISTENERS.add(listener);
    }

    public void removeListener(TrendsObjectEvent<T> listener) {
        LISTENERS.remove(listener);
    }

    protected void set(T oldValue, T newValue) {
        for (TrendsObjectEvent<T> listener : LISTENERS) {
            listener.onSet(oldValue, newValue);
        }
    }

    public interface TrendsObjectEvent<T> {
        void onSet(T oldValue, T newValue);
    }
}
