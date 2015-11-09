package liquibase.structure.core;

import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.ObjectReference;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class UniqueConstraint extends AbstractDatabaseObject {

	public List<ObjectReference> columns = new ArrayList<>();
	public Boolean deferrable;
	public Boolean initiallyDeferred;
	public Boolean disabled;
	public ObjectReference backingIndex;
	public String tablespace;

	public UniqueConstraint() {
	}

	public UniqueConstraint(String name) {
		super(name);
	}

	public UniqueConstraint(ObjectReference nameAndContainer) {
		super(nameAndContainer);
	}

	public UniqueConstraint(ObjectReference container, String name) {
		super(container, name);
	}

	public UniqueConstraint(ObjectReference container, String name, ObjectReference table, String... columns) {
		super(container, name);
		for (String column : columns) {
			this.columns.add(new Column.ColumnReference(table, column));
		}
	}


	public ObjectReference getTableName() {
		if (columns == null || columns.size() == 0) {
			return null;
		}
		return columns.get(0).container;
	}

	@Override
	public String toString() {
		return getName() + "(" + StringUtils.join(this.columns, ",", new StringUtils.ToStringFormatter()) + ")";
	}

}