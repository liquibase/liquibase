package liquibase.logging.mdc.customobjects;

import liquibase.integration.commandline.Banner;
import liquibase.logging.mdc.CustomMdcObject;
import lombok.Getter;

import java.util.List;

@Getter
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

    public void setLiquibaseVersion(Banner liquibaseVersion) {
        this.liquibaseVersion = liquibaseVersion;
    }

    public void setJavaHome(JavaHome javaHome) {
        this.javaHome = javaHome;
    }

    public void setLiquibaseLibraries(LiquibaseLibraries liquibaseLibraries) {
        this.liquibaseLibraries = liquibaseLibraries;
    }

    @Getter
    public static class JavaHome {
        private String path;
        private String version;

        public JavaHome() {
        }

        public JavaHome(String path, String version) {
            this.path = path;
            this.version = version;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }

    @Getter
    public static class LiquibaseLibraries {
        private int libraryCount;
        private List<Library> libraries;

        public LiquibaseLibraries() {
        }

        public LiquibaseLibraries(int libraryCount, List<Library> libraries) {
            this.libraryCount = libraryCount;
            this.libraries = libraries;
        }

        public void setLibraryCount(int libraryCount) {
            this.libraryCount = libraryCount;
        }

        public void setLibraries(List<Library> libraries) {
            this.libraries = libraries;
        }
    }

    @Getter
    public static class Library {
        private String name;
        private String path;

        public Library() {
        }

        public Library(String name, String path) {
            this.name = name;
            this.path = path;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }

}
