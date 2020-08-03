package liquibase.harness.util

class FileUtils {
    static final String resourceBaseDir = "src/test/resources/harness/"

     static String getFileContent (String expected, String dbName, String expectedFile){
        return new File(new StringBuilder(resourceBaseDir)
                .append(expected)
                .append("/")
                .append(dbName)
                .append("/")
                .append(expectedFile).toString())
            .getText("UTF-8")
    }

}
