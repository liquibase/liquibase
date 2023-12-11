package liquibase.serializer.core.yaml;

import liquibase.GlobalConfiguration;
import liquibase.diff.compare.DatabaseObjectCollectionComparator;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.serializer.SnapshotSerializer;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.SequenceCurrentValueFunction;
import liquibase.statement.SequenceNextValueFunction;
import liquibase.structure.DatabaseObject;
import liquibase.structure.DatabaseObjectCollection;
import liquibase.structure.core.Column;
import liquibase.util.ISODateFormat;
import liquibase.util.StringUtil;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.util.*;

public class YamlSnapshotSerializer extends YamlSerializer implements SnapshotSerializer {

    private boolean alreadySerializingObject;

    @Override
    public void write(DatabaseSnapshot snapshot, OutputStream out) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue()));
        writer.write(serialize(snapshot, true));
    }

    @Override
    protected Object toMap(final LiquibaseSerializable object) {
        if (object instanceof DatabaseObject) {
            if (object instanceof Column && ((Column) object).isForIndex()) {
                //not really a "real" column that has a snapshot to reference, just serialize the ColumnObject
                return super.toMap(object);
            } else if (alreadySerializingObject) {
                String snapshotId = ((DatabaseObject) object).getSnapshotId();
                if (snapshotId == null) {
                    String name = ((DatabaseObject) object).getName();
                    Object table = ((DatabaseObject) object).getAttribute("table", Object.class);
                    if (table == null) {
                        table = ((DatabaseObject) object).getAttribute("relation", Object.class);
                    }
                    if (table != null) {
                        name = table + "." + name;
                    }

                    if (((DatabaseObject) object).getSchema() != null) {
                        name = ((DatabaseObject) object).getSchema().toString() + "." + name;
                    }

                    throw new UnexpectedLiquibaseException("Found a null snapshotId for " + StringUtil.lowerCaseFirst(object.getClass().getSimpleName()) + " " + name);
                }
                return ((DatabaseObject) object).getClass().getName() + "#" + snapshotId;
            } else {
                alreadySerializingObject = true;
                Object map = super.toMap(object);
                alreadySerializingObject = false;
                return map;
            }
        }
        if (object instanceof DatabaseObjectCollection) {
            SortedMap<String, Object> returnMap = new TreeMap<>();
            for (Map.Entry<Class<? extends DatabaseObject>, Set<? extends DatabaseObject>> entry : ((DatabaseObjectCollection) object).toMap().entrySet()) {
                ArrayList value = new ArrayList(entry.getValue());
                value.sort(new DatabaseObjectCollectionComparator());
                returnMap.put(entry.getKey().getName(), value);
            }
            return returnMap;
        }
        return super.toMap(object);
    }

    @Override
    protected LiquibaseRepresenter getLiquibaseRepresenter(DumperOptions options) {
        return new SnapshotLiquibaseRepresenter(options);
    }

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    public static class SnapshotLiquibaseRepresenter extends LiquibaseRepresenter {

        public SnapshotLiquibaseRepresenter(DumperOptions options) {
            super(options);
        }

        @Override
        protected void init() {
            multiRepresenters.put(DatabaseFunction.class, new TypeStoringAsStringRepresenter());
            multiRepresenters.put(SequenceNextValueFunction.class, new TypeStoringAsStringRepresenter());
            multiRepresenters.put(SequenceCurrentValueFunction.class, new TypeStoringAsStringRepresenter());
            multiRepresenters.put(java.util.Date.class, new TypeStoringAsStringRepresenter());
            multiRepresenters.put(java.sql.Date.class, new TypeStoringAsStringRepresenter());
            multiRepresenters.put(Integer.class, new TypeStoringAsStringRepresenter());
            multiRepresenters.put(BigInteger.class, new TypeStoringAsStringRepresenter());
            multiRepresenters.put(Number.class, new TypeStoringAsStringRepresenter());
            multiRepresenters.put(Enum.class, new TypeStoringAsStringRepresenter());
        }

        private class TypeStoringAsStringRepresenter implements Represent {
            @Override
            public Node representData(Object data) {
                String value;
                if (data instanceof Date) {
                    value = new ISODateFormat().format((Date) data);
                } else if (data instanceof Enum) {
                    value = ((Enum<?>) data).name();
                } else {
                    value = data.toString();
                }


                return representScalar(Tag.STR, value + "!{" + data.getClass().getName() + "}");
            }
        }
    }

}
