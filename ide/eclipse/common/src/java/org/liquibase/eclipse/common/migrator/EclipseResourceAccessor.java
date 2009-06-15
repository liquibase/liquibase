package org.liquibase.eclipse.common.migrator;

import liquibase.resource.ResourceAccessor;
import org.liquibase.eclipse.common.LiquibasePreferences;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;


public class EclipseResourceAccessor implements ResourceAccessor {
    private ClassLoader loader;

    public EclipseResourceAccessor()  {
        try {
    	List<URL> urls = new ArrayList<URL>();
	    	for (File root : LiquibasePreferences.getRoots()) {
	    		urls.add(root.toURI().toURL());
	    	}
			this.loader = new URLClassLoader(urls.toArray(new URL[urls.size()]));
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

    public ClassLoader toClassLoader() {
        return loader;
    }
}
