package liquibase.util;

public class SpringBootFatJar {

    public static String getPathForResource(String path) {
        String[] components = path.split("!");
        if (components.length == 3) {
            if (components[1].endsWith(".jar")){
                return components[1].substring(1);
            }
            return components[1].substring(1) + components[2];
        } else if (components.length == 2) {
            return components[1].substring(1);
        }
        return path;
    }

    /**
     * Method used to simplify an entryName
     *
     * Ex: with path jar:/some/jar.jar!/BOOT-INF/classes!/db/changelog and entryName /BOOT-INF/classes/db/changelog
     * The simple entry name for Spring is db/changelog
     * (/BOOT-INF/classes/ is not needed and break the liquibase alphabetical sort order)
     *
     * @param entryName the entryName to simplify
     * @param path file path
     * @return the simple path
     */
    public static String getSimplePathForResources(String entryName, String path) {
        String[] components = path.split("!");
        if (components.length == 3) {
            if (components[1].endsWith(".jar")) {
                return components[2].substring(1);
            } else {
                return entryName.replaceFirst(components[1], "").substring(1);
            }
        }
        return entryName;
    }
}
