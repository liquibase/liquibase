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

The "sdk" directory contains liquibase-sdk shell and batch scripts for running the Liquibase SDK application.
The Liquibase-sdk application allows you to create and manage test databases in virtual machines, execute tests, and more.
For more information, see http://liquibase.org/documentation/sdk

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

Standard Network Setup:

    The vagrant boxes are configured to create host-only IPs on 10.10.100.100 range.
    Firewalls are disabled since they are host-only. Check the output of "liquibase-sdk vagrant init" for any variation

Standard Database Setup:

    Databases are configured with a username of "liquibase" and a password of "liquibase". If that is combination is not
    supported on a database, something as close as possible will be used.
    Check the output of "liquibase-sdk vagrant init" for any variation

    If the database supports catalogs, a catalog called "liquibase" is created
    If the database supports schemas, a schema called "lqschema" is created.
    Check the output of "liquibase-sdk vagrant init" for any variation

Provisioning:

    Provisioning in Vagrant is handed primarily by puppet, although there is a shell provisioning that is done first to
    bootstrap anything that cannot be done in puppet. The starting puppet file will be manifests/init.pp within each
    vagrant config. For example, linux-standard/manifests/init.pp

Windows Vagrant Box:

    To run windows-based databases, you need a base windows box. The easiest way is to download the Windows 2008R2 180 day trial
    image from http://www.microsoft.com/en-us/download/confirmation.aspx?id=16572. Since it is a trial that times out, you will need
    to re-do this process every 180 days. If you have a valid MSDN license you can use the same process to create a image that does not time out.

    Once downloaded, create a new VirtualBox machine to be your vagrant base box. In the initial wizard, do not create or a virtual hard drive because
    attaching the .vhd you downloaded will attach it as a SATA drive which does not boot. Once it is created, go to the Storage settings and add the downloaded
    .vhd in the IDE controller. The trial server Administrator password is Pass@word1

    Once booted, follow the steps in https://github.com/WinRb/vagrant-windows.
    Before "vagrant package":
       - Make sure to set the host name to "vagrant" and create a "vagrant" user as administrator
       - Install VirtualBox guest tools
       - Disable audio and USB (need to change mouse to ps/2 to disable usb)
       - Activate Windows
       - Install windows updates (it may take several rounds)
       - Disable firewall
       - Install puppet from http://puppetlabs.com/misc/download-options
       - Enable remote desktop (optional)
   Package with: vagrant package --base YOUR_VIRTUALBOX_MACHINE_NAME
   Install with: vagrant box add liquibase.windows.2008r2.x64 package.box




