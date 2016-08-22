package liquibase.changelog;

import liquibase.ContextExpression;
import liquibase.serializer.AbstractLiquibaseSerializable;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class ChangeLogIncludeAll extends AbstractLiquibaseSerializable implements ChangeLogChild {
	private String path;
	private Boolean errorIfMissingOrEmpty;
	private Boolean relativeToChangelogFile;
	private String resourceFilter;
	private ContextExpression context;

	public ChangeLogIncludeAll() {

	}

	public ChangeLogIncludeAll(String path, Boolean errorIfMissingOrEmpty, Boolean relativeToChangelogFile, String resourceFilter,
			ContextExpression context) {
		this.path = path;
		this.errorIfMissingOrEmpty = errorIfMissingOrEmpty;
		this.relativeToChangelogFile = relativeToChangelogFile;
		this.resourceFilter = resourceFilter;
		this.context = context;
	}

	@Override
	public Set<String> getSerializableFields() {
		return new LinkedHashSet<String>(Arrays.asList(
				"path",
				"errorIfMissingOrEmpty",
				"relativeToChangelogFile",
				"resourceFilter",
				"context"));
	}

	@Override
	public String getSerializedObjectName() {
		return "includeAll";
	}

	@Override
	public String getSerializedObjectNamespace() {
		return STANDARD_CHANGELOG_NAMESPACE;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Boolean getErrorIfMissingOrEmpty() {
		return errorIfMissingOrEmpty;
	}

	public void setErrorIfMissingOrEmpty(Boolean errorIfMissingOrEmpty) {
		this.errorIfMissingOrEmpty = errorIfMissingOrEmpty;
	}

	public Boolean getRelativeToChangelogFile() {
		return relativeToChangelogFile;
	}

	public void setRelativeToChangelogFile(Boolean relativeToChangelogFile) {
		this.relativeToChangelogFile = relativeToChangelogFile;
	}

	public String getResourceFilter() {
		return resourceFilter;
	}

	public void setResourceFilter(String resourceFilter) {
		this.resourceFilter = resourceFilter;
	}

	public ContextExpression getContext() {
		return context;
	}

	public void setContext(ContextExpression context) {
		this.context = context;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof ChangeLogIncludeAll))
			return false;
		ChangeLogIncludeAll that = (ChangeLogIncludeAll) o;
		return ((path == that.path) || path != null && path.equals(that.path)) &&
				((errorIfMissingOrEmpty == that.errorIfMissingOrEmpty) || errorIfMissingOrEmpty != null && errorIfMissingOrEmpty
						.equals(that.errorIfMissingOrEmpty)) &&
				((relativeToChangelogFile == that.relativeToChangelogFile) || relativeToChangelogFile != null && relativeToChangelogFile
						.equals(that.relativeToChangelogFile)) &&
				((resourceFilter == that.resourceFilter) || resourceFilter != null && resourceFilter.equals(that.resourceFilter)) &&
				((context == that.context) || context != null && context.equals(that.context));
	}

	@Override
	public int hashCode() {
		int result = 7;
		result = 37 * result + (path != null ? path.hashCode() : 0);
		result = 37 * result + (errorIfMissingOrEmpty != null ? errorIfMissingOrEmpty.hashCode() : 0);
		result = 37 * result + (relativeToChangelogFile != null ? relativeToChangelogFile.hashCode() : 0);
		result = 37 * result + (resourceFilter != null ? resourceFilter.hashCode() : 0);
		result = 37 * result + (context != null ? context.hashCode() : 0);
		return result;
	}
}
