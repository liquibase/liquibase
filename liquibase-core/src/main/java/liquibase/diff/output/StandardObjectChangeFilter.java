package liquibase.diff.output;

import liquibase.database.Database;
import liquibase.diff.ObjectDifferences;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.ServiceLocator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class StandardObjectChangeFilter implements ObjectChangeFilter {

    private final FilterType filterType;

    private final List<Filter> filters = new ArrayList<>();

    private static final List<Class<? extends DatabaseObject>> DATABASE_OBJECTS = new ArrayList<>();

    private boolean catalogOrSchemaFilter;

    public StandardObjectChangeFilter(FilterType type, String filter) {
        this.filterType = type;
        if (DATABASE_OBJECTS.isEmpty()) {
            Class<? extends DatabaseObject>[] classes = ServiceLocator.getInstance()
                    .findClasses(DatabaseObject.class);
            DATABASE_OBJECTS.addAll(Arrays.asList(classes));
        }
        parseFilter(filter);
    }

    protected void parseFilter(String filter) {
        filter = StringUtils.trimToNull(filter);
        if (filter == null) {
            return;
        }

        for (String subfilter : filter.split("\\s*,\\s*")) {
            String[] split = subfilter.split(":");
            if (split.length == 1) {
                filters.add(new Filter(null, Pattern.compile(split[0])));
            } else {
                final String className = StringUtils.upperCaseFirst(split[0]);
                Optional<Class<? extends DatabaseObject>> databaseObject = DATABASE_OBJECTS.stream()
                        .filter(instance -> instance.getSimpleName().equalsIgnoreCase(className)).findAny();
                if (databaseObject.isPresent()) {
                    filters.add(new Filter(databaseObject.get(), Pattern.compile(split[1])));
                } else {
                    throw new UnexpectedLiquibaseException(className + " not found");
                }
                catalogOrSchemaFilter |= "Catalog".equals(className) || "Schema".equals(className);
            }
        }
    }

    @Override
    public boolean includeMissing(DatabaseObject object, Database referenceDatabase, Database comparisionDatabase) {
        return include(object);
    }

    @Override
    public boolean includeUnexpected(DatabaseObject object, Database referenceDatabase, Database comparisionDatabase) {
        return include(object);
    }

    @Override
    public boolean includeChanged(DatabaseObject object, ObjectDifferences differences, Database referenceDatabase, Database comparisionDatabase) {
        return include(object);
    }

    @Override
    public boolean include(DatabaseObject object) {
        if (filters.isEmpty()) {
            return true;
        }

        for (Filter filter : filters) {
            if (filter.matches(object)) {
                if (filterType == FilterType.INCLUDE) {
                    return true;
                }
                if (filterType == FilterType.EXCLUDE) {
                    return false;
                }
            }
        }
        // Assumes that if no filter is specified for a catalog or schema all should be accepted.
        return (filterType == FilterType.EXCLUDE) || (((object instanceof Catalog) || (object instanceof Schema)) &&
                !catalogOrSchemaFilter);
    }

    /**
     * The Filter class is used internally to do the actual work. A Filter consists of
     * an objectType and a regex Pattern.
     *
     * The main method is matches(), which returns true if the given DatabaseObject
     * matches the filter, and false if it does not match the Filter.
     *
     * If the objectType is null, then just the Pattern is used to compare the "name" of
     * the object whether it matches or not.
     *
     * If the objectType is not null, then the objectType of the Filter must be
     * assignableFrom the given DatabaseObject, AND the "name" of the DatabaseObject
     * must match the Pattern.
     *
     * The "name" of the object might be what is returned from getName(), or it might
     * be a different 'identifier' for different objet types.
     */
    public enum FilterType {
        INCLUDE,
        EXCLUDE,
    }

    protected static class Filter {

        private Class<? extends DatabaseObject> objectType;
        private Pattern nameMatcher;

        public Filter(Class<? extends DatabaseObject> objectType, Pattern nameMatcher) {
            this.objectType = objectType;
            this.nameMatcher = nameMatcher;
        }

        protected boolean matches(DatabaseObject object) {
            if (object == null) {
                return false;
            }

            Boolean matches = null;
            if (objectType != null && !objectType.isAssignableFrom(object.getClass())) {
                matches = false;
            }
            if (matches == null) {
                matches = object.getName() != null && nameMatcher.matcher(object.getName()).matches();
            }

            if (!matches) {
                if (object instanceof Column) {
                    return matches(((Column) object).getRelation());
                }
                if (object instanceof Index) {
                    return matches(((Index) object).getTable());
                }
                if (object instanceof ForeignKey) {
                    return matches(((ForeignKey) object).getForeignKeyTable());
                }
                if (object instanceof PrimaryKey) {
                    return matches(((PrimaryKey) object).getTable());
                }
                if (object instanceof UniqueConstraint) {
                    return matches(((UniqueConstraint) object).getTable());
                }
                if (object instanceof Data) {
                    return matches(((Data) object).getTable());
                }
            }
            return matches;
        }
    }

}
