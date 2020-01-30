########################################
GETTING STARTED
########################################

Welcome to Liquibase!

To get started, download your database's JDBC driver to the lib directory then run the `liquibase` script in the root of this directory.

########################################
EXAMPLE PROJECTS
########################################

The "examples" directory contains everything you need to start trying Liquibase against a sample in-memory database.

There are two projects under examples: one that uses an sql formatted changelog and one that uses an xml based one.
The setup and examples are the same for both, they only differ in what you write in your changelog files.


Starting the Database
---------------------
To start the example database, run `examples/start-h2`. Stop the database with ctrl-c.
This will start a local h2 database that listends on port 9090 as well as open a browser to the database console on port 9090.
NOTE: The database does not persist data, so when you stop the "start-h2" process, it will reset back to the starting state.

The start-h2 script will actually start two databases: a "dev" database which corresponds to what you'd use a local, developer database
and an "integration" database which corresponds to another database in your pipeline.

The web-based console allows you to see all objects in your database in the left navigation and run sql statements on the right side.
Along the top is a toolbar which includes a "Refresh" button as the 2nd to the left item.
You can use the refresh button to reload the left-side object view if you make changes to your database outside of the console (like with Liquibase).
We only auto-open a browser to the dev database, but you can use the provided link to view the integration database.
You can see which database you are connected to by looking at the url at the top of the object view.

If you've not used H2 before, it is just a standard SQL database so almost anything you can do in your database you can do in H2.
Try entering `create table test_table (id int)` in the text area and hit "Run" and you should see "TEST_TABLE" appear in the object view.


Setting Up Your Workspace
-------------------------
Copy the contents of EITHER the examples/sql or examples/xml directory to another location on your machine.
For example, a new USER_HOME/liquibase-test directory.

What got copied?
There will be a liquibase.properties file in the root of your workspace which provides default values for CLI arguments.
This file is pre-configured to point to the copied changelog as well as the dev database you started with `start-h2`.

There will be a sample changelog in the com/example directory.
While not required, it is a best practice to keep your changelogs in a unique and descriptive directory structure.
Normally they are stored in version control along with the rest of your application code and will match the directory structures you use there.


Running Your First Update
-------------------------
The main Liquibase command is "update" which applies any unrun changes in your changelog to your database.
From a command prompt in your workspace directory, run `liquibase update`.
You should see a message saying "Update has been successful"

If you go back to the dev database console in your browser and refresh, you should now see the following tables added to the object view:
- COMPANY
- DATABASECHANGELOG
- DATABASECHANGELOGLOCK
- PERSON

By running `liquibase update`, your database now matches the desired database state as defined by the changelog script.
The DATABASECHANGELOG and DATABASECHANGELOGLOCK tables are liquibase-metadata tables, COMPANY and PERSON are tables created by the changelog.

If you open com/example/sample.changelog.xml or com/example/sample.changelog.sql in your favorite text editor, you'll see how it defines a series of "changeSets".
Each changeSet is uniquely identified by a combination of the "id" and "author" fields, and it is by that combination that Liquibase tracks what has ran and what has not.
When you ran "update", Liquibase saw which changeSets had not been ran against your target database and ran them.


Adding New ChangeSets
---------------------
Now that your dev database matches the defined state, you can start adding the changes you need.

Suppose you realize you need a "works for" reference from the person tablet to the company table.

Open the sample changelog file in your existing editor, and (depending on your changelog format) add either:

    <changeSet id="3" author="your.name">
        <addColumn tableName="person">
            <column name="worksfor_company_id" type="int"/>
        </addColumn>
    </changeSet>

    <changeSet id="4" author="your.name">
        <addForeignKeyConstraint constraintName="fk_person_worksfor"
                                 baseTableName="person" baseColumnNames="worksfor_company_id"
                                 referencedTableName="company" referencedColumnNames="id"/>
    </changeSet>

OR

TODO:
    --changeSet
    ALTER TABLE PUBLIC.person ADD worksfor_company_id INT;

to the END of the file.

NOTE: it is a best practice to wrap every statement in its own changeset block.

You can now run `liquibase update` again, when you refresh your db console you will see the new column on the person table.


Promoting Changes
-----------------
Now that we know the database structure is what we want it to be, we are ready to apply the changes to our integration database.
Normally, you will have committed your changelog to version control and/or built an artifact containing it, but we will just run it directly against our other database.

Run `liquibase --url=jdbc:h2:tcp://localhost:9090/mem:integration update`.
By passing along the --url parameter, we are overriding that value from the liquibase.properties file. All the rest are still used.
After running update against the integration database, you should now see the COMPANY and PERSON tables in your integration web console.

Standard Development Workflow
-----------------------------
Lets try the standard workflow end-to-end.

First, pretending you are on your local development machine, add changeset(s) to your changelog for the next changes you want to make.
Its your choice what you to add.
Then run `liquibase update` to apply the changes to the database.
Feel free to iterate adding changesets and running update until you are happy with the final state.

Now, pretend you are considering promoting the changelog to the integration database.
It is always good to make sure you know what is going to be changed BEFORE applying the changelog. Especially early on in your process.
Remember: you're pretending that it is not the same person writing the changelog and promoting it to staging.

You can see that integration has changeSets not applied by running `liquibase --url=jdbc:h2:tcp://localhost:9090/mem:integration status --verbose`
You can see exactly what SQL would be ran by update by running `liquibase --url=jdbc:h2:tcp://localhost:9090/mem:integration updateSQL`
You can also see a sanity-check comparision by running `liquibase --referenceUrl=jdbc:h2:tcp://localhost:9090/mem:integration --referenceUsername dbuser --referencePassword letmein diff`

When you are happy with the changes that will be applied, run `liquibase --url=jdbc:h2:tcp://localhost:9090/mem:integration update` to apply the new changesets.
You can re-run the staus, updateSql, and diff commands as way to see that all the changes have now been applied to integration.

Now, just keep iterating on the "dev adds and tests changes" then "check and deploy to integration" cycle until you have a good feel for Liquibase.


Next Steps
----------
Now that you have a feel for Liquibase against your sample database, you can try it against your standard databases.

Add your database's jdbc drivers to the "lib" directory in the Liquibase install directory and update the url, username, and password in the liquibase.properties file.
Check your database's documentation for where to download the driver jar and the url format.
You can then try running the same changelog or a new one against your own database.

To learn more of what Liquibase can do for you, visit https://liquibase.org
