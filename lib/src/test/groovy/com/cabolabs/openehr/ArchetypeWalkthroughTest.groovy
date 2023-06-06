/**
 * junit4 test http://docs.groovy-lang.org/docs/latest/html/documentation/core-testing-guide.html#_junit_4
 */
package com.cabolabs.openehr;

import static org.junit.Assert.*
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import com.cabolabs.openehr.adl.ArchetypeManager
import com.cabolabs.openehr.adl.ArchetypeWalkthrough

/**
 * @author Pablo Pazos
 *
 */
class ArchetypeWalkthroughTest {

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

   @Test
   public void testGetConstraintCodes()
   {
      def loader = ArchetypeManager.getInstance(path)
      loader.loadAll()

      def walk = new ArchetypeWalkthrough()

      /* find constraint references acNNNN en DvCodedText */

      def arch_constraint_codes_paths = [
      "/data[at0001]/events[at0002]/data[at0003]/items[at0004]/value/defining_code": "ac0001",
      "/data[at0001]/events[at0002]/data[at0003]/items[at0005]/value/defining_code": "ac0002",
      "/data[at0001]/events[at0002]/data[at0003]/items[at0009]/value/defining_code": "ac0003"
      ]


      walk.observeCR { node, parchetype, walkt, parent -> // parent no deberia venir de la recorrida porque lo arregle en el parser... actualizar los jars

         /*
         println "ConstraintRef: "+ parchetype.archetypeId.value + node.path() +" "+
                          node.rmTypeName +" "+ node.nodeID +" "+ node.reference
         */

         assert parchetype.archetypeId.value == 'openEHR-EHR-OBSERVATION.test_servicios_terminologicos.v1'
         assert node.rmTypeName == "CODE_PHRASE"
         assert arch_constraint_codes_paths.keySet().contains(node.path()) // path that contains the constraint ref
         assert arch_constraint_codes_paths[node.path()] == node.reference // acNNNN

         // Donde pongo el mapeo entre path del nodo y el codigo de restriccion
         if (!walkt.memory['path_ccode']) walkt.memory['path_ccode'] = [:]

         String fullPath = parchetype.archetypeId.value + node.path()
         walkt.memory['path_ccode'][fullPath] = node.reference
      }



      /*
      arquetipo:

      	constraint_bindings = <
            ["ICD10_1998"] = <
               items = <
                  ["ac0002"] = <terminology:ICD10_1998?subset=XIX%20Trauma>
               >
            >
         >
      */
      // FIXME: esto no distingue entre constraint_binding y term_binding.
      //        y aca solo quiero constraint_binding...
      walk.observeOntBind { bindings, parchetype, walkt, attr ->

         //println "bindings class: "+ bindings.class //  List<OntologyBinding>

         // Verifica si hay codigos de restricciones en constraint_bindings
         // Si hay, quiero la URL que define el codigo que se usa para identificar
         // el servicio terminologico donde se an a buscar los terminos
         // println attr // term_bindings o constraint_bindings
         if (attr == 'constraint_bindings')
         {
            // Donde pongo los mapeos entre codigo de restriccion y query
            if (!walkt.memory['ccode_query']) walkt.memory['ccode_query'] = [:]

            assert bindings.size() == 1

            def ontologyBinding = bindings[0]

            assert ontologyBinding.terminology == "ICD10_1998"


            // List<OntologyBindingItem>
            assert ontologyBinding.bindingList.size() == 1

            def queryBindingItem = ontologyBinding.bindingList[0]

            assert queryBindingItem.code == "ac0002"
            assert queryBindingItem.query.url == "terminology:ICD10_1998?subset=XIX%20Trauma"


            bindings.each { ontobind ->

               ontobind.bindingList.each { item ->

                  walkt.memory['ccode_query'] << [(item.code): item.query.url]
               }
            }




            //OntBinding: [org.openehr.am.archetype.ontology.OntologyBinding@b56559[
            //  terminology=ICD10_1998
            //  bindingList=[org.openehr.am.archetype.ontology.QueryBindingItem@17b6178[
            //    query=org.openehr.am.archetype.ontology.Query@11b92e6[
            //      url=terminology:ICD10_1998?subset=XIX%20Trauma
            //    ]
            //    code=ac0002
            //  ]]
            //]]
         }
      }


      def archetype = loader.getArchetype("openEHR-EHR-OBSERVATION.test_servicios_terminologicos.v1")
      walk.init(archetype)
      walk.start()
      println "memory: "+ walk.memory

      /*
      memory: [
        path_ccode: [
          openEHR-EHR-OBSERVATION.test_servicios_terminologicos.v1/data[at0001]/events[at0002]/data[at0003]/items[at0004]/value/defining_code:ac0001,
          openEHR-EHR-OBSERVATION.test_servicios_terminologicos.v1/data[at0001]/events[at0002]/data[at0003]/items[at0005]/value/defining_code:ac0002,
          openEHR-EHR-OBSERVATION.test_servicios_terminologicos.v1/data[at0001]/events[at0002]/data[at0003]/items[at0009]/value/defining_code:ac0003
        ],
        ccode_query: [
          ac0002:terminology:ICD10_1998?subset=XIX%20Trauma
        ]
      ]
      */
   }
}
