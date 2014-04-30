package liquibase.serializer.core.yaml;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.serializer.ChangeLogSerializer;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.SequenceCurrentValueFunction;
import liquibase.statement.SequenceNextValueFunction;
import liquibase.util.StringUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.FieldProperty;
import org.yaml.snakeyaml.introspector.GenericProperty;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

import java.beans.IntrospectionException;
import java.io.*;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.*;

public class YamlChangeLogSerializer implements ChangeLogSerializer {

    protected Yaml yaml;

    public YamlChangeLogSerializer() {
        yaml = createYaml();
    }

    protected Yaml createYaml() {
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        return new Yaml(new LiquibaseRepresenter(), dumperOptions);
    }

    @Override
    public String[] getValidFileExtensions() {
        return new String[]{
                "yaml"
        };
    }

    @Override
    public String serialize(LiquibaseSerializable object, boolean pretty) {
        return yaml.dumpAsMap(toMap(object));
    }

    protected Map<String, Object> toMap(LiquibaseSerializable object) {
        Comparator<String> comparator;
        if (object instanceof ChangeSet) {
            comparator = new ChangeSetComparator();
        } else {
            comparator = new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareTo(o2);
                }
            };
        }
        Map<String, Object> objectMap = new TreeMap<String, Object>(comparator);

        for (String field : object.getSerializableFields()) {
            Object value = object.getSerializableFieldValue(field);
            if (value != null) {
                if (value instanceof LiquibaseSerializable) {
                    value = toMap((LiquibaseSerializable) value);
                }
                if (value instanceof Collection) {
                    List valueAsList = new ArrayList((Collection) value);
                    if (valueAsList.size() == 0) {
                        continue;
                    }
                    for (int i = 0; i < valueAsList.size(); i++) {
                        if (valueAsList.get(i) instanceof LiquibaseSerializable) {
                            valueAsList.set(i, toMap((LiquibaseSerializable) valueAsList.get(i)));
                        }
                    }
                    value = valueAsList;

                }
                objectMap.put(field, value);
            }
        }

        Map<String, Object> containerMap = new HashMap<String, Object>();
        containerMap.put(object.getSerializedObjectName(), objectMap);
        return containerMap;
    }

    @Override
    public void write(List<ChangeSet> changeSets, OutputStream out) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
        writer.write("databaseChangeLog:\n");
        for (ChangeSet changeSet : changeSets) {
            writer.write(StringUtils.indent(serialize(changeSet, true), 2));
            writer.write("\n");
        }
        writer.flush();
    }

    @Override
    public void append(ChangeSet changeSet, File changeLogFile) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public static class LiquibaseRepresenter extends Representer {

        public LiquibaseRepresenter() {
            multiRepresenters.put(DatabaseFunction.class, new AsStringRepresenter());
            multiRepresenters.put(SequenceNextValueFunction.class, new AsStringRepresenter());
            multiRepresenters.put(SequenceCurrentValueFunction.class, new AsStringRepresenter());
        }

        @Override
        protected Tag getTag(Class<?> clazz, Tag defaultTag) {
            return super.getTag(clazz, defaultTag);    //To change body of overridden methods use File | Settings | File Templates.
        }

        @Override
        protected MappingNode representJavaBean(Set<Property> properties, Object javaBean) {
            return super.representJavaBean(properties, javaBean);    //To change body of overridden methods use File | Settings | File Templates.
        }

        @Override
        protected NodeTuple representJavaBeanProperty(Object javaBean, Property property, Object propertyValue, Tag customTag) {
            return super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);    //To change body of overridden methods use File | Settings | File Templates.
        }

        @Override
        public Node represent(Object data) {
            return super.represent(data);    //To change body of overridden methods use File | Settings | File Templates.
        }


        @Override
        protected Set<Property> getProperties(Class<? extends Object> type) throws IntrospectionException {
            Set<Property> returnSet = new HashSet<Property>();
            LiquibaseSerializable serialzableType = null;
            try {
                if (type.equals(ChangeSet.class)) {
                    serialzableType = new ChangeSet("x", "y", false, false, null, null, null, null);
                } else if (LiquibaseSerializable.class.isAssignableFrom(type)) {
                    serialzableType = (LiquibaseSerializable) type.newInstance();
                } else {
                    return super.getProperties(type);
                }
            } catch (InstantiationException e) {
                throw new UnexpectedLiquibaseException(e);
            } catch (IllegalAccessException e) {
                throw new UnexpectedLiquibaseException(e);
            }
            for (String property : serialzableType.getSerializableFields()) {
                LiquibaseSerializable.SerializationType fieldType = serialzableType.getSerializableFieldType(property);
                returnSet.add(new LiquibaseProperty(property, String.class, String.class));
            }
            return returnSet;
        }

        private static class LiquibaseProperty extends GenericProperty {

            private LiquibaseProperty(String name, Class<?> aClass, Type aType) {
                super(name, aClass, aType);
            }

            @Override
            public void set(Object object, Object value) throws Exception {
                //not supported
            }

            @Override
            public Object get(Object object) {
                return ((LiquibaseSerializable) object).getSerializableFieldValue(getName());
            }
        }

        private class AsStringRepresenter implements Represent {
            @Override
            public Node representData(Object data) {
                return representScalar(Tag.STR, data.toString());
            }
        }
    }

    private static class ChangeSetComparator implements Comparator<String> {
        private static final Map<String, Integer> order = new HashMap<String, Integer>();

        static {
            order.put("id", 1);
            order.put("author", 2);
            order.put("changes", Integer.MAX_VALUE);
        }

        @Override
        public int compare(String o1, String o2) {
            Integer o1Order = order.get(o1);
            if (o1Order == null) {
                o1Order = 10;
            }

            Integer o2Order = order.get(o2);
            if (o2Order == null) {
                o2Order = 10;
            }

            int orderCompare = o1Order.compareTo(o2Order);

            if (orderCompare == 0) {
                return o1.compareTo(o2);
            }
            return orderCompare;
        }
    }
}
