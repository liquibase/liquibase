package liquibase.sdk.supplier.resource;

import liquibase.Scope;
import liquibase.change.ChangeFactory;
import liquibase.change.core.CreateProcedureChange;
import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.core.HsqlDatabase;
import liquibase.resource.ResourceAccessor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ResourceSupplier {

    private static final liquibase.resource.ResourceAccessor RESOURCE_ACCESSOR = new SimpleResourceAccessor();

    private static final String USERS_CSV = "username, fullname, pk_id\n" +
            "nvoxland, Nathan Voxland, 1\n" +
            "bob, Bob Bobson, 2";

    private static final String EXAMPLE_SQL_COMMAND = "select * from person";

    public ResourceAccessor getSimpleResourceAccessor() {
        return RESOURCE_ACCESSOR;
    }

    private static class SimpleResourceAccessor implements liquibase.resource.ResourceAccessor {

        @Override
        public Set<InputStream> getResourcesAsStream(String path) throws IOException {
            InputStream stream = null;
            String encoding = LiquibaseConfiguration.getInstance().getConfiguration(
                    GlobalConfiguration.class).getOutputEncoding();
            if (path.toLowerCase().endsWith("csv")) {
                stream = new ByteArrayInputStream(USERS_CSV.getBytes(encoding));
            } else if (path.toLowerCase().endsWith("my-logic.sql")) {
                stream = new ByteArrayInputStream(((String) Scope.getCurrentScope().getSingleton(ChangeFactory.class).getChangeMetaData(
                        new CreateProcedureChange()).getParameters().get("procedureBody").getExampleValue(
                        new HsqlDatabase())).getBytes(encoding)
                );
            } else if (path.toLowerCase().endsWith("sql")) {
                stream = new ByteArrayInputStream(EXAMPLE_SQL_COMMAND.getBytes(encoding));
            } else {
                throw new RuntimeException("Unknown resource type: "+ path);
            }
            return new HashSet<>(Arrays.asList(stream));
        }

        @Override
        public Set<String> list(String relativeTo, String path, boolean includeFiles, boolean includeDirectories,
                                boolean recursive) throws IOException {
            return null;
        }

        @Override
        public ClassLoader toClassLoader() {
            return this.getClass().getClassLoader();
        }
    }
}
