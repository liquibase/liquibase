package liquibase.changelog;

import liquibase.serializer.AbstractLiquibaseSerializable;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class ChangeLogProperty extends AbstractLiquibaseSerializable implements ChangeLogChild {
	private String file;
	private String name;
	private String value;
	private String context;
	private String labels;
	private String dbms;
	private Boolean global;

	public ChangeLogProperty() {
	}

	public ChangeLogProperty(String file, String name, String value, String context, String labels, String dbms, Boolean global) {
		this.file = file;
		this.name = name;
		this.value = value;
		this.context = context;
		this.labels = labels;
		this.dbms = dbms;
		this.global = global;
	}

	@Override
	public Set<String> getSerializableFields() {
		return new LinkedHashSet<String>(Arrays.asList(
				"file",
				"name",
				"value",
				"context",
				"labels",
				"dbms",
				"global"));
	}

	@Override
	public String getSerializedObjectName() {
		return "property";
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public String getLabels() {
		return labels;
	}

	public void setLabels(String labels) {
		this.labels = labels;
	}

	public String getDbms() {
		return dbms;
	}

	public void setDbms(String dbms) {
		this.dbms = dbms;
	}

	public Boolean getGlobal() {
		return global;
	}

	public void setGlobal(Boolean global) {
		this.global = global;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof ChangeLogProperty))
			return false;
		ChangeLogProperty that = (ChangeLogProperty) o;
		return ((file == that.file) || file != null && file.equals(that.file)) &&
				((name == that.name) || name != null && name.equals(that.name)) &&
				((value == that.value) || value != null && value.equals(that.value)) &&
				((context == that.context) || context != null && context.equals(that.context)) &&
				((labels == that.labels) || labels != null && labels.equals(that.labels)) &&
				((dbms == that.dbms) || dbms != null && dbms.equals(that.dbms)) &&
				((global == that.global) || global != null && global.equals(that.global));

	}

	@Override
	public int hashCode() {
		int result = 7;
		result = 37 * result + (file != null ? file.hashCode() : 0);
		result = 37 * result + (name != null ? name.hashCode() : 0);
		result = 37 * result + (value != null ? value.hashCode() : 0);
		result = 37 * result + (context != null ? context.hashCode() : 0);
		result = 37 * result + (labels != null ? labels.hashCode() : 0);
		result = 37 * result + (dbms != null ? dbms.hashCode() : 0);
		result = 37 * result + (global != null ? global.hashCode() : 0);
		return result;
	}

}
