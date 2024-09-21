package online.pigeonshouse.moreinterfaces.trendsobject;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
public class StringTrendsObject extends TrendsObject<String> {
    public StringTrendsObject() {
        this("");
    }

    public StringTrendsObject(String value) {
        super(value);
    }

    @Override
    public Class<String> getWrapperClass() {
        return String.class;
    }

    @Override
    public String toString() {
        return "StringTrendsObject{" +
                "value='" + get() + '\'' +
                '}';
    }
}
