package liquibase.util.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * A helper that allows reading Java resource bundles from XML resources.
 * This code is a direct copy from the JavaDoc of ResourceBundle.Control.
 */
public class XMLResourceBundle extends ResourceBundle {
    private Properties props;

    XMLResourceBundle(InputStream stream) throws IOException {
        props = new Properties();
        props.loadFromXML(stream);
    }

    protected Object handleGetObject(String key) {
        return props.getProperty(key);
    }

    @Override
    public Enumeration<String> getKeys() {
        return (Enumeration<String>) props.propertyNames();
    }
}