package liquibase.serializer.core.json;

import liquibase.serializer.LiquibaseSerializable;
import liquibase.serializer.core.yaml.YamlChangeLogSerializer;
import liquibase.serializer.core.yaml.YamlSnapshotSerializer;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;

public class JsonSnapshotSerializer extends YamlSnapshotSerializer {

    @Override
    public String[] getValidFileExtensions() {
        return new String[]{
                "json"
        };
    }

}
