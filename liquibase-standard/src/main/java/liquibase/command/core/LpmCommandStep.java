package liquibase.command.core;

import liquibase.Scope;
import liquibase.command.AbstractCommandStep;
import liquibase.command.CommandDefinition;
import liquibase.command.CommandResultsBuilder;
import liquibase.configuration.ConfigurationValueConverter;
import liquibase.configuration.ConfiguredValue;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.util.DownloadUtil;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * This command step is responsible for running the Liquibase Package Manager (LPM) command.
 */
public class LpmCommandStep extends AbstractCommandStep {

    public static final String[] COMMAND_NAME = {"lpm"};

    @Override
    public List<Class<?>> requiredDependencies() {
        return new ArrayList<>();
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][]{COMMAND_NAME};
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Liquibase Package Manager");
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        final String lpmHome = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class)
                .getCurrentConfiguredValue(ConfigurationValueConverter.STRING, null, "liquibase.home").getValue();
        final String lpmExecutable = lpmHome + File.separator + "lpm" + (System.getProperty("os.name").toLowerCase().contains("win") ? ".exe" : "");
        this.checkForLpmInstallation(lpmHome, lpmExecutable);
        this.runLpm(lpmExecutable);

    }

    private void runLpm(String lpmExecutable) {
        final String[] lpmArgs = Scope.getCurrentScope().get(Scope.Attr.lpmArgs, String.class).split(" ");
        final String[] lpmExecArgs = new String[1 + lpmArgs.length];
        lpmExecArgs[0] = lpmExecutable;
        System.arraycopy(lpmArgs, 0, lpmExecArgs, 1, lpmArgs.length);
        try {
            final Process process = new ProcessBuilder(lpmExecArgs).redirectErrorStream(true).start();
            InputStream stdOut = process.getInputStream();
            try (BufferedReader stdOutReader = new BufferedReader(new InputStreamReader(stdOut))) {
                String line ="";
                do {
                    Scope.getCurrentScope().getUI().sendMessage(line);
                    line = stdOutReader.readLine();
                } while (line != null);
            }

            process.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    private void checkForLpmInstallation(String lpmHome, String lpmExecutable) {
        Scope.getCurrentScope().getLog(getClass()).fine("Checking for LPM at " + lpmExecutable);
        File lpmExecutableFile = new File(lpmExecutable);
        if (!lpmExecutableFile.exists() || !lpmExecutableFile.isFile()) {
            Scope.getCurrentScope().getUI().sendMessage("LPM not found at + '" + lpmExecutable + "' +. Don't worry, I'll install it for you.");
            this.installLpm(lpmHome);
        }

    }

    private void installLpm(String lpmHome) {
        ConfiguredValue<String> lpmVersionProperty = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class)
                .getCurrentConfiguredValue(ConfigurationValueConverter.STRING, null, "liquibase.lpmVersion");
        final String version =  lpmVersionProperty != null && lpmVersionProperty.getValue() != null ? lpmVersionProperty.getValue() : "0.2.4";
        final String lpmUrl = String.format("https://github.com/liquibase/liquibase-package-manager/releases/download/v%s/lpm-%s-%s.zip", version, version, System.getProperty("os.name").toLowerCase().split(" ")[0]);
        final String lpmZip = String.format("%s%slpm.zip", lpmHome, File.separator);
        Path downloadedFilePath = DownloadUtil.downloadToFile(lpmUrl, new File(lpmZip));
        this.unzipLpm(downloadedFilePath, lpmHome);
        try {
            Files.delete(downloadedFilePath);
        } catch (IOException e) {
            Scope.getCurrentScope().getLog(getClass()).warning("Could not delete lpm installer " + downloadedFilePath);
        }
    }

    private void unzipLpm(Path lpmZip, String lpmHome) {
        Scope.getCurrentScope().getUI().sendMessage("Unzipping LPM to " + lpmHome);
        try {
            FileInputStream fis = new FileInputStream(lpmZip.toFile());
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry ze = zis.getNextEntry();
            byte[] buffer = new byte[1024];
            while(ze != null) {
                String fileName = ze.getName();
                File newFile = new File(lpmHome + File.separator + fileName);
                Scope.getCurrentScope().getLog(getClass()).info("Unzipping to "+newFile.getAbsolutePath());
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = null;
                fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                zis.closeEntry();
                ze = zis.getNextEntry();
                if (newFile.getName().startsWith("lpm")) {
                    newFile.setExecutable(true);
                }
            }
            zis.closeEntry();
            zis.close();
            fis.close();
        } catch (IOException e) {
            throw new UnexpectedLiquibaseException(e);
        }

    }
}
