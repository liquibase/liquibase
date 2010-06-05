/*
 * Copyright (C) 2005 Whitestein Technologies AG, Poststrasse 22, CH-6300 Zug, Switzerland.
 * All rights reserved. The use of this file in source or binary form requires a written license from Whitestein Technologies AG.
 *
 */
package liquibase.dbtest.maxdb;

import liquibase.dbtest.AbstractIntegrationTest;

/**
 * create tablespace liquibase2 datafile 'C:\ORACLEXE\ORADATA\XE\LIQUIBASE2.DBF' SIZE 5M autoextend on next 5M
 */
public class MaxDbIntegrationTest extends AbstractIntegrationTest {

  public MaxDbIntegrationTest() throws Exception {
      super("maxdb", "jdbc:sapdb://"+ getDatabaseServerHostname("MaxDB") +"/liquibas");
  }

}
