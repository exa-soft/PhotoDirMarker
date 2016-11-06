/**
 * 
 */
package ch.ebexasoft.fototools

import static groovy.test.GroovyAssert.*

import groovy.json.JsonSlurper

import java.io.File;
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date;

import org.junit.Before
import org.junit.Test

/**
 * @author edith
 *
 */
class NodeStatusTest {

    File resourcesDir
    File testRoot
    Date testDate
    Date testDate2
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss-zzz");

	
	@Before
	void before() {
        resourcesDir = new File ('src/test/resources')
        testRoot = new File("/home/edith/Bilder/fürUli")
        testDate = df.parse("2016-10-29T18:59:45-MESZ")
        testDate2 = df.parse("2016-12-07T05:35:57-MESZ")
	}
	
	
	/**
	 * Test method for {@link ch.ebexasoft.fototools.MyNodeStatus#printValue(java.lang.String)}.
	 */
	@Test
	public void testPrintJson() {
		
		MyNodeStatus treeStatus = new MyNodeStatus(testRoot)
		
		String json = treeStatus.toJson()
		assertNotNull (json)
		println "json String:\n$json"
		assertTrue (System.out instanceof PrintStream)
		assertEquals ( 
'''{
    "dir": "/home/edith/Bilder/f\u00fcrUli",
    "status": {
        
    }
}''', json)
        // TODO why is not stored: "dir": "/home/edith/Bilder/fürUli",
        
        treeStatus.status['name'] = ["true", testDate]
        treeStatus.status['copyright'] = ["false", testDate2]
        
        json = treeStatus.toJson()
        assertNotNull (json)
        println "json String:\n$json"
        
        assertEquals (
'''{
    "dir": "/home/edith/Bilder/fürUli",
    "status": {
        "name": ["true", "2016-10-29T18:59:45-MESZ"],
        "copyright": ["false", "2016-12-07T05:35:57-MESZ"]
    }
}''', json)
        
	}

    /**
     * Test method to read a json String (with empty status map)  
     */
	@Test
	public void testReadJson_emptyStatus () {
				
//		The JSON standard supports the following primitive data types: string, number, object, true, false and null. JsonSlurper converts these JSON types into corresponding Groovy types.
        def jsonSlurper = new JsonSlurper()
        
        // object with empty status
        def object = jsonSlurper.parseText '''{
       		"dir": "/home/edith/irgendwo/Fotos",
			"status": {
            
			}
		}
		}'''
		assert object instanceof Map
		assert object.dir instanceof String
        assertEquals "/home/edith/irgendwo/Fotos", object.dir
		assert object.status instanceof Map
        assertEquals null, object.status.size       							
	}

    
    /**
     * Test method to read a json String (with 2 elements in the empty status map)
     */
    @Test
	public void testReadJson_nonemptyStatus () {
				
        def jsonSlurper = new JsonSlurper()
        
        // object with non-empty status
		def object = jsonSlurper.parseText '''{
			"dir": "/home/edith/irgendwo/Fotos",
			"status": {
				"name": ["true", "2016-10-29T18:59:45-MESZ"],
				"copyright": ["false", "2016-12-07T05:35:57-MESZ"]
			}
		}'''
        assert object instanceof Map
        assert object.dir instanceof String
        assertEquals "/home/edith/irgendwo/Fotos", object.dir
        assert object.status instanceof Map
        Map sts = object.status
        assertEquals 2, sts.size()
        
        assert sts.keySet().contains('name')
        def values = sts['name']
        assertEquals 2, values.size()
        assertEquals 'true', values[0]
        assertEquals '2016-10-29T18:59:45-MESZ', values[1]
        
        assert sts.keySet().contains('copyright')
        values = sts['copyright']
        assertEquals 2, values.size()
        assertEquals 'false', values[0]
        assertEquals '2016-12-07T05:35:57-MESZ', values[1]
	}
	
    
    /**
     * Test method to create a MyNodeStatus object from a json File 
     * (with 2 elements in the empty status map)
     */
    @Test
    public void testReadJson_file2Object () {

        assertEquals '/data/DevelopmentEB/Groovy/PhotoDirMarker/src/test/resources',
            resourcesDir.absolutePath
        File testDir = new File (resourcesDir, 'jsonSource1').absoluteFile
        assertEquals '/data/DevelopmentEB/Groovy/PhotoDirMarker/src/test/resources/jsonSource1',
            testDir.absolutePath
        
        MyNodeStatus st = MyNodeStatus.fromDir(testDir)
        
        assert st instanceof MyNodeStatus        
        assert st.parentDir instanceof File
        assertEquals testDir.absoluteFile, st.parentDir
        assert st.status instanceof Map
        Map sts = st.status
        assertEquals 2, sts.size()
        
        assert sts.keySet().contains('name')
        def values = sts['name']
        assertEquals 2, values.size()
        assertEquals 'true', values[0]
        assertEquals '2016-10-29T18:59:45-MESZ', values[1]
        
        assert sts.keySet().contains('copyright')
        values = sts['copyright']
        assertEquals 2, values.size()
        assertEquals 'false', values[0]
        assertEquals '2016-12-07T05:35:57-MESZ', values[1]
    }

    
    /**
     * Test method to create a json file MyNodeStatus object from a json String
     * (with 2 elements in the empty status map)
     */
    @Test
    public void testWriteJson_obj2File () {

        assertEquals '/data/DevelopmentEB/Groovy/PhotoDirMarker/src/test/resources',
            resourcesDir.absolutePath
        File testDir = new File (resourcesDir, 'jsonTarget1').absoluteFile
        assertEquals '/data/DevelopmentEB/Groovy/PhotoDirMarker/src/test/resources/jsonTarget1',
            testDir.absolutePath

        if (!testDir.exists()) assert testDir.mkdir()
        File testTarget = new File (testDir, MyNodeStatus.FILENAME)
        if (testTarget.exists()) assert testTarget.delete()
        assert !testTarget.exists()
        
        MyNodeStatus st = new MyNodeStatus (testDir)
        st.status['name'] = ['true', '2016-10-29T18:59:45-MESZ']
        st.status['copyright'] = ['false', '2016-12-07T05:35:57-MESZ']
        
        st.toFile()
        assert testTarget.exists()
                
        String fileContents = testTarget.getText('UTF-8')
        assertEquals '''{
    "dir": "/data/DevelopmentEB/Groovy/PhotoDirMarker/src/test/resources/jsonTarget1",
    "status": {
        "name": [
            "true",
            "2016-10-29T18:59:45-MESZ"
        ],
        "copyright": [
            "false",
            "2016-12-07T05:35:57-MESZ"
        ]
    }
}''', fileContents

    }

    /**
     * Test method for {@link ch.ebexasoft.fototools.DirStatus#combineValue (java.lang.String, java.lang.String)}.
     */
    @Test
    public void testCombineValue () {
        
        DirStatus dst = new DirStatus (testRoot)
        assertNotNull dst
        
        dst.combineValue 'key1', 'true'
        dst.combineValue 'key2', 'true'
        dst.combineValue 'key3', 'false'
        dst.combineValue 'key4', 'false'
        assertEquals 'true', dst.status['key1']
        assertEquals 'true', dst.status['key2']
        assertEquals 'false', dst.status['key3']
        assertEquals 'false', dst.status['key4']
        
        dst.combineValue 'key1', 'true'
        dst.combineValue 'key2', 'false'
        dst.combineValue 'key3', 'true'
        dst.combineValue 'key4', 'false'
        assertEquals 'true', dst.status['key1']
        assertEquals DirStatus.MIXEDVALUE, dst.status['key2']
        assertEquals DirStatus.MIXEDVALUE, dst.status['key3']
        assertEquals 'false', dst.status['key4']
     
           
    }
        

    /**
     * Test method to work on status with closure
     * (needs success of part of testReadJson_file2Object)
     */
    @Test
    public void testClosureOnStatus () {

        assertEquals '/data/DevelopmentEB/Groovy/PhotoDirMarker/src/test/resources',
            resourcesDir.absolutePath
        File testDir = new File (resourcesDir, 'jsonSource1').absoluteFile
        assertEquals '/data/DevelopmentEB/Groovy/PhotoDirMarker/src/test/resources/jsonSource1',
            testDir.absolutePath
        
        MyNodeStatus st = MyNodeStatus.fromDir(testDir)
        
        assert st instanceof MyNodeStatus        
        assert st.status instanceof Map
        Map sts = st.status
        assertEquals 2, sts.size()
        
        assert sts.keySet().contains('name')
        def values = sts['name']
        assertEquals 2, values.size()
        assertEquals 'true', values[0]
        
        assert sts.keySet().contains('copyright')
        values = sts['copyright']
        assertEquals 2, values.size()
        assertEquals 'false', values[0]

        
        
    }
    
    
    /**
     * Test method to work on status with closure
     * (needs sucess of part of testReadJson_file2Object)
     */
    @Test
    public void testVariousWithClosure () {

        assert [1, 2, 3].find { it > 1 } == 2           // find 1st element matching criteria
        assert [1, 2, 3].findAll { it > 1 } == [2, 3]   // find all elements matching critieria
        assert ['a', 'b', 'c', 'd', 'e'].findIndexOf {      // find index of 1st element matching criteria
            it in ['c', 'e', 'g']
        } == 2
    
        String[] tags = ['name', 'copyright', 'keywords']
        Map someMap = [name: 'true', copyright: 'false', keywords: '?']
                
        assert tags.find { it == 'copyright' } == 'copyright'
        
        def found = someMap.find { it.key == 'copyright' }
        assert found instanceof Map.Entry
        assert found.value == 'false' 
    
        Map map1 = [y1y2: 'tru', y1n2: 'tru', n1y2: 'fals', n1n2: 'fals']
        Map map2 = [y1y2: 'tru', y1n2: 'fals', n1y2: 'tru', n1n2: 'fals']
        
        List maps = [map1, map2]
        assert maps[0].y1n2 == 'tru'
        assert maps[1].y1n2 == 'fals'
        
        // test to get all values for one key and compare them
        List only1Tag = maps.collect { it.get('y1n2') }
        boolean b2 = only1Tag.every { it == only1Tag[0] }
        assertEquals 2, only1Tag.size()
        assert ['tru', 'fals'] == only1Tag
        assert !b2
        only1Tag = maps.collect { it.get('n1n2') }
        b2 = only1Tag.every { it == only1Tag[0] }
        assertEquals 2, only1Tag.size()
        assert ['fals', 'fals'] == only1Tag
        assert b2
        
        // test to change all values for one key 
        
        maps.each { map ->
            map.put 'y1n2', 'newValue'
        }
        assert ['newValue', 'newValue'] == maps.collect { it.get('y1n2') }
        
//        def getFromMap = { Collection keys, Map map, String s -> 
//            map.findAll keys
//        }
//        def getFromStatus (Collection keys) = someMap.find {
//            
//        }
        
    }
    
    
}
