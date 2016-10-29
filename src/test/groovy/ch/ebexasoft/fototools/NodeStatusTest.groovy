/**
 * 
 */
package ch.ebexasoft.fototools

import static groovy.test.GroovyAssert.assertEquals
import static groovy.test.GroovyAssert.assertNotNull
import static groovy.test.GroovyAssert.assertNull
import static groovy.test.GroovyAssert.assertTrue
import static groovy.test.GroovyAssert.shouldFail

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
	Date testDate
	DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss-zzz");
	
	@Before
	void before() {
		testRoot = new File("/home/edith/Bilder/fürUli")
		testDate = df.parse("2016-10-29T18:59:45-MESZ")
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
	public void testChildren () {
			
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
	public void testInitChildren() {
		
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
	public void testPrintValue() {
		
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
	}

	
}
