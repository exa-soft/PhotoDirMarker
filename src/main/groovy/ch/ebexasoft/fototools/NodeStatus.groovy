package ch.ebexasoft.fototools

/**
 * Common class for node status. Will be subclassed for photo nodes
 * and children nodes.
 * 
 * @author edith
 */
abstract class NodeStatus {
	
	File parentDir
	
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
		
		String value = myStatus[key]
		println "value is $value"
		return sprintf ('%1$-40s: %2$-10s = %3$s', [parentDir.absolutePath, key, value])		
	}
				
//		assert 'Groovy is cool!' == sprintf( '%2$s %3$s %1$s', ['cool!', 'Groovy', 'is'])
//		assert '00042' == sprintf('%05d', 42)
}

/**
 * Class that encapsulates status of this directory 
 * 
 * @author edith
 *
 */
class TreeNodeStatus extends NodeStatus 
{

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
