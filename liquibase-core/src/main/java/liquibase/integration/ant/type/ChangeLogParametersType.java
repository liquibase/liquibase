package liquibase.integration.ant.type;

import liquibase.Liquibase;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Property;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.PropertySet;
import org.apache.tools.ant.types.Reference;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ChangeLogParametersType extends DataType {
    private List<PropertySet> propertySets;
    private List<Property> parameters;

    public ChangeLogParametersType(Project project) {
        setProject(project);
        propertySets = new LinkedList<PropertySet>();
        parameters = new LinkedList<Property>();
    }

    public void applyParameters(Liquibase liquibase) {
        for(Property parameter : getChangeLogParameters()) {
            liquibase.setChangeLogParameter(parameter.getName(), parameter.getValue());
        }

        for(PropertySet propertySet : getPropertySets()) {
            Properties properties = propertySet.getProperties();
            for(Map.Entry<Object, Object> entry : properties.entrySet()) {
                liquibase.setChangeLogParameter((String) entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public void setRefid(Reference ref) {
        if(!propertySets.isEmpty() || !parameters.isEmpty()) {
            throw tooManyAttributes();
        }
        super.setRefid(ref);
    }

    public List<PropertySet> getPropertySets() {
        return isReference() ? ((ChangeLogParametersType) getCheckedRef()).getPropertySets() : propertySets;
    }

    public void addConfigured(PropertySet propertySet) {
        propertySets.add(propertySet);
    }

    public List<Property> getChangeLogParameters() {
        return isReference() ? ((ChangeLogParametersType) getCheckedRef()).getChangeLogParameters() : parameters;
    }

    public void addConfiguredChangeLogParameter(Property parameter) {
        parameters.add(parameter);
    }
}
