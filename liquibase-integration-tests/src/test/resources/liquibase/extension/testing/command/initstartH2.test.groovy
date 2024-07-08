package liquibase.extension.testing.command

CommandTests.define {

    command = ["init", "startH2"]

    signature = """
Short Description: Launches H2, an included open source in-memory database. This Java application is shipped with Liquibase, and is useful in the Getting Started experience and for testing out Liquibase commands.
Long Description: NOT SET
Required Args:
  NONE
Optional Args:
  bindAddress (String) Network address to bind to
    Default: 127.0.0.1
  dbPort (Integer) Port to run h2 database on
    Default: 9090
  detached (Boolean) When set to true, Liquibase initiates the H2 database in a new thread without blocking, allowing use within the flow command. Regardless of the parameter setting, data stored in the H2 database is cleared when the JVM exits, such as at the end of the flow command.
    Default: false
  launchBrowser (Boolean) Whether to open a browser to the database's web interface
    Default: true
  password (String) Password to use for created h2 user
    Default: letmein
    OBFUSCATED
  username (String) Username to create in h2
    Default: dbuser
  webPort (Integer) Port to run h2's web interface on
    Default: 8080
"""
}
