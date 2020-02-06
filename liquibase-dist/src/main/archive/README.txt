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
