package liquibase.serializer;

import liquibase.Scope;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.PrioritizedService;
import liquibase.servicelocator.ServiceLocator;

import java.util.*;

public class ChangeLogSerializerFactory {
    private static ChangeLogSerializerFactory instance;

    private Map<String, List<ChangeLogSerializer>> serializers = new HashMap<>();

    public static synchronized void reset() {
        instance = new ChangeLogSerializerFactory();
    }

    public static synchronized ChangeLogSerializerFactory getInstance() {
        if (instance == null) {
            instance = new ChangeLogSerializerFactory();
        }

        return instance;
    }

    private ChangeLogSerializerFactory() {
        try {
            for (ChangeLogSerializer serializer : Scope.getCurrentScope().getServiceLocator().findInstances(ChangeLogSerializer.class)) {
                register(serializer);
            }
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    public Map<String, List<ChangeLogSerializer>> getSerializers() {
        return serializers;
    }

    public List<ChangeLogSerializer> getSerializers(String fileNameOrExtension) {
        fileNameOrExtension = fileNameOrExtension.replaceAll(".*\\.", ""); //just need the extension
        List<ChangeLogSerializer> changeLogSerializers = serializers.get(fileNameOrExtension);
        if (changeLogSerializers == null) {
            return Collections.emptyList();
        }
        return changeLogSerializers;
    }

    public ChangeLogSerializer getSerializer(String fileNameOrExtension) {
        List<ChangeLogSerializer> changeLogSerializers = getSerializers(fileNameOrExtension);
        if (changeLogSerializers.isEmpty()) {
            throw new RuntimeException("No serializers associated with the filename or extension '" + fileNameOrExtension + "'");
        }
        return changeLogSerializers.get(0);
    }

    public void register(ChangeLogSerializer changeLogSerializer) {
        for (String extension : changeLogSerializer.getValidFileExtensions()) {
            List<ChangeLogSerializer> changeLogSerializers = serializers.get(extension);
            if (changeLogSerializers == null) {
                changeLogSerializers = new ArrayList<>();
                serializers.put(extension, changeLogSerializers);
            }
            changeLogSerializers.add(changeLogSerializer);
            Collections.sort(changeLogSerializers, PrioritizedService.COMPARATOR);
        }
    }

    public void unregister(ChangeLogSerializer changeLogSerializer) {
        for (Iterator<Map.Entry<String, List<ChangeLogSerializer>>> entryIterator = serializers.entrySet().iterator(); entryIterator.hasNext();) {
            Map.Entry<String, List<ChangeLogSerializer>> entry = entryIterator.next();
            List<ChangeLogSerializer> changeLogSerializers = entry.getValue();
            for (Iterator<ChangeLogSerializer> iterator = changeLogSerializers.iterator(); iterator.hasNext();) {
                ChangeLogSerializer value = iterator.next();
                if (value.equals(changeLogSerializer)) {
                    iterator.remove();
                }
            }
            if (changeLogSerializers.isEmpty()) {
                entryIterator.remove();
            }
        }
    }
}
