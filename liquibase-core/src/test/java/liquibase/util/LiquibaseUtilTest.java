package liquibase.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(LiquibaseUtil.class)
public class LiquibaseUtilTest {

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
}
