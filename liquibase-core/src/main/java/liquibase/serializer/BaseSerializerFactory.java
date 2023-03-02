package liquibase.serializer;

import liquibase.Scope;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.PrioritizedService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public abstract class BaseSerializerFactory<T extends LiquibaseSerializer> {
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
        List<T> Ts = getSerializers(fileNameOrExtension);
        if (Ts.isEmpty()) {
            throw new RuntimeException("No serializers associated with the filename or extension '" + fileNameOrExtension + "'");
        }
        return Ts.get(0);
    }

    public void register(T serializer) {
        for (String extensionPtr : serializer.getValidFileExtensions()) {
            serializers.compute(extensionPtr, (extension, serializers) -> {
                if (serializers == null) {
                    serializers = new ArrayList<>();
                }
                serializers.add(serializer);
                serializers.sort(PrioritizedService.COMPARATOR);
                return serializers;
            });
        }
    }

    public void unregister(T serializer) {
        for (String extensionPtr : serializers.keySet()) {
            serializers.compute(extensionPtr, (extension, serializers) -> {
                if (serializers == null) return null;
                serializers.removeIf(Predicate.isEqual(serializer));
                if (serializers.isEmpty()) return null;
                return serializers;
            });
        }
    }
}
