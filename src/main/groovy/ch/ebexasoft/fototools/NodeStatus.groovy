package ch.ebexasoft.fototools

import java.util.Date;

import groovy.json.JsonOutput
import groovy.json.JsonParserType
import groovy.json.JsonSlurper
import groovy.json.StreamingJsonBuilder


/**
 * Common class for node status. Will be subclassed for photo nodes
 * and children nodes.
 * 
 * @author edith
 */
abstract class NodeStatus {
	
	File parentDir
	
	/**
	 * Map with the tags and their values. The tag name (e.g. 'name', 
	 * 'copyright', 'keywords') is the key. The value is a heterogenous
	 * list, with the value for the tag as first element, the date/time
	 * when it has been set as the second element. 
	 */
	Map status = [:] // will create new LinkedHashMap ()
	
//	assert myStatus instanceof java.util.LinkedHashMap
//	def colors = [red: '#FF0000', green: '#00FF00', blue: '#0000FF']
//	assert colors['red'] == '#FF0000'
//	assert colors.green  == '#00FF00'	
//	colors['pink'] = '#FF00FF'
//	colors.yellow  = '#FFFF00'
//	assert colors.pink == '#FF00FF'
//	assert colors['yellow'] == '#FFFF00'	
//	assert colors instanceof java.util.LinkedHashMap
	
	NodeStatus(File parentDir) {
		this.parentDir = parentDir.absoluteFile
	}
	
	/**
	 * Returns a line containing the dir name, the key and value, nicely formatted. 		
	 * @param key	key
	 * @return		formatted string
	 */
	def String printValue (String key) {
		
		List value = status[key]
		println "value is ${value[0]}, date is ${value[1]}"
		return sprintf ('%1$-40s: %2$-10s = %3$s (%4$tFT%4$tT)', 
			[parentDir.absolutePath, key, value[0], value[1]]
		)		
	}
	
	/**
	 * Create a list with a value and the date
	 * @param value		value
	 * @param date for the value (optional, default is now) 
	 * @return
	 */
	def List createValueWithDate (String value, Date date = new Date()) {
		[ value, date ]
	}

	/**
	 * Write this node to a JSON string
	 * @return the JSON String (pretty)
	 */
	def String toJson () {
		
		StringWriter writer = new StringWriter()
		StreamingJsonBuilder builder = new StreamingJsonBuilder(writer)
        toJsonBuilder builder
		String json = JsonOutput.prettyPrint(writer.toString())
	}

    /**
     * Inner format for JSON. Can be overwritten by subclasses
     * @param builder 
     * @return the JSON String (pretty)
     */
    def void toJsonBuilder (StreamingJsonBuilder builder) {
        
        builder {
            dir parentDir.absolutePath
            status status
        }
    }

	
	/**
	 * Write this node to the given file in JSON format
	 */
	def toFile (File target) {
		
		target.withPrintWriter('UTF-8') {
			it.write(toJson())
		}
	} 

    /** 
     * Helper method to do something with all entries in the status map    
     * @param action    closure to apply to all elements
     */
    def list (Closure action) {
        status.each {
            action(it)
        }
    }

    
    /**
     * Helper method to instatiate objects from JSON (via pure Groovy objects).
     * Must be a map with two entries: 
     * <ul>
     * <li>key 'dir', value the parent directory path</li>
     * <li>key 'status', value a map with tag values, where each has its tag name 
     * as key and an array for the values (first value the tag value string, second 
     * value the date)</li>
     * </ul>
     * However, the values will not be tested, will only be read from the map and 
     * put into the object (parentDir and status map).  
     * @param map   a map with the object
     * @param obj   the object to initialize (fill) with values from the map
     * @return  the filled object
     */
    static NodeStatus fillFromMap (Map map, NodeStatus obj) {
                
        assert map.keySet().contains('dir')
        assert map.keySet().contains('status')
        assert map['dir'] instanceof String
        assert map['status'] instanceof Map
        
        obj.parentDir = new File(map['dir'])
        obj.status.putAll(map['status'])
    }

    def String toString () {
        "[parentDir=$parentDir,\nstatus=$status"
    }

}


/**
 * Class that encapsulates status of this directory (no links to children)
 * and can be read/written to the file 'thisDirFileStatus.txt'.
 * 
 * @author edith
 *
 */
class MyNodeStatus extends NodeStatus {
	
	public static final String FILENAME = 'thisDirFileStatus.txt'

	MyNodeStatus (File parentDir) {
		super(parentDir)
	}
	
	/**
	 * Write the status to the file #FILENAME in the node's 
	 * directory. 
	 */
	def toFile () {
		
		toFile (new File (parentDir, FILENAME))
	}
	
    /**
     * Helper method to instantiate objects from JSON (via pure Groovy objects).
     * @see NodeStatus
     * @param dir   the directory where to read the file from
     * @return  the filled object
     */	
	static MyNodeStatus fromDir (File parentDir) {
		
        if (!NodeUtils.containsPics(parentDir)) return null
        
        File statusFile = new File (parentDir, FILENAME)
        if (!statusFile.exists()) {
            System.err.println "missing status file $FILENAME in ${parentDir.absolutePath}"
            return null
        }
        
        def jsonSlurper = new JsonSlurper(type: JsonParserType.LAX)
        def obj = jsonSlurper.parse(statusFile, 'UTF-8')
                
        MyNodeStatus my = new MyNodeStatus(parentDir)
        assert obj instanceof Map
        assert my instanceof NodeStatus
        NodeStatus.fillFromMap (obj, my)
        // TODO should we warn if obj.parentDir not equals parentDir? (file in wrong directory)
        my.parentDir = parentDir.absoluteFile
        return my
	} 
	
} 


/**
 * Class that encapsulates the combined status of this directory and all 
 * its children. 
 * It can be written to the file 'collectedFileStatus.txt', but these files 
 * will not be read, they remain in the directory as a marker for the (human)
 * user and for usage outside of this tool.<br>
 * Status is combined as follows: 
 * <ul>
 * <li>Process starts at deepest point in folder structure. It collects 
 * all tags of all folders on that level into one map. If a tag already 
 * exists with a different value, its value in the combined map will be 
 * set to "mixed".</li>
 * <li>Then the result is propagated to the next higher level and again 
 * combined with those of the sister folders.</li>
 * </ul>    
 *
 * @author edith
 *
 */
class DirStatus extends NodeStatus {
    
    public static final String FILENAME = 'collectedFileStatus.txt'
    public static final String MIXEDVALUE = 'mixed'
    
    Date creationDate
    

    DirStatus (File parentDir) {
        super(parentDir)
        creationDate = new Date()
    }
    
    /**
     * Write the status to the file #FILENAME in the node's
     * directory.
     */
    def toFile () {
        
        toFile (new File (parentDir, FILENAME))
    }
    
    /**
     * Inner format for JSON. Adds date field to the code from superclass
     * (copy/paste - could not find another way).
     * @param builder
     * @return the JSON String (pretty)
     */
    def void toJsonBuilder (StreamingJsonBuilder builder) {
        
        builder {
            dir parentDir.absolutePath
            created this.creationDate
            status status
        }
    }
    
    /** 
     * Combines the given value with the existing ones: when the key does not 
     * exist, the value will be  added. If it does exist and has a different 
     * value than the one from the parameter, it will be changed to "mixed" 
     * (@link #MIXEDVALUE}.
     * (A value being null thus also ends up in changing the existing value 
     * to "mixed" if the map previously contained a value other than null.)
     * @param key       the key
     * @param value     the value
     */
    def combineValue (String key, String value) {
        
        if (!status.containsKey(key)) { 
            status[key] = value
        }
        else {
            String existingValue = status[key]
            if (!existingValue.equals(value))
                status[key] = MIXEDVALUE
        }
    }
    
    /**
     * Same as {@link #combineValue (String, String), but the value parameter can be 
     * given as array (first element the tag value, second element the date/time).
     * If values 
     * @param key       the key
     * @param values     the value list (tag value, date/time) 
     */
    def combineValueList (String key, String[] values) {
        
        if (values == null)
            combineValue (key, null)
        else
            combineValue (key, values[0])
    }
    
    /** 
     * Clears the given key (removes it from the tag list)
     * @param key       the key
     */
    def clearValue (String key) {
        status.remove(key)
    }
    
}

