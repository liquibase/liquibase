package liquibase.structure.core;

import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;
import liquibase.structure.ObjectName;

import java.math.BigInteger;

public class Sequence extends AbstractDatabaseObject {

    public Sequence() {
    }

    public Sequence(ObjectName name) {
        setName(name);
    }

    @Override
    public DatabaseObject[] getContainingObjects() {
        return null;
    }

    public BigInteger getStartValue() {
        return get("startValue", BigInteger.class);
    }

    public Sequence setStartValue(BigInteger startValue) {
        this.set("startValue", startValue);
        return this;
    }

    public BigInteger getIncrementBy() {
        return get("incrementBy", BigInteger.class);
    }

    public Sequence setIncrementBy(BigInteger incrementBy) {
        this.set("incrementBy", incrementBy);
        return this;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Sequence sequence = (Sequence) o;

        return !(getName() != null ? !getName().equalsIgnoreCase(sequence.getName()) : sequence.getName() != null);

    }

	/**
	 * @return Returns the schema.
	 */
	@Override
    public Schema getSchema () {
		return get("schema", Schema.class);
	}

	/**
	 * @param schema The schema to set.
	 */
	public Sequence setSchema (Schema schema) {
		this.set("schema", schema);
        return this;
	}

    public Sequence setSchema(String catalog, String schema) {
        return setSchema(new Schema(catalog, schema));
    }

    public BigInteger getMinValue() {
        return get("minValue", BigInteger.class);
    }

    public Sequence setMinValue(BigInteger minValue) {
        this.set("minValue", minValue);
        return this;
    }

    public BigInteger getMaxValue() {
        return get("maxValue", BigInteger.class);
    }

    public Sequence setMaxValue(BigInteger maxValue) {
        this.set("maxValue", maxValue);

        return this;
    }

    public Boolean getWillCycle() {
        return get("willCycle", Boolean.class);
    }

    public Sequence setWillCycle(Boolean willCycle) {
        this.set("willCycle", willCycle);
        return this;
    }

    public Boolean getOrdered() {
        return get("ordered", Boolean.class);
    }

    public Sequence setOrdered(Boolean isOrdered) {
        this.set("ordered", isOrdered);

        return this;
    }

    public BigInteger getLastReturnedValue() {
        return get("lastReturnedValue", BigInteger.class);
    }

    public Sequence setLastReturnedValue(BigInteger lastReturnedValue) {
        this.set("lastReturnedValue", lastReturnedValue);

        return this;
    }

    public BigInteger getCacheSize() {
        return get("cacheSize", BigInteger.class);
    }

    public Sequence setCacheSize(BigInteger cacheSize) {
        this.set("cacheSize", cacheSize);

        return this;
    }
}
