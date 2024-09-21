package online.pigeonshouse.moreinterfaces.utils;

import java.util.Arrays;
import java.util.List;

public class ListUtil {
    // toList
    public static <T> List<T> of(T... items) {
        return Arrays.asList(items);
    }
}
