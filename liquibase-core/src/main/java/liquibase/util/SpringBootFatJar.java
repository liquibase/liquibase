package liquibase.util;

public class SpringBootFatJar {
    public static String getPathForResource(String path) {
        String[] components = path.split("!");
        if (components.length == 3) {
            return String.format("%s%s", components[1].substring(1), components[2]);
        } else {
            return path;
        }
    }
}