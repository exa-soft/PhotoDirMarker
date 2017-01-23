package ch.ebexasoft.fototools

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.List

import groovy.json.JsonParserType
import groovy.json.JsonSlurper


/**
 * Class that encapsulates status of this directory (no links to children)
 * and can be read/written to the file 'thisDirFileStatus.txt'.
 *
 * @author edith
 *
 */
class MyNodeStatus extends NodeStatus {
  
  public static final String FILENAME = 'thisDirFileStatus.txt'
  public static final String DATEFORMAT_STRING = "yyyy-MM-dd'T'HH:mm:ss-z"
  public static final DateFormat DATEFORMAT = new SimpleDateFormat (MyNodeStatus.DATEFORMAT_STRING)
  
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


    /**
     * Set a value for a specified key, together with a timestamp of the date/time when
     * it has been set.
     * @param key the key
     * @param value the value to be set
     * @param overwrite if false, the value is only set if it does not yet exist
     * @return the new value, if changed; null otherwise
     */
    public List setValue (String key, String value, boolean overwrite) {
      
        println "called setValue with Boolean object as parameter"
        if (overwrite || !status.containsKey(key)) {
            String timestamp = MyNodeStatus.DATEFORMAT.format(new Date())
            return put (key, [value, timestamp])
        }
        return null
    }
    
    /**
     * @param key
     * @param value the value as String (will be added to the map together with a time-stamp)
     * @return
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    def Object put(String key, String value) {
      List l = createValueWithDate (value)
      return put (key, l)
    }
    
    /**
     * Create a list with a value and the date
     * @param value   value
     * @param date for the value (optional, default is now)
     * @return
     */
    def List createValueWithDate (String value, Date date = new Date()) {
      [ value, date ]
    }
    
}

