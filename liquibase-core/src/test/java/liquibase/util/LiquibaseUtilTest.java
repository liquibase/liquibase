package liquibase.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.jar.Manifest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(LiquibaseUtil.class)
public class LiquibaseUtilTest {

    private static final String BUILD_VERSION   = "DUMMY_VERSION";
    private static final String BUILD_TIMESTAMP = "DUMMY_TIMESTAMP";

    private static final String MANIFEST_STRING = "Bundle-Version: "
            + BUILD_VERSION
            + System.lineSeparator()
            + "Build-Time: "
            + BUILD_TIMESTAMP
            + System.lineSeparator();

    @Before
    public void setUp() {
        spy(LiquibaseUtil.class);
    }

    @Test
    public void testShouldReturnUnknownBuildTimeWhenReadingFromManifestAndPropertiesReturnsNull() {
        when(LiquibaseUtil.readFromManifest(anyString())).thenReturn(null);
        when(LiquibaseUtil.readFromProperties(anyString())).thenReturn(null);

        assertEquals("UNKNOWN", LiquibaseUtil.getBuildTime());
    }

    @Test
    public void testShouldReturnUnknownBuildVersionWhenReadingFromManifestAndPropertiesReturnsNull() {
        when(LiquibaseUtil.readFromManifest(anyString())).thenReturn(null);
        when(LiquibaseUtil.readFromProperties(anyString())).thenReturn(null);

        assertEquals("UNKNOWN", LiquibaseUtil.getBuildVersion());
    }

    @Test
    public void testShouldReturnNotNullBuildTime() {
        final String buildTime = LiquibaseUtil.getBuildTime();
        assertNotNull(buildTime);
        assertNotEquals("UNKNOWN", buildTime);
    }

    @Test
    public void testShouldReturnNotNullBuildVersion() {
        final String buildVersion = LiquibaseUtil.getBuildVersion();
        assertNotNull(buildVersion);
        assertNotEquals("UNKNOWN", buildVersion);
    }

    @Test
    public void testShouldReturnNotNullBuildTimeWhenReadingFromManifestReturnsNull() {
        when(LiquibaseUtil.readFromManifest(anyString())).thenReturn(null);

        final String buildTime = LiquibaseUtil.getBuildTime();

        assertNotNull(buildTime);
        assertNotEquals("UNKNOWN", buildTime);
    }

    @Test
    public void testShouldReturnNotNullBuildVersionWhenReadingFromManifestReturnsNull() {
        when(LiquibaseUtil.readFromManifest(anyString())).thenReturn(null);

        final String buildVersion = LiquibaseUtil.getBuildVersion();
        assertNotNull(buildVersion);
        assertNotEquals("UNKNOWN", buildVersion);
    }

    @Test
    public void testReadBuildTimeFromManifest() {
        when(LiquibaseUtil.readManifestFromJar(anyString())).thenReturn(mockManifest());
        assertEquals(BUILD_TIMESTAMP, LiquibaseUtil.getBuildTime());
    }

    @Test
    public void testReadBuildVersionFromManifest() {
        when(LiquibaseUtil.readManifestFromJar(anyString())).thenReturn(mockManifest());
        assertEquals(BUILD_VERSION, LiquibaseUtil.getBuildVersion());
    }

    @Test
    public void testReadBuildTimeFromProperties() {
        when(LiquibaseUtil.readFromManifest(anyString())).thenReturn(null);
        when(LiquibaseUtil.readProperties()).thenReturn(mockProperties());
        assertEquals(BUILD_TIMESTAMP, LiquibaseUtil.getBuildTime());
    }

    @Test
    public void testReadBuildVersionFromProperties() {
        when(LiquibaseUtil.readFromManifest(anyString())).thenReturn(null);
        when(LiquibaseUtil.readProperties()).thenReturn(mockProperties());
        assertEquals(BUILD_VERSION, LiquibaseUtil.getBuildVersion());
    }

    private static Manifest mockManifest() {
        try {
            return new Manifest(new ByteArrayInputStream(MANIFEST_STRING.getBytes()));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Properties mockProperties() {
        final Properties properties = new Properties();
        properties.setProperty("build.version", BUILD_VERSION);
        properties.setProperty("build.timestamp", BUILD_TIMESTAMP);
        return properties;
    }
}
