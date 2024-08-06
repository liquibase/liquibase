package liquibase.logging.mdc.customobjects;

import liquibase.integration.commandline.Banner;
import liquibase.logging.mdc.CustomMdcObject;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
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

    @Setter
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

    }

    @Setter
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

    }

    @Setter
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

    }

}
