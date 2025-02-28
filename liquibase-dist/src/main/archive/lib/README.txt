## Global Library Directory

This directory contains the global library files managed by the end user. Jar files in this directory are automatically added to all Liquibase CLI runs.
Any jar files you would like to include in only certain projects can be added to a `liquibase_libs` directory in your current working directory.

The libraries which ship with Liquibase can be found in the `LIQUIBASE_HOME/internal/lib` directory.

The extensions which ship with Liquibase can be found in the `LIQUIBASE_HOME/internal/extensions` directory.
