/**
 * 
 */
package ch.ebexasoft.fototools

/**
 * @author edith
 *
 */
//class MarkerTool {
//
//}


// specify parameters
def cli = new CliBuilder(usage: '''groovy MarkerTool options rootDir taglist [value]
    tagList: comma separated list of tags; if multiple tags, enclose them in double quotation marks: "tag1, another, number3"

''')
//def cli = new CliBuilder(usage: 'groovy MarkerTool options rootDir taglist [value]')

cli.width = 75
// TODO where to include option rootDir?
cli.i (longOpt: 'init', args: 1, argName: 'taglist', 'initialize non-existing tags (set to "false"), does not change existing values')
cli.m (longOpt: 'mark', args: 1, argName: 'taglist', 'set given tags to "true" (creating non-existing ones)')
cli.u (longOpt: 'unmark', args: 1, argName: 'taglist', 'set given tags to "false" (creating non-existing ones)')
cli.c (longOpt: 'clear', args: 1, argName: 'taglist', 'removes given tags')

//cli.D (args:2, valueSeparator:'=', argName:'property=value', 'use value for given property')
cli.p (longOpt: 'print', args: 2, argName:'rootDir, taglist', 'print status of the given tags, starting at given root directory')
cli.r (longOpt: 'recursive', 'works recursive into all subdirectories (default). Opposite of -R')
cli.R (longOpt: 'nonrecursive', 'works only in the given directory, ignores subdirectories. Opposite of -r')

cli.s (longOpt: 'set', args: 2, argName:'taglist, value', '[developer option, use with care] overwrite given tags with given value (true or false). To use other values, use -free')
cli._ (longOpt: 'free', 'allows values other than "true" and "false" for -s')

cli.h (longOpt: 'help', 'display usage')


// parse and process parameters
def options = cli.parse(args)
assert options // would be null (false) on failure
if (options.h) cli.usage()
else if (options.arguments().size() < 2) {
    println "ERROR: missing arguments, required rootDir and taglist"
    cli.usage()
}
else {
    File rootDir = new File (options.arguments()[0])
    println "rootDir is ${rootDir.absolutePath}"
    String[] tags = options.arguments()[1]
    println "tags:"
    tags.each {
        println "- tag '$it'"
    }
    
//    if 
//    assert options.arguments() == ['*.groovy']
//    else if (options.c) {
//        println "will call clear for "
//    }
//    else println "Hello ${options.a ? options.a : 'World'}"
}



//assert options.a && options.l && options.t

