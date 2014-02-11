This directory allows you both run Liquibase in a normal production setting to manage your database as well as develop
and test Liquibase extensions.

ROOT DIRECTORY STRUCTURE:
----------------------------------------

The root of this directory is designed to run Liquibase. There are shell and batch scripts (liquibase and liquibase.bat)
which will run the Liquibase command line application.

The "lib" directory is automatically scanned by the liquibase scripts and all .jar files are added to the classpath.
If you have JDBC drivers or Liquibase extensions that you want to be automatically included, add them to this directory.

The "lib-other" directory is not automatically scanned by the liquibase scripts. This directory can be used to store jar
files that are referenced with manual --classpath references. Storing JDBC drivers in lib-other instead of lib allows
you to control which versions of the driver are used by liquibase. Storing extensions in lib-other instead of lib allows
you to control which extensions are used by liquibase.

The "sdk" directory is designed to allow you to develop and test Liquibase extensions as well as test Liquibase itself.
See below for more information.


SDK DIRECTORY STRUCTURE
----------------------------------------

** For more information, see http://liquibase.org/documentation/sdk**

The "sdk" directory contains liquibase-sdk shell and batch scripts for running the Liquibase SDK application.
The Liquibase-sdk application allows you to create and manage test databases in virtual machines, execute tests, and more.

The "javadoc" directory contains the Liquibase core library API documentation.

The "lib-sdk" directory is used to store jars used by liquibase-sdk but not standard liquibase usage. Anything added to
this directory is automatically included in the liquibase-sdk classpath, but if you have any additional jars to
include, add them to LIQUIBASE_HOME/lib or LIQUIBASE_HOME/lib-other instead of here.

The "vagrant" directory is where images created by the "liquibase-sdk vagrant" command are stored.
See below for more information.

The "workspace" directory is a simple structure designed to allow testing of liquibase and liquibase extensions. For
real usage, you should have your changelog files managed with your application code but the workspace directory has a
starting example changelog as well as properties files for several databases that leverage the virtual machines managed
through the "liqubiase-sdk vagrant" command.

VAGRANT USAGE
----------------------------------------

You can easily create databases for testing Liquibase using the vagrant configurations the vagrant directory and with
the "liquibase-sdk vagrant" command.

For commercial databases, the generated vagrant configurations expect the installers/zip/etc. files to be placed in
LIQUIBASE_HOME/sdk/vagrant/install-files/PRODUCT. Where PRODUCT is "oracle", "mssql", "windows" etc. Running
"liquibase-sdk vagrant init" should give you information on what files are expected in this directory.