package liquibase.statement

import liquibase.AbstractExtensibleObjectTest
import spock.lang.Specification

class ForeignKeyConstraintTest extends AbstractExtensibleObjectTest {

    @Override
    protected Object getDefaultPropertyValue(String propertyName) {
        if (propertyName in ["initiallyDeferred", "deleteCascade", "deferrable"]) {
            return false;
        }
        return super.getDefaultPropertyValue(propertyName)
    }
}
