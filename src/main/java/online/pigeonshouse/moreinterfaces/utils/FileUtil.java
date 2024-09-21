package online.pigeonshouse.moreinterfaces.utils;

import java.io.FileOutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtil {
    /**
     * 传入Path，返回Reader
     */
    public static Reader getReader(Path file) {
        if (!Files.exists(file) || Files.isDirectory(file)) return null;

        try {
            return Files.newBufferedReader(file);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static String getAbsolutePath(Path path, String fileName) {
        return path.resolve(fileName).toAbsolutePath().toString();
    }

    public static void notDir(Path path) {
        if (!Files.isDirectory(path)) {
            throw new RuntimeException("Not a directory: " + path);
        }
    }


    public static void notFile(Path path) {
        if (!Files.isRegularFile(path)) {
            throw new RuntimeException("Not a file: " + path);
        }
    }

    public static void writeString(Path configPath, String string) {
        if (!Files.exists(configPath)) {
            try {
                Files.createFile(configPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try(FileOutputStream fos = new FileOutputStream(configPath.toFile(), false)) {
            fos.write(string.getBytes(StandardCharsets.UTF_8));
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 连续创建文件夹
     */
    public static void createDir(Path path) {
        try {
            Files.createDirectories(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
