/**
 * 
 */
package ch.ebexasoft.fototools

import java.io.File
import java.io.PrintStream;
import java.util.List

import groovy.util.IndentPrinter;;

/**
 * Class that stores the MyNodeStatus object of this directory (if there
 * are images) and the DirStatus object (combination of status of all its
 * children).
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
//            println "initialized myNodeStatus for $parentDir: $myNodeStatus"

            // in recursion, first look deeper: collect children
            this.children = []
            parentDir.eachDir () {
                DirStatusTree childStatus = new DirStatusTree (it)
                childStatus.initChildren ()
                this.children.add(childStatus)
            }
//            println "added status objects for all children of ${parentDir.absolutePath}"
            
            if (!children.empty) {
                this.dirStatus = new DirStatus(parentDir)
                
                // from the children, collect all existing tag keys (we have to
                // do this because some keys may not be present in all files)
                Set keys = new HashSet()
                children.each { childNode ->
                    // collect keys from both dirStatus and myNodeStatus
                    if (childNode.dirStatus != null) {
//                        println "childNode ${childNode.parentDir} has a dirStatus, collecting its keys"
                        keys.addAll (childNode.dirStatus.status.keySet())
                    }
                    if (childNode.myNodeStatus != null) {
//                        println "childNode ${childNode.parentDir} has a myNodeStatus, collecting its keys"
                        keys.addAll (childNode.myNodeStatus.status.keySet())
                    }
                }
                println "all children of $parentDir combined have ${keys.size()} keys"
//                keys.each { key ->
//                    println "- key $key" 
//                }
                
                // loop through the keys and collect the values from the children's dirStatus and my own myNodeStatus
                keys.each { key ->
//                    println "collecting values for key '$key'"
                    children.each { childNode ->
                        if (childNode.dirStatus?.status != null) {
//                            println "childNode ${childNode.parentDir} has a dirStatus, will combine its values"
                            String value = childNode.dirStatus?.status[key]
                            dirStatus.combineValue (key, value)     // values in dirStatus are only Strings (true, false, etc.)
                        }
                        else {
//                            println "childNode ${childNode.parentDir} has no dirStatus, will combine values from myNodeStatus"
                            String[] values = childNode.myNodeStatus?.status[key]     // values in myNodeStatus are String[] (first value true, false, etc., second value timestamp)
                            dirStatus.combineValue (key, (values != null ? values[0] : null))
                        }
                    }
                    // combine my own status
                    if (myNodeStatus != null) {
                        String[] values = myNodeStatus.status[key]
//                        if (values == null) println "myNodeStatus for $parentDir has no value for key $key"
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
        
        if (overwrite)
            this.traverseDirStatus (_setValueOverwrite2DirStatus(key, value))
        else
            this.traverseDirStatus (_setValueNew2DirStatus(key, value))
    }

    
    /**
     * Prints the status for the given keys
     * @param keys
     */
    def print (String[] keys) {
        
        this.traverseDirStatus (_print (keys))
    }

    /**
     * Writes all dirStatus objects to their files (recursively)
     * @return
     */
    def writeAllFiles () {
        
        this.traverseTree (_toFile())
    }

    
    // TODO also create a traversal of the tree itself (DirStatusTree objects), not on the DirStatus objects
    // Use that for setting values (also set values to myNodeStatus, and also save those objects) 
    /**
     * Traverse the tree of dir status (depth-first) and do something 
     * with the dirStatus
     * @param action    a closure working on a DirStatus
     */
    def traverseDirStatus (Closure action) {
        
        children.each {
            traverseDirStatus (action(it))
//            if (it.dirStatus) action(it.dirStatus)
        }
        action(this.dirStatus)
    }

    /**
     * Traverse the tree of DirStatusTree objects (depth-first) and do something
     * with the dirStatusTree or its sub-objects (myNodeStatus, dirStatus)
     * @param action    a closure working on a DirStatusTree
     */
    def traverseTree (Closure action) {
        
        children.each {
            traverseTree (action(it))
        }
        action(this)
    }

    
    /**
     * Works on a DirStatus and sets a value (overwrite if existing)
     * @param key
     * @param value
     * @return
     */
    private def Closure _setValueOverwrite2DirStatus (String key, String value) {
        
        { dirStatus ->
            dirStatus.status?.put(key, value)
        }
    }
    
    
    /**
     * Works on a DirStatus and sets a value (only if it does not exist)
     * @param key
     * @param value
     * @return
     */
    private def Closure _setValueNew2DirStatus (String key, String value) {
        
        { dirStatus ->
            if (!dirStatus.status?.containsKey(key)) {
                println "list does not contain key $key"
                dirStatus.status?.put(key, value)
            }
        }
    }

    
    /**
     *
     * @param keys  array of keys to print
     * @return
     */
    private def Closure _print (String[] keys) {
        
        { dirStatus ->
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
                println "written status to file '${MyNodeStatus.FILENAME}' for '${treeObj.parentDir}'"
            }
            if (treeObj.dirStatus) {
                treeObj.dirStatus.toFile()
                println "written status to file '${DirStatus.FILENAME}' for '${treeObj.parentDir}'"
            }            
        }
    }
    

    
    /**
     * String representation of this class
     */
    def String toString () {
        
        "[parentDir=${parentDir.name},\n    myNodeStatus=$myNodeStatus,\n    dirStatus=$dirStatus]"
    }
    
}
