package liquibase.structure.core;

import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;
import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;
import liquibase.structure.ObjectName;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UniqueConstraint extends AbstractDatabaseObject {

	public List<ObjectName> columns = new ArrayList<>();
	public Boolean deferrable;
	public Boolean initiallyDeferred;
	public Boolean disabled;
	public ObjectName backingIndex;
	public String tablespace;

	public UniqueConstraint() {
	}

	public UniqueConstraint(ObjectName name, String... columns) {
		setName(name);
		ObjectName tableName = name.container;

		for (String columnName : columns) {
			this.columns.add(new ObjectName(tableName, columnName));
		}
	}


	@Override
	public DatabaseObject[] getContainingObjects() {
		return null;
	}

	@Override
	public Schema getSchema() {
		return null;
	}

	public ObjectName getTableName() {
		if (name == null) {
			return null;
		}
		return name.container;
	}

	@Override
	public int compareTo(Object other) {
		UniqueConstraint that = (UniqueConstraint) other;

		if (that == null) {
			return -1;
		}

		ObjectName thisTableName = getTableName();
		ObjectName thatTableName = that.getTableName();


		if (thisTableName != null && thatTableName != null) {
			return thisTableName.compareTo(thatTableName);
		} else {
			if (this.getSimpleName() == null) {
				if (that.getSimpleName() == null) {
					return 0;
				} else {
					return 1;
				}
			} else {
				return this.getSimpleName().compareTo(that.getSimpleName());
			}
		}
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		UniqueConstraint that = (UniqueConstraint) o;

		ObjectName thisTableName = getTableName();
		ObjectName thatTableName = that.getTableName();

		if (thisTableName != null && thatTableName != null) {
			return thisTableName.equals(thatTableName);
		} else {
			if (this.getSimpleName() == null) {
				return that.getSimpleName() == null;
			} else {
				return this.getSimpleName().equals(that.getSimpleName());
			}
		}
	}

	@Override
	public int hashCode() {
		int result;
		if (name == null) {
			return 0;
		}

		ObjectName tableName = getTableName();
		if (tableName == null) {
			return 0;
		} else {
			return tableName.hashCode();
		}
	}

	@Override
	public String toString() {
		return getName() + "(" + StringUtils.join(this.columns, ",", new StringUtils.ToStringFormatter()) + ")";
	}

}