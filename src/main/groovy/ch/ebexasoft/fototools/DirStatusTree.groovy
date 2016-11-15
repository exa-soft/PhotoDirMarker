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
    List children = null
    
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
                    println "collecting values for key '$key'"
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

    
    /**
     * 
     * @param key
     * @param value
     * @param overwrite
     * @return
     */
    def setValue (String key, String value, boolean overwrite) {
        
        if (overwrite)
            this.traverse (_setValueOverwrite(key, value))
        else
            this.traverse (_setValueNew(key, value))
    }

    
    // TODO try to use closure as parameter, so we could combine printing and setting values into one structure/function 
    /**
     * Traverse the tree of dir status (depth-first) and do something 
     * with the dirStatus
     * @param action    a closure working on a DirStatus
     */
    def traverse (Closure action) {
        
        children.each {
            if (it.dirStatus) action(it.dirStatus)
        }
        action(this.dirStatus)
    }
    
    
    /**
     * 
     * @param key
     * @param value
     * @return
     */
    private def Closure _setValueOverwrite (String key, String value) {
        
        { dirStatus ->
            dirStatus.status?.put(key, value)
        }
    }
    
    /**
     *
     * @param key
     * @param value
     * @return
     */
    private def Closure _setValueNew (String key, String value) {
        
        { dirStatus ->
            if (!dirStatus.status?.containsKey(key)) {
                println "list does not contain key $key"
                dirStatus.status?.put(key, value)
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
