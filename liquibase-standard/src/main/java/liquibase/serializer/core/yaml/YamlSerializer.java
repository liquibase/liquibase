package liquibase.serializer.core.yaml;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.RollbackContainer;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.parser.core.yaml.YamlParser;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.serializer.LiquibaseSerializer;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.SequenceCurrentValueFunction;
import liquibase.statement.SequenceNextValueFunction;
import liquibase.structure.core.Column;
import liquibase.structure.core.DataType;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.introspector.GenericProperty;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.resolver.Resolver;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.*;

public abstract class YamlSerializer implements LiquibaseSerializer {

    protected Yaml yaml;
    protected boolean preserveNullValues = true;

    public YamlSerializer() {
        yaml = createYaml();
    }

    public void preserveNullValues(boolean preserveNullValues) {
        this.preserveNullValues = preserveNullValues;
    }

    protected DumperOptions createDumperOptions() {
        DumperOptions dumperOptions = new DumperOptions();

        if (isJson()) {
            dumperOptions.setPrettyFlow(true);
            dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.FLOW);
            dumperOptions.setDefaultScalarStyle(DumperOptions.ScalarStyle.DOUBLE_QUOTED);
            dumperOptions.setWidth(Integer.MAX_VALUE);
        } else {
            dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        }
        return dumperOptions;
    }

    protected Yaml createYaml() {
        DumperOptions dumperOptions= createDumperOptions();
        return new Yaml(new SafeConstructor(YamlParser.createLoaderOptions()), getLiquibaseRepresenter(dumperOptions), dumperOptions, getLiquibaseResolver());
    }

    protected LiquibaseRepresenter getLiquibaseRepresenter(DumperOptions options) {
        return new LiquibaseRepresenter(options);
    }

    protected LiquibaseResolver getLiquibaseResolver() {
        return new LiquibaseResolver();
    }

    protected boolean isJson() {
        return "json".equals(getValidFileExtensions()[0]);
    }

    @Override
    public String[] getValidFileExtensions() {
        return new String[]{
                "yaml",
                "yml"
        };
    }

    @Override
    public String serialize(LiquibaseSerializable object, boolean pretty) {
        if (isJson()) {
            String out = yaml.dumpAs(toMap(object), Tag.MAP, DumperOptions.FlowStyle.FLOW);
            return removeClassTypeMarksFromSerializedJson(out);
        } else {
            return yaml.dumpAsMap(toMap(object));
        }
    }

    protected Set<String> getSerializableObjectFields(LiquibaseSerializable object) {
        return object.getSerializableFields();
    }

    protected Object toMap(LiquibaseSerializable object) {
        Comparator<String> comparator;
        comparator = getComparator(object);
        Map<String, Object> objectMap = new TreeMap<>(comparator);

        for (String field : getSerializableObjectFields(object)) {
            Object value = object.getSerializableFieldValue(field);
            if (value != null) {
                if (value instanceof DataType) {
                    value = ((Map) toMap((DataType) value)).values().iterator().next();
                }
                if (value instanceof Column.AutoIncrementInformation) {
                    value = ((Map) toMap((Column.AutoIncrementInformation) value)).values().iterator().next();
                }
                if (value instanceof ConstraintsConfig) {
                    value = ((Map) toMap((ConstraintsConfig) value)).values().iterator().next();
                }
                if (value instanceof LiquibaseSerializable) {
                    if(value instanceof RollbackContainer) {
                        List<Change> changesToRollback = ((RollbackContainer) value).getChanges();
                        if(changesToRollback.size() == 1) {
                            value = toMap(changesToRollback.get(0));
                        } else {
                            value = toMap((LiquibaseSerializable) value);
                        }
                    } else {
                        value = toMap((LiquibaseSerializable) value);
                    }
                }
                if (value instanceof Collection) {
                    List valueAsList = new ArrayList((Collection) value);
                    if (valueAsList.isEmpty()) {
                        continue;
                    }
                    for (int i = 0; i < valueAsList.size(); i++) {
                        Object o = valueAsList.get(i);
                        if (o instanceof LiquibaseSerializable) {
                            if (!preserveNullValues && o instanceof  ColumnConfig) {
                                ColumnConfig columnConfig = (ColumnConfig) o;
                                if (columnConfig.isNullValue() && !columnConfig.hasDefaultValue()) {
                                    continue;
                                }
                            }
                            Object m = convertToMap(valueAsList, i);
                            valueAsList.set(i, m);
                        }
                    }
                    value = valueAsList;
                }
                if (value instanceof Map) {
                    if  (((Map<?, ?>) value).isEmpty()) {
                        continue;
                    }

                    for (Object key : new HashSet<>(((Map) value).keySet())) {
                        Object mapValue = ((Map<?, ?>) value).get(key);
                        if (mapValue == null) {
                            ((Map<?, ?>) value).remove(key);
                        }

                        if (mapValue instanceof LiquibaseSerializable) {
                            ((Map) value).put(key, toMap((LiquibaseSerializable) mapValue));
                        } else if (mapValue instanceof Collection) {
                            List valueAsList = new ArrayList((Collection) mapValue);
                            if (valueAsList.isEmpty()) {
                                continue;
                            }
                            for (int i = 0; i < valueAsList.size(); i++) {
                                if (valueAsList.get(i) instanceof LiquibaseSerializable) {
                                    valueAsList.set(i, toMap((LiquibaseSerializable) valueAsList.get(i)));
                                }
                            }
                            ((Map) value).put(key, valueAsList);
                        }
                    }


                }
                objectMap.put(field, value);
            }
        }

        Map<String, Object> containerMap = new HashMap<>();
        containerMap.put(object.getSerializedObjectName(), objectMap);
        return containerMap;
    }

    protected Object convertToMap(List valueAsList, int index) {
        return toMap((LiquibaseSerializable) valueAsList.get(index));
    }

    protected Comparator<String> getComparator(LiquibaseSerializable object) {
        return Comparator.naturalOrder();
    }

    public static String removeClassTypeMarksFromSerializedJson(String json) {
        // Handle both negative and positive numbers
        json = json.replaceAll("!!int \"(-?\\d+)\"", "$1");
        json = json.replaceAll("!!bool \"(\\w+)\"", "$1");
        json = json.replaceAll("!!timestamp \"([^\"]*)\"", "$1");
        json = json.replaceAll("!!float \"([^\"]*)\"", "$1");
        json = json.replaceAll("!!liquibase.[^\\s]+ (\"\\w+\")", "$1");
        json = json.replace("!!null \"null\"", "\"null\"");
        return json;
    }

    public static class LiquibaseRepresenter extends Representer {

        public LiquibaseRepresenter(DumperOptions options) {
            super(options);
            init();
        }

        protected void init() {
            multiRepresenters.put(DatabaseFunction.class, new AsStringRepresenter());
            multiRepresenters.put(SequenceNextValueFunction.class, new AsStringRepresenter());
            multiRepresenters.put(SequenceCurrentValueFunction.class, new AsStringRepresenter());
        }

        @Override
        protected Set<Property> getProperties(Class<? extends Object> type) {
            Set<Property> returnSet = new HashSet<>();
            LiquibaseSerializable serializableType = null;
            try {
                if (type.equals(ChangeSet.class)) {
                    serializableType = new ChangeSet("x", "y", false, false, null, null, null, null);
                } else if (LiquibaseSerializable.class.isAssignableFrom(type)) {
                    serializableType = (LiquibaseSerializable) type.getConstructor().newInstance();
                } else {
                    return super.getProperties(type);
                }
            } catch (ReflectiveOperationException e) {
                throw new UnexpectedLiquibaseException(e);
            }
            for (String property : serializableType.getSerializableFields()) {
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

            @Override
            public List<Annotation> getAnnotations() {
                throw new UnsupportedOperationException();
            }

            @Override
            public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
                throw new UnsupportedOperationException();
            }
        }

        private class AsStringRepresenter implements Represent {
            @Override
            public Node representData(Object data) {
                return representScalar(Tag.STR, data.toString());
            }
        }
    }

    public static class LiquibaseResolver extends Resolver {

        // Adapted from: CustomResolver.java (YAML Sources)
        @Override
        protected void addImplicitResolvers() {
            // This adds all the YAML standard resolvers except for the one that resolves Date and Timestamp
            // values automatically.
            addImplicitResolver(Tag.BOOL, BOOL, "yYnNtTfFoO");
            addImplicitResolver(Tag.INT, INT, "-+0123456789");
            addImplicitResolver(Tag.FLOAT, FLOAT, "-+0123456789.");
            addImplicitResolver(Tag.MERGE, MERGE, "<");
            addImplicitResolver(Tag.NULL, NULL, "~nN\0");
            addImplicitResolver(Tag.NULL, EMPTY, null);
            // Do not "resolve" (read: mess with) dates and timestamps (Tag.TIMESTAMP removed)
            addImplicitResolver(Tag.YAML, YAML, "!&*");
        }
    }
}
