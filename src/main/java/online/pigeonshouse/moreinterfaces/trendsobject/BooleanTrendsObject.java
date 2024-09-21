package online.pigeonshouse.moreinterfaces.trendsobject;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
public class BooleanTrendsObject extends TrendsObject<Boolean>{
    public BooleanTrendsObject() {
        this(false);
    }

    public BooleanTrendsObject(Boolean value) {
        super(value);
    }

    @Override
    public Class<Boolean> getWrapperClass() {
        return Boolean.class;
    }

    @Override
    public String toString() {
        return "BooleanTrendsObject{" +
                "value=" + get() +
                '}';
    }
}
