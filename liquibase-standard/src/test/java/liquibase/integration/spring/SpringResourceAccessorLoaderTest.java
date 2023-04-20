package liquibase.integration.spring;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@RunWith(MockitoJUnitRunner.class)
public class SpringResourceAccessorLoaderTest {

    @Mock
    ResourceLoader resourceLoader;

    @Mock
    Resource resource;

    SpringResourceAccessor springResourceAccessor;

    @Before
    public void setUp() {
        springResourceAccessor = new SpringResourceAccessor(resourceLoader);
    }

    @Test
	public void resourceIsFile_should_be_true_for_native_file() throws IOException {
	    when(resource.exists()).thenReturn(true);
	    when(resource.isFile()).thenReturn(true);
	    when(resource.getFile()).thenThrow(UnsupportedOperationException.class);
	    assertTrue(springResourceAccessor.resourceIsFile(resource));
	}

    @Test
    public void resourceIsFile_should_be_true_for_normal_file() throws IOException {
        File file = Mockito.mock(File.class);
        when(resource.exists()).thenReturn(true);
        when(resource.isFile()).thenReturn(true);
        when(resource.getFile()).thenReturn(file);
        when(file.isFile()).thenReturn(true);
        assertTrue(springResourceAccessor.resourceIsFile(resource));
    }

    @Test
    public void resourceIsFile_should_be_true_by_filename() throws IOException {
        when(resource.exists()).thenReturn(true);
        when(resource.isFile()).thenReturn(false);
        when(resource.getFilename()).thenReturn("changelog.sql");
        assertTrue(springResourceAccessor.resourceIsFile(resource));
    }

    @Test
    public void resourceIsFile_should_be_false_by_filename() throws IOException {
        when(resource.exists()).thenReturn(false);
        when(resource.getFilename()).thenReturn("changelog");
        assertFalse(springResourceAccessor.resourceIsFile(resource));
    }
}
