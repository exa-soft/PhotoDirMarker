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
     * Will be null if parentDir does not contain pictures.   
     * If there are pictures, but no file exists, an object will be created and marked 
     * as changed (so it will be later written to disk).
     * @see NodeStatus
     * @param dir   the directory where to read the file from
     * @return  the filled object, or null if parentDir does not contain pictures 
     */
    static MyNodeStatus fromDir (File parentDir) {
    
        if (!NodeUtils.containsPics(parentDir)) return null

        MyNodeStatus my = new MyNodeStatus(parentDir)
        assert my instanceof NodeStatus

        File statusFile = new File (parentDir, FILENAME)
        if (!statusFile.exists()) {
            System.out.println "missing status file $FILENAME in ${parentDir.absolutePath}, will be created"
            my.parentDir = parentDir.absoluteFile
            my.changed = true
            return my
        }
        
        def jsonSlurper = new JsonSlurper(type: JsonParserType.LAX)
        def obj = jsonSlurper.parse(statusFile, NodeStatus.ENCODING)
        assert obj instanceof Map
        NodeStatus.fillFromMap (obj, my, parentDir.absoluteFile)
        // this sets changed flag to true only if value for dir from file is different than parentDir.absoluteFile 
        
        return my
    }


    /**
     * Set a value for a specified key, together with a time-stamp of the date/time when
     * it has been set.
     * @param key the key
     * @param value the value to be set
     * @param overwrite if false, the value is only set if it does not yet exist
     * @return the new value, if changed; null otherwise
     */
    public List putStatus (String key, String value, boolean overwrite) {
        if (overwrite || !status.containsKey(key)) {
            List l = createValueWithDate (value)
            return putStatus (key, l)
        }
        return null
    }
    
    /**
     * Create a list with a value and the date as formatted String
     * @param value   value
     * @param date  time-stamp for the value (optional, default is now)
     * @return the list with value and time-stamp
     */
    def List createValueWithDate (String value, Date date = new Date()) {
      String timestamp = MyNodeStatus.DATEFORMAT.format(date)
      [ value, timestamp ]
    }
    
}

