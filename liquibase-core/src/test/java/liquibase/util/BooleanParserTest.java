package liquibase.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class BooleanParserTest {

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"-1", false}, {" -1", false}, {"-1 ", false},
                {"0", false}, {" 0", false}, {"0 ", false},
                //
                {"1", true}, {" 1", true}, {"1 ", true},
                {"2", true},
                //
                {"true", true}, {" true", true}, {"true ", true},
                {"True", true}, {" True", true}, {"True ", true},
                {"TRUE", true}, {"TRUE ", true}, {" TRUE", true},
                {"t", true}, {" t", true}, {"t ", true},
                {"T", true}, {" T", true}, {"T ", true},
                {"y", true}, {" y", true}, {"y ", true},
                {"Y", true}, {" Y", true}, {"Y ", true},
                {"yes", true}, {" yes", true}, {"yes ", true},
                {"Yes", true}, {" Yes", true}, {"Yes ", true},
                {"YES", true}, {" YES", true}, {"YES ", true},
                //
                {"false", false}, {"false ", false}, {" false", false},
                {"False", false}, {" False", false}, {"False ", false},
                {"FALSE", false}, {" FALSE", false}, {"FALSE ", false},
                {"f", false}, {" f", false}, {"f ", false},
                {"F", false}, {" F", false}, {"F ", false},
                {"n", false}, {" n", false}, {"n ", false},
                {"N", false}, {" N", false}, {"N ", false},
                {"no", false}, {" no", false}, {"no ", false},
                {"No", false}, {" No", false}, {"No ", false},
                {"NO", false}, {" NO", false}, {"NO ", false},
                //
                {null, false},
                {" any dummy text!", false},
        });
    }

    private String input;
    private boolean expected;

    public BooleanParserTest(String input, boolean expected) {
        this.input = input;
        this.expected = expected;
    }

    @Test
    public void checkParseBoolean() {
        assertEquals("Value '" + input + "' should be treated as '" + expected + "'",
                expected, BooleanParser.parseBoolean(input));
    }
}
