/**
 * 
 */
package ch.ebexasoft.fototools

import static groovy.test.GroovyAssert.assertEquals
import static groovy.test.GroovyAssert.assertNotNull
import static groovy.test.GroovyAssert.assertNull
import static groovy.test.GroovyAssert.assertTrue
import static groovy.test.GroovyAssert.shouldFail
import groovy.json.JsonSlurper
import jdk.nashorn.internal.ir.LiteralNode.NullLiteralNode;

import java.text.DateFormat
import java.text.SimpleDateFormat

import org.junit.Before
import org.junit.Test

/**
 * @author edith
 *
 */
class NodeStatusTest {

	File testRoot
    File resourcesDir
	Date testDate, testDate2
	DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss-zzz");
	
	@Before
	void before() {
		testRoot = new File("/home/edith/Bilder/fürUli")
        resourcesDir = new File ('src/test/resources')
		testDate = df.parse("2016-10-29T18:59:45-MESZ")
        testDate2 = df.parse("2016-12-07T05:35:57-MESZ")
	}
	
	/**
	 * Test method for {@link ch.ebexasoft.fototools.NodeStatus#NodeStatus(java.io.File)}.
	 */
	@Test
	public void testTreeNodeStatus() {
			
		TreeNodeStatus treeStatus = new TreeNodeStatus(testRoot)
		assertNotNull(treeStatus)
		assertNotNull(treeStatus.status)
		assertNull(treeStatus.children)		
	}

	
	@Test
	public void testInitChildren () {
			
		TreeNodeStatus treeStatus = new TreeNodeStatus(testRoot)
		assertNotNull(treeStatus)
		assertNotNull(treeStatus.status)
		assertNull(treeStatus.children)
				
		treeStatus.initChildren()
		assertNotNull(treeStatus.children)
		assertTrue (treeStatus.children instanceof List)
		treeStatus.children.each { assertNotNull(it) }
	}

	
	/**
	 * Test method for {@link ch.ebexasoft.fototools.NodeStatus#initChildren()}.
	 */
	@Test
	public void testPrintValue() {
		
		TreeNodeStatus treeStatus = new TreeNodeStatus(testRoot)
		treeStatus.initChildren()
		assertNotNull(treeStatus.children)
		
		treeStatus.status['copyright'] = ["true", testDate]
		assertEquals (
			"/home/edith/Bilder/fürUli               : copyright  = true (2016-10-29T18:59:45)", 
			treeStatus.printValue("copyright")
		)				
	}

	
	
	/**
	 * Test method for {@link ch.ebexasoft.fototools.NodeStatus#printValue(java.lang.String)}.
	 */
	@Test
	public void testListTree() {
		
		TreeNodeStatus treeStatus = new TreeNodeStatus(testRoot)
		assertNotNull (System.out)
		assertTrue (System.out instanceof PrintStream)
		
		treeStatus.listTree(System.out)
	}

	
	/**
	 * Test method for {@link ch.ebexasoft.fototools.MyNodeStatus#printValue(java.lang.String)}.
	 */
	@Test
	public void testPrintJson() {
		
		MyNodeStatus treeStatus = new MyNodeStatus(testRoot)
		
		String json = treeStatus.toJson()
		assertNotNull (json)
		println "json String:\n$json"
		assertTrue (System.out instanceof PrintStream)
		assertEquals ( 
'''{
    "dir": "/home/edith/Bilder/fürUli",
    "status": {
        
    }
}''', json)
        
        treeStatus.status['name'] = ["true", testDate]
        treeStatus.status['copyright'] = ["false", testDate2]
        
        json = treeStatus.toJson()
        assertNotNull (json)
        println "json String:\n$json"
        
        assertEquals (
'''{
    "dir": "/home/edith/Bilder/fürUli",
    "status": {
        "name": ["true", "2016-10-29T18:59:45-MESZ"],
        "copyright": ["false", "2016-12-07T05:35:57-MESZ"]
    }
}''', json)
        
	}

    /**
     * Test method to read a json String (with empty status map)  
     */
	@Test
	public void testReadJson_emptyStatus () {
				
//		The JSON standard supports the following primitive data types: string, number, object, true, false and null. JsonSlurper converts these JSON types into corresponding Groovy types.
        def jsonSlurper = new JsonSlurper()
        
        // object with empty status
        def object = jsonSlurper.parseText '''{
       		"dir": "/home/edith/irgendwo/Fotos",
			"status": {
            
			}
		}
		}'''
		assert object instanceof Map
		assert object.dir instanceof String
        assertEquals "/home/edith/irgendwo/Fotos", object.dir
		assert object.status instanceof Map
        assertEquals null, object.status.size       							
	}

    
    /**
     * Test method to read a json String (with 2 elements in the empty status map)
     */
    @Test
	public void testReadJson_nonemptyStatus () {
				
        def jsonSlurper = new JsonSlurper()
        
        // object with non-empty status
		def object = jsonSlurper.parseText '''{
			"dir": "/home/edith/irgendwo/Fotos",
			"status": {
				"name": ["true", "2016-10-29T18:59:45-MESZ"],
				"copyright": ["false", "2016-12-07T05:35:57-MESZ"]
			}
		}'''
        assert object instanceof Map
        assert object.dir instanceof String
        assertEquals "/home/edith/irgendwo/Fotos", object.dir
        assert object.status instanceof Map
        Map sts = object.status
        assertEquals 2, sts.size()
        
        assert sts.keySet().contains('name')
        def values = sts['name']
        assertEquals 2, values.size()
        assertEquals 'true', values[0]
        assertEquals '2016-10-29T18:59:45-MESZ', values[1]
        
        assert sts.keySet().contains('copyright')
        values = sts['copyright']
        assertEquals 2, values.size()
        assertEquals 'false', values[0]
        assertEquals '2016-12-07T05:35:57-MESZ', values[1]
	}
	
    
    /**
     * Test method to create a MyNodeStatus object from a json File 
     * (with 2 elements in the empty status map)
     */
    @Test
    public void testReadJson_file2Object () {

        assertEquals '/data/DevelopmentEB/Groovy/PhotoDirMarker/src/test/resources',
            resourcesDir.absolutePath
        File testDir = new File (resourcesDir, 'jsonSource1').absoluteFile
        assertEquals '/data/DevelopmentEB/Groovy/PhotoDirMarker/src/test/resources/jsonSource1',
            testDir.absolutePath
        
        MyNodeStatus st = MyNodeStatus.fromDir(testDir)
        
        assert st instanceof MyNodeStatus        
        assert st.parentDir instanceof File
        assertEquals testDir.absoluteFile, st.parentDir
        assert st.status instanceof Map
        Map sts = st.status
        assertEquals 2, sts.size()
        
        assert sts.keySet().contains('name')
        def values = sts['name']
        assertEquals 2, values.size()
        assertEquals 'true', values[0]
        assertEquals '2016-10-29T18:59:45-MESZ', values[1]
        
        assert sts.keySet().contains('copyright')
        values = sts['copyright']
        assertEquals 2, values.size()
        assertEquals 'false', values[0]
        assertEquals '2016-12-07T05:35:57-MESZ', values[1]
    }

    
    /**
     * Test method to create a json file MyNodeStatus object from a json String
     * (with 2 elements in the empty status map)
     */
    @Test
    public void testWriteJson_obj2File () {

        assertEquals '/data/DevelopmentEB/Groovy/PhotoDirMarker/src/test/resources',
            resourcesDir.absolutePath
        File testDir = new File (resourcesDir, 'jsonTarget1').absoluteFile
        assertEquals '/data/DevelopmentEB/Groovy/PhotoDirMarker/src/test/resources/jsonTarget1',
            testDir.absolutePath

        if (!testDir.exists()) assert testDir.mkdir()
        File testTarget = new File (testDir, MyNodeStatus.FILENAME)
        if (testTarget.exists()) assert testTarget.delete()
        assert !testTarget.exists()
        
        MyNodeStatus st = new MyNodeStatus (testDir)
        st.status['name'] = ['true', '2016-10-29T18:59:45-MESZ']
        st.status['copyright'] = ['false', '2016-12-07T05:35:57-MESZ']
        
        st.toFile()
        assert testTarget.exists()
                
        String fileContents = testTarget.getText('UTF-8')
        assertEquals '''{
    "dir": "/data/DevelopmentEB/Groovy/PhotoDirMarker/src/test/resources/jsonTarget1",
    "status": {
        "name": [
            "true",
            "2016-10-29T18:59:45-MESZ"
        ],
        "copyright": [
            "false",
            "2016-12-07T05:35:57-MESZ"
        ]
    }
}''', fileContents

    }

    
}
