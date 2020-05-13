package liquibase.util;

import org.junit.After;
import org.junit.Test;

import java.util.regex.PatternSyntaxException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author lujop
 */
public class RegexMatcherTest {
    private RegexMatcher matcher;
    private static final String text="Pulp Fiction\n" +
                "Reservoir Dogs\n" +
                "Kill Bill\n";

    @After
    public void tearDown() {
        matcher=null;
    }

    @Test(expected=PatternSyntaxException.class)
    public void testBadPatternFails() {
        matcher=new RegexMatcher(text,new String[]{"a(j"});
    }

    @Test
    public void testMatchingInSequentialOrder() {
        matcher=new RegexMatcher(text,new String[]{"Pulp","Reservoir","Kill"});
        assertTrue("All matched",matcher.allMatchedInSequentialOrder());

        matcher=new RegexMatcher(text,new String[]{"Pulp","ion"});
        assertTrue("All matched",matcher.allMatchedInSequentialOrder());

        matcher=new RegexMatcher(text,new String[]{"Pu.p","^Ki.+ll$"});
        assertTrue("All matched",matcher.allMatchedInSequentialOrder());

        matcher=new RegexMatcher(text,new String[]{"pulP","kiLL"});
        assertTrue("Case insensitive",matcher.allMatchedInSequentialOrder());

        matcher=new RegexMatcher(text,new String[]{"Reservoir","Pulp","Dogs"});
        assertFalse("Not in order",matcher.allMatchedInSequentialOrder());

        matcher=new RegexMatcher(text,new String[]{"Memento"});
        assertFalse("Not found",matcher.allMatchedInSequentialOrder());
    }

}