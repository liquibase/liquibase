package liquibase.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
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
}
