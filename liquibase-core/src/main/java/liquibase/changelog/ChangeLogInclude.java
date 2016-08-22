package liquibase.changelog;

import liquibase.ContextExpression;
import liquibase.serializer.AbstractLiquibaseSerializable;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class ChangeLogInclude extends AbstractLiquibaseSerializable implements ChangeLogChild {
	private String file;
	private Boolean relativeToChangelogFile;
	private ContextExpression context;

	public ChangeLogInclude() {

	}

	public ChangeLogInclude(String file, Boolean relativeToChangelogFile, ContextExpression context) {
		this.file = file;
		this.relativeToChangelogFile = relativeToChangelogFile;
		this.context = context;
	}

	@Override
	public Set<String> getSerializableFields() {
		return new LinkedHashSet<String>(Arrays.asList(
				"file",
				"relativeToChangelogFile",
				"context"));
	}

	@Override
	public String getSerializedObjectName() {
		return "include";
	}

	@Override
	public String getSerializedObjectNamespace() {
		return STANDARD_CHANGELOG_NAMESPACE;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public Boolean getRelativeToChangelogFile() {
		return relativeToChangelogFile;
	}

	public void setRelativeToChangelogFile(Boolean relativeToChangelogFile) {
		this.relativeToChangelogFile = relativeToChangelogFile;
	}

	public ContextExpression getContext() {
		return context;
	}

	public void setContext(ContextExpression context) {
		this.context = context;
	}

	@Override
	public int hashCode() {
		int result = 7;
		result = 37 * result + (file != null ? file.hashCode() : 0);
		result = 37 * result + (relativeToChangelogFile != null ? relativeToChangelogFile.hashCode() : 0);
		result = 37 * result + (context != null ? context.hashCode() : 0);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		final ChangeLogInclude that = (ChangeLogInclude) obj;
		return ((file == that.file) || file != null && file.equals(that.file)) &&
				((relativeToChangelogFile == that.relativeToChangelogFile) || relativeToChangelogFile != null && relativeToChangelogFile
						.equals(that.relativeToChangelogFile)) &&
				((context == that.context) || context != null && context.equals(that.context));
	}
}
