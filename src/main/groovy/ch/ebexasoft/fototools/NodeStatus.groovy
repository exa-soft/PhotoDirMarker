package ch.ebexasoft.fototools

import groovy.json.JsonOutput
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
	Map myStatus = new LinkedHashMap ()
	
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
		this.parentDir = parentDir
	}
	
	/**
	 * Returns a line containing the dir name, the key and value, nicely formatted. 		
	 * @param key	key
	 * @return		formatted string
	 */
	def String printValue (String key) {
		
		List value = myStatus[key]
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
		builder.node {
			dir parentDir.absolutePath
			status myStatus
		}
		String json = JsonOutput.prettyPrint(writer.toString())
	}

	
//		assert 'Groovy is cool!' == sprintf( '%2$s %3$s %1$s', ['cool!', 'Groovy', 'is'])
//		assert '00042' == sprintf('%05d', 42)
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
//	Set children = null
	
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
//	def listTree () {
		
		initChildren()
		
		IndentPrinter p = new IndentPrinter (new PrintWriter(ps))
		listTreeInternal(p)
		p.flush()
	}
	
	
	private def listTreeInternal (IndentPrinter p) {
		
		p.printIndent()
		p.println(parentDir.absolutePath)
		
		p.incrementIndent()
		children.each { it.listTreeInternal(p) }
		p.decrementIndent()
	}
		
}
