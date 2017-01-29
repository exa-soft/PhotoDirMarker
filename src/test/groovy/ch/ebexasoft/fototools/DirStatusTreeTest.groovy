/**
 * 
 */
package ch.ebexasoft.fototools

import static groovy.test.GroovyAssert.assertEquals
import static groovy.test.GroovyAssert.assertNotNull
import static groovy.test.GroovyAssert.assertNull
import static groovy.test.GroovyAssert.assertTrue
import static groovy.test.GroovyAssert.fail

import groovy.util.AntBuilder

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.text.DateFormat
import java.text.SimpleDateFormat

import org.junit.Before
import org.junit.Test

import ch.ebexasoft.Functor


/**
 * @author edith
 * 
 * TODO some methods only work for paths without umlauts, therefore later test with 'fuerUli' instead of 'fürUli'
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
        if (testSourcesWork.exists()) deleteDir(testSourcesWork.absolutePath)
        assert !testSourcesWork.exists()
        assert testSourcesWork.mkdir()
        copyDir (testSourcesOrig.absolutePath, testSourcesWork.absolutePath)
        
        testDate = df.parse("2016-10-29T18:59:45-MESZ")
        testDate2 = df.parse("2016-12-07T05:35:57-MESZ")
    }
    
    private void deleteDir (String dirToDelete) {
        new AntBuilder().delete (dir: dirToDelete)
    }

    private void copyDir (String sourceDir, String destDir) {
        new AntBuilder().copy(todir: destDir) {
            fileset(dir: sourceDir)
        }
        // - See more at: http://www.tothenew.com/blog/copy-filesfolders-from-one-location-to-another-in-groovy
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
        
        // LATER should init be done automatically?
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
        
        // String thisTestDir = '/data/DevelopmentEB/Groovy/PhotoDirMarker/src/test/resources/TreeNodeStatusTests/work/fürUli/gezeigt'
        String thisTestDir = '/data/DevelopmentEB/Groovy/PhotoDirMarker/src/test/resources/TreeNodeStatusTests/work/fürUli/gezeigt'
        //File testRoot = new File (testSourcesWork, 'fuerUli/gezeigt')
        File testRoot = new File (testSourcesWork, 'fürUli/gezeigt')
        assert testRoot.exists()
        DirStatusTree treeStatus = _testInitChildren(testRoot)
        
        assertNotNull treeStatus
        assertEquals thisTestDir, treeStatus.parentDir.absolutePath
        assertNull treeStatus.myNodeStatus
        
        assertNotNull treeStatus.dirStatus
        Date createdDate = treeStatus.dirStatus.creationDate
        assertNotNull createdDate
        assertTrue (createdDate - new Date() < 5000)
        
        Map tagValues = treeStatus.dirStatus.status
        assertNotNull tagValues
        tagValues.forEach {key, value ->
            //println "key '$key', values '$value'"
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

        String thisTestDir = '/data/DevelopmentEB/Groovy/PhotoDirMarker/src/test/resources/TreeNodeStatusTests/work'
        File testRoot = testSourcesWork
        assert testRoot.exists()
        DirStatusTree treeStatus = _testInitChildren(testRoot)
        
        assertNotNull treeStatus
        assertEquals thisTestDir, treeStatus.parentDir.absolutePath
        assertNull treeStatus.myNodeStatus
        
        assertNotNull treeStatus.dirStatus
        Date createdDate = treeStatus.dirStatus.creationDate
        assertNotNull createdDate
        assertTrue (createdDate - new Date() < 5000)
        
        // TODO find out why file in /work is empty

        Map tagMap = treeStatus.dirStatus.status
        assertNotNull tagMap
        
        def expectedTags = [
            'y1y2y3', 'y1y2n3', 'y1y2q3', 'y1y2_3', 'y1n2', 'y1_2', 'y1q2',
            'n1y2', 'n1n2y3', 'n1n2n3', 'n1n2q3', 'n1n2_3', 'n1_2', 'n1q2',
            'q1y2', 'q1n2', 'q1_2', 'q1q2y3', 'q1q2n3', 'q1q2q3', 'q1q2_3',
            '_1y2', '_1n2', '_1q2', '_1_2y3', '_1_2n3', '_1_2q3']
        assert expectedTags.containsAll(tagMap.keySet())
        assert tagMap.keySet().containsAll(expectedTags)

        tagMap.forEach {key, value ->
            println "key '$key', values '$value'"
            switch (key) {
                case "y1y2y3":
                assertEquals 'true', value
                break

                case "n1n2n3":
                    assertEquals 'false', value
                    break

                case "q1q2q3":
                    assertEquals '?', value
                    break
            
                default:
                    assertEquals 'mixed', value
                    break
            }
            
        }
        
        assertEquals 1, treeStatus.children.size()
        assertTrue treeStatus.children[0] instanceof DirStatusTree
        DirStatusTree child = treeStatus.children[0]
        child.parentDir.absolutePath.startsWith thisTestDir
        assertNull child.myNodeStatus
        assertNotNull child.dirStatus
        assertNotNull child.children
        assertEquals 2, child.children.size()
        
        
        
//        fail "compare more values for $testRoot"
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
     * Test method for {@link ch.ebexasoft.fototools.DirStatusTree#printValue()}.
     */
    @Test
    public void testPrintValue() {

        File testRoot = new File (testSourcesWork, 'fürUli/2016Schweden-Makro')
        assert testRoot.exists()
        
        
        DirStatusTree treeStatus = new DirStatusTree (testRoot)
        treeStatus.initChildren()
        assertNotNull(treeStatus.children)
        
        treeStatus.myNodeStatus.putStatus('copyright', ["true", testDate])
        assertEquals (
            "/data/DevelopmentEB/Groovy/PhotoDirMarker/src/test/resources/TreeNodeStatusTests/work/fürUli/2016Schweden-Makro: copyright  = true (2016-10-29T18:59:45)",
            treeStatus.myNodeStatus.printValue("copyright")
        )
    }


    /**
     * Test method for {@link ch.ebexasoft.fototools.DirStatusTree#traverse()}.
     * Depends on testInitChildrenMoreLevels
     */
    @Test
    public void testTraverse () {
        
        String thisTestDir = '/data/DevelopmentEB/Groovy/PhotoDirMarker/src/test/resources/TreeNodeStatusTests/work'
        File testRoot = testSourcesWork
        assert testRoot.exists()
        DirStatusTree treeStatus = _testInitChildren(testRoot)
        
        // test traverse with printing
        def printClosure = { dst ->
            if (dst == null)
                println "print: DirStatus is null"
            else if (dst.dirStatus == null) {
                println "this is DirStatus, but is null"
            }
            else {
                println "this is DirStatus from ${dst?.dirStatus.parentDir}"
            } 
        }
        def printRecursive = Functor.applyRecursiveMeFirst.curry(printClosure)
        printRecursive (treeStatus)
        //treeStatus.traverseDirStatus (printClosure)

                
        // test traverse with changing value
        def changeValueClosure = { dst ->
            dst?.dirStatus?.status?.put ('y1n2', 'changed')
        }
        def changeValueRecursive = Functor.applyRecursiveMeFirst.curry(changeValueClosure)
        changeValueRecursive(treeStatus)
        //treeStatus.traverseDirStatus (changeValueClosure)
        assert treeStatus.dirStatus.status.y1n2 == 'changed'
        treeStatus.children.each { child ->
            assert child.dirStatus.status.y1n2 == 'changed'
        }

        // test traverse with new value
        def addValueClosure = { dst ->
            dst?.dirStatus?.status?.put('someNew', 'newValue')
        }
        def addValueRecursive = Functor.applyRecursiveMeFirst.curry(addValueClosure)
        //treeStatus.traverseDirStatus (addValueClosure)
        addValueRecursive(treeStatus)
        assert treeStatus.dirStatus.status.someNew == 'newValue'
        treeStatus.children.each { child ->
            assert child.dirStatus.status.someNew == 'newValue'
        }
        
    }
        

    /**
     * Test method for {@link ch.ebexasoft.fototools.DirStatusTree#setValue()}.
     * Depends on testInitChildrenMoreLevels
     */
    @Test
    public void testSetValue () {
        
        //String thisTestDir = '/data/DevelopmentEB/Groovy/PhotoDirMarker/src/test/resources/TreeNodeStatusTests/work/fuerUli'
        File testRoot = testSourcesWork
        assert testRoot.exists()
        DirStatusTree treeStatus = _testInitChildren(testRoot)

        // test values 
        Map parentSt = treeStatus.dirStatus.status
        assertContains (parentSt, 'y1y2y3', 'true')
        assertContains (parentSt, 'n1n2n3', 'false')

        // TODO change tests to account for changing status (should also update dirStatus)
        
        
        // with the test setup, treeStatus.myNodeStatus is null (because top dir has no pictures),
        // so none of the tests must check treeStatus.myNodeStatus.status....
        // We can check a grandchild instead:

        def DirStatusTree firstGrandchild = treeStatus.children[0].children[0]
        
        // overwrite existing value (last param true)
        treeStatus.setValue('y1y2y3', 'newValue', true)
        //assert treeStatus.myNodeStatus.status.y1y2y3[0] == 'newValue'
        assert firstGrandchild.myNodeStatus.status.y1y2y3[0] == 'newValue'
        treeStatus.children.each { child ->
            if (child.myNodeStatus?.status) 
                assert child.myNodeStatus?.status.y1y2y3[0] == 'newValue'
        }
        firstGrandchild.children.each { child ->
            if (child.myNodeStatus?.status)
                assert child.myNodeStatus?.status.y1y2y3[0] == 'newValue'
        }

        // existing value, do not overwrite (last param false)
        treeStatus.setValue('n1n2n3', 'someNew', false)
        assert firstGrandchild.myNodeStatus?.status.n1n2n3[0] == 'false'
        treeStatus.children.each { child ->
            if (child.myNodeStatus?.status) 
                assert child.myNodeStatus?.getStatus('n1n2n3')[0] == 'false'
        }
        firstGrandchild.children.each { child ->
            if (child.myNodeStatus?.status)
                assert child.myNodeStatus?.getStatus('n1n2n3')[0] == 'false'
        }

        // add new value
        String newKey = '_4'
        String newValue = 'someNew'
        assert firstGrandchild != null
        assert firstGrandchild.myNodeStatus.getStatus(newKey) == null
        treeStatus.setValue(newKey, newValue, false)
        assert firstGrandchild.myNodeStatus.getStatus(newKey)[0] == newValue  // value [1] is a timestamp
        assertEquals 1, treeStatus.children.size() 
        treeStatus.children.each { child ->
            if (child.myNodeStatus?.status)
                assert child.myNodeStatus?.getStatus(newKey)[0] == newValue
        }
    }
        
    def assertContains (Map map, String key, String expected) {
        assert map.containsKey(key)
        assert map.get(key) == expected
    }


    /**
     * Test method for {@link ch.ebexasoft.fototools.DirStatusTree#recollect()}.
     * Depends on testInitChildrenMoreLevels
     */
    @Test
    public void testRecollect () {
        
        File testRoot = testSourcesWork
        assert testRoot.exists()
        DirStatusTree treeStatus = _testInitChildren(testRoot)
        String[] testValues = ['for-y1_2-NewValue', 'for-_1y2-NewValue', 'for--unchanged']

        // all thisDirFileStatus.txt should be unchanged (are new and read from disk), all 
        // collectedFileStatus.txt should be marked as changed (because they have not yet been written to disk)
        Map flagMap = treeStatus.collectChangedFlags()
        assert flagMap[testRoot.absolutePath + '/fürUli/gezeigt/2016Mai/thisDirFileStatus.txt'] == false
        assert flagMap[testRoot.absolutePath + '/fürUli/gezeigt/2016Mai/collectedFileStatus.txt'] == null
        assert flagMap[testRoot.absolutePath + '/fürUli/gezeigt/2016Berge/thisDirFileStatus.txt'] == false
        assert flagMap[testRoot.absolutePath + '/fürUli/gezeigt/2016Berge/collectedFileStatus.txt'] == null
        assert flagMap[testRoot.absolutePath + '/fürUli/gezeigt/thisDirFileStatus.txt'] == null
        assert flagMap[testRoot.absolutePath + '/fürUli/gezeigt/collectedFileStatus.txt'] == true
        assert flagMap[testRoot.absolutePath + '/fürUli/2016Schweden-Makro/thisDirFileStatus.txt'] == false
        assert flagMap[testRoot.absolutePath + '/fürUli/2016Schweden-Makro/collectedFileStatus.txt'] == null
        assert flagMap[testRoot.absolutePath + '/fürUli/thisDirFileStatus.txt'] == null
        assert flagMap[testRoot.absolutePath + '/fürUli/collectedFileStatus.txt'] == true
        assert flagMap[testRoot.absolutePath + '/thisDirFileStatus.txt'] == null
        assert flagMap[testRoot.absolutePath + '/collectedFileStatus.txt'] == true

        treeStatus.writeChangesToFiles()        
        // so far this is the same as testWriteAllFiles
        
        // check if changed flag was adjusted - we expect only false now
        flagMap = treeStatus.collectChangedFlags()
        flagMap.each { k, v -> assert v == false }
        
        // set a value that changes a lot of files (because it overwrites an existing value)
                
        // set a value to every node (with overwrite=true)
        treeStatus.setValue ('y1_2', testValues[0], true)
        
        // collect changed flags for the directories:
        flagMap = treeStatus.collectChangedFlags()
        // as we have not re-collected yet, all (existing) thisDirFileStatus.txt should be changed, 
        // all collectedFileStatus.txt unchanged
        assert flagMap[testRoot.absolutePath + '/fürUli/gezeigt/2016Mai/thisDirFileStatus.txt'] == true
        assert flagMap[testRoot.absolutePath + '/fürUli/gezeigt/2016Mai/collectedFileStatus.txt'] == null
        assert flagMap[testRoot.absolutePath + '/fürUli/gezeigt/2016Berge/thisDirFileStatus.txt'] == true
        assert flagMap[testRoot.absolutePath + '/fürUli/gezeigt/2016Berge/collectedFileStatus.txt'] == null
        assert flagMap[testRoot.absolutePath + '/fürUli/gezeigt/thisDirFileStatus.txt'] == null
        assert flagMap[testRoot.absolutePath + '/fürUli/gezeigt/collectedFileStatus.txt'] == false
        assert flagMap[testRoot.absolutePath + '/fürUli/2016Schweden-Makro/thisDirFileStatus.txt'] == true
        assert flagMap[testRoot.absolutePath + '/fürUli/2016Schweden-Makro/collectedFileStatus.txt'] == null
        assert flagMap[testRoot.absolutePath + '/fürUli/thisDirFileStatus.txt'] == null
        assert flagMap[testRoot.absolutePath + '/fürUli/collectedFileStatus.txt'] == false
        assert flagMap[testRoot.absolutePath + '/thisDirFileStatus.txt'] == null
        assert flagMap[testRoot.absolutePath + '/collectedFileStatus.txt'] == false
 
        // re-collect -> all should now be changed
        int keySize = treeStatus.recollect()
        assert keySize == 27
        flagMap = treeStatus.collectChangedFlags()
        flagMap.each { k, v -> assert v == true }
        
        // write the changes - now all should be unchanged
        treeStatus.writeChangesToFiles()
        flagMap = treeStatus.collectChangedFlags()
        flagMap.each { k, v -> assert v == false }
        
        // change some values that do not occur in every node
        treeStatus.setValue ('_1y2', testValues[0], false)
        // This should affect thisDirFileStatus.txt in fürUli/gezeigt/2016Berge and in fürUli/2016Schweden-Makro, 
        // but not fürUli/gezeigt/2016Mai.
        // So collectedFileStatus.txt changes in fürUli/gezeigt, but not in fürUli/2016Schweden-Makro.
        // So collectedFileStatus.txt changes in fürUli/ and in test root.
        // But before re-collecting, we only see the changes in thisDirFileStatus.txt, all collectedFileStatus.txt are still unchanged
        flagMap = treeStatus.collectChangedFlags()
        assert flagMap[testRoot.absolutePath + '/fürUli/gezeigt/2016Berge/thisDirFileStatus.txt'] == true   // tag newly created
        assert flagMap[testRoot.absolutePath + '/fürUli/gezeigt/2016Berge/collectedFileStatus.txt'] == null
        assert flagMap[testRoot.absolutePath + '/fürUli/gezeigt/2016Mai/thisDirFileStatus.txt'] == false  //! tag existed, unchanged
        assert flagMap[testRoot.absolutePath + '/fürUli/gezeigt/2016Mai/collectedFileStatus.txt'] == null
        assert flagMap[testRoot.absolutePath + '/fürUli/gezeigt/thisDirFileStatus.txt'] == null
        assert flagMap[testRoot.absolutePath + '/fürUli/gezeigt/collectedFileStatus.txt'] == false  // not yet
        assert flagMap[testRoot.absolutePath + '/fürUli/2016Schweden-Makro/thisDirFileStatus.txt'] == true  // tag newly created
        assert flagMap[testRoot.absolutePath + '/fürUli/2016Schweden-Makro/collectedFileStatus.txt'] == null
        assert flagMap[testRoot.absolutePath + '/fürUli/thisDirFileStatus.txt'] == null
        assert flagMap[testRoot.absolutePath + '/fürUli/collectedFileStatus.txt'] == false  // not yet
        assert flagMap[testRoot.absolutePath + '/thisDirFileStatus.txt'] == null
        assert flagMap[testRoot.absolutePath + '/collectedFileStatus.txt'] == false  // not yet

        // now it should be as described above
        keySize = treeStatus.recollect()
        assert keySize == 27
        flagMap = treeStatus.collectChangedFlags()
        assert flagMap[testRoot.absolutePath + '/fürUli/gezeigt/2016Berge/thisDirFileStatus.txt'] == true  // as before
        assert flagMap[testRoot.absolutePath + '/fürUli/gezeigt/2016Berge/collectedFileStatus.txt'] == null
        assert flagMap[testRoot.absolutePath + '/fürUli/gezeigt/2016Mai/thisDirFileStatus.txt'] == false   // as before
        assert flagMap[testRoot.absolutePath + '/fürUli/gezeigt/2016Mai/collectedFileStatus.txt'] == null
        assert flagMap[testRoot.absolutePath + '/fürUli/gezeigt/thisDirFileStatus.txt'] == null
        assert flagMap[testRoot.absolutePath + '/fürUli/gezeigt/collectedFileStatus.txt'] == true   // now!
        assert flagMap[testRoot.absolutePath + '/fürUli/2016Schweden-Makro/thisDirFileStatus.txt'] == true  // as before
        assert flagMap[testRoot.absolutePath + '/fürUli/2016Schweden-Makro/collectedFileStatus.txt'] == null
        assert flagMap[testRoot.absolutePath + '/fürUli/thisDirFileStatus.txt'] == null
        assert flagMap[testRoot.absolutePath + '/fürUli/collectedFileStatus.txt'] == true   // now!
        assert flagMap[testRoot.absolutePath + '/thisDirFileStatus.txt'] == null
        assert flagMap[testRoot.absolutePath + '/collectedFileStatus.txt'] == true   // now!

    }

    /**
     * Test method for {@link ch.ebexasoft.fototools.DirStatusTree#writeAllFiles()}.
     * Depends on testInitChildrenMoreLevels
     */
    @Test
    public void testWriteChangesToFiles () {
      
        File testRoot = testSourcesWork.absoluteFile
        assert testRoot.exists()
        DirStatusTree treeStatus = _testInitChildren(testSourcesWork)
        String[] testValues = ['for-y1_2-NewValue', 'for-_1y2-NewValue', 'for--unchanged']
  
        // all thisDirFileStatus.txt should be unchanged (are new and read from disk), all
        // collectedFileStatus.txt should be marked as changed (because they have not yet been written to disk)
        String[] unchangedFilesPaths = [
            '/fürUli/gezeigt/2016Mai/thisDirFileStatus.txt',
            '/fürUli/gezeigt/2016Berge/thisDirFileStatus.txt',
            '/fürUli/2016Schweden-Makro/thisDirFileStatus.txt',
        ]
        String[] changedFilesPaths = [
            '/fürUli/gezeigt/collectedFileStatus.txt',
            '/fürUli/collectedFileStatus.txt',
            '/collectedFileStatus.txt',
        ]
        String[] nonexistingFilesPaths = [
            '/fürUli/gezeigt/2016Mai/collectedFileStatus.txt',
            '/fürUli/gezeigt/2016Berge/collectedFileStatus.txt',
            '/fürUli/gezeigt/thisDirFileStatus.txt',
            '/fürUli/2016Schweden-Makro/collectedFileStatus.txt',
            '/fürUli/thisDirFileStatus.txt',
            '/thisDirFileStatus.txt',
        ]
        Map flagMap = treeStatus.collectChangedFlags()
        unchangedFilesPaths.each { path ->
            assert flagMap[testRoot.absolutePath + path] == false
        }
        changedFilesPaths.each { path ->
            assert flagMap[testRoot.absolutePath + path] == true
        }
        nonexistingFilesPaths.each { path ->
            assert flagMap[testRoot.absolutePath + path] == null
        }
  
        // to be sure which files will be written, delete them all and then write the changes
        // and see if the correct files appear
        [changedFilesPaths, unchangedFilesPaths, nonexistingFilesPaths].flatten().each { path ->
            new File (testRoot, path).delete()
        }

        treeStatus.writeChangesToFiles()
        changedFilesPaths.each { path ->
            assert (new File (testRoot, path)).exists()
        }
        [unchangedFilesPaths, nonexistingFilesPaths].flatten().each { path ->
            assert !((new File (testRoot, path)).exists())
        }
    }
}
