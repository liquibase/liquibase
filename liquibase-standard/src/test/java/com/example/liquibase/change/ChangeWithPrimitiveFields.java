package com.example.liquibase.change;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;

@DatabaseChange(
        name = "primitiveChange",
        description = "Used in unit tests",
        priority = ChangeMetaData.PRIORITY_DEFAULT
)
public class ChangeWithPrimitiveFields extends AbstractChange {

    private boolean aBoolean;
    private byte aByte;
    private char aChar;
    private double aDouble;
    private float aFloat;
    private int anInt;
    private long aLong;
    private short aShort;

    @Override
    public String getConfirmationMessage() {
        return "Test confirmation message";
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return SqlStatement.EMPTY_SQL_STATEMENT;
    }

    public boolean isaBoolean() {
        return aBoolean;
    }

    public void setaBoolean(boolean aBoolean) {
        this.aBoolean = aBoolean;
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

    public int getAnInt() {
        return anInt;
    }

    public void setAnInt(int anInt) {
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
    public Object clone() {
        return super.clone();
    }
}
