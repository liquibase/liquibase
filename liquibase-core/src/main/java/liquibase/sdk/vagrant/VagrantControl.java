package liquibase.sdk.vagrant;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.sdk.Main;
import liquibase.sdk.exception.UnexpectedLiquibaseSdkException;
import liquibase.sdk.supplier.database.ConnectionConfiguration;
import liquibase.sdk.supplier.database.ConnectionConfigurationFactory;
import liquibase.util.StringUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import java.io.*;
import java.util.*;

public class VagrantControl {
    private final Main mainApp;
    private String vagrantPath;

    public VagrantControl(Main mainApp) {
        this.mainApp = mainApp;

        this.vagrantPath = this.mainApp.getPath("vagrant.bat", "vagrant.sh", "vagrant");

        if (vagrantPath == null) {
            throw new UnexpectedLiquibaseSdkException("Cannot find vagrant in " + mainApp.getPath());
        }

        mainApp.debug("Vagrant path: " + vagrantPath);
    }

    public void execute(CommandLine commandCommandLine) throws Exception {

        List<String> commandArgs = commandCommandLine.getArgList();

        VagrantInfo vagrantInfo = new VagrantInfo();
        if (commandArgs.size() == 0) {
            mainApp.fatal("Missing vagrant command");
        }

        if (commandArgs.size() == 1) {
            mainApp.fatal("Missing vagrant box name");
        }

        String command = commandArgs.get(0);

        vagrantInfo.configName = commandArgs.get(1);
        vagrantInfo.vagrantRoot = new File(mainApp.getSdkRoot(), "vagrant");
        vagrantInfo.boxDir = new File(vagrantInfo.vagrantRoot, vagrantInfo.configName).getCanonicalFile();

        if (command.equals("init")) {
            this.init(vagrantInfo, commandCommandLine);
        } else if (command.equals("up")) {
            this.up(vagrantInfo, commandCommandLine);
        } else if (command.equals("provision")) {
            this.provision(vagrantInfo, commandCommandLine);
        } else if (command.equals("destroy")) {
            this.destroy(vagrantInfo, commandCommandLine);
        } else if (command.equals("halt")) {
            this.halt(vagrantInfo, commandCommandLine);
        } else if (command.equals("reload")) {
            this.reload(vagrantInfo, commandCommandLine);
        } else if (command.equals("resume")) {
            this.resume(vagrantInfo, commandCommandLine);
        } else if (command.equals("status")) {
            this.status(vagrantInfo, commandCommandLine);
        } else if (command.equals("suspend")) {
            this.suspend(vagrantInfo, commandCommandLine);
        } else {
            mainApp.fatal("Unknown vagrant command '"+ command+"'");
        }
    }

    public void init(VagrantInfo vagrantInfo, CommandLine commandLine) throws Exception {

        List<String> configs = commandLine.getArgList().subList(2, commandLine.getArgList().size());

        mainApp.out("Vagrant Machine Setup:");
        mainApp.divider();
        mainApp.out(StringUtils.indent("Local Path: " + vagrantInfo.boxDir.getAbsolutePath()));
        mainApp.out(StringUtils.indent("Config Name: " + vagrantInfo.configName));
        mainApp.out(StringUtils.indent("Database Config(s): " + StringUtils.join(configs, ", ")));

        Collection<ConnectionConfiguration> databases = null;
        try {
            databases = ConnectionConfigurationFactory.getInstance().findConfigurations(configs);
        } catch (ConnectionConfigurationFactory.UnknownDatabaseException e) {
            mainApp.fatal(e);
        }

        String[] boxInfo = null;
        for (ConnectionConfiguration connectionConfig : databases) {
            String[] absoluteBox = getAbsoluteBox(connectionConfig.getVagrantBoxName(), vagrantInfo);

            if (boxInfo == null) {
                boxInfo = absoluteBox;
            } else {
                if (!boxInfo[0].equals(absoluteBox[0])) {
                    throw new UnexpectedLiquibaseException("Configuration " + connectionConfig + " needs vagrant box " + absoluteBox[0] + ", not " + boxInfo[0] + " like other configurations");
                }
            }

            if (vagrantInfo.hostName == null) {
                vagrantInfo.hostName = connectionConfig.getHostname();
            } else {
                if (!vagrantInfo.hostName.equals(connectionConfig.getHostname())) {
                    throw new UnexpectedLiquibaseException("Configuration " + connectionConfig + " does not match previously defined hostname " + vagrantInfo.hostName);
                }
            }
        }

        if (boxInfo == null) {
            throw new UnexpectedLiquibaseException("Null boxInfo");
        }

        vagrantInfo.boxName = boxInfo[0];
        vagrantInfo.boxUrl = boxInfo[1];

        mainApp.out(StringUtils.indent("Base Box Url: " + vagrantInfo.boxUrl));
        mainApp.out(StringUtils.indent("Hostname: " + vagrantInfo.hostName));

        mainApp.out("");

        for (ConnectionConfiguration config : databases) {
            mainApp.out("Connection Configuration For '" + config.toString() + "':");
            mainApp.divider();
            mainApp.out(StringUtils.indent(config.getDescription()));
            mainApp.out("");
        }

        writeVagrantFile(vagrantInfo);
        writePuppetFiles(vagrantInfo, databases);

        mainApp.out("Vagrant Box "+vagrantInfo.configName+" created. To start the box, run 'liquibase-sdk vagrant up "+vagrantInfo.configName+"'");
    }

    public void provision(VagrantInfo vagrantInfo, CommandLine commandLine) {
        runVagrant(vagrantInfo, "provision");
    }

    public void destroy(VagrantInfo vagrantInfo, CommandLine commandLine) {
        runVagrant(vagrantInfo, "destroy");
    }

    public void halt(VagrantInfo vagrantInfo, CommandLine commandLine) {
        runVagrant(vagrantInfo, "halt");
    }

    public void reload(VagrantInfo vagrantInfo, CommandLine commandLine) {
        runVagrant(vagrantInfo, "reload");
    }

    public void resume(VagrantInfo vagrantInfo, CommandLine commandLine) {
        runVagrant(vagrantInfo, "resume");
    }

    public void status(VagrantInfo vagrantInfo, CommandLine commandLine) {
        runVagrant(vagrantInfo, "status");
    }

    public void suspend(VagrantInfo vagrantInfo, CommandLine commandLine) {
        runVagrant(vagrantInfo, "suspend");
    }

    public void up(VagrantInfo vagrantInfo, CommandLine commandLine) {
        mainApp.out("Starting vagrant in " + vagrantInfo.boxDir.getAbsolutePath());
        mainApp.out("Config Name: " + vagrantInfo.configName);
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

    private void writePuppetFiles(VagrantInfo vagrantInfo, Collection<ConnectionConfiguration> databases) throws Exception {
        copyFile("liquibase/sdk/vagrant/puppet-bootstrap.sh", vagrantInfo.boxDir).setExecutable(true);

        writePuppetFile(vagrantInfo, databases);

        writeManifestsInit(vagrantInfo, databases);


        File modulesDir = new File(vagrantInfo.boxDir, "modules");
        copyFile("liquibase/sdk/vagrant/modules/my_firewall/manifests/pre.pp", new File(modulesDir, "my_firewall/manifests"));
        copyFile("liquibase/sdk/vagrant/modules/my_firewall/manifests/post.pp", new File(modulesDir, "my_firewall/manifests"));
    }

    private void writePuppetFile(VagrantInfo vagrantInfo, Collection<ConnectionConfiguration> databases) throws Exception {
        Set<String> forges = new HashSet<String>();
        Set<String> modules = new HashSet<String>();

        for (ConnectionConfiguration config : databases) {
            forges.addAll(config.getPuppetForges(vagrantInfo.configName));
            modules.addAll(config.getPuppetModules());
        }

        Map<String, Object> context = new HashMap<String, Object>();
        context.put("puppetForges", forges);
        context.put("puppetModules", modules);

        writeVelocityFile("liquibase/sdk/vagrant/Puppetfile.vm", vagrantInfo.boxDir, context);
    }

    private void writeManifestsInit(VagrantInfo vagrantInfo, Collection<ConnectionConfiguration> databases) throws Exception {
        File manifestsDir = new File(vagrantInfo.boxDir, "manifests");
        manifestsDir.mkdirs();

        Set<String> requiredPackages = new HashSet<String>();
        requiredPackages.add("unzip");

        Set<String> puppetBlocks = new HashSet<String>();

        for (ConnectionConfiguration config : databases) {
            requiredPackages.addAll(config.getRequiredPackages(vagrantInfo.configName));
            String thisInit = config.getPuppetInit(vagrantInfo.configName);
            if (thisInit != null) {
                puppetBlocks.add(thisInit);
            }
        }

        Map<String, Object> context = new HashMap<String, Object>();
        context.put("requiredPackages", requiredPackages);
        context.put("puppetBlocks", puppetBlocks);

        writeVelocityFile("liquibase/sdk/vagrant/manifests/init.pp.vm", manifestsDir, context);
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
        context.put("configVmBox", vagrantInfo.boxName);
        context.put("configVmBoxUrl", vagrantInfo.boxUrl);
        context.put("configVmNetworkIp", vagrantInfo.hostName);
        context.put("vmCustomizeMemory", "8192");

        writeVelocityFile("liquibase/sdk/vagrant/Vagrantfile.vm", vagrantInfo.boxDir, context);
    }

    protected String[] getAbsoluteBox(String vagrantBoxName, VagrantInfo vagrantInfo) {
        Properties properties = new Properties();
        try {
            properties.load(new FileReader(new File(vagrantInfo.vagrantRoot, "vagrant-boxes.default.properties")));
        } catch (IOException e) {
            throw new UnexpectedLiquibaseException(e);
        }

        File file = new File(vagrantInfo.vagrantRoot, "vagrant-boxes.properties");
        if (file.exists()) {
            try {
                properties.load(new FileReader(file));
            } catch (IOException e) {
                throw new UnexpectedLiquibaseException(e);
            }
        }

        String absoluteKey = vagrantBoxName;
        String value = properties.getProperty(absoluteKey);
        while (value != null && !value.contains("://")) {
            absoluteKey = properties.getProperty(absoluteKey);
            value = properties.getProperty(absoluteKey);
        }

        if (value == null) {
            throw new UnexpectedLiquibaseException("Could not determine box url for " + absoluteKey);
        }
        return new String[]{absoluteKey, value};

    }

    private void writeVelocityFile(String templatePath, File outputDir, Map<String, Object> contextParams) throws Exception {
        VelocityEngine engine = new VelocityEngine();
        engine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        engine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        engine.init();

        InputStream input = this.getClass().getClassLoader().getResourceAsStream(templatePath);
        if (input == null) {
            throw new IOException("Template file " + templatePath + " doesn't exist");
        }

        VelocityContext context = new VelocityContext();
        for (Map.Entry<String, Object> entry : contextParams.entrySet()) {
            context.put(entry.getKey(), entry.getValue());
        }

        Template template = engine.getTemplate(templatePath, "UTF-8");
        outputDir.mkdirs();
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outputDir, templatePath.replaceFirst(".*/", "").replaceFirst(".vm$", ""))));

        template.merge(context, writer);

        writer.flush();
        writer.close();
    }

    public Options getOptions() {
        return new Options();
    }

    private static final class VagrantInfo {
        public String configName;
        private File vagrantRoot;
        private String boxName;
        private File boxDir;
        private String boxUrl;
        private String hostName;
    }
}
