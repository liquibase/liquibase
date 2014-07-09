package liquibase.statement.core;

import liquibase.AbstractExtensibleObject;
import liquibase.statement.AbstractStatementTest;

public class InsertDataStatementTest extends AbstractStatementTest {

    @Override
    protected Object getDefaultPropertyValue(String propertyName) {
        if (propertyName == "needsPreparedStatement") {
            return false;
        }
        return super.getDefaultPropertyValue(propertyName)
    }

    @Override
    protected List<String> getStandardProperties() {
        def properties = super.getStandardProperties()
        properties.remove("columnNames")
        return properties
    }
}
