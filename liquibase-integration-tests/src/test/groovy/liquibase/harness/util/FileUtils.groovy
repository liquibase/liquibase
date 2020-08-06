package liquibase.harness.util

import liquibase.harness.config.DatabaseUnderTest
import org.yaml.snakeyaml.Yaml

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

    static Map<String, DatabaseUnderTest> readYamlConfig(String fileName) {
        Yaml configFileYml = new Yaml()
        return configFileYml.load(new File(resourceBaseDir, fileName).newInputStream())
    }

}
