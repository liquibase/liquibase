package liquibase.serializer;

import liquibase.parser.ChangeLogParser;
import liquibase.util.plugin.ClassPathScanner;
import liquibase.exception.UnexpectedLiquibaseException;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class ChangeLogSerializerFactory {
    private static ChangeLogSerializerFactory instance = new ChangeLogSerializerFactory();

    private Map<String, ChangeLogSerializer> serializers = new HashMap<String, ChangeLogSerializer>();


    public static void reset() {
        instance = new ChangeLogSerializerFactory();
    }

    public static ChangeLogSerializerFactory getInstance() {
        return instance;
    }

    private ChangeLogSerializerFactory() {
        Class<? extends ChangeLogSerializer>[] classes;
        try {
            classes = ClassPathScanner.getInstance().getClasses(ChangeLogSerializer.class);

            for (Class<? extends ChangeLogSerializer> clazz : classes) {
                    register((ChangeLogSerializer) clazz.getConstructor().newInstance());
            }
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }

    }

    public Map<String, ChangeLogSerializer> getSerializers() {
        return serializers;
    }

    public ChangeLogSerializer getSerializer(String fileNameOrExtension) {
        fileNameOrExtension = fileNameOrExtension.replaceAll(".*\\.", ""); //just need the extension
        return serializers.get(fileNameOrExtension);
    }

    public void register(ChangeLogSerializer changeLogSerializer) {
        for (String extension : changeLogSerializer.getValidFileExtensions()) {
            serializers.put(extension, changeLogSerializer);
        }
    }

    public void unregister(ChangeLogSerializer changeLogSerializer) {
        List<Map.Entry<String, ChangeLogSerializer>> entrysToRemove = new ArrayList<Map.Entry<String, ChangeLogSerializer>>();
        for (Map.Entry<String, ChangeLogSerializer> entry : serializers.entrySet()) {
            if (entry.getValue().equals(changeLogSerializer)) {
                entrysToRemove.add(entry);
            }
        }

        for (Map.Entry<String, ChangeLogSerializer> entry : entrysToRemove) {
            serializers.remove(entry.getKey());
        }

    }

}
