/**
 * 
 */
package ch.ebexasoft.fototools


import groovy.json.JsonOutput

/**
 * Utils to work with nodes.
 *
 * @author edith
 *
 */
class NodeUtils {

	/**
	 * 
	 */
	public NodeUtils() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Put a NodeStatus object as pretty JSON into a String.
	 *
	 * @param nodeStatus 	the node status object
	 * @return	the nodeStatus as JSON String
	 */
	static String toJson (MyNodeStatus nodeStatus) {
		
		def json = JsonOutput.toJson (nodeStatus)
		JsonOutput.prettyPrint(json)
	}
	


	public String toFile (MyNodeStatus nodeStatus) {
		
		String filename = nodeStatus.FILENAME
		File file = new File (nodeStatus.parentDir, filename)
		String json = toJson (nodeStatus)
		
		new File(file, filename).withWriter ('UTF-8') { writer ->
			writer.write (json)
		}
	
	}

	
}
