package online.pigeonshouse.moreinterfaces.trendsobject;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
public class DoubleTrendsObject extends TrendsObject<Double>{
    public DoubleTrendsObject() {
        this(0.0);
    }

    public DoubleTrendsObject(Double value) {
        super(value);
    }

    @Override
    public Class<Double> getWrapperClass() {
        return Double.class;
    }

    @Override
    public String toString() {
        return "DoubleTrendsObject{" +
                "value=" + get() +
                '}';
    }
}
