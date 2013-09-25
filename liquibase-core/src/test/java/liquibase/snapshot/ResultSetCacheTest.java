package liquibase.snapshot;

import org.hamcrest.Matcher;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class ResultSetCacheTest {

//    @Test
//    public void permutations() {
//        assertEquals(4, new ResultSetCache().permutations(new String[]{"a", "b"}).length);
//        assertEquals(8, new ResultSetCache().permutations(new String[]{"a", "b", "c"}).length);
//        assertEquals(16, new ResultSetCache().permutations(new String[]{"a", "b", "c", "d"}).length);
//        assertEquals(32, new ResultSetCache().permutations(new String[]{"a", "b", "c", "d", "e"}).length);
//
//        assertThat(Arrays.asList(new ResultSetCache().permutations(new String[]{"a"})), containsInAnyOrder(new String[] {"a"}, new String[] {null}));
//
//        assertThat(Arrays.asList(new ResultSetCache().permutations(new String[]{"a", "b"})), containsInAnyOrder(
//                new String[]{"a", "b"},
//                new String[]{null, "b"},
//                new String[]{"a", null},
//                new String[]{null, null}
//        ));
//
//        assertThat(Arrays.asList(new ResultSetCache().permutations(new String[]{"a", "b", "c"})), containsInAnyOrder(
//                new String[]{"a", "b", "c"},
//                new String[]{"a", "b", null},
//                new String[]{"a", null, "c"},
//                new String[]{null, "b", "c"},
//                new String[]{null, null, "c"},
//                new String[]{"a", null, null},
//                new String[]{null, "b", null},
//                new String[]{null, null, null}
//        ));
//    }
}
