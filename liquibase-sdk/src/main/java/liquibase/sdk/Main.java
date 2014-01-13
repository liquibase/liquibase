package liquibase.sdk;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.sdk.vagrant.VagrantControl;
import liquibase.util.StringUtils;
import org.apache.commons.cli.*;

import java.io.File;
import java.util.*;

public class Main {

    private boolean debug = false;

    private CommandLine globalArguments;
    private String command;
    private List<String> commandArgs = new ArrayList<String>();

    private Options globalOptions;

    public static void main(String[] args) {
        Main main = new Main();

        main.header("Liquibase SDK");

        try {
            main.init(args);

            if (main.command == null) {
                throw new UserError("No command passed");
            }

            if (main.command.equals("help")) {
                main.printHelp();
                return;
            }

            VagrantControl vagrantControl;
            if (main.command.equals("vagrant")) {
                vagrantControl = new VagrantControl(main);
            } else {
                throw new UserError("Unknown command: "+main.command);
            }

            CommandLineParser commandParser = new GnuParser();
            try {
                CommandLine commandArguments = commandParser.parse(vagrantControl.getOptions(), main.commandArgs.toArray(new String[main.commandArgs.size()]));

                vagrantControl.execute(commandArguments);
            } catch (ParseException e) {
                throw new UserError("Error parsing command arguments: "+e.getMessage());
            }

            main.divider();
            main.out("Command executed successfully");


        } catch (UserError userError) {
            main.out("");
            main.header("ERROR EXECUTING COMMAND");
            main.out(userError.getMessage());
            main.out("");
            main.out("");
            return;
        } catch (Throwable exception) {
            System.out.println("Unexpected error: "+exception.getMessage());
            exception.printStackTrace();
        }
    }

    public Main() {
        globalOptions = new Options();
    }

    public void init(String[] args) throws UserError {
        Context.reset();
        CommandLineParser globalParser = new GnuParser();

        List<String> globalArgs = new ArrayList<String>();

        boolean inGlobal = true;
        for (String arg : args) {
            if (inGlobal) {
                if (arg.startsWith("--")) {
                    globalArgs.add(arg);
                } else {
                    this.command = arg;
                    inGlobal = false;
                }
            } else {
                commandArgs.add(arg);
            }
        }

        try {
            this.globalArguments = globalParser.parse(globalOptions, globalArgs.toArray(new String[globalArgs.size()]));
        } catch (ParseException e) {
            throw new UserError("Error parsing global command line argument: " + e.getMessage());
        }
    }

//        Context context = Context.getInstance();
//        if (context.getSeenExtensionClasses().size() == 0) {
//            System.out.println("No extension classes found in "+StringUtils.join(context.getPackages(), ","));
//            return;
//        }
//
//        System.out.println("Extension classes found:");
//        for (Map.Entry<Class, Set<Class>> entry : context.getSeenExtensionClasses().entrySet()) {
//            System.out.println(StringUtils.indent(entry.getKey().getName()+" extensions:", 4));
//
//            System.out.println(StringUtils.indent(StringUtils.join(entry.getValue(), "\n", new StringUtils.StringUtilsFormatter() {
//                @Override
//                public String toString(Object obj) {
//                    return ((Class) obj).getName();
//                }
//            }), 8));
//        }

//        header("Running Tests");
//
//        JUnitCore junit = new JUnitCore();
//        junit.addListener(new TextListener(System.out));
//        Result result = junit.run(new Computer(), StandardChangeTests.class);


    public File getSdkRoot() {
        return new File(".").getAbsoluteFile();
    }

    public String getCommand() {
        return command;
    }

    public void header(String... header) {
        divider();
        for (String line : header) {
            System.out.println(line);
        }
        divider();
    }

    public void divider() {
        System.out.println("---------------------------------------------------");
    }


    public void out(String message) {
        System.out.println(message);
    }

    public void debug(String message) {
        if (debug) {
            System.out.println("DEBUG: "+message);
        }
    }

    public void fatal(String error) throws UserError {
        throw new UserError(error);
    }

    public void fatal(Throwable exception) {
        fatal(exception.getMessage(), exception);
    }

    public void fatal(String error, Throwable exception) throws UserError {
        throw new UserError(error, exception);
    }

    public String getPath() {
        Map<String, String> environment = new ProcessBuilder().environment();

        String path = environment.get("Path");
        if (path == null) {
            path = environment.get("PATH");
        }
        if (path == null) {
            path = environment.get("path");
        }
        if (path == null) {
            throw new UnexpectedLiquibaseException("Cannot find path variable in environment. Possible variables are " + StringUtils.join(environment.keySet(), ","));
        }

        return path;
    }

    public String getPath(String... possibleFileNames) {
        Set<String> fileNames = new HashSet<String>();

        for (String dir : getPath().split("[:;]")) {
            for (String fileName : possibleFileNames) {
                File file = new File(dir, fileName);
                if (file.exists()) {
                    return file.getAbsolutePath();
                }
            }
        }
        return null;

    }

    public void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("liquibase-sdk [global options] [command] [command options]", globalOptions);
    }

    private static class UserError extends RuntimeException {

        public UserError(String message) {
            super(message);
        }

        private UserError(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
