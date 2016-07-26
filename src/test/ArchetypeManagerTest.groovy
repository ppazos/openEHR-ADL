/**
 * junit4 test http://docs.groovy-lang.org/docs/latest/html/documentation/core-testing-guide.html#_junit_4
 */
package test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.cabolabs.openehr.adl.ArchetypeManager

/**
 * @author pab
 *
 */
class ArchetypeManagerTest {

   private static String PS = File.separator
   private static String path = "."+ PS +"resources"+ PS +"archetypes"
   
   /**
    * @throws java.lang.Exception
    */
   @BeforeClass
   public static void setUpBeforeClass() throws Exception
   {
   }

   /**
    * @throws java.lang.Exception
    */
   @AfterClass
   public static void tearDownAfterClass() throws Exception
   {
   }

   /**
    * @throws java.lang.Exception
    */
   @Before
   public void setUp() throws Exception
   {
      //def f = new File(".")
      //println "Current folder: "+ f.getCanonicalPath()
   }

   /**
    * @throws java.lang.Exception
    */
   @After
   public void tearDown() throws Exception
   {
   }

   /**
    * Test method for {@link com.cabolabs.openehr.archetypes.ArchetypeManager#ArchetypeManager(java.lang.String)}.
    */
   @Test
   public void testArchetypeManager()
   {
      //def loader = ArchetypeManager.getInstance(path)
      //def archetype = loader.getArchetype("openEHR-EHR-INSTRUCTION.test_ordenes.v1")
   }

   /**
    * Test method for {@link com.cabolabs.openehr.archetypes.ArchetypeManager#getInstance(java.lang.String)}.
    */
   @Test
   public void testGetInstance()
   {
      def loader = ArchetypeManager.getInstance(path)
      assert loader != null : "getInstance is returning null"
   }

   /**
    * Test method for {@link com.cabolabs.openehr.archetypes.ArchetypeManager#loadAll()}.
    */
   @Test
   public void testLoadAll()
   {
      def loader = ArchetypeManager.getInstance(path)
      loader.loadAll()
      assert loader.getArchetypes("composition", ".*").size() == 3 : "No se cargaron todos los arquetipos"
      assert loader.getArchetypes("observation", ".*").size() == 7 : "No se cargaron todos los arquetipos"
   }

   /**
    * Test method for {@link com.cabolabs.openehr.archetypes.ArchetypeManager#getArchetype(java.lang.String)}.
    */
   @Test
   public void testGetArchetypeString()
   {
      def loader = ArchetypeManager.getInstance(path)
      loader.loadAll()
      def arch = loader.getArchetype("openEHR-EHR-OBSERVATION.blood_pressure.v1")
      assert arch.archetypeId.value == "openEHR-EHR-OBSERVATION.blood_pressure.v1"
      println arch.definition.getClass().getSimpleName()
      /*
      arch.physicalPaths().each {
         println it
      }
      */
      
      
      // sistolica
      println arch.node('/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value').getClass().getSimpleName()
      
      assert arch.node('/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value').path() == '/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value'
      
      def cdvquantity = arch.node('/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value')
      def listItems = cdvquantity.getList() // lista de restricciones
      listItems.each { item ->
         println item.units // String
         println item.magnitude // Interval<Double>
         println item.magnitude.lower
         println item.magnitude.upper
      }
      
      def datoUnits = 'mm[Hg]'
      def datoMagnitude = 134.0d
      
      def valid = listItems.find { item ->
         if (item.units == datoUnits &&item.magnitude.has(datoMagnitude)) return true
      } != null
      
      if (valid) println "valid"
      else println "invalid"
      
      
      // diastolica
      //println arch.node('/data[at0001]/events[at0006]/data[at0003]/items[at0005]/value')
   }

   /**
    * Test method for {@link com.cabolabs.openehr.archetypes.ArchetypeManager#getArchetype(java.lang.String, java.lang.String)}.
    */
   @Test
   public void testGetArchetypeStringString()
   {
      //fail("Not yet implemented");
   }

   /**
    * Test method for {@link com.cabolabs.openehr.archetypes.ArchetypeManager#getArchetypes(java.lang.String, java.lang.String)}.
    */
   @Test
   public void testGetArchetypes()
   {
      //fail("Not yet implemented");
   }

   /**
    * Test method for {@link com.cabolabs.openehr.archetypes.ArchetypeManager#getArchetypeNode(java.lang.String)}.
    */
   @Test
   public void testGetArchetypeNode()
   {
      //fail("Not yet implemented");
   }

   /**
    * Test method for {@link com.cabolabs.openehr.archetypes.ArchetypeManager#toString()}.
    */
   @Test
   public void testToString()
   {
      //fail("Not yet implemented");
   }

   /**
    * Test method for {@link com.cabolabs.openehr.archetypes.ArchetypeManager#getLoadedArchetypes()}.
    */
   @Test
   public void testGetLoadedArchetypes()
   {
      //fail("Not yet implemented");
   }

   /**
    * Test method for {@link com.cabolabs.openehr.archetypes.ArchetypeManager#getLastUse()}.
    */
   @Test
   public void testGetLastUse()
   {
      //fail("Not yet implemented");
   }

   /**
    * Test method for {@link com.cabolabs.openehr.archetypes.ArchetypeManager#unloadAll()}.
    */
   @Test
   public void testUnloadAll()
   {
      //fail("Not yet implemented");
   }

   /**
    * Test method for {@link com.cabolabs.openehr.archetypes.ArchetypeManager#findArchetypeIds(java.lang.String)}.
    */
   @Test
   public void testFindArchetypeIds()
   {
      //fail("Not yet implemented");
   }
}
