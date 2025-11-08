package liquibase.change;

import liquibase.Scope;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.resource.Resource;
import liquibase.resource.ResourceAccessor;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

import static org.apache.commons.lang3.ObjectUtils.getIfNull;

public interface HasFileProperty extends Change {
	Boolean isRelativeToChangelogFile();
	void setRelativeToChangelogFile(Boolean b);
	String file();
	void file(String file);
	String getEncoding();

	default void setFile(String file) {
		file(file);
		if(isRelativeToChangelogFile() == null && StringUtils.trimToEmpty(file).startsWith(".")) {
			setRelativeToChangelogFile(true);
		}
	}

	default String relativeTo() {
		return getIfNull( isRelativeToChangelogFile(), false ) ?
				getChangeSet().getChangeLog().getPhysicalFilePath() : null;
	}

	default Resource getResource() throws IOException {
		ResourceAccessor resourceAccessor = Scope.getCurrentScope().getResourceAccessor();
		if (resourceAccessor == null) {
			throw new UnexpectedLiquibaseException("No resourceAccessor specified for file '" + file() + "'");
		}
		return resourceAccessor.getExistingFile(file(), relativeTo(), locationReference("path") );
	}

//	default UnexpectedLiquibaseException asUnexpectedLiquibaseException(IOException ioex) {
//		return new UnexpectedLiquibaseException(ioex.getMessage() + " set for '" + getSerializedObjectName() + "' in " + getChangeSet());
//	}
}
