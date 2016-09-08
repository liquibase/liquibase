package liquibase.resource;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.util.Validate;

public abstract class AbstractResourceAccessor implements ResourceAccessor {

    protected final ClassLoader classLoader;
    protected final Logger logger;

    protected AbstractResourceAccessor(ClassLoader classLoader) {
        this.classLoader = Validate.notNullArgument(classLoader,"classLoader is required");
        this.logger = LogFactory.getInstance().getLog();
        this.logger.debug("Created ResourceAccessor: "+this);
    }

    @Override
    public final ClassLoader toClassLoader() {
        return classLoader;
    }

    @Override
    public Set<InputStream> getResourcesAsStream(String path) throws IOException {
        Enumeration<URL> resources = classLoader.getResources(path);
        if (resources == null || !resources.hasMoreElements()) {
            return null;
        }
        Set<String> seenUrls = new HashSet<String>();
        Set<InputStream> returnSet = new HashSet<InputStream>();
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            if (seenUrls.contains(url.toExternalForm())) {
                continue;
            }
            seenUrls.add(url.toExternalForm());
            InputStream in = openStream(url,path);
            if (in != null) {
                returnSet.add(in);
            }
        };
        return returnSet;
    }


    protected InputStream openStream(URL url,String path) throws IOException {
        logger.debug(this.getClass().getSimpleName() + " openStream " + url.toExternalForm() + " as " + path);
        URLConnection connection = url.openConnection();
        connection.setUseCaches(false);
        InputStream resourceAsStream = connection.getInputStream();
        if (resourceAsStream != null && path.endsWith(".gz")) {
            resourceAsStream = new BufferedInputStream(new GZIPInputStream(resourceAsStream)); // TODO: rm buffered so we don't double buffer when caller creates buffers.
        } else if (resourceAsStream != null && path.endsWith(".zip")) {
            resourceAsStream = new BufferedInputStream(new ZipInputStream(resourceAsStream)); // TODO: rm buffered so we don't double buffer when caller creates buffers.
        }

        return resourceAsStream;
    }

}
