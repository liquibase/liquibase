package liquibase.resource;

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
        List<Resource> returnList = new ArrayList<>();
        for (ResourceAccessor accessor : resourceAccessors) {
            final List<Resource> list = accessor.list(path, recursive);
            if (list != null) {
                returnList.addAll(list);
            }
        }

        return returnList;
    }

    @Override
    public SortedSet<Resource> getAll(String path) throws IOException {
        SortedSet<Resource> returnList = new TreeSet<>();
        for (ResourceAccessor accessor : resourceAccessors) {
            final SortedSet<Resource> list = accessor.getAll(path);
            if (list != null) {
                returnList.addAll(list);
            }
        }

        return returnList;
    }

    @Override
    public SortedSet<String> describeLocations() {
        SortedSet<String> returnSet = new TreeSet<>();

        for (ResourceAccessor accessor : resourceAccessors) {
            returnSet.addAll(accessor.describeLocations());
        }

        return returnSet;
    }
}
