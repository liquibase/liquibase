package liquibase.serializer.core.yaml;

import liquibase.changelog.ChangeSet;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.serializer.LiquibaseSerializer;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.SequenceCurrentValueFunction;
import liquibase.statement.SequenceNextValueFunction;
import liquibase.structure.core.Column;
import liquibase.structure.core.OldDataType;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.GenericProperty;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

import java.beans.IntrospectionException;
import java.lang.reflect.Type;
import java.util.*;

public abstract class YamlSerializer implements LiquibaseSerializer {

    protected Yaml yaml;

    public YamlSerializer() {
        yaml = createYaml();
    }

    protected Yaml createYaml() {
        if (isJson()) {
            DumperOptions dumperOptions = new DumperOptions();
            dumperOptions.setPrettyFlow(true);
            dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.FLOW);
            dumperOptions.setDefaultScalarStyle(DumperOptions.ScalarStyle.DOUBLE_QUOTED);
            dumperOptions.setWidth(Integer.MAX_VALUE);

            return new Yaml(getLiquibaseRepresenter(), dumperOptions);
        }


        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        return new Yaml(getLiquibaseRepresenter(), dumperOptions);
    }

    protected LiquibaseRepresenter getLiquibaseRepresenter() {
        return new LiquibaseRepresenter();
    }

    protected boolean isJson() {
        return getValidFileExtensions()[0].equals("json");
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
            out = out.replaceAll("!!int \"(\\d+)\"", "$1");
            out = out.replaceAll("!!bool \"(\\w+)\"", "$1");
            out = out.replaceAll("!!timestamp \"([^\"]*)\"", "$1");
            out = out.replaceAll("!!float \"([^\"]*)\"", "$1");
            return out;
        } else {
            return yaml.dumpAsMap(toMap(object));
        }
    }

    protected Object toMap(LiquibaseSerializable object) {
        Comparator<String> comparator;
        comparator = getComparator(object);
        Map<String, Object> objectMap = new TreeMap<String, Object>(comparator);

        for (String field : object.getSerializableFields()) {
            Object value = object.getSerializableFieldValue(field);
            if (value != null) {
                if (value instanceof OldDataType) {
                    value = ((Map) toMap((OldDataType) value)).values().iterator().next();
                }
                if (value instanceof Column.AutoIncrementInformation) {
                    value = ((Map) toMap((Column.AutoIncrementInformation) value)).values().iterator().next();
                }
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
                if (value instanceof Map) {
                    if  (((Map) value).size() == 0) {
                        continue;
                    }

                    for (Object key : ((Map) value).keySet()) {
                        Object mapValue = ((Map) value).get(key);
                        if (mapValue instanceof LiquibaseSerializable) {
                            ((Map) value).put(key, toMap((LiquibaseSerializable) mapValue));
                        } else if (mapValue instanceof Collection) {
                            List valueAsList = new ArrayList((Collection) mapValue);
                            if (valueAsList.size() == 0) {
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

        Map<String, Object> containerMap = new HashMap<String, Object>();
        containerMap.put(object.getSerializedObjectName(), objectMap);
        return containerMap;
    }

    protected Comparator<String> getComparator(LiquibaseSerializable object) {
        return new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        };
    }

    public static class LiquibaseRepresenter extends Representer {

        public LiquibaseRepresenter() {
            init();
        }

        protected void init() {
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
}
