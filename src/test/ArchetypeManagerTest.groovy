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
      
      
      def observation_adl_files = new File(path + PS + "entry" + PS + "observation").listFiles().count { it.name ==~ /.*\.adl/ }
      
      assert loader.getArchetypes("composition", ".*").size() == 3 : "No se cargaron todos los arquetipos"
      assert loader.getArchetypes("observation", ".*").size() == observation_adl_files : "No se cargaron todos los arquetipos"
   }
   
   @Test
   public void testGetText()
   {
      def loader = ArchetypeManager.getInstance(path)
      loader.loadAll()
      
      // Todos estos casos de test son correctos
      // los caracteres acentuados se escriben en unicode para evitar problemas de Java en la comparacion
      // https://dtrinf.wordpress.com/2012/02/29/como-escribir-enes-n-y-acentos-en-java/
      def testCases = [
        ['openEHR-EHR-OBSERVATION.blood_pressure.v1', 'en', 'at0000', 'Blood Pressure'],
        ['openEHR-EHR-OBSERVATION.blood_pressure.v1', 'en', 'at0004', 'Systolic'],
        ['openEHR-EHR-OBSERVATION.blood_pressure.v1', 'en', 'at0005', 'Diastolic'],
        ['openEHR-EHR-OBSERVATION.blood_pressure.v1', 'en', 'at0013', 'Cuff size'],
        ['openEHR-EHR-OBSERVATION.blood_pressure.v1', 'es-ar', 'at0000', 'Presi\u00f3n Arterial'],
        ['openEHR-EHR-OBSERVATION.blood_pressure.v1', 'es-ar', 'at0004', 'Sist\u00f3lica'],
        ['openEHR-EHR-OBSERVATION.blood_pressure.v1', 'es-ar', 'at0005', 'Di\u00e1stole'],
        ['openEHR-EHR-OBSERVATION.blood_pressure.v1', 'es-ar', 'at0013', 'Tama\u00f1o del manguito']
      ]
      
      def text
      testCases.each { testCase ->

         text = loader.getText(testCase[0], testCase[1], testCase[2])
         
         assert text != null
         
         assert text == testCase[3]
         
         println text
      }
   }


   /**
    * Test method for {@link com.cabolabs.openehr.archetypes.ArchetypeManager#getArchetype(java.lang.String)}.
    */
   @Test
   public void testGetArchetypeString()
   {
      def loader = ArchetypeManager.getInstance(path)
      loader.loadAll()
      
      // Obtiene un arquetipo por id
      def arch = loader.getArchetype("openEHR-EHR-OBSERVATION.blood_pressure.v1")
      
      
      assert arch.archetypeId.value == "openEHR-EHR-OBSERVATION.blood_pressure.v1"
      assert arch.definition.getClass().getSimpleName() == "CComplexObject" // Clase del modelo de arquetipos
      
      
      
      // Muestra todas las paths del arquetipo y a que tipo del modelo de informacion corresponden
      println ""
      arch.physicalPaths().sort().each { path ->
         println path +" ("+ arch.node(path).rmTypeName +")"
      }
      println ""
      
      
      // operaciones sobre nodos del arquetipo
      // restriccion (CDvQuantity) sobre presion sistolica (DvQuantity)
      assert arch.node('/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value').getClass().getSimpleName() == "CDvQuantity"
      
      assert arch.node('/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value').path() == '/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value'
      
      def cdvquantity = arch.node('/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value')
      def constraintList = cdvquantity.getList() // lista de restricciones
      
      assert constraintList.size() == 1 // hay una sola restriccion
      
      def constraint = constraintList[0]

      // datos de la restriccion: unidades y rango valido para la magnitud
      assert constraint.units == "mm[Hg]"
      assert constraint.magnitude.getClass().getSimpleName() == "Interval"
      assert constraint.magnitude.lower == 0.0d
      assert constraint.magnitude.upper == 1000.0d
      
      
      
      // validacion de un dato clinico contra una restriccion del arquetipo
      def datoUnits = 'mm[Hg]'
      def datoMagnitude = 134.0d
      
      // se podria simplificar porque se sabe que el arquetipo tiene solo una restriccion en la lista
      def valid = constraintList.find { item ->
         // para la restriccion que tiene las mismas unidades que los datos
         // verifica si la magnitud de los datos esta dentro del rango definido en la restriccion
         if (item.units == datoUnits && item.magnitude.has(datoMagnitude)) return true
      } != null
      
      assert valid
      
      
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
      def loader = ArchetypeManager.getInstance(path)
      loader.loadAll()
      
      // Obtiene un arquetipo por id
      def archs = loader.getArchetypes("OBSERVATION", ".*")
      
      def observation_adl_files = new File(path + PS + "entry" + PS + "observation").listFiles().count { it.name ==~ /.*\.adl/ }
      
      assert archs.size() == observation_adl_files
      
      // arquetipos de observacion que hay en /resources
      def existingArchetypes = [
      "openEHR-EHR-OBSERVATION.blood_pressure.v1",
      "openEHR-EHR-OBSERVATION.body_temperature.v1",
      "openEHR-EHR-OBSERVATION.body_weight.v1",
      "openEHR-EHR-OBSERVATION.height.v1",
      "openEHR-EHR-OBSERVATION.pulse.v1",
      "openEHR-EHR-OBSERVATION.respiration.v1",
      "openEHR-EHR-OBSERVATION.test_all_datatypes.v1",
      "openEHR-EHR-OBSERVATION.test_servicios_terminologicos.v1"
      ]
      
      archs.each { arch ->
      
         assert existingArchetypes.contains ( arch.archetypeId.value )
      }
   }

   /**
    * Test method for {@link com.cabolabs.openehr.archetypes.ArchetypeManager#getArchetypeNode(java.lang.String)}.
    */
   @Test
   public void testGetArchetypeNode()
   {
      def loader = ArchetypeManager.getInstance(path)
      loader.loadAll()
      
      // en lugar de pedirle un nodo al arquetipo se le puede pedir al ArchetypeManager
      def cdvquantity = loader.getArchetypeNode('openEHR-EHR-OBSERVATION.blood_pressure.v1/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value')
   
      assert cdvquantity.getClass().getSimpleName() == "CDvQuantity"
   
      def constraintList = cdvquantity.getList() // lista de restricciones
      
      assert constraintList.size() == 1 // hay una sola restriccion
      
      def constraint = constraintList[0]

      // datos de la restriccion: unidades y rango valido para la magnitud
      assert constraint.units == "mm[Hg]"
      assert constraint.magnitude.getClass().getSimpleName() == "Interval"
      assert constraint.magnitude.lower == 0.0d
      assert constraint.magnitude.upper == 1000.0d
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
