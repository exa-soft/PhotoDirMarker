/**
 * 
 */
package ch.ebexasoft.fototools

import static groovy.test.GroovyAssert.*
import groovy.json.JsonOutput
import groovy.json.StreamingJsonBuilder

import org.junit.Before
import org.junit.Test

/**
 * @author edith
 *
 */
class SomeObjectForTestingTest {

	SomeObjectForTesting so1	
	SomeObjectForTesting so2
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		
		so1 = new SomeObjectForTesting ('object 1')
		so1 = new SomeObjectForTesting ('some object 2')
	}

	/**
	 * Test method for {@link ch.ebexasoft.fototools.SomeObjectForTesting#SomeObjectForTesting(java.lang.String)}.
	 */
	@Test
	public void testSomeObjectForTesting() {
		
		def json = JsonOutput.toJson (so1)
		def json2 = JsonOutput.prettyPrint(json)
		assertNotNull (json2)
//		println (json2)
	}
	
	
	@Test
	public void testMyNodeStatusWithStreaming () {
		
		MyNodeStatus myNodeStatus = new MyNodeStatus(new File ('/home'))
		myNodeStatus.status['someKey'] = 'value1'
		myNodeStatus.status['key2'] = 'value 222'
	
		StringWriter writer = new StringWriter()
		StreamingJsonBuilder builder = new StreamingJsonBuilder(writer)
		builder.node {
			dir myNodeStatus.parentDir.absolutePath
			status myNodeStatus.status
		}
		String json = JsonOutput.prettyPrint(writer.toString())

		assertNotNull (json)
//		println (json)
				
	}

}
