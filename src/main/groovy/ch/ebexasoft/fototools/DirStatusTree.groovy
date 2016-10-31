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
            children = []
            parentDir.eachDirRecurse() {
                DirStatusTree childStatus = new DirStatusTree (it)
                childStatus.initChildren ()
                children.add(childStatus)
            }
            println "have children for ${parentDir.absolutePath}"
            
            if (!children.empty) {
                dirStatus = new DirStatus(parentDir)
                
                // from the children, collect all existing tag keys (we have to
                // do this because some keys may not be present in all files)
                Set keys = new HashSet()
                children.each { childNode ->
                    keys.addAll (childNode.myNodeStatus.status.keySet())
                }
                
                // loop through the keys and collect the values from the children
                keys.each { key ->
                    println "collecting values for key '$key'"
                    children.each { childNode ->
                        dirStatus.combineValue (key, childNode.myNodeStatus.status[value])
                    }
                }
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
    
    def String toString () {
        "[parentDir=$parentDir, myNodeStatus=$myNodeStatus, dirStatus=$dirStatus]"
    }

}
