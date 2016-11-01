/**
 * 
 */
package ch.ebexasoft.fototools

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * @author edith
 *
 */
class NodeUtilsTest {

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * Test method for {@link ch.ebexasoft.fototools.NodeUtils#toJson(ch.ebexasoft.fototools.MyNodeStatus)}.
     */
    @Test
    public void testToJson() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link ch.ebexasoft.fototools.NodeUtils#toFile(ch.ebexasoft.fototools.MyNodeStatus)}.
     */
    @Test
    public void testToFile() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link ch.ebexasoft.fototools.NodeUtils#containsPics(java.io.File)}.
     */
    @Test
    public void testContainsPics() {
        
        assert (!NodeUtils.containsPics (new File("src/test/resources/TreeNodeStatusTests/testDirOriginal/fürUli")))
        assert (!NodeUtils.containsPics (new File("src/test/resources/TreeNodeStatusTests/testDirOriginal/fürUli/gezeigt")))
        assert (NodeUtils.containsPics (new File("src/test/resources/TreeNodeStatusTests/testDirOriginal/fürUli/gezeigt/2016Mai")))
    }

}
