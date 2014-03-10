package liquibase.util

import spock.lang.Specification
import spock.lang.Unroll

import static org.assertj.core.api.Assertions.assertThat;

class CollectionUtilSpec extends Specification {

    @Unroll("powerSet on #original")
    def "powerSet method" () {

        def comparator = new Comparator<Collection>() {

            @Override
            int compare(Collection o1, Collection o2) {
                List set1 = new ArrayList(o1);
                List set2 = new ArrayList(o2);
                Collections.sort set1
                Collections.sort set2

                return set1.toString().compareTo(set2.toString())
            }
        }

        expect:
        def powerSet = CollectionUtil.powerSet(new HashSet(original))

        for (Set set : powerSet) {
            assertThat(set).usingComparator(comparator).isIn(expected)
        }

        for (set in expected) {
            assertThat(set).usingComparator(comparator).isIn(powerSet)
        }

        where:
        original | expected
        [] | [[]]
        ['a'] | [[], ['a']]
        ['a', 'b'] | [[], ['a'], ['b'], ['a','b']]
        ['a', 'b', 'c'] | [[], ['a'], ['b'], ['c'], ['a','b'], ['a','c'], ['b','c'], ['a', 'b', 'c']]

    }

}