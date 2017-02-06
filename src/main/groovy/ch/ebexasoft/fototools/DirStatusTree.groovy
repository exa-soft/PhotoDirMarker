package ch.ebexasoft.fototools

import java.io.File
import java.io.PrintStream
import java.util.List

import groovy.lang.Closure
import groovy.util.IndentPrinter

import ch.ebexasoft.Functor


/**
 * Class that holds the information about one node in the directory tree. It stores:
 * <ul>
 * <li>the MyNodeStatus object of this directory (if there are images)</li>
 * <li>the DirStatus object (combination of status of all the children of this directory)</li>
 * <li>a list of all children DirStatusTree objects</li>
 * </ul>
 *
 * Each node combines the MyNodeStatus of all children into a DirStatus object.
 *
 * @author edith
 *
 */
class DirStatusTree {

    File parentDir
    MyNodeStatus myNodeStatus
    DirStatus dirStatus
    List children = null // contains DirStatusTree objects
    
    /**
     * 
     */
    public DirStatusTree(File parentDir) {
        this.parentDir = parentDir
//        println "created new DirStatusTree object for $parentDir"
    }
    
    /**
     * Start at parentDir and traverse the folder structure to collect
     * all information into an object structure.
     * Will be done only once (does nothing if children is not null).
     *
     * @return
     */
    def initChildren () {

        // collect children, if not already done
        if (children == null) {

            // read info for the current dir (this is non-recursive)
            myNodeStatus = MyNodeStatus.fromDir(parentDir)
            println "initialized myNodeStatus for $parentDir: $myNodeStatus"

            // in recursion, first look deeper: collect children
            this.children = []
            parentDir.eachDir () {
                DirStatusTree childStatus = new DirStatusTree (it)
                childStatus.initChildren ()
                this.children.add(childStatus)
            }
            // println "added status objects for all children of ${parentDir.absolutePath}"
            
            if (!children.empty) {
                this.dirStatus = new DirStatus(parentDir)
                int keySize = recollect()
                println "Collected keys: there are $keySize keys"
                
//                dirStatus.toFile()
//                println "written to file: dirStatus for $parentDir"
            }   // if there are children
           
        }
    }
    
    /**
     * Traverses the tree bottom up, scanning the MyNodeStatus objects and updating the
     * DirStatus objects accordingly, by creating or combining values for the keys.
     * @return size of the keys map
     */
    public recollect () {
      
        def recollectRecursive = Functor.applyRecursiveChildrenFirst.curry(recollect1)
        return recollectRecursive (this)
    }
    
    /**
     * Does .initChildren, then lists the tree (with indenting the levels)
     */
    def listTree (PrintStream ps) {
        
        initChildren()
        
        IndentPrinter p = new IndentPrinter (new PrintWriter(ps), '+---')
        listTreeInternal(p)
        p.flush()
    }
    
    private def listTreeInternal (IndentPrinter p) {
        
        p.printIndent()
//      p.println(parentDir.absolutePath)
        p.println(parentDir.name)
        
        p.incrementIndent()
        children.each { it.listTreeInternal(p) }
        p.decrementIndent()
    }  
    
    
    // LATER set multiple values in one go?
    /**
     * Set a value on the tree (recursively). The value is set to the myNodeStatus objects,
     * together with a timestamp when it has been changed.
     * @param key
     * @param value
     * @param overwrite
     */
    def setValue (String key, String value, boolean overwrite) {

        //println "\nsetValue: key='$key' to value '$value' recursively"
        
        // How to call the function setValue2Obj recursively? Very cool generic recursion solution in Groovy:
        // We define the rightmost 3 parameters for setValue2Obj and use that as new function
        def setTheseValuesToObj = setValue2Obj1.rcurry (key, value, overwrite)
        // the new function we give to the applyRecursive... method
        def setTheseValuesRecursive = Functor.applyRecursiveChildrenFirst.curry(setTheseValuesToObj)
        // finally, we run it with 'this' as starting value
        setTheseValuesRecursive (this)
    }
    
    /**
     * Clears a value on the tree (recursively). The value is cleared from the myNodeStatus 
     * objects, as well as from the DirStatus objects (although when recollecting, it would 
     * not appear there anyway).  
     * @param key
     */
    def clearValue (String key) {

        def clearThisValueFromObj = clearValue2Obj1.rcurry (key)
        // the new function we give to the applyRecursive... method
        def clearThisValueRecursive = Functor.applyRecursiveChildrenFirst.curry(clearThisValueFromObj)
        // finally, we run it with 'this' as starting value
        clearThisValueRecursive (this)
    }
    
    /**
     * Writes all changed dirStatus and myObjectStatus objects to their files (recursively).
     */
    def writeChangesToFiles () {
        
        def toFile = Functor.applyRecursiveMeFirst.curry(toFile1)
        toFile (this)
    }

    // TODO test print
    def print (String key) {
        
        def printThatKey = printDirStatus1.rcurry(key)
        def printThatKeyRecursive = Functor.applyRecursiveWithFeedback.curry (printThatKey)
        printThatKeyRecursive(this)
    }
    
    /**
     * Prints the status for the given keys
     * @param keys
     */
    def printNewMulti (String[] keys) {
        for (key in keys) {
            print(key)
        }
    }
    
    // for debugging: 
    
    
    
    /**
     * Set a value on the tree (recursively). The value is set to the myNodeStatus objects,
     * together with a timestamp when it has been changed.
     * @returns a map with the flags if the file should been written because changed
     */
    def Map collectChangedFlags () {

        //println "\nsetValue: key='$key' to value '$value' recursively"
        
        Map flagMap = [:]
        // How to call the function setValue2Obj recursively? Very cool generic recursion solution in Groovy:
        // We define the rightmost 3 parameters for setValue2Obj and use that as new function
        def collectInThisMap = collectChangedFlags1.rcurry (flagMap)
        // the new function we give to the applyRecursive... method
        def collectInThisMapRecursive = Functor.applyRecursiveMeFirst.curry(collectInThisMap)
        // finally, we run it with 'this' as starting value
        collectInThisMapRecursive (this)
        return flagMap
    }
    
  
    
/*---------------------------------------------------------------------------------------
 * Below are functions that work on one DirStatusTree object (without calling children). 
 * Therefore they can be made to work recursively by currying and calling them with one 
 * of the Functor.applyRecursive... methods.
 * These methods are marked with an appended '1' (to show that they only work on 1 object).
 */
    
    private listChildList1 = { DirStatusTree dst ->
      
        println "listChildren called on $dst"
        dst?.children.each {
            println "- child $it"
        }
    }
    private listChildren = Functor.apply.curry (listChildList1)
    
    
    /**
     * It works on the given treeObj and sets (in the myNodeStatus, if that is not null) 
     * the given value for the given key, together with a time-stamp.
     * If there is already a value for the given key, the overwrite parameter defines if 
     * the value will be overwritten. 
     * Note that this function will be called recursively, so it must not call its children. 
     * 
     * @param treeObj the tree object to work on
     * @param key   the key
     * @param value the value
     * @param overwrite   if true, an existing value will be overwritten; if false, it will be only set if new
     */
    private setValue2Obj1 = { DirStatusTree treeObj, String key, String value, boolean overwrite ->
        
        println "setValue2Obj1 (overwrite='$overwrite'): set value='$value' for key='$key', on $treeObj"
        if (treeObj?.myNodeStatus != null) {
            def newValue = treeObj?.myNodeStatus.putStatus (key, value, overwrite)
            //println "newvalue is $newValue"
        }
    }

    /**
     * It works on the given treeObj and clears (in the myNodeStatus, if that is not null)
     * the value for the given key.
     * Note that this function will be called recursively, so it must not call its children.
     *
     * @param treeObj the tree object to work on
     * @param key   the key
     */
    private clearValue2Obj1 = { DirStatusTree treeObj, String key ->
        
        println "clearValue2Obj1: clear for key='$key', on $treeObj"
        if (treeObj?.myNodeStatus != null) {
            def newValue = treeObj?.myNodeStatus.removeStatus (key)
        }
    }

    private printDirStatus1 = { DirStatusTree treeObj, String key ->  
    // private printDirStatus = { DirStatusTree treeObj, String[] keys ->
        if (treeObj?.dirStatus) {
            String value = dirStatus?.getStatus(key)
            if (value == null) {
                printf ('%1$-100s: %2$-10s = %3$s\n', [treeObj.dirStatus.parentDir.absolutePath, key, value])
                //return false
                return true
            }
            else if (value in ['true', 'false', '?']) {
                printf ('%1$-100s: %2$-10s = %3$s\n', [treeObj.dirStatus.parentDir.absolutePath, key, value])
                //return false
                return true
            }
            else {
                printf ('%1$-100s: %2$-10s = %3$s\n', [treeObj.dirStatus.parentDir.absolutePath, key, value])
                return true
            }
        }      
    } 
    
    /**
     * Works on a DirStatusTree object and writes the status of its objects 
     * to files (both DirStatus and MyNodeStatus objects)
     */
    private def toFile1 = { DirStatusTree treeObj ->
        
        //println "toFile1 for ${treeObj.parentDir}"
        if (treeObj.myNodeStatus) {
            treeObj.myNodeStatus.toFile()
            println "(toFile1) written myNodeStatus to file '${MyNodeStatus.FILENAME}' for '${treeObj.parentDir}'"
        }
        if (treeObj.dirStatus) {
            treeObj.dirStatus.toFile()
            println "(toFile1) written dirStatus to file '${DirStatus.FILENAME}' for '${treeObj.parentDir}'"
        }
    }
    
    private def collectChangedFlags1 = { DirStatusTree treeObj, Map collectedFlags ->

        println "collectChangedFlags for ${treeObj.parentDir}"
        String sep = treeObj.parentDir.separator
        if (treeObj.myNodeStatus) {
            String key = treeObj.myNodeStatus.parentDir.path + sep + MyNodeStatus.FILENAME
            collectedFlags.put (key, treeObj.myNodeStatus.changed)
            println "(collectChangedFlags1) added to map: key $key, value ${treeObj.myNodeStatus.changed}"
        }
        if (treeObj.dirStatus) {
            String key = treeObj.dirStatus.parentDir.path + sep + DirStatus.FILENAME
            collectedFlags.put (key, treeObj.dirStatus.changed)
            println "(collectChangedFlags1) added to map: key $key, value ${treeObj.dirStatus.changed}"
        }
    }
    
    /**
     * Combines the status of the children of the given treeObj into its dirStatus
     * @return number of keys in dirStatus (0 if the given treeObj has no children)
     */
    private def recollect1 = { DirStatusTree treeObj -> 

        if (treeObj.children.empty)
            return 0
            
        // from the children, collect all existing tag keys (we have to do this because some 
        // keys may not be present in all children)
        Set keys = new HashSet()
        treeObj.children.each { childNode ->
            // collect keys from both dirStatus and myNodeStatus
            if (childNode.dirStatus != null) {
                //println "childNode ${childNode.parentDir} has a dirStatus, collecting its keys"
                keys.addAll (childNode.dirStatus.statusKeySet())
            }
            if (childNode.myNodeStatus != null) {
                //println "childNode ${childNode.parentDir} has a myNodeStatus, collecting its keys"
                keys.addAll (childNode.myNodeStatus.statusKeySet())
            }
        }
        println "all children of ${treeObj.parentDir} combined have ${keys.size()} keys"
        println "keys: $keys"
        
        // loop through the keys and collect the values from all the children's dirStatus and the treeObj's 
        // myNodeStatus into the treeObj's dirStatus (forget the old value from treeObj's dirStatus)
        keys.each { key -> 
            println "collecting values for key '$key'"
            treeObj.dirStatus.removeStatus(key)
            treeObj.children.each { childNode ->
                //if (childNode.dirStatus?.status != null) {
                if (childNode.dirStatus != null) {
                    //println "childNode ${childNode.parentDir} has a dirStatus, will combine its values"
                    String value = childNode.dirStatus?.getStatus(key)
                    treeObj.dirStatus.combineValue (key, value)     // values in dirStatus are only Strings (true, false, etc.)
                }
                else {
                    //println "childNode ${childNode.parentDir} has no dirStatus, will combine values from myNodeStatus"
                    String[] values = childNode.myNodeStatus?.getStatus(key)     // values in myNodeStatus are String[] (first value true, false, etc., second value timestamp)
                    treeObj.dirStatus.combineValue (key, (values != null ? values[0] : null))
                }
            }
            // combine my own status
            if (treeObj.myNodeStatus != null) {
                String[] values = treeObj.myNodeStatus.getStatus(key)
                //if (values == null) println "myNodeStatus for $parentDir has no value for key $key"
                treeObj.dirStatus.combineValue (key, (values != null ? values[0] : null))
            }
        }
        return treeObj.dirStatus.size()
    }
    

/*------------------------------
 * other functions
 */
    
    /**
     * String representation of this class
     */
    def String toString () {
        
        //"[parentDir=${parentDir.name},\n    myNodeStatus=$myNodeStatus,\n    dirStatus=$dirStatus]"
      if (children)
          "[DirStatusTree: parentDir=${parentDir.name}, with ${children.size()} children]"
      else
          "[DirStatusTree: parentDir=${parentDir.name}, with no children]"
    }

}
