 


package liquibase.util;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author asales
 */
public class BooleanParserTest {
    
    @Test
    public void testparseBoolean(){
        Assert.assertFalse(BooleanParser.parseBoolean("-1"));
        Assert.assertFalse(BooleanParser.parseBoolean(" -1"));
        Assert.assertFalse(BooleanParser.parseBoolean("-1 "));
        Assert.assertFalse(BooleanParser.parseBoolean("0"));
        Assert.assertFalse(BooleanParser.parseBoolean(" 0"));
        Assert.assertFalse(BooleanParser.parseBoolean("0 "));
        //
        Assert.assertTrue(BooleanParser.parseBoolean("1"));
        Assert.assertTrue(BooleanParser.parseBoolean(" 1"));
        Assert.assertTrue(BooleanParser.parseBoolean("1 "));
        Assert.assertTrue(BooleanParser.parseBoolean("2"));
        //
        Assert.assertTrue(BooleanParser.parseBoolean("true"));
        Assert.assertTrue(BooleanParser.parseBoolean(" true"));
        Assert.assertTrue(BooleanParser.parseBoolean("true "));
        Assert.assertTrue(BooleanParser.parseBoolean("True"));
        Assert.assertTrue(BooleanParser.parseBoolean(" True"));
        Assert.assertTrue(BooleanParser.parseBoolean("True "));
        Assert.assertTrue(BooleanParser.parseBoolean("TRUE"));
        Assert.assertTrue(BooleanParser.parseBoolean("TRUE "));
        Assert.assertTrue(BooleanParser.parseBoolean(" TRUE"));
        Assert.assertTrue(BooleanParser.parseBoolean("t"));
        Assert.assertTrue(BooleanParser.parseBoolean(" t"));
        Assert.assertTrue(BooleanParser.parseBoolean("t "));
        Assert.assertTrue(BooleanParser.parseBoolean("T"));
        Assert.assertTrue(BooleanParser.parseBoolean(" T"));
        Assert.assertTrue(BooleanParser.parseBoolean("T "));
        Assert.assertTrue(BooleanParser.parseBoolean("y"));
        Assert.assertTrue(BooleanParser.parseBoolean(" y"));
        Assert.assertTrue(BooleanParser.parseBoolean("y "));
        Assert.assertTrue(BooleanParser.parseBoolean("Y"));
        Assert.assertTrue(BooleanParser.parseBoolean(" Y"));
        Assert.assertTrue(BooleanParser.parseBoolean("Y "));
        Assert.assertTrue(BooleanParser.parseBoolean("yes"));
        Assert.assertTrue(BooleanParser.parseBoolean(" yes"));
        Assert.assertTrue(BooleanParser.parseBoolean("yes "));
        Assert.assertTrue(BooleanParser.parseBoolean("Yes"));
        Assert.assertTrue(BooleanParser.parseBoolean(" Yes"));
        Assert.assertTrue(BooleanParser.parseBoolean("Yes "));
        Assert.assertTrue(BooleanParser.parseBoolean("YES"));
        Assert.assertTrue(BooleanParser.parseBoolean(" YES"));
        Assert.assertTrue(BooleanParser.parseBoolean("YES "));
        //
        
        Assert.assertFalse(BooleanParser.parseBoolean("false"));
        Assert.assertFalse(BooleanParser.parseBoolean("false "));
        Assert.assertFalse(BooleanParser.parseBoolean(" false"));
        Assert.assertFalse(BooleanParser.parseBoolean("False"));
        Assert.assertFalse(BooleanParser.parseBoolean(" False"));
        Assert.assertFalse(BooleanParser.parseBoolean("False "));
        Assert.assertFalse(BooleanParser.parseBoolean("FALSE"));
        Assert.assertFalse(BooleanParser.parseBoolean(" FALSE"));
        Assert.assertFalse(BooleanParser.parseBoolean("FALSE "));
        Assert.assertFalse(BooleanParser.parseBoolean("f"));
        Assert.assertFalse(BooleanParser.parseBoolean(" f"));
        Assert.assertFalse(BooleanParser.parseBoolean("f "));
        Assert.assertFalse(BooleanParser.parseBoolean("F"));
        Assert.assertFalse(BooleanParser.parseBoolean(" F"));
        Assert.assertFalse(BooleanParser.parseBoolean("F "));
        Assert.assertFalse(BooleanParser.parseBoolean("n"));
        Assert.assertFalse(BooleanParser.parseBoolean(" n"));
        Assert.assertFalse(BooleanParser.parseBoolean("n "));
        Assert.assertFalse(BooleanParser.parseBoolean("N"));
        Assert.assertFalse(BooleanParser.parseBoolean(" N"));
        Assert.assertFalse(BooleanParser.parseBoolean("N "));
        Assert.assertFalse(BooleanParser.parseBoolean("no"));
        Assert.assertFalse(BooleanParser.parseBoolean(" no"));
        Assert.assertFalse(BooleanParser.parseBoolean("no "));
        Assert.assertFalse(BooleanParser.parseBoolean("No"));
        Assert.assertFalse(BooleanParser.parseBoolean(" No"));
        Assert.assertFalse(BooleanParser.parseBoolean("No "));
        Assert.assertFalse(BooleanParser.parseBoolean("NO"));
        Assert.assertFalse(BooleanParser.parseBoolean(" NO"));
        Assert.assertFalse(BooleanParser.parseBoolean("NO "));
        
        String test = null;
        Assert.assertFalse(BooleanParser.parseBoolean(test));
        Assert.assertFalse(BooleanParser.parseBoolean(" any dummy text!"));
    }
    
}
