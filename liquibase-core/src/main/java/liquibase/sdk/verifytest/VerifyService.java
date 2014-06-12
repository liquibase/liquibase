package liquibase.sdk.verifytest;

import liquibase.exception.UnexpectedLiquibaseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VerifyService {

    private static final Map<String, VerifyService> instances = new HashMap<String, VerifyService>();

    private final String testClass;
    private final String testName;
    private Map<String, TestPermutation> savedRuns;
    private List<TestPermutation> newRuns = new ArrayList<TestPermutation>();

    public static VerifyService getInstance(final String testClass, final String testName) {
        String key = testClass+":"+testName;
        if (!instances.containsKey(key)) {
            final VerifyService service = new VerifyService(testClass, testName);
            instances.put(key, service);

            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    if (service.newRuns.size() == 0) {
                        return;
                    }

                    for (TestPermutation permutation : service.newRuns) {
                        if (!permutation.getCanSave()) {
                            return;
                        }
                    }
                    File file = service.getFile();
                    file.getParentFile().mkdirs();

                    try {
                        FileWriter fileWriter = new FileWriter(file);
                        try {
                            new VerifiedTestWriter().write(testClass, testName, service.newRuns, fileWriter);
                        } finally {
                            fileWriter.flush();
                            fileWriter.close();
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                        throw new UnexpectedLiquibaseException(e);
                    }
                }
            }));
        }
        return instances.get(key);
    }

    private VerifyService(String testClass, String testName) {
        this.testClass = testClass;
        this.testName = testName;
    }

    public TestPermutation permutation(Map<String, Object> parameters) throws Exception {
        TestPermutation permutation = new TestPermutation(parameters);
        permutation.setPreviousRun(getSavedRun(permutation));

        newRuns.add(permutation);

        return permutation;
    }

    public TestPermutation getSavedRun(TestPermutation testPermutation) throws Exception {
        if (savedRuns == null) {
            savedRuns = new HashMap<String, TestPermutation>();

            File file = getFile();
            if (file.exists()) {
                FileReader reader = new FileReader(file);
                for (TestPermutation permutation : new VerifiedTestReader().read(reader)) {
                    savedRuns.put(permutation.getKey(), permutation);
                }
            }
        }
        return savedRuns.get(testPermutation.getKey());
    }

    protected File getFile() {
        String testPackageDir = testClass.replaceFirst("\\.[^\\.]*$", "").replace(".", "/");
        String fileName = testClass.replaceFirst(".*\\.", "") + "." + escapeFileName(testName) + ".accepted.md";

        return new File(new File(getBaseDirectory(), testPackageDir), fileName);
    }

    private String escapeFileName(String name) {
        return name.replaceAll("\\s+", "_").replaceAll("[\\-\\.]", "");
    }

    protected File getBaseDirectory() {
        String testClassName = testClass.replace(".", "/") + ".class";

        URL resource = this.getClass().getClassLoader().getResource(testClassName);
        if (resource == null) {
            return new File(".").getAbsoluteFile();
        }
        File testClass = new File(resource.getFile());
        File classesRoot = new File(testClass.getAbsolutePath().replace(testClassName.replace("/", File.separator), ""));

        return new File(classesRoot.getParentFile().getParentFile(), "src/test/resources");
    }


}
