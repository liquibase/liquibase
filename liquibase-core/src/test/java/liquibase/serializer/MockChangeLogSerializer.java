package liquibase.serializer;

import liquibase.changelog.ChangeSet;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;


public class MockChangeLogSerializer implements ChangeLogSerializer {

    private String[] validExtensions;

    public MockChangeLogSerializer(String... validExtensions) {
        this.validExtensions = validExtensions;
    }

    @Override
    public String[] getValidFileExtensions() {
        return validExtensions;
    }

	@Override
    public void write(List<ChangeSet> changeSets, OutputStream out)
			throws IOException {
		;
	}

    @Override
    public void append(ChangeSet changeSet, File changeLogFile) throws IOException {

    }

    @Override
    public String serialize(LiquibaseSerializable object, boolean pretty) {
        return null;
    }
}
