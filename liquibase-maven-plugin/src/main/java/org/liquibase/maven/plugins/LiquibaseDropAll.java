package org.liquibase.maven.plugins;

import liquibase.CatalogAndSchema;
import liquibase.Liquibase;
import liquibase.configuration.HubConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.exception.LiquibaseException;
import org.apache.maven.plugin.MojoFailureException;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Drops all database objects in the configured schema(s). Note that functions, procedures and packages are not dropped.</p>
 * 
 * @author Ferenc Gratzer
 * @description Liquibase DropAll Maven plugin
 * @goal dropAll
 * @since 2.0.2
 */
public class LiquibaseDropAll extends AbstractLiquibaseChangeLogMojo {

  	/**
		 *
  	 * The schemas to be dropped. Comma separated list.
  	 *
  	 * @parameter property="liquibase.schemas"
     *
  	 */
  	protected String schemas;

	  /**
  	 * Specifies the <i>Liquibase Hub Connection ID</i> for Liquibase to use.
  	 *
  	 * @parameter property="liquibase.hubConnectionId"
  	 *
  	 */
  	protected String hubConnectionId;

    protected String catalog;

  	@Override
  	protected void checkRequiredParametersAreSpecified() throws MojoFailureException {
    		//
	    	// Override because changeLogFile is not required
		    //
  			HubConfiguration hubConfiguration = LiquibaseConfiguration.getInstance().getConfiguration(HubConfiguration.class);
			  String liquibaseHubApiKey = hubConfiguration.getLiquibaseHubApiKey();
  			String hubMode = hubConfiguration.getLiquibaseHubMode();
  			if (liquibaseHubApiKey != null && ! hubMode.toLowerCase().equals("off")) {
  		  		if (hubConnectionId == null && changeLogFile == null) {
  		    			String errorMessage =
  									"\nThe dropAll command used with a hub.ApiKey and hub.mode='" + hubMode + "'\n" +
  									"can send reports to your Hub project. To enable this, please add the \n" +
  									"'--hubConnectionId =<hubConnectionId>' parameter to the CLI, or ensure\n" +
  									"a registered changelog file is passed in your defaults file or in the CLI.\n" +
  									"Learn more at https://hub.liquibase.com";
							getLog().warn(errorMessage);
  				}
        }
	  }

  	@Override
  	protected void performLiquibaseTask(Liquibase liquibase)
			throws LiquibaseException {
  		  super.performLiquibaseTask(liquibase);
  		  try {
				  	checkRequiredParametersAreSpecified();
				}
  		  catch (Exception e) {
  		      throw new LiquibaseException(e);
				}
    		if (schemas != null) {
            List<CatalogAndSchema> schemaObjs = new ArrayList<>();
            for (String name : schemas.split(",")) {
                schemaObjs.add(new CatalogAndSchema(catalog, name));
            }
	      		liquibase.dropAll(schemaObjs.toArray(new CatalogAndSchema[schemaObjs.size()]));
		    } else {
			      liquibase.dropAll();
		    }
	  }

	  @Override
	  protected void printSettings(String indent) {
	    	super.printSettings(indent);
		    getLog().info(indent + "schemas: " + schemas);
	  }
}
