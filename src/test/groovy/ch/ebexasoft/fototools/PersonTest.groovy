/**
 * 
 */
package ch.ebexasoft.fototools

import static groovy.test.GroovyAssert.*
import groovy.json.JsonOutput
import groovy.json.StreamingJsonBuilder

import org.junit.Before
import org.junit.Test

import ch.ebexasoft.codebin.Person

/**
 * @author edith
 *
 */
class PersonTest {

	Person so1	
	Person so2
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		
		so1 = new Person ('object 1', 1969)
		so1 = new Person ('some object 2', 1986)
	}

	/**
	 * Test method for {@link ch.ebexasoft.fototools.Person#Person(java.lang.String)}.
	 */
	//@Test
	public void testPerson() {
		
    // TODO move Tests from Scripts.groovy to here
//		println (json2)
	}
	
}
