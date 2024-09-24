package online.pigeonshouse.moreinterfaces.utils;

import java.nio.file.FileSystems;
import java.nio.file.Path;

public class PathUtil {
    public static Path of(String path) {
        return FileSystems.getDefault().getPath(path);
    }
}
