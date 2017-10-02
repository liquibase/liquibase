package liquibase.parser.core.yaml;

import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.exception.LiquibaseParseException;
import liquibase.parser.SnapshotParser;
import liquibase.parser.core.ParsedNode;
import liquibase.resource.ResourceAccessor;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.RestoredDatabaseSnapshot;
import liquibase.util.StreamUtil;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

public class YamlSnapshotParser extends YamlParser implements SnapshotParser {

    @Override
    public DatabaseSnapshot parse(String path, ResourceAccessor resourceAccessor) throws LiquibaseParseException {
        Yaml yaml = new Yaml();

        try (
            InputStream stream = StreamUtil.singleInputStream(path, resourceAccessor);
        ) {
            if (stream == null) {
                throw new LiquibaseParseException(path + " does not exist");
            }
    
            Map parsedYaml = getParsedYamlFromInputStream(yaml, stream);

            Map rootList = (Map) parsedYaml.get("snapshot");
            if (rootList == null) {
                throw new LiquibaseParseException("Could not find root snapshot node");
            }

            String shortName = (String) ((Map) rootList.get("database")).get("shortName");

            Database database = DatabaseFactory.getInstance().getDatabase(shortName).getClass().newInstance();
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
            throw (LiquibaseParseException) e;
        }
        catch (Exception e) {
            throw new LiquibaseParseException(e);
        }
    }
    
    private Map getParsedYamlFromInputStream(Yaml yaml, InputStream stream) throws LiquibaseParseException {
        Map parsedYaml;
        try (
            InputStreamReader inputStreamReader = new InputStreamReader(
                stream, LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class).getOutputEncoding()
            );
        ) {
            parsedYaml = yaml.loadAs(inputStreamReader, Map.class);
        } catch (Exception e) {
            throw new LiquibaseParseException("Syntax error in " + getSupportedFileExtensions()[0] + ": " + e.getMessage(), e);
        }
        return parsedYaml;
    }
}
