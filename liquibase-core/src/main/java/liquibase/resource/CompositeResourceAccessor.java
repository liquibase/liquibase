package liquibase.resource;

import liquibase.util.CollectionUtil;

import java.io.IOException;
import java.util.*;

/**
 * A {@link liquibase.resource.ResourceAccessor} that contains multiple sub-accessors and combines the results of all of them.
 * For the "overall" aggregate resource accessor, integrations should generally use {@link SearchPathResourceAccessor} instead of this.
 */
public class CompositeResourceAccessor extends AbstractResourceAccessor {

    private List<ResourceAccessor> resourceAccessors;

    public CompositeResourceAccessor(ResourceAccessor... resourceAccessors) {
        this.resourceAccessors = new ArrayList<>(); //Arrays.asList(CollectionUtil.createIfNull(resourceAccessors));
        this.resourceAccessors.addAll(Arrays.asList(resourceAccessors));
    }

    public CompositeResourceAccessor(Collection<ResourceAccessor> resourceAccessors) {
        this.resourceAccessors = new ArrayList<>(resourceAccessors);
    }

    @Override
    public void close() throws Exception {
        for (ResourceAccessor accessor : resourceAccessors) {
            accessor.close();
        }
    }

    public CompositeResourceAccessor addResourceAccessor(ResourceAccessor resourceAccessor) {
        this.resourceAccessors.add(resourceAccessor);

        return this;
    }

    public void removeResourceAccessor(ResourceAccessor resourceAccessor) {
        this.resourceAccessors.remove(resourceAccessor);
    }

    @Override
    public List<Resource> list(String path, boolean recursive) throws IOException {
        LinkedHashSet<Resource> returnList = new LinkedHashSet<>();
        for (ResourceAccessor accessor : resourceAccessors) {
            returnList.addAll(CollectionUtil.createIfNull(accessor.list(path, recursive)));
        }

        return new ArrayList<>(returnList);
    }

    @Override
    public List<Resource> getAll(String path) throws IOException {
        LinkedHashSet<Resource> returnList = new LinkedHashSet<>();
        for (ResourceAccessor accessor : resourceAccessors) {
            returnList.addAll(CollectionUtil.createIfNull(accessor.getAll(path)));
        }

        return new ArrayList<>(returnList);
    }

    @Override
    public List<String> describeLocations() {
        LinkedHashSet<String> returnSet = new LinkedHashSet<>();

        for (ResourceAccessor accessor : resourceAccessors) {
            returnSet.addAll(CollectionUtil.createIfNull(accessor.describeLocations()));
        }

        return new ArrayList<>(returnSet);
    }
}
