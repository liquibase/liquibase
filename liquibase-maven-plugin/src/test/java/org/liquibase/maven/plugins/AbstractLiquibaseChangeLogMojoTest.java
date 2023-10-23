package org.liquibase.maven.plugins;

import liquibase.resource.ResourceAccessor;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import static org.mockito.Mockito.*;

public class AbstractLiquibaseChangeLogMojoTest {


    @Test
    public void validateGetResourceAccessorDoesNotGeneratedTwoFileAccessorsWhenChangeLogDirectoryIsSet() throws MojoFailureException, IOException {
        //GIVEN
        AbstractLiquibaseChangeLogMojo mojo = new LiquibaseChangeLogMojoTest();
        mojo.project = mock(MavenProject.class);
        String userDir = Paths.get(System.getProperty("user.dir")).toString();
        when(mojo.project.getBasedir()).thenReturn(new File(userDir));
        mojo.changeLogDirectory = userDir;
        mojo.searchPath = null;
        //WHEN
        ResourceAccessor changeLogAccessor = mojo.getResourceAccessor(mock(ClassLoader.class));
        //THEN
        List<String> locations = changeLogAccessor.describeLocations();
        String dirLocation = locations.get(0).replace("\\", "/");
        Assert.assertEquals(1, locations.size());
        Assert.assertEquals(mojo.changeLogDirectory, dirLocation);
    }

    @Test
    public void validateGetResourceAccessorDoesNotGenerateFileAccessorsWhenChangeLogDirectoryAndSearchPathAreSet() throws IOException {

        try {
            //GIVEN
            AbstractLiquibaseChangeLogMojo mojo = new LiquibaseChangeLogMojoTest();
            mojo.project = mock(MavenProject.class);
            String userDir = Paths.get(System.getProperty("user.dir")).toString();
            when(mojo.project.getBasedir()).thenReturn(new File(userDir));
            mojo.changeLogDirectory = userDir;
            mojo.searchPath = userDir;
            //WHEN
            mojo.getResourceAccessor(mock(ClassLoader.class));
            //THEN
            Assert.fail("exception thrown");
        } catch (MojoFailureException  e) {
            Assert.assertEquals("Cannot specify searchPath and changeLogDirectory at the same time", e.getMessage());
        }
    }

    private static class LiquibaseChangeLogMojoTest extends AbstractLiquibaseChangeLogMojo {
        // For testing purposes have chose the strategy of creating an empty class as we are trying to test an abstract class
        // which otherwise we cannot instantiate
    }


}
