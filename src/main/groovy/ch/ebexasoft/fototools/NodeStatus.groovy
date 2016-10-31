package ch.ebexasoft.fototools

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
	Map status = new LinkedHashMap ()
	
//	assert myStatus instanceof java.util.LinkedHashMap
//	def colors = [red: '#FF0000', green: '#00FF00', blue: '#0000FF']
//	assert colors['red'] == '#FF0000'
//	assert colors.green  == '#00FF00'	
//	colors['pink'] = '#FF00FF'
//	colors.yellow  = '#FFFF00'
//	assert colors.pink == '#FF00FF'
//	assert colors['yellow'] == '#FFFF00'	
//	assert colors instanceof java.util.LinkedHashMap
	
	NodeStatus (File parentDir) {
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
        builder {
			dir parentDir.absolutePath
			status status
		}
		String json = JsonOutput.prettyPrint(writer.toString())
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
		
        def jsonSlurper = new JsonSlurper(type: JsonParserType.LAX)
        def obj = jsonSlurper.parse(new File (parentDir, FILENAME), 'UTF-8')
                
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
 * Class that encapsulates status of this directory, with links to children
 * 
 * @author edith
 *
 */
class TreeNodeStatus extends NodeStatus {

	TreeNodeStatus (File parentDir) {
		super(parentDir)
	}
		
	List children = null
	
	/**
	 * Initializes the tree, starting from my directory. 
	 * Will be done only once (does nothing if children is not null).
	 * 
	 * @return
	 */
	def initChildren ()	{
		
		if (children == null) {
		
			children = []
			this.parentDir.eachDirRecurse() {
				NodeStatus childStatus = new TreeNodeStatus(it)
				childStatus.initChildren ()
				children.add(childStatus)
			}
//			println "have children for ${parentDir.absolutePath}"
		}
	}
	
	/**
	 * Lists the tree (with spacing so to indent the levels)
	 * @return
	 */
	def listTree (PrintStream ps) {
		
		initChildren()
		
		IndentPrinter p = new IndentPrinter (new PrintWriter(ps), '+---')
		listTreeInternal(p)
		p.flush()
	}
	
	
	private def listTreeInternal (IndentPrinter p) {
		
		p.printIndent()
//		p.println(parentDir.absolutePath)
        p.println(parentDir.name)
		
		p.incrementIndent()
		children.each { it.listTreeInternal(p) }
		p.decrementIndent()
	}
		
}
