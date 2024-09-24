package online.pigeonshouse.moreinterfaces.trendsobject;

import lombok.EqualsAndHashCode;

@SuppressWarnings({"unused"})
@EqualsAndHashCode(callSuper = false)
public class ObjectTrendsObject<T> extends TrendsObject<T> {
    public ObjectTrendsObject() {
        this(null);
    }

    public ObjectTrendsObject(T value) {
        super(value);
    }

    @Override
    public Class getWrapperClass() {
        return Object.class;
    }

    @Override
    public String toString() {
        return "ObjectTrendsObject{" +
                "value=" + get() +
                '}';
    }
}
