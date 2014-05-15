package liquibase.util

import liquibase.test.TestContext
import spock.lang.Specification

class FileUtilTest extends Specification {

    def unzip() {
        when:
        def zipFile = new File(TestContext.instance.findIntegrationTestProjectRoot(), "src/test/resources/packaged-changelog.jar")
        def outDir = FileUtil.unzip(zipFile)

        then:
        outDir.exists()
        outDir.isDirectory()

        new File(outDir, "com").isDirectory()
        new File(outDir, "com/example").isDirectory()
        new File(outDir, "com/example/external-entity.xml").isFile()
        new File(outDir, "com/example/nonIncluded").isDirectory()
        new File(outDir, "com/example/nonIncluded/externalEntity.changelog.xml").isFile()
        new File(outDir, "com/example/packaged/packaged.changelog.xml").isFile()
        new File(outDir, "com/example/packaged/packaged.changelog2.xml").isFile()
    }
}
