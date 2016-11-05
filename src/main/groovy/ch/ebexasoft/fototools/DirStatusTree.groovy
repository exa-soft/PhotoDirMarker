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

            // in recursion, first look deeper: collect children
            this.children = []
            parentDir.eachDir () {
                DirStatusTree childStatus = new DirStatusTree (it)
                childStatus.initChildren ()
                this.children.add(childStatus)
            }
//            println "have children for ${parentDir.absolutePath}"
            
            if (!children.empty) {
                dirStatus = new DirStatus(parentDir)
                
                // from the children, collect all existing tag keys (we have to
                // do this because some keys may not be present in all files)
                Set keys = new HashSet()
                children.each { childNode ->
                    if (childNode.myNodeStatus != null) {
                        assert childNode.myNodeStatus.status != null
                        assert childNode.myNodeStatus.status.keySet() != null
                        keys.addAll (childNode.myNodeStatus.status.keySet())
                    }
                    
                    
                }
//                println "$parentDir contains ${keys.size()} keys:"
//                keys.each { key ->
//                    println "key $key" 
//                }
                                    
                // loop through the keys and collect the values from the children
                keys.each { key ->
//                    println "collecting values for key '$key'"
                    children.each { childNode ->
                        if (childNode?.myNodeStatus?.status != null) {
                            String[] values = childNode?.myNodeStatus?.status[key]
                            if (values != null) {
//                                println "values for key '$key' are '$values'"
                                dirStatus.combineValue (key, values[0])
                            }
                            else {
                                println "no value found for key '$key'"
                                dirStatus.combineValue (key, null)
                            }
                        }
                    }
                }
                dirStatus.toFile()
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
     * Traverse the tree of dir status and do something with the tags
     * @param elements
     * @param action
     * @return
     */
    def traverse (List elements, Closure action) {
        def result = []
        elements.each {
            result << action(it)
        }
        result
    }
        
    def String toString () {
        
        "[parentDir=${parentDir.name},\n    myNodeStatus=$myNodeStatus,\n    dirStatus=$dirStatus]"
    }
    
}
