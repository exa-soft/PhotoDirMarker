package ch.ebexasoft.fototools


import java.io.File;

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
	 * Put a AbstractNodeStatus object as pretty JSON into a String.
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
		
		new File(file, filename).withWriter (NodeStatus.ENCODING) { writer ->
			writer.write (json)
		}
	
	}
    
    
    /**
     * Untersucht, ob das Verzeichnis mindestens eine Bild-Datei enthält
     * (mit Endung aus  jpg|png|gif|bmp , nicht rekursiv).
     * @param dir   zu durchsuchendes Verzeichnis (File)
     * @return  true wienn das Verzeichnis min. 1 Bild enthält
     */
    static containsPics (File dir) {
        // have found pattern ([^\s]+(\.(?i)(jpg|png|gif|bmp))$)
        // in http://www.mkyong.com/regular-expressions/how-to-validate-image-file-extension-with-regular-expression/
        def found = dir.list().find {
            it =~ /([^\s]+(\.(?i)(jpg|jpeg|png|gif))$)/
        }
        return found    // is true if an image has been found
    }

	
}
