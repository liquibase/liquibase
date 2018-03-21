package liquibase.util;

public class SpringBootFatJar {
    public static String getPathForResource(String path) {
        String[] components = path.split("!");
        if (components.length == 3) {
            return components[2].substring(1);
        } else {
            return path;
        }
    }
}