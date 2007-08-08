package org.liquibase.eclipse.common.migrator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;

import liquibase.migrator.FileOpener;

import org.liquibase.eclipse.common.LiquibasePreferences;


public class EclipseFileOpener implements FileOpener {
    private ClassLoader loader;

    public EclipseFileOpener()  {
        try {
			this.loader = new URLClassLoader(new URL[] {
					new File(LiquibasePreferences.getCurrentChangeLogFileName()).getParentFile().toURL(),
			});
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
    }

    public InputStream getResourceAsStream(String file) throws IOException {
        URL resource = loader.getResource(file);
        if (resource == null) {
            throw new IOException(file + " could not be found");
        }
        return resource.openStream();
    }

    public Enumeration<URL> getResources(String packageName) throws IOException {
        return loader.getResources(packageName);
    }
}
