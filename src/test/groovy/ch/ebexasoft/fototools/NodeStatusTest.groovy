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
	Date testDate, testDate2
	DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss-zzz");
	
	@Before
	void before() {
		testRoot = new File("/home/edith/Bilder/fürUli")
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
		assertNotNull(treeStatus.myStatus)
		assertNull(treeStatus.children)		
	}

	
	@Test
	public void testInitChildren () {
			
		TreeNodeStatus treeStatus = new TreeNodeStatus(testRoot)
		assertNotNull(treeStatus)
		assertNotNull(treeStatus.myStatus)
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
		
		treeStatus.myStatus['copyright'] = ["true", testDate]
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
        
        treeStatus.myStatus['name'] = ["true", testDate]
        treeStatus.myStatus['copyright'] = ["false", testDate2]
        
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

	@Test
	public void testReadJson () {
				
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

        // object with non-empty status
		object = jsonSlurper.parseText '''{
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
	
}
