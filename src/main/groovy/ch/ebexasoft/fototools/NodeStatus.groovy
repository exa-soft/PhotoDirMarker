package ch.ebexasoft.fototools

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Map
import java.util.Set
import java.util.function.BiConsumer

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
  private Map status = [:] // will create new LinkedHashMap ()

  /**
   * Will be set to true if something has been changed, and set to false when 
   * file is written.  
   */
  boolean changed = false


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
        [parentDir.absolutePath, key, value[0], value[1]])
  }


  /**
   * @return
   * @see java.util.Map#size()
   */
  def int size() {
    return status.size()
  }

  /**
   * @param key
   * @return
   * @see java.util.Map#containsKey(java.lang.Object)
   */
  def boolean containsKey(String key) {
    return status.containsKey(key)
  }

  /**
   * @param key
   * @return
   * @see java.util.Map#get(java.lang.Object)
   */
  def Object get(String key) {
    return status.get(key);
  }

  /**
   * @param key
   * @param value  (can be a string or a list, subclasses will have different implementations) 
   * @return
   * @see java.util.Map#put(java.lang.Object, java.lang.Object)
   */
  def Object put(String key, Object value) {
    changed = true
    return status.put(key, value)
  }

  
  /**
   * @param m
   * @see java.util.Map#putAll(java.util.Map)
   */
  def void putAll(Map m) {
    status.putAll(m);
  }

  /**
   * @param key
   * @return
   * @see java.util.Map#remove(java.lang.Object)
   */
  def Object remove(String key) {
    return status.remove(key);
  }

  /**
   * @return
   * @see java.util.Map#keySet()
   */
  def Set keySet() {
    return status.keySet();
  }

  /**
   * @param action
   * @see java.util.Map#forEach(java.util.function.BiConsumer)
   */
  def void forEach(BiConsumer action) {
    status.forEach(action)
  }

  /**
   * Write this node to a JSON string
   * @return the JSON String (pretty)
   */
  def String toJson () {

    StringWriter writer = new StringWriter()
    StreamingJsonBuilder builder = new StreamingJsonBuilder(writer)
    toJsonBuilder builder
    return JsonOutput.prettyPrint(writer.toString())
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
    changed = false
  }

  /** 
   * Helper method to do something with all entries in the status map    
   * @param action    closure to apply to all elements
   */
  def list (Closure action) {
    status.each { action(it) }
  }


  /**
   * Helper method to instantiate objects from JSON (via pure Groovy objects).
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
    //obj.status.putAll(map['status'])
    obj.putAll(map['status'])
  }

  def String toString () {
    "[parentDir=$parentDir,\nstatus=$status]"
  }

}


