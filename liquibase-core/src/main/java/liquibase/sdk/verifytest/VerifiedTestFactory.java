package liquibase.sdk.verifytest;

import liquibase.exception.UnexpectedLiquibaseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class VerifiedTestFactory {

    private static final VerifiedTestFactory instance = new VerifiedTestFactory();

    Map<File, VerifiedTest> filesToWrite = new HashMap<File, VerifiedTest>();

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
                        FileWriter fileWriter = new FileWriter(file);
                        try {
                            new VerifiedTestWriter().write(testToWrite, fileWriter);
                        } finally {
                            fileWriter.flush();
                            fileWriter.close();
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
        File testClass = new File(this.getClass().getClassLoader().getResource(testClassName).getFile());
        File classesRoot = new File(testClass.getAbsolutePath().replace(testClassName.replace("/", File.separator), ""));
        return new File(classesRoot.getParentFile().getParentFile(), "src/test/resources");
    }

    protected File getFile(VerifiedTest test) {
        String testPackageDir = test.getTestClass().replaceFirst("\\.[^\\.]*$", "").replace(".", "/");
        String fileName = test.getTestClass().replaceFirst(".*\\.","")+"."+test.getTestName().replaceAll("\\s+", "_")+".accepted.md";

        return new File(new File(getBaseDirectory(test), testPackageDir), fileName);
    }

}
