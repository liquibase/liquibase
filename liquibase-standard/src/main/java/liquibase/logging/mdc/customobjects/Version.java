package liquibase.logging.mdc.customobjects;

import liquibase.integration.commandline.Banner;
import liquibase.logging.mdc.CustomMdcObject;

import java.util.List;

public class Version implements CustomMdcObject {

    private Banner liquibaseVersion;
    private JavaHome javaHome;
    private LiquibaseLibraries liquibaseLibraries;

    public Version() {
    }

    public Version(Banner liquibaseVersion, JavaHome javaHome, LiquibaseLibraries liquibaseLibraries) {
        this.liquibaseVersion = liquibaseVersion;
        this.javaHome = javaHome;
        this.liquibaseLibraries = liquibaseLibraries;
    }

    public Banner getLiquibaseVersion() {
        return liquibaseVersion;
    }

    public void setLiquibaseVersion(Banner liquibaseVersion) {
        this.liquibaseVersion = liquibaseVersion;
    }

    public JavaHome getJavaHome() {
        return javaHome;
    }

    public void setJavaHome(JavaHome javaHome) {
        this.javaHome = javaHome;
    }

    public LiquibaseLibraries getLiquibaseLibraries() {
        return liquibaseLibraries;
    }

    public void setLiquibaseLibraries(LiquibaseLibraries liquibaseLibraries) {
        this.liquibaseLibraries = liquibaseLibraries;
    }

    public static class JavaHome {
        private String path;
        private String version;

        public JavaHome() {
        }

        public JavaHome(String path, String version) {
            this.path = path;
            this.version = version;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }

    public static class LiquibaseLibraries {
        private int libraryCount;
        private List<Library> libraries;

        public LiquibaseLibraries() {
        }

        public LiquibaseLibraries(int libraryCount, List<Library> libraries) {
            this.libraryCount = libraryCount;
            this.libraries = libraries;
        }

        public int getLibraryCount() {
            return libraryCount;
        }

        public void setLibraryCount(int libraryCount) {
            this.libraryCount = libraryCount;
        }

        public List<Library> getLibraries() {
            return libraries;
        }

        public void setLibraries(List<Library> libraries) {
            this.libraries = libraries;
        }
    }

    public static class Library {
        private String name;
        private String path;

        public Library() {
        }

        public Library(String name, String path) {
            this.name = name;
            this.path = path;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }

}
