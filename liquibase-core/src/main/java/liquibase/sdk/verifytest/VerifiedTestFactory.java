package liquibase.sdk.verifytest;

import liquibase.exception.UnexpectedLiquibaseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class VerifiedTestFactory {

    private static final VerifiedTestFactory instance = new VerifiedTestFactory();

    Map<File, VerifiedTest> filesToWrite = new HashMap<File, VerifiedTest>();
    Map<String, File> baseDirectoriesByClass = new HashMap<String, File>();

    public static VerifiedTestFactory getInstance() {
        return instance;
    }

    private VerifiedTestFactory() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                for (Map.Entry<File, VerifiedTest> entry : filesToWrite.entrySet()) {
                    File file = entry.getKey();
                    VerifiedTest testToWrite = entry.getValue();
                    file.getParentFile().mkdirs();

                    try {
                        if (testToWrite.hasGroups()) {
                            for (String group : testToWrite.getGroups()) {
                                String groupValue = group.split(": ", 2)[1];
                                File groupFile = new File(file.getAbsolutePath().replaceFirst("\\.accepted.md$", "-"+escapeFileName(groupValue)+".accepted.md"));
                                FileWriter fileWriter = new FileWriter(groupFile);
                                try {
                                    new VerifiedTestWriter().write(testToWrite, fileWriter, group);
                                } finally {
                                    fileWriter.flush();
                                    fileWriter.close();
                                }
                            }
                        } else {
                            FileWriter fileWriter = new FileWriter(file);
                            try {
                                new VerifiedTestWriter().write(testToWrite, fileWriter, null);
                            } finally {
                                fileWriter.flush();
                                fileWriter.close();
                            }
                        }
                    } catch (IOException e) {
                        throw new UnexpectedLiquibaseException(e);
                    }
                }
            }
        }));
    }

    public TestPermutation getSavedRun(VerifiedTest test, TestPermutation testPermutation) throws Exception {
        File file = getFile(test);
        VerifiedTest readTest;
        if (filesToWrite.containsKey(file)) {
            readTest = filesToWrite.get(file);
        } else {
            if (!file.exists()) {
                return null;
            }
            FileReader reader = new FileReader(file);
            readTest = new VerifiedTestReader().read(reader);
        }

        return readTest.getPermutation(testPermutation.getKey());
    }

    public void saveRun(VerifiedTest test, TestPermutation testPermutation) throws Exception {
        File file = getFile(test);

        VerifiedTest testToWrite;
        if (filesToWrite.containsKey(file)) {
            testToWrite = filesToWrite.get(file);
        } else if (file.exists()) {
            FileReader reader = new FileReader(file);
            try {
                testToWrite = new VerifiedTestReader().read(reader);
            } finally {
                reader.close();
            }
        } else {
            testToWrite = test;
        }

        testToWrite.replacePermutation(testPermutation);

        filesToWrite.put(file, testToWrite);
    }

    protected File getBaseDirectory(VerifiedTest test) {
        String testClassName = test.getTestClass().replace(".", "/") + ".class";
        if (!baseDirectoriesByClass.containsKey(testClassName)) {
            URL resource = this.getClass().getClassLoader().getResource(testClassName);
            if (resource == null) {
                return new File(".").getAbsoluteFile();
            }
            File testClass = new File(resource.getFile());
            File classesRoot = new File(testClass.getAbsolutePath().replace(testClassName.replace("/", File.separator), ""));
            baseDirectoriesByClass.put(testClassName, new File(classesRoot.getParentFile().getParentFile(), "src/test/resources"));
        }
        return baseDirectoriesByClass.get(testClassName);
    }

    protected File getFile(VerifiedTest test) {
        String testPackageDir = test.getTestClass().replaceFirst("\\.[^\\.]*$", "").replace(".", "/");
        String fileName = test.getTestClass().replaceFirst(".*\\.","")+"."+ escapeFileName(test.getTestName()) +".accepted.md";

        return new File(new File(getBaseDirectory(test), testPackageDir), fileName);
    }

    private String escapeFileName(String name) {
        return name.replaceAll("\\s+", "_").replaceAll("[\\-\\.]", "");
    }

}
