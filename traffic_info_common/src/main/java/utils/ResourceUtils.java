package utils;

import org.apache.log4j.Logger;

import java.io.InputStream;
import java.net.URL;

/**
 * Created by Anna Kuranda
 */
public class ResourceUtils {
    private static Logger LOGGER = Logger.getLogger(ResourceUtils.class);

    public static String findResource(String resourceName) {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        final URL resource = classLoader.getResource(resourceName);
        if (resource==null){
            throw new RuntimeException("Failed to find resource: " + resourceName);
        }

        final String fileName = resource.getFile();
        LOGGER.info("Resource Name=" + resourceName + ", File Name=" + fileName);
        return fileName;
    }

    public static InputStream findStream(String resourceName) {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        final InputStream resource = classLoader.getResourceAsStream(resourceName);
        if (resource==null){
            throw new RuntimeException("Failed to find resource: " + resourceName);
        }

        return resource;
    }
}
