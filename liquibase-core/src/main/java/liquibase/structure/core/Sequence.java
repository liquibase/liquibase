package liquibase.structure.core;

import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;
import liquibase.util.StringUtil;

import java.math.BigInteger;

public class Sequence extends AbstractDatabaseObject {

    public Sequence() {
    }

    public Sequence(String catalogName, String schemaName, String sequenceName) {
        this.setSchema(new Schema(catalogName, schemaName));
        this.setName(sequenceName);
    }

    @Override
    public DatabaseObject[] getContainingObjects() {
        return null;
    }

    @Override
    public String getName() {
        return getAttribute("name", String.class);
    }

    @Override
    public Sequence setName(String name) {
        this.setAttribute("name", name);
        return this;
    }

    public BigInteger getStartValue() {
        return getAttribute("startValue", BigInteger.class);
    }

    public Sequence setStartValue(BigInteger startValue) {
        this.setAttribute("startValue", startValue);
        return this;
    }

    public BigInteger getIncrementBy() {
        return getAttribute("incrementBy", BigInteger.class);
    }

    public Sequence setIncrementBy(BigInteger incrementBy) {
        this.setAttribute("incrementBy", incrementBy);
        return this;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if ((o == null) || (getClass() != o.getClass())) return false;

        Sequence sequence = (Sequence) o;

        if ((this.getSchema() != null) && (sequence.getSchema() != null)) {
            return StringUtil.trimToEmpty(this.getSchema().getName()).equalsIgnoreCase(StringUtil.trimToEmpty(sequence.getSchema().getName()));
        }


        return !((getName() != null) ? !getName().equalsIgnoreCase(sequence.getName()) : (sequence.getName() != null));

    }

    @Override
    public int hashCode() {
        return ((getName() != null) ? getName().toUpperCase().hashCode() : 0);
    }


    @Override
    public String toString() {
        return getName();
    }

	/**
	 * @return Returns the schema.
	 */
	@Override
    public Schema getSchema () {
		return getAttribute("schema", Schema.class);
	}

	/**
	 * @param schema The schema to set.
	 */
	public Sequence setSchema (Schema schema) {
		this.setAttribute("schema", schema);
        return this;
	}

    public Sequence setSchema(String catalog, String schema) {
        return setSchema(new Schema(catalog, schema));
    }

    public BigInteger getMinValue() {
        return getAttribute("minValue", BigInteger.class);
    }

    public Sequence setMinValue(BigInteger minValue) {
        this.setAttribute("minValue", minValue);
        return this;
    }

    public BigInteger getMaxValue() {
        return getAttribute("maxValue", BigInteger.class);
    }

    public Sequence setMaxValue(BigInteger maxValue) {
        this.setAttribute("maxValue", maxValue);

        return this;
    }

    public Boolean getWillCycle() {
        return getAttribute("willCycle", Boolean.class);
    }

    public Sequence setWillCycle(Boolean willCycle) {
        this.setAttribute("willCycle", willCycle);
        return this;
    }

    public Boolean getOrdered() {
        return getAttribute("ordered", Boolean.class);
    }

    public Sequence setOrdered(Boolean isOrdered) {
        this.setAttribute("ordered", isOrdered);

        return this;
    }

    public BigInteger getLastReturnedValue() {
        return getAttribute("lastReturnedValue", BigInteger.class);
    }

    public Sequence setLastReturnedValue(BigInteger lastReturnedValue) {
        this.setAttribute("lastReturnedValue", lastReturnedValue);

        return this;
    }

    public BigInteger getCacheSize() {
        return getAttribute("cacheSize", BigInteger.class);
    }

    public Sequence setCacheSize(BigInteger cacheSize) {
        this.setAttribute("cacheSize", cacheSize);

        return this;
    }
}
