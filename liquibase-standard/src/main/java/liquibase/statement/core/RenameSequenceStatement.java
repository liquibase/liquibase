package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class RenameSequenceStatement  extends AbstractSqlStatement {
  
  private final String catalogName;
  private final String schemaName;
  private final String oldSequenceName;
  private final String newSequenceName;

  public RenameSequenceStatement(String catalogName, String schemaName, String oldSequenceName, String newSequenceName) {
      this.catalogName = catalogName;
      this.schemaName = schemaName;
      this.oldSequenceName = oldSequenceName;
      this.newSequenceName = newSequenceName;
  }

  public String getCatalogName() {
      return catalogName;
  }

  public String getSchemaName() {
      return schemaName;
  }

  public String getOldSequenceName() {
      return oldSequenceName;
  }

  public String getNewSequenceName() {
      return newSequenceName;
  }
}
