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
                
                // from the children, collect all existing tag keys (we have to
                // do this because some keys may not be present in all files)
                Set keys = new HashSet()
                children.each { childNode ->
                    // collect keys from both dirStatus and myNodeStatus
                    if (childNode.dirStatus != null) {
                        //println "childNode ${childNode.parentDir} has a dirStatus, collecting its keys"
                        keys.addAll (childNode.dirStatus.status.keySet())
                    }
                    if (childNode.myNodeStatus != null) {
                        //println "childNode ${childNode.parentDir} has a myNodeStatus, collecting its keys"
                        keys.addAll (childNode.myNodeStatus.status.keySet())
                    }
                }
                println "all children of $parentDir combined have ${keys.size()} keys"
//                keys.each { key ->
//                    println "- key $key" 
//                }
                
                // loop through the keys and collect the values from the children's dirStatus and my own myNodeStatus
                keys.each { key ->
                    println "collecting values for key '$key'"
                    children.each { childNode ->
                        if (childNode.dirStatus?.status != null) {
                            //println "childNode ${childNode.parentDir} has a dirStatus, will combine its values"
                            String value = childNode.dirStatus?.status[key]
                            dirStatus.combineValue (key, value)     // values in dirStatus are only Strings (true, false, etc.)
                        }
                        else {
                            //println "childNode ${childNode.parentDir} has no dirStatus, will combine values from myNodeStatus"
                            String[] values = childNode.myNodeStatus?.status[key]     // values in myNodeStatus are String[] (first value true, false, etc., second value timestamp)
                            dirStatus.combineValue (key, (values != null ? values[0] : null))
                        }
                    }
                    // combine my own status
                    if (myNodeStatus != null) {
                        String[] values = myNodeStatus.status[key]
                        //if (values == null) println "myNodeStatus for $parentDir has no value for key $key"
                        dirStatus.combineValue (key, (values != null ? values[0] : null))
                    }
                }
                dirStatus.toFile()
                println "written to file: dirStatus for $parentDir"
            }   // if there are children
           
        }
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
     * Set a value on the tree (recursively)
     * @param key
     * @param value
     * @param overwrite
     */
    def setValue (String key, String value, boolean overwrite) {

        println "\nsetValue: key='$key' to value '$value' recursively"
        
        // How to call the function setValue2Obj recursively? Very cool generic recursion solution in Groovy:
        // We define the rightmost 3 parameters for setValue2Obj and use that as new function
        def setTheseValuesToObj = setValue2Obj.rcurry (key, value, overwrite)
        // the new function we give to the applyRecursive... method
        def setTheseValuesRecursive = Functor.applyRecursiveChildrenFirst.curry(setTheseValuesToObj)
        // finally, we run it with 'this' as starting value
        setTheseValuesRecursive (this)
    }
    
    private listChildList = { DirStatusTree dst ->
      
        println "listChildren called on $dst"
        dst?.children.each {
            println "- child $it"
        }
    }
    private listChildren = Functor.apply.curry (listChildList)
    
    
    /**
     * It works on the given treeObj and sets (in the myNodeStatus, if that is not null) 
     * the given value for the given key. If there is already a value for the given key, 
     * the overwrite parameter defines if the value will be overwritten. 
     * Note that this function will be called recursively, so it must not call its children. 

     * @param treeObj the tree object to work on
     * @param key   the key
     * @param value the value
     * @param overwrite   if true, an existing value will be overwritten; if false, it will be only set if new
     */
    private setValue2Obj = { DirStatusTree treeObj, String key, String value, boolean overwrite ->
        
        println "setValue2Obj: on $treeObj, set value='$value' for key='$key', overwrite='$overwrite'"
        def status = treeObj?.myNodeStatus?.status
        if (status != null) {
            if (overwrite || !status.containsKey(key))
                status[key] = value
        }
    }

    
    private printDirStatus = { DirStatusTree treeObj, String key ->  
    // private printDirStatus = { DirStatusTree treeObj, String[] keys ->
        if (treeObj?.dirStatus) {
            String value = dirStatus.status?.get(key)
            if (value == null) {
                printf ('%1$-100s: %2$-10s = %3$s\n', [dirStatus.parentDir.absolutePath, key, value])
                return false
            }
            else if (value in ['true', 'false', '?']) {
                printf ('%1$-100s: %2$-10s = %3$s\n', [dirStatus.parentDir.absolutePath, key, value])
                return false
            }
            else {
                printf ('%1$-100s: %2$-10s = %3$s\n', [dirStatus.parentDir.absolutePath, key, value])
                return true
            }
        }      
    } 
    
    
    // TODO test printNew
    def printNew (String key) {
        
        def printThatKey = printDirStatus.rcurry(key)
        def printThatKeyRecursive = Functor.applyRecursiveWithFeedback.curry (printThatKey)
        printThatKeyRecursive(this)
    }   
    
    
    /**
     * Prints the status for the given keys
     * @param keys
     */
    def print (String[] keys) {
        
        this.traverseDirStatus (_print (keys))
    }
    def printNewMulti (String[] keys) {
        for (key in keys) {
            printNew(key)
        }
    }

    def traverseDirStatus (Closure action, DirStatusTree dst) {
      
        children.each {
            traverseDirStatus (action(it))
  //            if (it.dirStatus) action(it.dirStatus)
        }
        action(this.dirStatus)
    }
    
    /**
     * Writes all dirStatus objects to their files (recursively)
     * @return
     */
    def writeAllFiles () {
        
        this.traverseTree (_toFile())
    }
    
    
    /**
     * 
     * @param keys  array of keys to print
     * @return
     */
    private def Closure _print (String[] keys) { 
      {
        dirStatus ->
            for (key in keys) {
                String value = dirStatus.status?.get(key) 
                if (value == null) {
                    printf ('%1$-100s: %2$-10s = %3$s\n', [dirStatus.parentDir.absolutePath, key, value])                
                    // TODO do not continue (how?)
                }
                else if (value in ['true', 'false', '?']) {
                    printf ('%1$-100s: %2$-10s = %3$s\n', [dirStatus.parentDir.absolutePath, key, value])
                    // TODO do not continue (how?)
                }
                else {
                    // TODO continue recursively (this will be done because this function will be called through traverse)
                    printf ('%1$-100s: %2$-10s = %3$s\n', [dirStatus.parentDir.absolutePath, key, value])
                    
                }
            }
        }
    }

    
    /**
     * Works on a DirStatusTree object and writes the status of its objects to files  
     * (both DirStatus and MyNodeStatus objects)
     * @param key
     * @param value
     * @return
     */
    private def Closure _toFile () {
        
        { treeObj ->
//            println "working on tree object for ${treeObj.parentDir}"
            if (treeObj.myNodeStatus) {
                treeObj.myNodeStatus.toFile()
                println "(_toFile) written myNodeStatus to file '${MyNodeStatus.FILENAME}' for '${treeObj.parentDir}'"
            }
            if (treeObj.dirStatus) {
                treeObj.dirStatus.toFile()
                println "(_toFile) written dirStatus to file '${DirStatus.FILENAME}' for '${treeObj.parentDir}'"
            }            
        }
    }
    

    
    /**
     * String representation of this class
     */
    def String toString () {
        
        //"[parentDir=${parentDir.name},\n    myNodeStatus=$myNodeStatus,\n    dirStatus=$dirStatus]"
      if (children != null) 
          "[DirStatusTree: parentDir=${parentDir.name}, with ${children.size()} children]"
      else 
          "[DirStatusTree: parentDir=${parentDir.name}, with no children]"
    }
    
}
