package liquibase.util

import org.hamcrest.CoreMatchers
import org.junit.Assert
import spock.lang.Specification
import spock.lang.Timeout

class DependencyUtilTest extends Specification {

    private DependencyUtil.DependencyGraph<String> graph
    private List<String> dependencyOrder = new ArrayList<String>()

    void setup() {
        dependencyOrder.clear()
        DependencyUtil.NodeValueListener<String> listener = new DependencyUtil.NodeValueListener<String>() {
            @Override
            void evaluating(String nodeValue) {
                dependencyOrder.add(nodeValue)
            }
        }

        graph = new DependencyUtil.DependencyGraph<String>(listener)

    }

    void testBaseCase() {
        when:
        // a > b > c > d > e
        graph.add("a", "b")
        graph.add("b", "c")
        graph.add("c", "d")
        graph.add("d", "e")
        graph.computeDependencies()
        List<String> expected = Arrays.asList("a", "b", "c", "d", "e")

        then:
        dependencyOrder == expected
    }

    void testBranchingCase() {
        when:
        // a > b > c1
        //       > c2 > d
        graph.add("a", "b")
        graph.add("b", "c1")
        graph.add("b", "c2")
        graph.add("c2", "d")
        graph.computeDependencies()
        List<String> expected = Arrays.asList("a", "b", "c1", "c2", "d")

        then:
        dependencyOrder == expected
    }

    void testIndependentBranchesCase() {
        when:
        // a > b > c1
        //       > c2
        // o > p1 > r1 > s
        //     p2 > r2 > s2
        //             > s3
        // x > y
        graph.add("a", "b")
        graph.add("b", "c1")
        graph.add("b", "c2")
        graph.add("o", "p1")
        graph.add("p1", "r1")
        graph.add("r1", "s")
        graph.add("o", "p2")
        graph.add("p2", "r2")
        graph.add("r2", "s2")
        graph.add("r2", "s3")
        graph.add("x", "y")
        graph.computeDependencies()
        List<String> expected = Arrays.asList(
                "a", "o", "x",          // level 1
                "b", "p1", "p2", "y",   // level 2
                "c1", "c2", "r1", "r2", // level 3
                "s", "s2", "s3")       // roof

        then:
        dependencyOrder == expected
    }

    /* negative load */
    @Timeout(3)
    void recursionSafetyCheck_edgeCase() {
        when:
        graph.add("a", "a")
        graph.computeDependencies()

        then:
        // assert test executes in less than 3 seconds
        dependencyOrder != null
    }

    @Timeout(3)
    void recursionSafetyCheck() {
        when:
        // a > B > c > d > B
        graph.add("a", "B")
        graph.add("B", "c")
        graph.add("c", "d")
        graph.add("d", "B")
        graph.computeDependencies()

        then:
        Assert.assertThat(dependencyOrder, CoreMatchers.hasItem("a")) // we try to capture something
    }

    @Timeout(3)
    void recursionSafetyCheck2() {
        when:
        //     a > B > c > d
        // m > k > B
        // d > m (both have B in common)
        graph.add("a", "B")
        graph.add("B", "c")
        graph.add("c", "d")
        graph.add("m", "k")
        graph.add("k", "B")
        graph.add("d", "m")
        graph.computeDependencies()

        then:
        Assert.assertThat(dependencyOrder, CoreMatchers.hasItem("a"))
    }

    @Timeout(3)
    void recursionSafetyCheck_rand() {
        when:
        Random rand = new Random()
        for (int i = 0; i < 100; i++) {
            graph.add(String.valueOf(rand.nextInt(50)), String.valueOf(rand.nextInt(50)))
        }
        graph.add("a", "b")
        graph.computeDependencies()

        then:
        Assert.assertThat(dependencyOrder, CoreMatchers.hasItems("a", "b"))
    }
}
