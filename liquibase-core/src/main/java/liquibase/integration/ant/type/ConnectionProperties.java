package liquibase.integration.ant.type;

import org.apache.tools.ant.taskdefs.Property;
import org.apache.tools.ant.types.PropertySet;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class ConnectionProperties {
    private List<PropertySet> propertySets;
    private List<Property> properties;

    public ConnectionProperties() {
        propertySets = new LinkedList<PropertySet>();
        properties = new LinkedList<Property>();
    }

    public Properties buildProperties() {
        Properties retProps = new Properties();
        for(PropertySet propertySet : propertySets) {
            retProps.putAll(propertySet.getProperties());
        }

        for(Property property : properties) {
            retProps.setProperty(property.getName(), property.getValue());
        }
        return retProps;
    }

    public void add(PropertySet propertySet) {
        propertySets.add(propertySet);
    }

    public void addConnectionProperty(Property property) {
        properties.add(property);
    }
}
