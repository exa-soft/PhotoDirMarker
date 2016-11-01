/**
 * 
 */
package ch.ebexasoft.fototools

import static groovy.test.GroovyAssert.assertEquals
import static groovy.test.GroovyAssert.assertNotNull
import static groovy.test.GroovyAssert.assertNull
import static groovy.test.GroovyAssert.assertTrue
import static groovy.test.GroovyAssert.assertFalse

import java.io.File;
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.List;

import org.junit.Before
import org.junit.Test


/**
 * @author edith
 *
 */
class DirStatusTreeTest {

    File testSourcesWork
    Date testDate
    Date testDate2
    
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss-zzz");
    
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {

        File testRoot = new File("src/test/resources/TreeNodeStatusTests")

        // copy test files to 'work' directory
        File testSourcesOrig = new File (testRoot, 'testDirOriginal')
        testSourcesWork = new File (testRoot, 'work')
        assert testSourcesOrig.exists()
//        if (testSourcesWork.exists()) assert testSourcesWork.delete()
        
//        Path testSourcesPath = testSourcesOrig.toPath()
//        Path testTargetPath  = testSourcesWork.toPath()        
//        Files.copy(testSourcesPath, testTargetPath)
                       
        testDate = df.parse("2016-10-29T18:59:45-MESZ")
        testDate2 = df.parse("2016-12-07T05:35:57-MESZ")
    }

    
    /**
     * Test method for {@link ch.ebexasoft.fototools.DirStatusTree#DirStatusTree(java.io.File)}.
     */
    @Test
    public void testDirStatusTree() {
            
        DirStatusTree treeStatus = new DirStatusTree (testSourcesWork)
        assertNotNull treeStatus
        assertNull treeStatus.myNodeStatus
        assertNull treeStatus.dirStatus
        assertNull treeStatus.children
    
        // TODO should init be done automatically?
    }


    /**
     * Is used in the test methods testInitChildren...
     */
    private DirStatusTree _testInitChildren (File testRoot) {
            
        DirStatusTree treeStatus = new DirStatusTree (testRoot)
        assertNotNull(treeStatus)
        assertNull(treeStatus.myNodeStatus)
        assertNull(treeStatus.children)
        
        treeStatus.initChildren()
        assertNotNull(treeStatus.children)
        assertTrue (treeStatus.children instanceof List)
        treeStatus.children.each { assertNotNull(it) }
        
        return treeStatus
    }

    
    /**
     * Test method for {@link ch.ebexasoft.fototools.DirStatusTree#initChildren()}.
     */
    @Test
    public void testInitChildren1Level () {
        
        String thisTestDir = '/data/DevelopmentEB/Groovy/PhotoDirMarker/src/test/resources/TreeNodeStatusTests/work/fürUli/gezeigt'
        File testRoot = new File (testSourcesWork, 'fürUli/gezeigt')
        assert testRoot.exists()
        DirStatusTree treeStatus = _testInitChildren(testRoot)
        
        assertNotNull treeStatus
        assertEquals thisTestDir, treeStatus.parentDir.absolutePath
        assertNull treeStatus.myNodeStatus
        
        assertNotNull treeStatus.dirStatus
        Map tagValues = treeStatus.dirStatus.status
        assertNotNull tagValues
        tagValues.forEach {key, value ->
            println "key '$key', values '$value'"
            switch (key) {
                case "y1y2y3":
                case "y1y2n3":
                case "y1y2q3":
                case "y1y2_3":
                assertEquals 'true', value
                break

                case "n1n2y3":
                case "n1n2n3":
                case "n1n2q3":
                case "n1n2_3":
                    assertEquals 'false', value
                    break

                case "q1q2y3":
                case "q1q2n3":
                case "q1q2q3":
                case "q1q2_3":
                    assertEquals '?', value
                    break            
            
                default:
                    assertEquals 'mixed', value
                    break
            }
            
        }
        
        assertEquals 2, treeStatus.children.size()
        treeStatus.children.forEach { child ->
            assertTrue child instanceof DirStatusTree
            child.parentDir.absolutePath.startsWith thisTestDir
            assertNotNull child.myNodeStatus
            assertNull child.dirStatus
            assertNotNull child.children
            assertEquals 0, child.children.size()
        }
        
    }

    /**
     * Test method for {@link ch.ebexasoft.fototools.DirStatusTree#initChildren()}.
     */
    @Test
    public void testInitChildrenMoreLevels () {

        File testRoot = new File (testSourcesWork, 'fürUli')
        assert testRoot.exists()
        _testInitChildren(testRoot)
        
        fail ("compare more values for $testRoot")
        // TODO compare more values for $testRoot

    }

    /**
     * Test method for {@link ch.ebexasoft.fototools.DirStatusTree#listTree(java.io.PrintStream)}.
     */
    @Test
    public void testListTree() {
        
        DirStatusTree treeStatus = new DirStatusTree (testSourcesWork)
        assertNotNull (System.out)
        assertTrue (System.out instanceof PrintStream)
        
        treeStatus.listTree(System.out)
    }
  
    /**
     * Test method for {@link ch.ebexasoft.fototools.DirStatusTree#initChildren()}.
     */
    @Test
    public void testPrintValue() {

        // TODO move test to work on some other dir (that has a myNodeStatus)         
        DirStatusTree treeStatus = new DirStatusTree (testSourcesWork)
        treeStatus.initChildren()
        assertNotNull(treeStatus.children)
        
        treeStatus.myNodeStatus.status['copyright'] = ["true", testDate]
        assertEquals (
            "/home/edith/Bilder/fürUli               : copyright  = true (2016-10-29T18:59:45)",
            treeStatus.printValue("copyright")
        )

    }    

}
