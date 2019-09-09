package sed.inf.u_szeged.hu.androidiotsimulator;

import org.junit.Test;

import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Parameter;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }
    @Test
    public void paramDecimal() throws Exception {
        assertEquals("210", new Parameter.ParamDecimal(210,0));
        assertEquals("21.0", new Parameter.ParamDecimal(210,1));
        assertEquals("2.10", new Parameter.ParamDecimal(210,2));
        assertEquals("0.210", new Parameter.ParamDecimal(210,3));
        assertEquals("0.00210", new Parameter.ParamDecimal(210,5));
        assertEquals("2100", new Parameter.ParamDecimal(210,-1));
        assertEquals("210000", new Parameter.ParamDecimal(210,-3));
    }
}