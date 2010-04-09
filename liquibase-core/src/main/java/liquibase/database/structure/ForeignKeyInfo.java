package liquibase.database.structure;

/**
 * User: Nikitin.Maxim
 * Date: 08.04.2010
 * Time: 12:48:05
 * <br><br>
 * <b>Descrition:</b><br>
 * Class used only as container of FK properties.
 */
public class ForeignKeyInfo {

	private String fkName;
	private String fkSchema;
	private String fkTableName;
	private String fkColumn;

	private String pkTableName;
	private String pkColumn;

	private int keySeq = 0;
	private ForeignKeyConstraintType updateRule;
	private ForeignKeyConstraintType deleteRule;
	private short deferrablility = 0;

	// Some databases supports creation of FK with referention to column marked as unique, not primary
	// If FK referenced to such unique column this option should be set to false
	private boolean referencedToPrimary = true;


	public String getFkName() {
		return fkName;
	}

	public void setFkName(String fkName) {
		this.fkName = fkName;
	}

	public String getFkSchema() {
		return fkSchema;
	}

	public void setFkSchema(String fkSchema) {
		this.fkSchema = fkSchema;
	}

	public String getFkTableName() {
		return fkTableName;
	}

	public void setFkTableName(String fkTableName) {
		this.fkTableName = fkTableName;
	}

	public String getFkColumn() {
		return fkColumn;
	}

	public void setFkColumn(String fkColumn) {
		this.fkColumn = fkColumn;
	}

	public String getPkTableName() {
		return pkTableName;
	}

	public void setPkTableName(String pkTableName) {
		this.pkTableName = pkTableName;
	}

	public String getPkColumn() {
		return pkColumn;
	}

	public void setPkColumn(String pkColumn) {
		this.pkColumn = pkColumn;
	}

	public int getKeySeq() {
		return keySeq;
	}

	public void setKeySeq(int keySeq) {
		this.keySeq = keySeq;
	}

	public ForeignKeyConstraintType getUpdateRule() {
		return updateRule;
	}

	public void setUpdateRule(ForeignKeyConstraintType updateRule) {
		this.updateRule = updateRule;
	}

	public ForeignKeyConstraintType getDeleteRule() {
		return deleteRule;
	}

	public void setDeleteRule(ForeignKeyConstraintType deleteRule) {
		this.deleteRule = deleteRule;
	}

	public short getDeferrablility() {
		return deferrablility;
	}

	public void setDeferrablility(short deferrablility) {
		this.deferrablility = deferrablility;
	}

	public boolean isReferencedToPrimary() {
		return referencedToPrimary;
	}

	public void setReferencedToPrimary(boolean referencedToPrimary) {
		this.referencedToPrimary = referencedToPrimary;
	}
}