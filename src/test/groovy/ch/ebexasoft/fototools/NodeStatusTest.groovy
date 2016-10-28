/**
 * 
 */
package ch.ebexasoft.fototools

import org.junit.Test

import static groovy.test.GroovyAssert.assertEquals
import static groovy.test.GroovyAssert.assertNotNull
import static groovy.test.GroovyAssert.assertNull
import static groovy.test.GroovyAssert.assertTrue
import static groovy.test.GroovyAssert.shouldFail

import org.junit.Before
import org.junit.Test

/**
 * @author edith
 *
 */
class NodeStatusTest {

	File testRoot
	
	@Before
	void before() {
		testRoot = new File("/home/edith/Bilder/fürUli")
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
//		assertTrue (treeStatus.children instanceof Set)
		
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
		
		treeStatus.myStatus['copyright'] = "true"
		assertEquals ("/home/edith/Bilder/fürUli               : copyright  = true", treeStatus.printValue("copyright"))
				
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
//		fail("Not yet implemented"); // TODO
	}

}
