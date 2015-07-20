package liquibase.structure;

import liquibase.AbstractExtensibleObject;
import liquibase.util.StringUtils;

public class ObjectReference extends AbstractExtensibleObject {

    public ObjectName objectName;
    public Class<? extends DatabaseObject> objectType;

    public ObjectReference() {
    }

    public ObjectReference(Class<? extends DatabaseObject> objectType, ObjectName objectName) {
        this.objectType = objectType;
        this.objectName = objectName;
    }

    public ObjectReference(Class<? extends DatabaseObject> objectType, String... nameParts) {
        this.objectType = objectType;
        if (nameParts != null && nameParts.length > 0) {
            this.objectName = new ObjectName(nameParts);
        }
    }

    public boolean instanceOf(Class<? extends DatabaseObject> clazz) {
        return clazz.isAssignableFrom(objectType);
    }

    public String getSimpleName() {
        if (objectName == null) {
            return null;
        }
        return objectName.name;
    }

    @Override
    public String toString() {
        String string = "";
        if (objectType != null) {
            string = objectType.getSimpleName()+" ";
        }
        if (objectName == null) {
            return StringUtils.trimToEmpty(string);
        } else {
            return string + objectName.toString();
        }
    }
}
