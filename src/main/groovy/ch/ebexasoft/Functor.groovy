/**
 * 
 */
package ch.ebexasoft

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
  
}