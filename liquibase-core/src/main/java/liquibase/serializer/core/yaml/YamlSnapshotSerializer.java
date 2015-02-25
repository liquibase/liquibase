package liquibase.serializer.core.yaml;

import liquibase.serializer.LiquibaseSerializable;
import liquibase.serializer.SnapshotSerializer;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.structure.DatabaseObject;
import liquibase.structure.DatabaseObjectCollection;

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
            Map<String, Object> returnMap = new HashMap<String, Object>();
            for (Map.Entry<Class<? extends DatabaseObject>,Set<? extends DatabaseObject>> entry : ((DatabaseObjectCollection) object).toMap().entrySet()) {
                returnMap.put(entry.getKey().getName(), new ArrayList(entry.getValue()));
            }
            return returnMap;
        }
        return super.toMap(object);
    }
}
