package liquibase.sdk.vagrant;

import liquibase.command.AbstractCommand;
import liquibase.command.CommandValidationErrors;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.sdk.Main;
import liquibase.sdk.TemplateService;
import liquibase.sdk.exception.UnexpectedLiquibaseSdkException;
import liquibase.sdk.supplier.database.ConnectionSupplier;
import liquibase.sdk.supplier.database.ConnectionConfigurationFactory;
import liquibase.util.StringUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.*;
import java.util.*;

public class VagrantCommand extends AbstractCommand {
    private final Main mainApp;
    private String vagrantPath;
    private String command;
    private VagrantInfo vagrantInfo;
    private CommandLine commandCommandLine;

    public VagrantCommand(Main mainApp) {
        this.mainApp = mainApp;

        this.vagrantPath = this.mainApp.getPath("vagrant.exe", "vagrant.bat", "vagrant.sh", "vagrant");

        if (vagrantPath == null) {
            throw new UnexpectedLiquibaseSdkException("Cannot find vagrant in " + mainApp.getPath());
        }

        mainApp.debug("Vagrant path: " + vagrantPath);
    }


    @Override
    public String getName() {
        return "vagrant";
    }

    @Override
    public CommandValidationErrors validate() {
        return new CommandValidationErrors(this);
    }

    public void setup(CommandLine commandCommandLine) throws Exception {
        List<String> commandArgs = commandCommandLine.getArgList();

        vagrantInfo = new VagrantInfo();
        if (commandArgs.size() < 2) {
            mainApp.fatal("Usage: liquibase-sdk vagrant BOX_NAME COMMAND");
        }

        vagrantInfo.boxName = commandArgs.get(0);
        command = commandArgs.get(1);

        vagrantInfo.vagrantRoot = new File(mainApp.getSdkRoot(), "vagrant");
        vagrantInfo.boxDir = new File(vagrantInfo.vagrantRoot, vagrantInfo.boxName).getCanonicalFile();

        this.commandCommandLine = commandCommandLine;
    }

    @Override
    protected Object run() throws Exception {
        if (command.equals("init")) {
            this.init();
        } else if (command.equals("up")) {
            this.up();
        } else if (command.equals("provision")) {
            this.provision();
        } else if (command.equals("destroy")) {
            this.destroy();
        } else if (command.equals("halt")) {
            this.halt();
        } else if (command.equals("reload")) {
            this.reload();
        } else if (command.equals("resume")) {
            this.resume();
        } else if (command.equals("status")) {
            this.status();
        } else if (command.equals("suspend")) {
            this.suspend();
        } else {
            mainApp.fatal("Unknown vagrant command '"+ command+"'");
        }
        return "Successful";
    }

    public void init() throws Exception {

        if (!commandCommandLine.hasOption("databases")) {
            mainApp.fatal("vagrant init requires --databases option");
        }

        String[] databaseConfigs = commandCommandLine.getOptionValue("databases").split("\\s*,\\s*");


        mainApp.out("Vagrant Machine Setup:");
        mainApp.out(StringUtils.indent("Vagrant Box Name: " + vagrantInfo.boxName));
        mainApp.out(StringUtils.indent("Local Path: " + vagrantInfo.boxDir.getAbsolutePath()));
        mainApp.out(StringUtils.indent("Database Config(s): " + StringUtils.join(databaseConfigs, ", ")));

        Collection<ConnectionSupplier> databases = null;
        try {
            databases = ConnectionConfigurationFactory.getInstance().findConfigurations(databaseConfigs);
        } catch (ConnectionConfigurationFactory.UnknownDatabaseException e) {
            mainApp.fatal(e);
        }

        for (ConnectionSupplier connectionConfig : databases) {
            if (vagrantInfo.baseBoxName == null) {
                vagrantInfo.baseBoxName = connectionConfig.getVagrantBaseBoxName();
            } else {
                if (!vagrantInfo.baseBoxName.equals(connectionConfig.getVagrantBaseBoxName())) {
                    throw new UnexpectedLiquibaseException("Configuration " + connectionConfig + " needs vagrant box " + connectionConfig.getVagrantBaseBoxName() + ", not " + vagrantInfo.baseBoxName + " like other configurations");
                }
            }

            if (vagrantInfo.ipAddress == null) {
                vagrantInfo.ipAddress = connectionConfig.getIpAddress();
            } else {
                if (!vagrantInfo.ipAddress.equals(connectionConfig.getIpAddress())) {
                    throw new UnexpectedLiquibaseException("Configuration " + connectionConfig + " does not match previously defined hostname " + vagrantInfo.ipAddress);
                }
            }
        }

        mainApp.out(StringUtils.indent("Vagrant Base Box: " + vagrantInfo.baseBoxName));
        mainApp.out(StringUtils.indent("IP Address: " + vagrantInfo.ipAddress));

        Properties liquibaseVagrantProperties = new Properties();

        liquibaseVagrantProperties.put("box.ipaddress", vagrantInfo.ipAddress);
        liquibaseVagrantProperties.put("box.base", vagrantInfo.boxName);

        mainApp.out("");

        int i = 0;
        for (ConnectionSupplier config : databases) {
            mainApp.out("Database Configuration For '" + config.toString() + "':");
            mainApp.out(StringUtils.indent(config.getDescription()));
            mainApp.out("");

            addVagrantConfigProperty(i, "supplier", config.toString(), liquibaseVagrantProperties);
            addVagrantConfigProperty(i, "shortName", config.getDatabaseShortName(), liquibaseVagrantProperties);
            addVagrantConfigProperty(i, "version", config.getVersion(), liquibaseVagrantProperties);
            addVagrantConfigProperty(i, "jdbcUrl", config.getJdbcUrl(), liquibaseVagrantProperties);
            addVagrantConfigProperty(i, "adminUsername", config.getAdminUsername(), liquibaseVagrantProperties);
            addVagrantConfigProperty(i, "adminPassword", config.getAdminPassword(), liquibaseVagrantProperties);
            addVagrantConfigProperty(i, "primaryCatalog", config.getPrimaryCatalog(), liquibaseVagrantProperties);
            addVagrantConfigProperty(i, "primarySchema", config.getPrimarySchema(), liquibaseVagrantProperties);
            addVagrantConfigProperty(i, "altCatalog", config.getAlternateCatalog(), liquibaseVagrantProperties);
            addVagrantConfigProperty(i, "altSchema", config.getAlternateSchema(), liquibaseVagrantProperties);
            addVagrantConfigProperty(i, "username", config.getDatabaseUsername(), liquibaseVagrantProperties);
            addVagrantConfigProperty(i, "password", config.getDatabasePassword(), liquibaseVagrantProperties);
            addVagrantConfigProperty(i, "altUsername", config.getAlternateUsername(), liquibaseVagrantProperties);
            addVagrantConfigProperty(i, "altPassword", config.getAlternateUserPassword(), liquibaseVagrantProperties);
        }

        writeVagrantFile(vagrantInfo);
        writePuppetFiles(vagrantInfo, databases);
        writeConfigFiles(vagrantInfo, databases);

        Set<String> propertiesFiles = new HashSet<String>();
        for (ConnectionSupplier connectionSupplier : databases) {
            String fileName;
            fileName = "liquibase."+vagrantInfo.boxDir.getName()+"-"+connectionSupplier.getDatabaseShortName()+".properties";

            String propertiesFile =
                    "### Connection Property File For Vagrant Box '"+ vagrantInfo.boxName+"'\n"+
                    "### Example use: .."+File.separator+".."+File.separator+"liquibase --defaultsFile="+fileName+" update\n\n"+
                    "classpath: changelog\n" +
                    "changeLogFile=com/example/changelog.xml\n" +
                    "username="+connectionSupplier.getDatabaseUsername()+"\n" +
                    "password="+connectionSupplier.getDatabaseUsername()+"\n" +
                    "url="+connectionSupplier.getJdbcUrl()+"\n" +
                    "#logLevel=DEBUG\n" +
                    "#referenceUrl="+connectionSupplier.getJdbcUrl()+"\n" +
                    "#referenceUsername="+connectionSupplier.getDatabaseUsername()+"\n" +
                    "#referencePassword="+connectionSupplier.getDatabasePassword()+"\n";

            fileName = "workspace/" + fileName;


            File propertyFile = new File(mainApp.getSdkRoot(), fileName);
            if (propertyFile.exists()) {
                mainApp.out("NOTE: Not overwriting existing workspace properties file "+propertyFile.getAbsolutePath());
            } else {
                FileWriter writer = new FileWriter(propertyFile);
                try {
                    writer.write(propertiesFile);
                } finally {
                    writer.flush();
                    writer.close();
                }

                propertiesFiles.add(fileName);
            }

            FileOutputStream vagrantPropertiesStream = new FileOutputStream(new File(vagrantInfo.boxDir, "liquibase.vagrant.properties"));
            try {
                liquibaseVagrantProperties.store(vagrantPropertiesStream, "Original configuration for vagrant box "+ vagrantInfo.boxDir.getName());
            } finally {
                vagrantPropertiesStream.flush();
                vagrantPropertiesStream.close();
            }

        }

        mainApp.out("Vagrant Box "+vagrantInfo.boxName +" created. To start the box, run 'liquibase-sdk vagrant "+vagrantInfo.boxName +"' up");
        if (propertiesFiles.size() > 0) {
            mainApp.out("Created workspace properties file(s): "+StringUtils.join(propertiesFiles, ", "));
        }
        mainApp.out("Make sure any needed JDBC drivers are added to LIQUIBASE_HOME/lib");
        mainApp.out("NOTE: If you do not already have a vagrant box called "+vagrantInfo.baseBoxName +" installed, run 'vagrant box add "+vagrantInfo.baseBoxName +" VALID_URL'");
    }

    protected void addVagrantConfigProperty(int index, String name, Object value, Properties properties) {
        if (value != null) {
            properties.put("database."+index+"."+name, value);
        }
    }

    public void provision() {
        runVagrant(vagrantInfo, "provision");
    }

    public void destroy() {
        runVagrant(vagrantInfo, "destroy", "--force");
    }

    public void halt() {
        runVagrant(vagrantInfo, "halt");
    }

    public void reload() {
        runVagrant(vagrantInfo, "reload");
    }

    public void resume() {
        runVagrant(vagrantInfo, "resume");
    }

    public void status() {
        runVagrant(vagrantInfo, "status");
    }

    public void suspend() {
        runVagrant(vagrantInfo, "suspend");
    }

    public void up() {
        mainApp.out("Starting vagrant in " + vagrantInfo.boxDir.getAbsolutePath());
        mainApp.out("Config Name: " + vagrantInfo.boxName);
        mainApp.divider();

        runVagrant(vagrantInfo, "up");
    }

    private void runVagrant(VagrantInfo vagrantInfo, String... arguments) {
        if (!vagrantInfo.boxDir.exists()) {
            mainApp.fatal("Vagrant box directory "+vagrantInfo.boxDir.getAbsolutePath()+" does not exist");
        }

        List<String> finalArguments = new ArrayList<String>();
        finalArguments.add(vagrantPath);
        finalArguments.addAll(Arrays.asList(arguments));

        ProcessBuilder processBuilder = new ProcessBuilder(finalArguments.toArray(new String[finalArguments.size()]));
        processBuilder.directory(vagrantInfo.boxDir);
        Map<String, String> env = processBuilder.environment();

        processBuilder.redirectErrorStream(true);

        int out = 0;
        try {
            Process process = processBuilder.start();
            InputStream is = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            String line = null;
            while ((line = reader.readLine()) != null) {
                mainApp.out(line);
            }

            out = process.exitValue();

        } catch (Exception e) {
            mainApp.out("Error running vagrant");
            e.printStackTrace();
        }
        if (out != 0) {
            mainApp.out("Error running Vagrant. Return code " + out);
        }

    }

    private void writePuppetFiles(VagrantInfo vagrantInfo, Collection<ConnectionSupplier> databases) throws Exception {
        copyFile("liquibase/sdk/vagrant/shell/bootstrap.sh", new File(vagrantInfo.boxDir, "shell")).setExecutable(true);
        copyFile("liquibase/sdk/vagrant/shell/bootstrap.bat", new File(vagrantInfo.boxDir, "shell")).setExecutable(true);

        writePuppetFile(vagrantInfo, databases);

        writeManifestsInit(vagrantInfo, databases);


        File modulesDir = new File(vagrantInfo.boxDir, "modules");
        copyFile("liquibase/sdk/vagrant/modules/my_firewall/manifests/pre.pp", new File(modulesDir, "my_firewall/manifests"));
        copyFile("liquibase/sdk/vagrant/modules/my_firewall/manifests/post.pp", new File(modulesDir, "my_firewall/manifests"));
    }

    private void writePuppetFile(VagrantInfo vagrantInfo, Collection<ConnectionSupplier> databases) throws Exception {
        Set<String> forges = new HashSet<String>();
        Set<String> modules = new HashSet<String>();

        for (ConnectionSupplier config : databases) {
            forges.addAll(config.getPuppetForges(vagrantInfo.boxName));
            modules.addAll(config.getPuppetModules());
        }

        Map<String, Object> context = new HashMap<String, Object>();
        context.put("puppetForges", forges);
        context.put("puppetModules", modules);

        TemplateService.getInstance().write("liquibase/sdk/vagrant/Puppetfile.vm", new File(vagrantInfo.boxDir, "Puppetfile"), context);
    }

    private void writeManifestsInit(VagrantInfo vagrantInfo, Collection<ConnectionSupplier> databases) throws Exception {
        File manifestsDir = new File(vagrantInfo.boxDir, "manifests");
        manifestsDir.mkdirs();

        Set<String> puppetBlocks = new HashSet<String>();

        for (ConnectionSupplier config : databases) {
            Map<String, Object> context = new HashMap<String, Object>();
            context.put("supplier", config);

            ConnectionSupplier.ConfigTemplate thisInit = config.getPuppetTemplate(context);
            if (thisInit != null) {
                puppetBlocks.add(thisInit.output());
            }
        }

        Map<String, Object> context = new HashMap<String, Object>();
        context.put("puppetBlocks", puppetBlocks);

        String osLevelConfig;
        if (vagrantInfo.baseBoxName.contains("linux")) {
            osLevelConfig = "service { \"iptables\":\n" +
                    "  enable => false,\n"+
                    "  ensure => \"stopped\",\n" +
                    "}\n\n";

            Set<String> requiredPackages = new HashSet<String>();
            requiredPackages.add("unzip");

            for (ConnectionSupplier config : databases) {
                requiredPackages.addAll(config.getRequiredPackages(vagrantInfo.boxName));
            }

            for (String requiredPackage : requiredPackages) {
                osLevelConfig += "package { \""+requiredPackage+"\":\n" +
                        "    ensure => \"installed\"\n" +
                        "}\n\n";
            }
        } else {
            osLevelConfig = "package { '7zip':\n" +
                    "    ensure  => '9.20',\n" +
                    "    source\t=>\t\"http://downloads.sourceforge.net/sevenzip/7z920-x64.msi\",\n" +
                    "}\n";
        }
        context.put("osLevelConfig", osLevelConfig);

        TemplateService.getInstance().write("liquibase/sdk/vagrant/manifests/init.pp.vm", new File(manifestsDir, "init.pp"), context);
    }

    private File copyFile(String sourcePath, File outputDir) throws Exception {
        outputDir.mkdirs();

        InputStream input = this.getClass().getClassLoader().getResourceAsStream(sourcePath);
        if (input == null) {
            throw new UnexpectedLiquibaseSdkException("Missing source file: "+sourcePath);
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));

        String fileName = sourcePath.replaceFirst(".*/", "");
        File file = new File(outputDir, fileName);
        BufferedWriter output = new BufferedWriter(new FileWriter(file));

        String line;
        while ((line = reader.readLine()) != null) {
            output.write(line + "\n");
        }

        output.flush();
        output.close();

        return file;
    }

    private void writeVagrantFile(VagrantInfo vagrantInfo) throws Exception {

        Map<String, Object> context = new HashMap<String, Object>();
        context.put("configVmBox", vagrantInfo.baseBoxName);
        context.put("configVmNetworkIp", vagrantInfo.ipAddress);
        context.put("vmCustomizeMemory", "8192");

        String shellScript;
        String osLevelConfig;
        if (vagrantInfo.baseBoxName.contains("windows")) {
            osLevelConfig = "config.vm.communicator = \"winrm\"\n";
            shellScript = "shell/bootstrap.bat";
        } else {
            shellScript = "shell/bootstrap.sh";
            osLevelConfig = "";
        }

        context.put("osLevelConfig", StringUtils.indent(osLevelConfig, 4));
        context.put("configVmProvisionScript", shellScript);

        TemplateService.getInstance().write("liquibase/sdk/vagrant/Vagrantfile.vm", new File(vagrantInfo.boxDir,"Vagrantfile"), context);
    }

    private void writeConfigFiles(VagrantInfo vagrantInfo, Collection<ConnectionSupplier> databases) throws IOException {
        Map<String, Object> context = new HashMap<String, Object>();
        for (ConnectionSupplier config : databases) {
            context.put("supplier", config);

            Set<ConnectionSupplier.ConfigTemplate> configTemplates = config.generateConfigFiles(context);
            if (configTemplates != null) {
                for (ConnectionSupplier.ConfigTemplate configTemplate : configTemplates) {
                    File outputFile = new File(vagrantInfo.boxDir+"/modules/conf/" + config.getDatabaseShortName(), configTemplate.getOutputFileName());
                    outputFile.getParentFile().mkdirs();

                    configTemplate.write(outputFile);
                }
            }
        }
    }

    public Options getOptions() {
        Option databases = new Option("databases", true, "Database configurations");
        return new Options().addOption(databases);
    }

    private static final class VagrantInfo {
        public String boxName;
        private File vagrantRoot;
        private File boxDir;
        private String baseBoxName;
        private String ipAddress;
    }
}
