package ch.ebexasoft.fototools

import java.util.Map
import ch.ebexasoft.Functor
import ch.ebexasoft.fototools.Scripts

/**
 * Class to test recursion.
 * 
 * @author edith
 */
class Person {

	String name 
	Map myStatus = [:]     // will create new LinkedHashMap ()
  List children = []     // will create a new List
  int year 
	
	/**
	 * 
	 */
	public Person (String name, int year) {
		
		this.name = name
    this.year = year
		this.myStatus['key'] = 'a value'
	}
  

//  private listChildList = { Person obj ->
//    
//      println "(listChildList called on $obj)"
//      // TODO find notation for != null check
//      if (obj.children != null) {
//        if (obj.children.size() <= 0)
//          println "ich habe keine Kinder"
//        else {
//          println "ich habe ${obj.children.size()} Kinder:"
//          obj.children.each {
//              println "- Kind: ${it.presentMe()}"
//          }
//        }
//      }
//  }

  def presentPerson = { Person person ->
    //println "(called presentPerson for $person)"
    person.presentMe()
  }
    
  private listChildren = Person.applyOnChildren.curry (presentPerson)
  private listTree = Person.applyRecursiveMeFirst.curry (presentPerson)
  private listTree2 = Person.applyRecursiveChildrenFirst.curry (presentPerson)
  
  
  String toString() {
    "Person $name"
  }
  
  def presentMe () {
    println "ich bin $name, Jahrgang $year"
  }
  
  def stellKinderVor () {
    
    println ""
    presentMe()
    println "meine Kinder sind:"
    listChildren(this)
  }
  
  def stellStammbaumVor () {
    println ""
    presentMe()
    println "meine Kinder und Kindeskinder sind:"
    listTree(this)
  }

  def stellStammbaumVorVonUnten () {
    println ""
    presentMe()
    println "Stammbaum von zuunterst:"
    listTree2(this)
  }

  public static Closure applyOnChildren = { action, obj ->
      obj?.children.each (action)  
  }
  
  public static Closure applyOnObjAndChildren = { action, obj ->
    action (obj)
    obj?.children.each (action)
  }
  
  /**
   * Traverse a tree of objects. The objects must have a property <code>children</code> which is 
   * a list of objects also having children etc.
   * The action is performed first on the object itself, then on all children and so on.
   * @param action    a closure working on an object with children
   * @param obj       an object which has a list of children of the same type in obj.children 
   */
  public static Closure applyRecursiveMeFirst = { action, obj ->
    
      //println "(applyRecursive2 start: called for $person)"
      action (obj)
      obj?.children.each () { child ->
          //println "applyRecursive2: recursive calling for $child"
          Person.applyRecursiveMeFirst (action, child)
      }
  }
  
  
  /**
   * Traverse a tree of objects. The objects must have a property <code>children</code> which is
   * a list of objects also having children etc.
   * The action is performed first on all children (and their children etc.) and in the end 
   * on the object itself.
   * @param action    a closure working on an object with children
   * @param obj       an object which has a list of children of the same type in obj.children
   */
  public static Closure applyRecursiveChildrenFirst = { action, obj ->
    
      //println "(applyRecursive2 start: called for $person)"
      obj?.children.each () { child ->
          //println "applyRecursive2: recursive calling for $child"
          Person.applyRecursiveChildrenFirst (action, child)
      }
      action (obj)
  }
  
    
}



