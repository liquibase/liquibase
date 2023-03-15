package liquibase.serializer.core.json;

import liquibase.serializer.core.yaml.YamlSnapshotSerializer;

public class JsonSnapshotSerializer extends YamlSnapshotSerializer {

    @Override
    public String[] getValidFileExtensions() {
        return new String[]{
                "json"
        };
    }

}
