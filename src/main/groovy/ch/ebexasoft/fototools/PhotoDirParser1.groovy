package ch.ebexasoft.fototools

import groovy.io.FileType

/**
 * @author edith
 *
 */

def list = []

def dir = new File("/home/edith/Bilder/fürUli")

// dir.eachFileRecurse (FileType.DIRECTORIES) { subdir ->
dir.eachDirRecurse () { subdir ->
	if(subdir.name.endsWith('.groovy')) {
		println subdir
	}
	
  list << subdir
}

// now list contains all files (java.io.File) of the given directory and its subdirectories:
//list.each {
//  println it.path
//}
println "dirRecurse list has ${list.size} elements"
assert list.size == 7

/**
 * Findet rekursiv alle Verzeichnisse, die Bilder haben (mit Endung 
 * aus  jpg|jpeg|png|gif .
 * Etwas mit den Verzeichnissen zu tun, ist direkt möglich, (ohne die Liste im Memory zu bauen):
 * <code>fotoDirs () { println it }</code>
 * Oder wenn man die Liste doch braucht, kann sie einfach erstellt werden, in irgendwelche Collection:
 * <code>
 * def fotoDirsList = []
 * fotoDirs() { fotoDirsList << it }
 * </code>
 */
def fotoDirs (String root, Closure closure) { 
	new File(root).eachDirRecurse() {
		if (NodeUtils.containsPics (it)) {
			closure.call(it)
		}
	}
}
fotoDirs("/home/edith/Bilder/fürUli") {
	println "from fotoDirs: $it"
}
def fotoDirsList = []
fotoDirs("/home/edith/Bilder/fürUli") { fotoDirsList << it }
list.each {
	println "from fotoDirs: ${it.path}"
}
println "fotoDirsList has ${fotoDirsList.size} elements"
assert fotoDirsList.size == 7

def fotoDirsList1 = []


/*
String fileContents = new File('/path/to/file').text
If you need to specify the character encoding, use the following instead:
String fileContents = new File('/path/to/file').getText(NodeStatus.ENCODING)

new File( folder, 'file.txt' ).withWriterAppend { w ->
  w << "Some text\n"
}

 */



