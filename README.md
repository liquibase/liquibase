# dbmanul
A change management system for databases based on Liquibase

This fork of the Liquibase source aims to provide:
- More thorough integration testing
- Better Oracle database support
- Supporting different connections for changesets (e.g. use an administrative connection for creating tablespaces and a "regular" connection for normal schema updates)

PLEASE NOTE THAT THE FORK IS CURRENTLY UNSTABLE. Most integration tests do not work, and the Debian and RPM builds are most likely broken. This will be fixed shortly.
