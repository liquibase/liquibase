package liquibase.parser.core.yaml;

import liquibase.GlobalConfiguration;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.OfflineConnection;
import liquibase.exception.LiquibaseParseException;
import liquibase.parser.SnapshotParser;
import liquibase.parser.core.ParsedNode;
import liquibase.resource.Resource;
import liquibase.resource.ResourceAccessor;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.RestoredDatabaseSnapshot;
import liquibase.util.SnakeYamlUtil;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.resolver.Resolver;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

public class YamlSnapshotParser extends YamlParser implements SnapshotParser {

    public static final int CODE_POINT_LIMIT = Integer.MAX_VALUE;

    @SuppressWarnings("java:S2095")
    @Override
    public DatabaseSnapshot parse(String path, ResourceAccessor resourceAccessor) throws LiquibaseParseException {
        Yaml yaml = createYaml();

        try {
            Resource resource = resourceAccessor.get(path);
            if (resource == null) {
                throw new LiquibaseParseException(path + " does not exist");
            }

            Map parsedYaml;
            try (InputStream stream = resource.openInputStream()) {
                parsedYaml = getParsedYamlFromInputStream(yaml, stream);
            }

            Map rootList = (Map) parsedYaml.get("snapshot");
            if (rootList == null) {
                throw new LiquibaseParseException("Could not find root snapshot node");
            }

            String shortName = (String) ((Map<?, ?>) rootList.get("database")).get("shortName");

            Database database = DatabaseFactory.getInstance().getDatabase(shortName).getClass().getConstructor().newInstance();
            database.setConnection(new OfflineConnection("offline:" + shortName, null));

            DatabaseSnapshot snapshot = new RestoredDatabaseSnapshot(database);
            ParsedNode snapshotNode = new ParsedNode(null, "snapshot");
            snapshotNode.setValue(rootList);

            Map metadata = (Map) rootList.get("metadata");
            if (metadata != null) {
                snapshot.getMetadata().putAll(metadata);
            }

            snapshot.load(snapshotNode, resourceAccessor);

            return snapshot;
        } catch (LiquibaseParseException e) {
            throw e;
        }
        catch (Exception e) {
            throw new LiquibaseParseException(e);
        }
    }

    private Yaml createYaml() {
        LoaderOptions loaderOptions = new LoaderOptions();
        SnakeYamlUtil.setCodePointLimitSafely(loaderOptions, CODE_POINT_LIMIT);
        Representer representer = new Representer(new DumperOptions());
        DumperOptions dumperOptions = initDumperOptions(representer);
        return new Yaml(new SafeConstructor(loaderOptions), representer, dumperOptions, loaderOptions, new Resolver());
    }

    private static DumperOptions initDumperOptions(Representer representer) {
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(representer.getDefaultFlowStyle());
        dumperOptions.setDefaultScalarStyle(representer.getDefaultScalarStyle());
        dumperOptions
                .setAllowReadOnlyProperties(representer.getPropertyUtils().isAllowReadOnlyProperties());
        dumperOptions.setTimeZone(representer.getTimeZone());
        return dumperOptions;
    }

    private Map getParsedYamlFromInputStream(Yaml yaml, InputStream stream) throws LiquibaseParseException {
        Map parsedYaml;
        try (
            InputStreamReader inputStreamReader = new InputStreamReader(
                stream, GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue()
            )
        ) {
            parsedYaml = (Map) yaml.load(inputStreamReader);
        } catch (Exception e) {
            throw new LiquibaseParseException("Syntax error in " + getSupportedFileExtensions()[0] + ": " + e.getMessage(), e);
        }
        return parsedYaml;
    }
}
