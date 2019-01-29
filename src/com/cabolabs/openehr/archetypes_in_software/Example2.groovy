package com.cabolabs.openehr.archetypes_in_software

import com.cabolabs.openehr.adl.ArchetypeManager
import com.cabolabs.openehr.adl.ArchetypeWalkthrough
import groovy.xml.MarkupBuilder
import se.acode.openehr.parser.*
import org.openehr.am.archetype.*
import org.openehr.am.archetype.constraintmodel.ConstraintRef
import org.openehr.am.openehrprofile.datatypes.text.CCodePhrase
import org.openehr.rm.common.resource.TranslationDetails

class Example2 {

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

         def pathSystolic = '/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value'

         // https://github.com/openEHR/java-libs/blob/master/openehr-ap/src/main/java/org/openehr/am/openehrprofile/datatypes/quantity/CDvQuantity.java
         def cdvquantity = archetype.node(pathSystolic)
         def listItems = cdvquantity.getList() // constraints

         listItems.each { item ->
            println 'units: '+ item.units // String mm[Hg]
            println item.magnitude.getClass() // Interval<Double>
            println 'lower: '+ item.magnitude.lower // 0.0
            println 'upper: '+ item.magnitude.upper // 1000.0
         }
      }
   }
}
