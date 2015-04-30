package liquibase.change

import liquibase.exception.UnexpectedLiquibaseException
import liquibase.serializer.LiquibaseSerializable
import spock.lang.Specification
import spock.lang.Unroll

public class ConstraintsConfigTest extends Specification {

    def constructor() throws Exception {
        when:
        ConstraintsConfig constraints = new ConstraintsConfig();

        then:
        constraints.isDeleteCascade() == null
        constraints.isInitiallyDeferred() == null
        constraints.isNullable() == null
        constraints.isPrimaryKey() == null
        constraints.isUnique() == null
    }

    def setNullable() {
        expect:
        assert new ConstraintsConfig().setNullable(true).isNullable()
        assert !new ConstraintsConfig().setNullable(false).isNullable()
    }

    def setNullable_string() {
        expect:
        assert new ConstraintsConfig().setNullable("true").isNullable()
        assert new ConstraintsConfig().setNullable("TRUE").isNullable()
        assert new ConstraintsConfig().setNullable("1").isNullable()

        assert !new ConstraintsConfig().setNullable("false").isNullable()
        assert !new ConstraintsConfig().setNullable("FALSE").isNullable()
        assert !new ConstraintsConfig().setNullable("0").isNullable()

        new ConstraintsConfig().setNullable("").isNullable() == null
        new ConstraintsConfig().setNullable("null").isNullable() == null
        new ConstraintsConfig().setNullable("NULL").isNullable() == null
        def constraint = new ConstraintsConfig().setNullable((String) null)
        constraint.isNullable() == null
    }


    def setDeleteCascade() {
        expect:
        assert new ConstraintsConfig().setDeleteCascade(true).isDeleteCascade()
        assert !new ConstraintsConfig().setDeleteCascade(false).isDeleteCascade()
    }

    def setDeleteCascade_string() {
        expect:
        assert new ConstraintsConfig().setDeleteCascade("true").isDeleteCascade()
        assert new ConstraintsConfig().setDeleteCascade("TRUE").isDeleteCascade()
        assert new ConstraintsConfig().setDeleteCascade("1").isDeleteCascade()

        assert !new ConstraintsConfig().setDeleteCascade("false").isDeleteCascade()
        assert !new ConstraintsConfig().setDeleteCascade("FALSE").isDeleteCascade()
        assert !new ConstraintsConfig().setDeleteCascade("0").isDeleteCascade()

        new ConstraintsConfig().setDeleteCascade("").isDeleteCascade() == null
        new ConstraintsConfig().setDeleteCascade("null").isDeleteCascade() == null
        new ConstraintsConfig().setDeleteCascade("NULL").isDeleteCascade() == null
        def constraint = new ConstraintsConfig().setDeleteCascade((String) null)
        constraint.isDeleteCascade() == null
    }


    def setInitiallyDeferred() {
        expect:
        assert new ConstraintsConfig().setInitiallyDeferred(true).isInitiallyDeferred()
        assert !new ConstraintsConfig().setInitiallyDeferred(false).isInitiallyDeferred()
    }

    def setInitiallyDeferred_string() {
        expect:
        assert new ConstraintsConfig().setInitiallyDeferred("true").isInitiallyDeferred()
        assert new ConstraintsConfig().setInitiallyDeferred("TRUE").isInitiallyDeferred()
        assert new ConstraintsConfig().setInitiallyDeferred("1").isInitiallyDeferred()

        assert !new ConstraintsConfig().setInitiallyDeferred("false").isInitiallyDeferred()
        assert !new ConstraintsConfig().setInitiallyDeferred("FALSE").isInitiallyDeferred()
        assert !new ConstraintsConfig().setInitiallyDeferred("0").isInitiallyDeferred()

        new ConstraintsConfig().setInitiallyDeferred("").isInitiallyDeferred() == null
        new ConstraintsConfig().setInitiallyDeferred("null").isInitiallyDeferred() == null
        new ConstraintsConfig().setInitiallyDeferred("NULL").isInitiallyDeferred() == null
        def constraint = new ConstraintsConfig().setInitiallyDeferred((String) null)
        constraint.isInitiallyDeferred() == null
    }


    def setPrimaryKey() {
        expect:
        assert new ConstraintsConfig().setPrimaryKey(true).isPrimaryKey()
        assert !new ConstraintsConfig().setPrimaryKey(false).isPrimaryKey()
    }

    def setPrimaryKey_string() {
        expect:
        assert new ConstraintsConfig().setPrimaryKey("true").isPrimaryKey()
        assert new ConstraintsConfig().setPrimaryKey("TRUE").isPrimaryKey()
        assert new ConstraintsConfig().setPrimaryKey("1").isPrimaryKey()

        assert !new ConstraintsConfig().setPrimaryKey("false").isPrimaryKey()
        assert !new ConstraintsConfig().setPrimaryKey("FALSE").isPrimaryKey()
        assert !new ConstraintsConfig().setPrimaryKey("0").isPrimaryKey()

        new ConstraintsConfig().setPrimaryKey("").isPrimaryKey() == null
        new ConstraintsConfig().setPrimaryKey("null").isPrimaryKey() == null
        new ConstraintsConfig().setPrimaryKey("NULL").isPrimaryKey() == null
        def constraint = new ConstraintsConfig().setPrimaryKey((String) null)
        constraint.isPrimaryKey() == null
    }


    def setUnique() {
        expect:
        assert new ConstraintsConfig().setUnique(true).isUnique()
        assert !new ConstraintsConfig().setUnique(false).isUnique()
    }

    def setUnique_string() {
        expect:
        assert new ConstraintsConfig().setUnique("true").isUnique()
        assert new ConstraintsConfig().setUnique("TRUE").isUnique()
        assert new ConstraintsConfig().setUnique("1").isUnique()

        assert !new ConstraintsConfig().setUnique("false").isUnique()
        assert !new ConstraintsConfig().setUnique("FALSE").isUnique()
        assert !new ConstraintsConfig().setUnique("0").isUnique()

        new ConstraintsConfig().setUnique("").isUnique() == null
        new ConstraintsConfig().setUnique("null").isUnique() == null
        new ConstraintsConfig().setUnique("NULL").isUnique() == null
        def constraint = new ConstraintsConfig().setUnique((String) null)
        constraint.isUnique() == null
    }


    def setDeferrable() {
        expect:
        assert new ConstraintsConfig().setDeferrable(true).isDeferrable()
        assert !new ConstraintsConfig().setDeferrable(false).isDeferrable()
    }

    def setDeferrable_string() {
        expect:
        assert new ConstraintsConfig().setDeferrable("true").isDeferrable()
        assert new ConstraintsConfig().setDeferrable("TRUE").isDeferrable()
        assert new ConstraintsConfig().setDeferrable("1").isDeferrable()

        assert !new ConstraintsConfig().setDeferrable("false").isDeferrable()
        assert !new ConstraintsConfig().setDeferrable("FALSE").isDeferrable()
        assert !new ConstraintsConfig().setDeferrable("0").isDeferrable()

        new ConstraintsConfig().setDeferrable("").isDeferrable() == null
        new ConstraintsConfig().setDeferrable("null").isDeferrable() == null
        new ConstraintsConfig().setDeferrable("NULL").isDeferrable() == null
        def constraint = new ConstraintsConfig().setDeferrable((String) null)
        constraint.isDeferrable() == null
    }

    def setDeferrable_badString() {
        when:
        new ConstraintsConfig().setDeferrable("bad val");

        then:
        thrown(UnexpectedLiquibaseException)
    }

    def setPrimaryKeyName() {
        expect:
        new ConstraintsConfig().setPrimaryKeyName("xyz").getPrimaryKeyName() == "xyz"
    }

    def setPrimaryKeyTablespace() {
        expect:
        new ConstraintsConfig().setPrimaryKeyTablespace("xyz").getPrimaryKeyTablespace() == "xyz"
    }

    def setForeignKeyName() {
        expect:
        new ConstraintsConfig().setForeignKeyName("xyz").getForeignKeyName() == "xyz"
    }

    def setCheck() {
        expect:
        new ConstraintsConfig().setCheckConstraint("xyz").getCheckConstraint() == "xyz"
    }

    def setUniqueConstraintName() {
        expect:
        new ConstraintsConfig().setUniqueConstraintName("xyz").getUniqueConstraintName() == "xyz"
    }

    def setReferences() {
        expect:
        new ConstraintsConfig().setReferences("xyz").getReferences() == "xyz"
    }

    def getSerializedObjectName() {
        expect:
        new ConstraintsConfig().getSerializedObjectName() == "constraints"
    }

    def getFieldsToSerialize() {
        when:
        Set<String> fields = new ConstraintsConfig().getSerializableFields();

        then:
        assert fields.contains("nullable");
        assert fields.contains("primaryKey");
        assert fields.contains("primaryKeyName");
        assert fields.contains("nullable");
    }

    def getSerializableFieldValue() {
        expect:
        new ConstraintsConfig().getSerializableFieldValue("nullable") == null
        assert new ConstraintsConfig().setNullable(true).getSerializableFieldValue("nullable")

    }

    def getFieldSerializationType() {
        expect:
        new ConstraintsConfig().getSerializableFieldType("anythiny") == LiquibaseSerializable.SerializationType.NAMED_FIELD
    }

    @Unroll("#featureName: #expected")
    def "serialize"() {
        expect:
        config.serialize().toString() == expected

        where:
        config | expected
        new ConstraintsConfig() | "constraints"
        new ConstraintsConfig().setNullable(true) | "constraints[nullable=true]"
        new ConstraintsConfig().setNullable(true).setPrimaryKey(true) | "constraints[nullable=true,primaryKey=true]"
    }
}
