/**
 * 
 */
package ch.ebexasoft.fototools

import static groovy.test.GroovyAssert.assertEquals
import static groovy.test.GroovyAssert.assertNotNull
import static groovy.test.GroovyAssert.assertNull
import static groovy.test.GroovyAssert.assertTrue
import static groovy.test.GroovyAssert.fail

import java.text.DateFormat
import java.text.SimpleDateFormat

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
    private void _testInitChildren (File testRoot) {
            
        DirStatusTree treeStatus = new DirStatusTree (testRoot)
        assertNotNull(treeStatus)
        assertNull(treeStatus.myNodeStatus)
        assertNull(treeStatus.children)
        
        treeStatus.initChildren()
        assertNotNull(treeStatus.children)
        assertTrue (treeStatus.children instanceof List)
        treeStatus.children.each { assertNotNull(it) }
    }

    
    /**
     * Test method for {@link ch.ebexasoft.fototools.DirStatusTree#initChildren()}.
     */
    @Test
    public void testInitChildren1Level () {
        
        File testRoot = new File (testSourcesWork, 'fürUli/gezeigt')
        assert testRoot.exists()        
        _testInitChildren(testRoot)
        
        fail ("compare more values for $testRoot")
        // TODO compare more values for $testRoot
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
