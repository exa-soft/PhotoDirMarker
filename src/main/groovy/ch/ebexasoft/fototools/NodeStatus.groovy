package ch.ebexasoft.fototools

import java.util.function.BiConsumer

// import groovy.json.JsonGenerator
import groovy.json.JsonOutput
import groovy.json.StreamingJsonBuilder

/**
 * Common class for node status. Will be subclassed for photo nodes
 * and children nodes.
 * 
 * @author edith
 */
abstract class NodeStatus {

  
  // LATER with Groovy 2.5, use JsonGenerator for time zone configuration of output
  // LATER maybe in the JsonGenerator, encoding can be set (because now, the StreamingJsonBuilder converts ü to \u00fc etc.)
  // Specifying / changing encoding below does not help, conversion occurs not when 
  // writing the file, but when encoding to json in the StreamingJsonBuilder).
  //public static String ENCODING = 'ISO-8859-1'
  public static String ENCODING = 'UTF-8'

  // static JsonGenerator jsonGenerator = JsonGenerator.Options()
  //   .timezone('GMT+01:00')
  //   .build()
  // see http://docs.groovy-lang.org/next/html/gapi/index.html?groovy/json/JsonGenerator.Options
      
  File parentDir

  /**
   * Map with the tags and their values. The tag name (e.g. 'name', 
   * 'copyright', 'keywords') is the key. The value is a heterogenous
   * list, with the value for the tag as first element, the date/time
   * when it has been set as the second element.
   * The map is not private (it is easier to create JSON), but it should only 
   * be written via putStatus/removeStatus!
   */
  //private Map status = [:] // will create new LinkedHashMap ()
  Map status = [:] // will create new LinkedHashMap ()
  
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
  def boolean containsStatusKey(String key) {
    return status.containsKey(key)
  }

  /**
   * @param key
   * @return
   * @see java.util.Map#get(java.lang.Object)
   */
  def Object getStatus(String key) {
    return status.get(key);
  }

  /**
   * @param key
   * @param value  (can be a string or a list, subclasses will have different implementations) 
   * @return
   * @see java.util.Map#put(java.lang.Object, java.lang.Object)
   */
  def Object putStatus(String key, Object value) {
    changed = true
    return status.put(key, value)
  }

  
  /**
   * @param m
   * @see java.util.Map#putAll(java.util.Map)
   */
  def void putStatusAll(Map m) {
    changed = true
    status.putAll(m);
  }

  /**
   * @param key
   * @return
   * @see java.util.Map#remove(java.lang.Object)
   */
  def Object removeStatus(String key) {
    changed = true
    return status.remove(key);
  }

  /**
   * @return
   * @see java.util.Map#keySet()
   */
  def Set statusKeySet() {
    return status.keySet();
  }

  /**
   * @param action
   * @see java.util.Map#forEach(java.util.function.BiConsumer)
   */
  def void forEachStatus(BiConsumer action) {
    status.forEach(action)
  }

  /**
   * Write this node to a JSON string
   * @return the JSON String (pretty)
   */
  def String toJson () {

    StringWriter writer = new StringWriter()
    StreamingJsonBuilder builder = new StreamingJsonBuilder(writer)
    // LATER with Groovy 2.5, use JsonGenerator for time zone configuration of output (see also top of this class)
    //StreamingJsonBuilder builder = new StreamingJsonBuilder(writer, generator)
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
    
    if (changed) {
      target.withPrintWriter(NodeStatus.ENCODING) {
        it.write(toJson())
      }
      println "file written to ${target.name}"
      changed = false
    }
    else {
      println ("file was unchanged: ${target.name}"
)
    }
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
    obj.putStatusAll(map['status'])
  }

  def String toString () {
    "[parentDir=$parentDir,\nstatus=$status]"
  }

}


