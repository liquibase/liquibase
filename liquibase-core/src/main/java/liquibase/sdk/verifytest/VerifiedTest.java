package liquibase.sdk.verifytest;

import java.util.*;

public class VerifiedTest {

    private String testClass;
    private String testName;

    private Map<String, TestPermutation> permutationsByKey = new HashMap<String, TestPermutation>();
    private List<TestPermutation> unkeyedPermutations = new ArrayList<TestPermutation>();

    public VerifiedTest(String testClass, String testName) {
        this.testName = testName;
        this.testClass = testClass;
    }

    public String getTestClass() {
        return testClass;
    }

    public boolean hasGroups() {
        Collection<TestPermutation> permutations = getPermutations();
        return permutations != null && permutations.size() > 0 && permutations.iterator().next().getGroup() != null;
    }

    public Set<String> getGroups() {
        Set<String> returnSet = new HashSet<String>();
        for (TestPermutation permutation : getPermutations()) {
            if (permutation.getGroup() != null) {
                returnSet.add(permutation.getGroup());
            }
        }

        return returnSet;
    }

    public String getTestName() {
        return testName;
    }

    public Collection<TestPermutation> getPermutations() {
        ArrayList<TestPermutation> returnList = new ArrayList<TestPermutation>();
        returnList.addAll(permutationsByKey.values());
        returnList.addAll(unkeyedPermutations);

        return Collections.unmodifiableCollection(returnList);
    }

    public TestPermutation getPermutation(String key) {
        if (!permutationsByKey.containsKey(key)) {
            rebuildPermutationsByKey();
        }
        return permutationsByKey.get(key);
    }

    public TestPermutation addPermutation(TestPermutation permutation) {
        String key = permutation.getKey();
        if (key.equals("")) {
            unkeyedPermutations.add(permutation);
        } else {
            permutationsByKey.put(key, permutation);
        }

        return permutation;
    }

    public void replacePermutation(TestPermutation permutation) {
        removePermutation(permutation.getKey());
        addPermutation(permutation);
    }

    public void removePermutation(String key) {
        TestPermutation permutation = getPermutation(key);
        if (permutation != null) {
            permutationsByKey.remove(permutation.getKey());
        }
    }

    private void rebuildPermutationsByKey() {
        List<TestPermutation> newUnkeyed = new ArrayList<TestPermutation>();
        for (TestPermutation permutation : unkeyedPermutations) {
            if (permutation.getKey().equals("")) {
                newUnkeyed.add(permutation);
            } else {
                permutationsByKey.put(permutation.getKey(), permutation);
            }
        }
        this.unkeyedPermutations = newUnkeyed;
    }
}
