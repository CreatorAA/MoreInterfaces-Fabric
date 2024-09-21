package online.pigeonshouse.moreinterfaces.trendsobject;

@SuppressWarnings({"rawtypes"})
public class NullTrendsObject extends TrendsObject<Object> {
    public static final NullTrendsObject NULL = new NullTrendsObject();

    private NullTrendsObject() {
        super(null);
    }

    @Override
    public void set(Object value) {
        throw new UnsupportedOperationException("NullTrendsObject can not be set");
    }

    @Override
    public Object get() {
        return null;
    }

    @Override
    public void refresh() {

    }



    @Override
    public <R> R get(Class<R> clazz) {
        return null;
    }

    @Override
    public Class<Object> getWrapperClass() {
        return null;
    }
}
