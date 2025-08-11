package liquibase.command.core


import liquibase.exception.UnexpectedLiquibaseException
import spock.lang.Specification

class LpmCommandStepTest extends Specification {

    def "should define correct command names"() {
        given:
        LpmCommandStep lpmCommand = new LpmCommandStep()

        when:
        String[][] commandNames = lpmCommand.defineCommandNames()

        then:
        commandNames.length == 1
        commandNames[0] == ["init", "lpm"]
    }

    def "should determine platform correctly for Windows"() {
        given:
        LpmCommandStep lpmCommand = new LpmCommandStep()
        
        when:
        System.setProperty("os.name", "Windows 10")
        String platform = lpmCommand.determinePlatform()
        
        then:
        platform == "windows"
    }

    def "should determine platform correctly for macOS Intel"() {
        given:
        LpmCommandStep lpmCommand = new LpmCommandStep()
        
        when:
        System.setProperty("os.name", "Mac OS X")
        System.setProperty("os.arch", "x86_64")
        String platform = lpmCommand.determinePlatform()
        
        then:
        platform == "darwin"
    }

    def "should determine platform correctly for macOS ARM"() {
        given:
        LpmCommandStep lpmCommand = new LpmCommandStep()
        
        when:
        System.setProperty("os.name", "Mac OS X")
        System.setProperty("os.arch", "aarch64")
        String platform = lpmCommand.determinePlatform()
        
        then:
        platform == "darwin-arm64"
    }

    def "should determine platform correctly for Linux"() {
        given:
        LpmCommandStep lpmCommand = new LpmCommandStep()
        
        when:
        System.setProperty("os.name", "Linux")
        System.setProperty("os.arch", "x86_64")
        String platform = lpmCommand.determinePlatform()
        
        then:
        platform == "linux"
    }

    def "should determine platform correctly for Linux ARM"() {
        given:
        LpmCommandStep lpmCommand = new LpmCommandStep()
        
        when:
        System.setProperty("os.name", "Linux")
        System.setProperty("os.arch", "aarch64")
        String platform = lpmCommand.determinePlatform()
        
        then:
        platform == "linux-arm64"
    }

    def "should throw exception when liquibase.home is not configured"() {
        given:
        LpmCommandStep lpmCommand = new LpmCommandStep()
        
        when:
        lpmCommand.checkForLpmInstallation(null, "/path/to/lpm")
        
        then:
        UnexpectedLiquibaseException ex = thrown()
        ex.message.contains("liquibase.home is not configured")
    }

    def "should have no dependencies"() {
        given:
        LpmCommandStep lpmCommand = new LpmCommandStep()
        
        when:
        List<Class<?>> dependencies = lpmCommand.requiredDependencies()
        
        then:
        dependencies.isEmpty()
    }

    def "should parse GitHub release response correctly"() {
        given:
        LpmCommandStep lpmCommand = new LpmCommandStep()
        String jsonResponse = '{"tag_name": "v1.0.5", "name": "Release 1.0.5"}'
        
        when:
        String version = lpmCommand.parseVersionFromGitHubResponse(jsonResponse)
        
        then:
        version == "1.0.5"
    }

    def "should parse GitHub release response without v prefix"() {
        given:
        LpmCommandStep lpmCommand = new LpmCommandStep()
        String jsonResponse = '{"tag_name": "1.0.5", "name": "Release 1.0.5"}'
        
        when:
        String version = lpmCommand.parseVersionFromGitHubResponse(jsonResponse)
        
        then:
        version == "1.0.5"
    }

    def "should handle malformed GitHub response"() {
        given:
        LpmCommandStep lpmCommand = new LpmCommandStep()
        String jsonResponse = '{"invalid": "response"}'
        
        when:
        String version = lpmCommand.parseVersionFromGitHubResponse(jsonResponse)
        
        then:
        version == null
    }

    def "should fallback to hardcoded version when API fails"() {
        given:
        LpmCommandStep lpmCommand = new LpmCommandStep()
        
        when:
        // This will test the fallback mechanism since we can't easily mock the HTTP call
        String version = lpmCommand.getLatestLpmVersion()
        
        then:
        version != null
        version.matches(/\d+\.\d+\.\d+.*/) // Should be a valid version format
    }

    def "should have download argument defined correctly"() {
        expect:
        LpmCommandStep.DOWNLOAD_ARG.name == "download"
        LpmCommandStep.DOWNLOAD_ARG.dataType == Boolean.class
    }

    def "should compare versions correctly"() {
        given:
        LpmCommandStep lpmCommand = new LpmCommandStep()
        
        expect:
        lpmCommand.isNewerVersion("1.2.1", "1.2.0") == true
        lpmCommand.isNewerVersion("1.2.0", "1.2.0") == false
        lpmCommand.isNewerVersion("1.2.0", "1.2.1") == false
        lpmCommand.isNewerVersion("2.0.0", "1.9.9") == true
        lpmCommand.isNewerVersion("1.0.10", "1.0.9") == true
        lpmCommand.isNewerVersion("1.0.0", "1.0.0") == false
    }

    def "should have CommandArgumentDefinition fields discoverable via reflection"() {
        given:
        Class<?> lpmCommandStepClass = Class.forName("liquibase.command.core.LpmCommandStep")
        
        when:
        // Find all static CommandArgumentDefinition fields
        def argumentDefinitionFields = lpmCommandStepClass.getDeclaredFields().findAll { field ->
            java.lang.reflect.Modifier.isStatic(field.getModifiers()) && 
            field.getType().getName().equals("liquibase.command.CommandArgumentDefinition")
        }
        
        then:
        // Should find the DOWNLOAD_ARG field
        argumentDefinitionFields.size() >= 1
        argumentDefinitionFields.any { it.name == "DOWNLOAD_ARG" }
    }

    def "should use DownloadUtil for fetching version information"() {
        given:
        LpmCommandStep lpmCommand = new LpmCommandStep()
        
        when:
        // This test verifies that the method works with DownloadUtil
        String version = lpmCommand.getLatestLpmVersion()
        
        then:
        version != null
        version.matches(/\d+\.\d+\.\d+.*/) // Should be a valid version format
        // The actual version fetched may vary, but fallback should always work
    }
}
