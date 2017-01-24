package ch.ebexasoft.fototools

import java.io.File
import java.util.Date

import groovy.json.StreamingJsonBuilder

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
        
      if (!containsStatusKey(key)) {
        putStatus (key, value)
      }
      else {
        String existingValue = getStatus(key)
        if (!existingValue.equals(value))
          putStatus (key, MIXEDVALUE)
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
        removeStatus(key)
    }
    
}
