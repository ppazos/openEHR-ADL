package com.cabolabs.openehr.archetypes_in_software

import com.cabolabs.openehr.adl.ArchetypeManager
import com.cabolabs.openehr.adl.ArchetypeWalkthrough
import groovy.xml.MarkupBuilder
import se.acode.openehr.parser.*
import org.openehr.am.archetype.*
import org.openehr.am.archetype.constraintmodel.ConstraintRef
import org.openehr.am.openehrprofile.datatypes.text.CCodePhrase
import org.openehr.rm.common.resource.TranslationDetails
import org.openehr.am.archetype.ontology.*

class Example3 {

   private static String PS = File.separator
   private static String path = "."+ PS +"resources"+ PS +"archetypes"

   static void main(String[] args)
   {
      def f = new File(path + PS + 'entry/observation/openEHR-EHR-OBSERVATION.blood_pressure.v1.adl')
      ADLParser parser = null

      try {
         parser = new ADLParser(f)
      } catch (IOException e) {
         println e.message
      }
      Archetype archetype = null
      try {
         archetype = parser.archetype()
      } catch (Exception e) {
         println e.message
      }

      if (archetype) {
         assert archetype.archetypeId.value == 'openEHR-EHR-OBSERVATION.blood_pressure.v1'

         ArchetypeTerm term = archetype.ontology.termDefinition('en', 'at0005')

         assert term
         assert term.getText() == 'Diastolic'
         assert term.getDescription() == 'Minimum systemic arterial blood pressure - measured in the diastolic or relaxation phase of the heart cycle.'

         println ""
         println "Supported languages:"
         archetype.ontology.languages.each {
            println it
         }

         println ""
         println "Terms defined:"
         // OntologyDefinitions
         archetype.ontology.termDefinitionsList.each { odefs ->

            println "Lang: "+ odefs.language

            // ArchetypeTerm
            odefs.definitions.each { aterm ->
               println "\t"+ aterm.code +" "+ aterm.text
            }
         }
      }
   }
}
