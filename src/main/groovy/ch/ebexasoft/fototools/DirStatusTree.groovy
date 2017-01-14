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

          
        if (overwrite) {
            //this.traverseTree (_setValueOverwrite2DirStatus(key, value))
            //this.traverseTree (_toString(key, value))
            //def action = this.&_toString (key, value)
            //traverse2 (this, action)
          
            println "\ncalling Functor.apply DirStatusTree (this) with listChildren"
            listChildren (this)

            println "\ncalling Functor.apply DirStatusTree (this.children) with listChildren"
            listChildren (this.children)

            println "\ncalling actionOnChildrenAndMyself for DirStatusTree (this) with listChildren2"
            listChildren2 (this)
            
            println "\ncalling actionOnChildrenAndMyself for DirStatusTree (this) with toStringTest"
            toStringTest (this)
                        
            println "\ncalling setValueTest for DirStatusTree (this, key, value) with setValueTest"
            setValueTest (this, key, value)
        }
        else
            this.traverseTree (_setValueNew2DirStatus(key, value))
      /*
        if (overwrite)
            this.traverseDirStatus (_setValueOverwrite2DirStatus(key, value))
        else
            this.traverseDirStatus (_setValueNew2DirStatus(key, value))
      */
    }
    
    private listChildList = { DirStatusTree dst ->
      
        println "listChildren called on $dst"
        // TODO find notation for != null check
        if (dst.children != null) {
            dst.children.each {
                println "- child $it"
            }
        }
    }
    private listChildren = Functor.apply.curry (listChildList)
    private listChildren2 = actionOnChildrenAndMyself.curry (listChildList)
    
    private dummy = { DirStatusTree dst -> 
        println "dummy called on $dst"
        println dst?.toString()
    }
    private toStringTest = actionOnChildrenAndMyself.curry (dummy)
    
    private setValueNew = { DirStatusTree treeObj, String key, String value ->
        
        println "visitChildrenMethod: start for ${treeObj.toString()}, setValueNew DUMMY key='$key', value='$value'"
        // TODO find notation for != null check
        if (treeObj.children != null) {
            treeObj.children.each {
                println "- child $it"
            }
        }
        println "visitChildren: end for ${treeObj.toString()}"
    }
    
    private setValueTest = { String key, String value -> 
        actionOnChildrenAndMyself.curry (setValueNew (key, value))
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
    def traverseDirStatus (Closure action, DirStatusTree dst) {
        
        children.each {
            traverseDirStatus (action(it))
//            if (it.dirStatus) action(it.dirStatus)
        }
        action(this.dirStatus)
    }

    /**
     * Traverse the given DirStatusTree and do something (depth-first, i.e. first to all 
     * children, then to the object itseslf)  
     * with the dirStatus
     * @param action    a closure working on a DirStatus
     */
    public static Closure actionOnChildrenAndMyself = { Closure dstreeAction, DirStatusTree treeObj ->
      
        println "actionOnChildrenAndMyself: >>> start for $treeObj"
      
        if (treeObj.children != null) {
            treeObj.children.each { it ->
                if (it != null) {
                    println "actionOnChildrenAndMyself: do action on $it"
                    dstreeAction (it)
                }
            }
        }
        println "actionOnChildrenAndMyself: do action on myself ($treeObj)"
        dstreeAction (treeObj)

        println "actionOnChildrenAndMyself: <<< end for $treeObj"
    }

    
    private def setValueInternal (String key, String value) {
        
        if (!dirStatus.status?.containsKey(key)) {
            println "setValueInternal: list does not contain key $key"
            dirStatus.status?.put(key, value)
        }

        children.each { it -> it.setValueInternal(key, value) }
    }

    
    /**
     * Traverse the tree of DirStatusTree objects (depth-first) and do something
     * with the dirStatusTree or its sub-objects (myNodeStatus, dirStatus)
     * @param action    a closure working on a DirStatusTree
     */
    def traverseTree = { Closure action -> 
      
        println "traverseTree start: I am  $this"
        
        /*
        parentDir.eachDir () {
          DirStatusTree childStatus = new DirStatusTree (it)
          childStatus.initChildren ()
          this.children.add(childStatus)
        }
        */
        
        if (children != null) {
            children.each () {
                println "traverseTree: recursive calling for $it"
                traverseTree (action(it))
            }
        }
        println "traverseTree end: I am  $this"
        action(this)
    }

    
    
    /**
     * TEMP method, called with
     *             println "\nlist of DirStatusTree (this.children) with toString"
            def resultList = transformList (this.children, toStringAction)
            println "printing resultlist:\n"
            resultList.each {
                println "$it"
            }
     */
    def transformList (List elements, Closure action) {
        def result = []
        println "transformList: element.class is ${it.class}"
        elements.each {
            result << action(it)
        }
        result
    }
    
    
    def visitList (Closure action, List elements) {
        println "visitList: start for ${elements}"
        println "visitList: this is $this"
        //println "visitList: action.this is ${action.this}"
        println "visitList: action.owner is ${action.owner}"
        println "visitList: action.delegate is ${action.delegate}"

        elements.each {
            action(it)
        }
        println "visitList: end for ${elements}"
    }
        
    /*
    def traverse = { DirStatusTree treeObj, treeAction ->
      
        println "traverse: called for $treeObj"
        
        def traverse_sub = { DirStatusTree obj, act -> 
            def recur = this
            obj.children.each {
                act (it)
                recur (it, act)
            }
        }
        println "traverse: before calling action for $treeObj"
        treeAction (treeObj)
        println "traverse: after calling action for $treeObj"
        traverse_sub (treeObj, treeAction)
    }
    */
    
    /**
     * Works on a DirStatusTree and only prints the toString of each node
     * @param key   unused
     * @param value unused
     */
    /*
    private def Closure _toString (String key, String value) {
        
        println "_toString: this is $this"
        println "_toString: owner is $owner"
        println "_toString: delegate is $delegate"

        { it ->
            println "_toString: ${it.toString()}"
        }
    }
    */
    def _toString = { String key, String value -> 
      
        println "_toString: this is $this"
        println "_toString: owner is $owner"
        println "_toString: delegate is $delegate"

        { it ->
            println "_toString: ${it.toString()}"
        }
    } 

    /**
     * Works on a DirStatusTree and sets a value (overwrite if existing)
     * @param key
     * @param value
     */
    private def Closure _setValueOverwrite2DirStatus (String key, String value) {
        
        { it ->
            println "_setValueOverwrite2DirStatus: it.dirStatus is: ${it.dirStatus}"
            println "_setValueOverwrite2DirStatus: it.dirStatus.status is: ${it.dirStatus.status}"
            it.dirStatus.status?.put(key, value)
            println "_setValueOverwrite2DirStatus: done setting value $value to key $key"
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
