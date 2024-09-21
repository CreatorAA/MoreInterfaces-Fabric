package online.pigeonshouse.moreinterfaces.trendsobject;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
public class IntegerTrendsObject extends TrendsObject<Integer>{

    public IntegerTrendsObject() {
        this(0);
    }

    public IntegerTrendsObject(Integer value) {
        super(value);
    }

    @Override
    public Class<Integer> getWrapperClass() {
        return Integer.class;
    }

    @Override
    public String toString() {
        return "IntegerTrendsObject{" +
                "value=" + get() +
                '}';
    }
}
