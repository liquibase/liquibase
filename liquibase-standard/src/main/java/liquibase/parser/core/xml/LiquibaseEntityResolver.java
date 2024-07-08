package liquibase.parser.core.xml;

import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.logging.Logger;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.InputStreamList;
import liquibase.resource.Resource;
import liquibase.resource.ResourceAccessor;
import liquibase.util.LiquibaseUtil;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.EntityResolver2;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Finds the Liquibase schema from the classpath rather than fetching it over the Internet.
 * Also resolve external entities using a resourceAccessor if it's provided
 */
public class LiquibaseEntityResolver implements EntityResolver2 {

    private static final String XSD_VERSION_REGEX = "(?:-pro-|-)(?<version>[\\d.]*)\\.xsd";
    private static final Pattern XSD_VERSION_PATTERN = Pattern.compile(XSD_VERSION_REGEX);
    private boolean shouldWarnOnMismatchedXsdVersion = false;
    /**
     * The warning message should only be printed once.
     */
    private static boolean hasWarnedAboutMismatchedXsdVersion = false;

    @Override
    @java.lang.SuppressWarnings("squid:S2095")
    public InputSource resolveEntity(String name, String publicId, String baseURI, String systemId) throws SAXException, IOException {
        Logger log = Scope.getCurrentScope().getLog(getClass());

        log.fine("Resolving XML entity name='" + name + "', publicId='" + publicId + "', baseURI='" + baseURI + "', systemId='" + systemId + "'");

        if (systemId == null) {
            log.fine("Cannot determine systemId for name=" + name + ", publicId=" + publicId + ". Will load from network.");
            return null;
        }

        String path = systemId.toLowerCase()
                .replace("http://www.liquibase.org/xml/ns/migrator/", "http://www.liquibase.org/xml/ns/dbchangelog/")
                .replaceFirst("https?://", "");

        if (shouldWarnOnMismatchedXsdVersion && !hasWarnedAboutMismatchedXsdVersion) {
            warnForMismatchedXsdVersion(systemId);
        }

        InputStream stream = null;
        URL resourceUri = getSearchClassloader().getResource(path);
        if (resourceUri == null) {
            Resource resource = Scope.getCurrentScope().getResourceAccessor().get(path);
            if (resource.exists()) {
                stream = resource.openInputStream();
            }
        } else {
            stream = resourceUri.openStream();
        }

        if (stream == null) {
            if (GlobalConfiguration.SECURE_PARSING.getCurrentValue()) {
                String errorMessage = "Unable to resolve xml entity " + systemId + ". " +
                        GlobalConfiguration.SECURE_PARSING.getKey() + " is set to 'true' which does not allow remote lookups. " +
                        "Check for spelling or capitalization errors and missing extensions such as liquibase-commercial in your XSD definition. Or, set it to 'false' to allow remote lookups of xsd files.";
                throw new XSDLookUpException(errorMessage);
            } else {
                log.fine("Unable to resolve XML entity locally. Will load from network.");
                return null;
            }
        }

        org.xml.sax.InputSource source = new org.xml.sax.InputSource(stream);
        source.setPublicId(publicId);
        source.setSystemId(systemId);

        return source;

    }

    /**
     * Return the classloader used to look for XSD files in the classpath.
     */
    protected ClassLoader getSearchClassloader() {
        return new CombinedClassLoader();
    }

    /**
     * Print a warning message to the logs and UI if the build version does not match the XSD version. This is a best
     * effort check, this method will never throw an exception.
     */
    private void warnForMismatchedXsdVersion(String systemId) {
        try {
            Matcher versionMatcher = XSD_VERSION_PATTERN.matcher(systemId);
            boolean found = versionMatcher.find();
            if (found) {
                String buildVersion = LiquibaseUtil.getBuildVersion();
                if (!buildVersion.equals(LiquibaseUtil.DEV_VERSION)) {
                    String xsdVersion = versionMatcher.group("version");
                    if (!buildVersion.startsWith(xsdVersion)) {
                        hasWarnedAboutMismatchedXsdVersion = true;
                        String msg = "INFO: An older version of the XSD is specified in one or more changelog's <databaseChangeLog> header. This can lead to unexpected outcomes. If a specific XSD is not required, please replace all XSD version references with \"-latest\". Learn more at https://docs.liquibase.com/concepts/changelogs/xml-format.html";
                        Scope.getCurrentScope().getLog(getClass()).info(msg);
                        Scope.getCurrentScope().getUI().sendMessage(msg);
                    }
                }
            }
        } catch (Exception e) {
            Scope.getCurrentScope().getLog(getClass()).fine("Failed to compare XSD version with build version.", e);
        }
    }

    @Override
    public InputSource getExternalSubset(String name, String baseURI) throws SAXException, IOException {
        return null;
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        Scope.getCurrentScope().getLog(getClass()).warning("The current XML parser does not seems to not support EntityResolver2. External entities may not be correctly loaded");
        return resolveEntity(null, publicId, null, systemId);
    }

    /**
     * When set to true, a warning will be printed to the console if the XSD version used does not match the version
     * of Liquibase. If "latest" is used as the XSD version, no warning is printed.
     */
    public void setShouldWarnOnMismatchedXsdVersion(boolean shouldWarnOnMismatchedXsdVersion) {
        this.shouldWarnOnMismatchedXsdVersion = shouldWarnOnMismatchedXsdVersion;
    }

    /**
     * Only currently implementing getResource() since that's the only method used by our logic
     */
    private static class CombinedClassLoader extends ClassLoader {

        private final List<ClassLoader> classLoaders;

        public CombinedClassLoader() {
            this.classLoaders = Arrays.asList(Thread.currentThread().getContextClassLoader(), getClass().getClassLoader());
        }

        @Override
        public URL getResource(String name) {
            for (ClassLoader classLoader : classLoaders) {
                URL resource = classLoader.getResource(name);
                if (resource != null) {
                    return resource;
                }
            }

            return null;
        }
    }
}
