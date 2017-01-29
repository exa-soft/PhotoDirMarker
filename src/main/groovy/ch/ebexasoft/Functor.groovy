/**
 * 
 */
package ch.ebexasoft

import groovy.lang.Closure

/**
 * Common computational patterns, inspired by http://www.ibm.com/developerworks/library/j-pg08235/
 * 
 * @author edith
 */

abstract class Functor {
  
  //  arithmetic (binary, left commute and right commute)
  public static Closure bMultiply     = { x, y -> return x * y }
  public static Closure rMultiply     = { y, x -> return x * y }
  public static Closure lMultiply     = { x, y -> return x * y }

  // composition
  public static Closure composition   = { f, g, x -> return f(g(x)) }

  // lists
  public static Closure map    = { action, list -> return list.collect(action) }

  /**
   * apply an action to each element in the list
   */
  public static Closure apply  = { action, list -> list.each (action) }

  public static Closure forAll = { predicate, list ->
    for (element in list) {
      if (predicate(element) == false) {
        return false
      }
    }
    return true
  }
  
  
  /**
   * Traverse a tree of objects. The objects must have a property <code>children</code> which is
   * a list of objects also having children etc.
   * The action is performed first on the object itself, then on all children and so on.
   * @param action    a closure working on an object with children
   * @param obj       an object which has a list of children of the same type in obj.children
   */
  public static Closure applyRecursiveMeFirst = { action, obj ->
    
    // TODO remove printing when null
      if (obj == null) {
          //println "(applyRecursiveMeFirst start: obj is null)"
          return
      }
      if (action == null) {
          //println "(applyRecursiveMeFirst start: action is null)"
          return
      }
      //println "(applyRecursiveMeFirst start: called for $obj)"
      action (obj)
      obj?.children.each () { child ->
          //println "applyRecursiveMeFirst: recursive calling for $child"
          Functor.applyRecursiveMeFirst (action, child)
      }
  }

  /**
   * Traverse a tree of objects, but allows variable depths as follows: The objects must have a 
   * property <code>children</code> which is a list of objects also having children etc.
   * The action is performed first on the object itself. If that returns false, recursion is not 
   * done deeper. If it returns true, the action is performed on all children of the object, and so on.
   * @param action    a closure working on an object with children, returning true if children of the object should also be handled
   * @param obj       an object which has a list of children of the same type in obj.children
   */
  public static Closure applyRecursiveWithFeedback = { action, obj ->
    
      //println "(applyRecursiveMeFirst start: called for $obj)"
      def cont = action (obj)
      if (cont) {
          obj?.children.each () { child ->
              //println "applyRecursiveMeFirst: recursive calling for $child"
              Functor.applyRecursiveWithFeedback (action, child)
          }
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
    
      //println "(applyRecursiveChildrenFirst start: called for $obj)"
      obj?.children.each () { child ->
          //println "applyRecursiveChildrenFirst: recursive calling for $child"
          Functor.applyRecursiveChildrenFirst (action, child)
      }
      action (obj)
  }

}