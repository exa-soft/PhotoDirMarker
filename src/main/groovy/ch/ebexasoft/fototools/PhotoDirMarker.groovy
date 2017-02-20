package ch.ebexasoft.fototools

/**
 * Script to be called from command line. Option -help or no option prints usage.
 *
 * @author edith
 */

//class PhotoDirMarker {
//
//}


// specify parameters for command-line call
def cli = new CliBuilder(usage: '''groovy PhotoDirMarker [-d directory] options tags [-v value]
    tags: tag or tags; if multiple tags, separate them with commas (no spaces!) and enclose in double quotation marks: "tag1,another,number3"

''')

cli.width = 79

cli.d (longOpt: 'dir', args: 1, argName: 'directory', 'path to root directory for operation (without -d option, current directory is used)')

cli.f (longOpt: 'fresh', 'does not change any tags, but re-writes the collectedFileStatus.txt files') 
cli.i (longOpt: 'init', args: 1, argName: 'tags', 'initialize non-existing tags (set to "false"), does not change existing values')
cli.m (longOpt: 'mark', args: 1, argName: 'tags', 'set given tags to "true" (creating non-existing ones)')
cli.u (longOpt: 'unmark', args: 1, argName: 'tags', 'set given tags to "false" (creating non-existing ones)')
cli.c (longOpt: 'clear', args: 1, argName: 'tags', 'removes given tags')
cli.p (longOpt: 'print', args: 1, argName:'tags', 'print status of given tags, starting at given root directory')
cli.s (longOpt: 'set', valueSeparator:'=', args: 2, argName:'tags=value', 'overwrite given tags with given value (true or false). To use other values, use -free [developer option, use with care] ')
cli._ (longOpt: 'free', 'for -s, allow values other than "true" and "false" [developer option, use with care]')

cli.r (longOpt: 'recursive', 'works recursive into all subdirectories (default). Opposite of -R')
cli.R (longOpt: 'nonrecursive', 'works only in the given directory, ignores subdirectories. Opposite of -r')

cli.h (longOpt: 'help', 'display usage')

// from example (with 2 arguments): 
//cli.D (args:2, valueSeparator:'=', argName:'property=value', 'use value for given property')

// parse and process parameters
def options = cli.parse(args)
assert options // would be null (false) on failure

if (options.h) {
  cli.usage()
  System.exit(0)
}

// define workdir
println "have '-d' option: ${options.d}"
def File workdir = new File (options.d ?: '.')  // use current directory if no d option defined
println "will work on dir ${workdir}"

// define recursive
def boolean recursive = true
if (options.R) {
    recursive = false
    println "have '-R' option, will work only in directory (not recursively) '${workdir.absolutePath}'"
}
else if (options.r)  println "have '-r' option, will work recursively, starting from '${workdir.absolutePath}'"
else println "will work recursively, starting from '$workdir'"

// define taglist and value
def String tags
def String value
def overwrite = true
if (options.i) { tags = options.i; value = 'false'; overwrite = false } 
if (options.m) { tags = options.m; value = 'true' }
if (options.u) { tags = options.u; value = 'false' }
if (options.c) tags = options.c
if (options.p) tags = options.p
if (options.s) { 
    tags = options.ss[0]
    value = options.ss[1]
    if (value.toLowerCase() in ['true', 'false']) {
        value = value.toLowerCase()
    }
    else {
        // only allowed with -free optioni
        if (!options.free) {
            println "ERROR: value '$value' is not allowed (must be 'true' or 'false', or use option '-free')"
            println "Call PhotoDirMarker -h to display usage."
            System.exit(-13) 
        }
    }
    println "value to set is '$value'"
}
if (!tags) {
    println "ERROR: No operation specified. You must specify one option of i, m, u, c, p, s (if specified multiple, the first one wins, in the given order)."
    println "Call PhotoDirMarker -h to display usage."
    System.exit(-15)

}

def String[] taglist = tags.split(',')
println "will run for the following tags: "
taglist.each {
    println "- tag '$it'"
}

// collect status into object
def DirStatusTree dirStatusTree = new DirStatusTree(workdir)
dirStatusTree.initChildren()
//println "collected status of ${workdir.absolutePath}"


// call the methods for the options

if (options.p) {
    println "will print for the following tags: "
    taglist.each {
        println "- tag '$it'"
    }
    print (dirStatusTree, taglist)
}
else {
    switch (options) {
        case (options.c):
            clearValue (dirStatusTree, taglist)
            // fallthrough is intentional (clearValue also needs recollect & write)
        case (options.f):   
            dirStatusTree.recollect()
            dirStatusTree.writeChangesToFiles()
            break
        case (options.i):
        case (options.m):
        case (options.u):
        case (options.s):
        default:
            def count = setValue (dirStatusTree, taglist, value)
            println "\nWorked on $count tags: set '$value' to the following tags: '$tags'."
            dirStatusTree.recollect()
            dirStatusTree.writeChangesToFiles()
            break
    }
}

System.exit(0)

//-------------------------------------------------------------------------

/**
 * Prints the status of the tags in tagList
 * LATER print only as deep as necessary (such as not to print "mixed") 
 * @param statusTree
 * @param tags
 */
def print (DirStatusTree statusTree, String[] tags) {
    
//    for (tagName in tags) {
//        statusTree.print (tags)
//    }
//    for (tagName in tags) {
//       statusTree.print (tagName)
//    }
    statusTree.printNewMulti(tags)
}

/**
 * Set a value recursively. Uses values set in script: 
 * dirStatusTree object to work on
 * @param tags      taglist array of tags to work with
 * @param overwrite true to overwrite existing values, false to keep them
 * @return      count how many values have been set
 */
def int setValue (DirStatusTree statusTree, String[] tags, String value) {
    
    int count = 0
    for (tagName in tags) {
        println "\nsetting tag '$tagName' to value '$value'"
        statusTree.setValue (tagName, value, true)   // true to overwrite
        count++
    }
    return count
}

/**
 * Clear a value recursively. Uses values set in script:
 * @param statusTree object to work on
 * @param tags      taglist array of tags to work with
 * @return      count how many values have been cleared
 */
def int clearValue (DirStatusTree statusTree, String[] tags) {
    
    int count = 0
    for (tagName in tags) {
        println "\nsetting tag '$tagName' to value '$value'"
        statusTree.setValue (tagName, value, true)   // true to overwrite
        count++
    }
    return count
}
