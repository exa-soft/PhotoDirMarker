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
class PhotoDirMarkerTest {

    public static final String SOURCE_DIR = 'src/main/groovy' 
    PhotoDirMarker mt
    StringWriter stringWriter
    File sourceRoot = new File (SOURCE_DIR)
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        
//        stringWriter = new StringWriter()
        mt = new PhotoDirMarker()
//        mt.cli.writer = stringWriter
         
    }

    /**
     * Test method for {@link ch.ebexasoft.fototools.MarkerTool#main(java.lang.String[])}.
     */
    // @Test
    public void testMain() {
        
        File groovyMarkerSource = new File (sourceRoot, 'ch/ebexasoft/fototools/PhotoDirMarker.groovy')
        String[] args = ['-h']
//        mt.run(groovyMarkerSource, args)      // doing this stops hangs execution of JUnit tests 
        
                
        fail("Not yet implemented"); // TODO
    }

}
