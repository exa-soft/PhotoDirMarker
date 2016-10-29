/**
 * 
 */
package ch.ebexasoft.fototools

import java.util.Map;

/**
 * @author edith
 *
 */
class SomeObjectForTesting {

	String name 
	Map myStatus = new LinkedHashMap ()
//	File myRoot
	
	/**
	 * 
	 */
	public SomeObjectForTesting(String name) {
		
		this.name = name
		this.myStatus['key'] = 'a value'
//		this.myRoot = new File ('/home')
	}
		

}
