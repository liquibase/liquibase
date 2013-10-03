package liquibase.serializer;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.ServiceLocator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChangeLogSerializerFactory {
    private static ChangeLogSerializerFactory instance;

    private Map<String, ChangeLogSerializer> serializers = new HashMap<String, ChangeLogSerializer>();


    public static void reset() {
        instance = new ChangeLogSerializerFactory();
    }

    public static ChangeLogSerializerFactory getInstance() {
        if (instance == null) {
             instance = new ChangeLogSerializerFactory();
        }

        return instance;
    }

    private ChangeLogSerializerFactory() {
        Class<? extends ChangeLogSerializer>[] classes;
        try {
            classes = ServiceLocator.getInstance().findClasses(ChangeLogSerializer.class);

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
        ChangeLogSerializer changeLogSerializer = serializers.get(fileNameOrExtension);
        if (changeLogSerializer == null) {
			throw new RuntimeException("No serializer associated with the filename or extension '" + fileNameOrExtension + "'");
		}
        return changeLogSerializer;
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
