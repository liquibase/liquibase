package liquibase.serializer.core.yaml;

import liquibase.serializer.LiquibaseSerializable;
import liquibase.serializer.SnapshotSerializer;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.structure.DatabaseObject;
import liquibase.structure.DatabaseObjectCollection;
import liquibase.structure.DatabaseObjectComparator;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.*;

public class YamlSnapshotSerializer extends YamlSerializer implements SnapshotSerializer {

    private boolean alreadySerializingObject = false;

    @Override
    public void write(DatabaseSnapshot snapshot, OutputStream out) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
        writer.write(serialize(snapshot, true));
    }

//    @Override
//    public String serialize(LiquibaseSerializable object, boolean pretty) {
//        if (object instanceof DatabaseObject) {
//            if (alreadySerializingObject) {
//                return ((DatabaseObject) object).getObjectTypeName()+"#"+((DatabaseObject) object).getSnapshotId();
//            } else {
//                alreadySerializingObject = true;
//                String string = super.serialize(object, pretty);
//                alreadySerializingObject = false;
//                return string;
//            }
//        }
//        return super.serialize(object, pretty);
//    }

    @Override
    protected Object toMap(LiquibaseSerializable object) {
        if (object instanceof DatabaseObject) {
            if (alreadySerializingObject) {
                return ((DatabaseObject) object).getClass().getName()+"#"+((DatabaseObject) object).getSnapshotId();
            } else {
                alreadySerializingObject = true;
                Object map = super.toMap(object);
                alreadySerializingObject = false;
                return map;
            }
        }
        if (object instanceof DatabaseObjectCollection) {
            SortedMap<String, Object> returnMap = new TreeMap<String, Object>();
            for (Map.Entry<Class<? extends DatabaseObject>,Set<? extends DatabaseObject>> entry : ((DatabaseObjectCollection) object).toMap().entrySet()) {
                ArrayList value = new ArrayList(entry.getValue());
                Collections.sort(value, new DatabaseObjectComparator());
                returnMap.put(entry.getKey().getName(), value);
            }
            return returnMap;
        }
        return super.toMap(object);
    }
}
