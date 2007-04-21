package liquibase.migrator.preconditions;

import java.sql.SQLException;

import liquibase.migrator.Migrator;

public class Dbms {
	private String type;
	public Dbms() {
	  type ="";	
	}
	
	public String getType() {
		return type;
	}
	public void setType(String atype) {
		this.type = atype.toLowerCase();
	}
    public boolean checkDatabaseType(Migrator migrator) {
    	
    	String product;
	  	boolean flag = true;
		try {
			product = migrator.getDatabase().getConnection().getMetaData().getDatabaseProductName();
	
	    	if(type.equals(product.toLowerCase()))
	    		flag = true;
	    	else 
	    		flag = false;
	    	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 
    	return flag;
    }
}
