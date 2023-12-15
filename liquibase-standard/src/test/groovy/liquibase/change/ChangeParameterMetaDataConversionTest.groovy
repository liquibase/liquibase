package liquibase.change

import liquibase.ExtensibleObject
import liquibase.ObjectMetaData
import liquibase.change.visitor.ChangeVisitor
import liquibase.changelog.ChangeSet
import liquibase.database.Database
import liquibase.exception.RollbackImpossibleException
import liquibase.exception.SetupException
import liquibase.exception.ValidationErrors
import liquibase.exception.Warnings
import liquibase.parser.core.ParsedNode
import liquibase.parser.core.ParsedNodeException
import liquibase.resource.ResourceAccessor
import liquibase.serializer.LiquibaseSerializable
import liquibase.statement.SqlStatement
import liquibase.structure.DatabaseObject
import spock.lang.Specification

class ChangeParameterMetaDataConversionTest extends Specification {

    def "supports invoking primitive setter with boxed values"() {
        given:
        def change = new PrimitiveSettersChange()
        def metaData = new ChangeParameterMetaData(change, parameterName, parameterName, "", new HashMap<String, Object>(), "", type, new String[0], new String[0], "", LiquibaseSerializable.SerializationType.DIRECT_VALUE)

        and:
        metaData.setValue(change, boxedValue)

        expect:
        getter(change) == expectedResult

        where:
        parameterName | type          | boxedValue                    | getter                                           | expectedResult
        "aBool"       | boolean.class | Boolean.TRUE                  | { PrimitiveSettersChange it -> it.isaBool() }    | true
        "aByte"       | byte.class    | Byte.valueOf((byte) 10)       | { PrimitiveSettersChange it -> it.getaByte() }   | (byte) 10
        "aChar"       | char.class    | Character.valueOf((char) 'a') | { PrimitiveSettersChange it -> it.getaChar() }   | (char) 'a'
        "aDouble"     | double.class  | Double.valueOf(20.0d)         | { PrimitiveSettersChange it -> it.getaDouble() } | 20.0d
        "aFloat"      | float.class   | Float.valueOf(30.0f)          | { PrimitiveSettersChange it -> it.getaFloat() }  | 30.0f
        "anInt"       | int.class     | Integer.valueOf(40)           | { PrimitiveSettersChange it -> it.getanInt() }   | 40
        "aLong"       | long.class    | Long.valueOf(50L)             | { PrimitiveSettersChange it -> it.getaLong() }   | 50L
        "aShort"      | short.class   | Short.valueOf((short) 60)     | { PrimitiveSettersChange it -> it.getaShort() }  | (short) 60
    }

}


class PrimitiveSettersChange implements Change {
    private boolean aBool;
    private byte aByte;
    private char aChar;
    private double aDouble;
    private float aFloat;
    private int anInt;
    private long aLong;
    private short aShort;

    public boolean isaBool() {
        return aBool;
    }

    public void setaBool(boolean aBool) {
        this.aBool = aBool;
    }

    public byte getaByte() {
        return aByte;
    }

    public void setaByte(byte aByte) {
        this.aByte = aByte;
    }

    public char getaChar() {
        return aChar;
    }

    public void setaChar(char aChar) {
        this.aChar = aChar;
    }

    public double getaDouble() {
        return aDouble;
    }

    public void setaDouble(double aDouble) {
        this.aDouble = aDouble;
    }

    public float getaFloat() {
        return aFloat;
    }

    public void setaFloat(float aFloat) {
        this.aFloat = aFloat;
    }

    public int getanInt() {
        return anInt;
    }

    public void setanInt(int anInt) {
        this.anInt = anInt;
    }

    public long getaLong() {
        return aLong;
    }

    public void setaLong(long aLong) {
        this.aLong = aLong;
    }

    public short getaShort() {
        return aShort;
    }

    public void setaShort(short aShort) {
        this.aShort = aShort;
    }

    @Override
    public SortedSet<String> getAttributes() {
        return null;
    }

    @Override
    public ObjectMetaData getObjectMetaData() {
        return null;
    }

    @Override
    public boolean has(String attribute) {
        return false;
    }

    @Override
    public List getValuePath(String attributePath, Class lastType) {
        return null;
    }

    @Override
    public <T> T get(String attribute, Class<T> type) {
        return null;
    }

    @Override
    public <T> T get(String attribute, T defaultValue) {
        return null;
    }

    @Override
    public ExtensibleObject set(String attribute, Object value) {
        return null;
    }

    @Override
    public String describe() {
        return null;
    }

    @Override
    public Object clone() {
        return null;
    }

    @Override
    public void finishInitialization() throws SetupException {

    }

    @Override
    public ChangeMetaData createChangeMetaData() {
        return null;
    }

    @Override
    public ChangeSet getChangeSet() {
        return null;
    }

    @Override
    public void setChangeSet(ChangeSet changeSet) {

    }

    @Override
    public void setResourceAccessor(ResourceAccessor resourceAccessor) {

    }

    @Override
    public boolean supports(Database database) {
        return false;
    }

    @Override
    public Warnings warn(Database database) {
        return null;
    }

    @Override
    public ValidationErrors validate(Database database) {
        return null;
    }

    @Override
    public Set<DatabaseObject> getAffectedDatabaseObjects(Database database) {
        return null;
    }

    @Override
    public CheckSum generateCheckSum() {
        return null;
    }

    @Override
    public String getConfirmationMessage() {
        return null;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[0];
    }

    @Override
    public boolean generateStatementsVolatile(Database database) {
        return false;
    }

    @Override
    public boolean supportsRollback(Database database) {
        return false;
    }

    @Override
    public SqlStatement[] generateRollbackStatements(Database database) throws RollbackImpossibleException {
        return new SqlStatement[0];
    }

    @Override
    public boolean generateRollbackStatementsVolatile(Database database) {
        return false;
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void modify(ChangeVisitor changeVisitor) throws ParsedNodeException {

    }

    @Override
    public String getSerializedObjectName() {
        return null;
    }

    @Override
    public Set<String> getSerializableFields() {
        return null;
    }

    @Override
    public Object getSerializableFieldValue(String field) {
        return null;
    }

    @Override
    public SerializationType getSerializableFieldType(String field) {
        return null;
    }

    @Override
    public String getSerializableFieldNamespace(String field) {
        return null;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return null;
    }

    @Override
    public void load(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException {

    }

    @Override
    public ParsedNode serialize() throws ParsedNodeException {
        return null;
    }


    @Override
    public String toString() {
        return "PrimitiveSettersChange{" +
                "aBool=" + aBool +
                ", aByte=" + aByte +
                ", aChar=" + aChar +
                ", aDouble=" + aDouble +
                ", aFloat=" + aFloat +
                ", anInt=" + anInt +
                ", aLong=" + aLong +
                ", aShort=" + aShort +
                '}';
    }
}
