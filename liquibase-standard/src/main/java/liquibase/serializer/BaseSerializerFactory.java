package liquibase.serializer;

import liquibase.Scope;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.PrioritizedService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

abstract class BaseSerializerFactory<T extends LiquibaseSerializer> {
    protected final Map<String, List<T>> serializers = new ConcurrentHashMap<>();

    protected BaseSerializerFactory(Class<T> serializerClass) {
        try {
            for (T serializer : Scope.getCurrentScope().getServiceLocator().findInstances(serializerClass)) {
                register(serializer);
            }
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    public List<T> getSerializers(String fileNameOrExtension) {
        fileNameOrExtension = fileNameOrExtension.replaceAll(".*\\.", ""); //just need the extension
        List<T> candidates = serializers.get(fileNameOrExtension);
        if (candidates == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(candidates);
    }

    public T getSerializer(String fileNameOrExtension) {
        List<T> forExtension = getSerializers(fileNameOrExtension);
        if (forExtension.isEmpty()) {
            throw new RuntimeException("No serializers associated with the filename or extension '" + fileNameOrExtension + "'");
        }
        return forExtension.get(0);
    }

    public void register(T serializer) {
        for (String extensionPtr : serializer.getValidFileExtensions()) {
            serializers.compute(extensionPtr, (extension, forExtension) -> {
                if (forExtension == null) {
                    forExtension = new ArrayList<>();
                }
                forExtension.add(serializer);
                forExtension.sort(PrioritizedService.COMPARATOR);
                return forExtension;
            });
        }
    }

    public void unregister(T serializer) {
        for (String extensionPtr : serializers.keySet()) {
            serializers.compute(extensionPtr, (extension, forExtension) -> {
                if (forExtension == null) return null;
                forExtension.removeIf(Predicate.isEqual(serializer));
                if (forExtension.isEmpty()) return null;
                return forExtension;
            });
        }
    }
}
