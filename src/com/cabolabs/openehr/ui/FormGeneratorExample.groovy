package com.cabolabs.openehr.ui

import com.cabolabs.openehr.adl.ArchetypeManager
import com.cabolabs.openehr.adl.ArchetypeWalkthrough
import groovy.xml.MarkupBuilder
import org.openehr.am.archetype.constraintmodel.ConstraintRef
import org.openehr.am.openehrprofile.datatypes.text.CCodePhrase

class FormGeneratorExample {

   private static String PS = File.separator
   private static String path = "."+ PS +"resources"+ PS +"archetypes"
   private static List datavalues = ['DV_TEXT', 'DV_CODED_TEXT', 'DV_QUANTITY', 'DV_COUNT',
                                     'DV_ORDINAL', 'DV_DATE', 'DV_DATE_TIME']

   static void main(String[] args)
   {
      if (args.size() == 0 || args[0] == 'help')
      {
         println 'usage: uigen archetypeId'
         System.exit(0)
      }
      
      // TODO> args
      def locale = "es"
      
      def archetypeId = args[0]
      def loader = ArchetypeManager.getInstance(path)
      loader.loadAll()
      def arch

      try
      {
         arch = loader.getArchetype( archetypeId )
      }
      catch (e)
      {
         println "Verify the archetypeId format"
         System.exit(0)
      }
      
      if (!arch)
      {
         println "Archetype $archetypeId doesn't exists"
         System.exit(0)
      }
      
      def walk = new ArchetypeWalkthrough()
      
      
      def writer = new StringWriter()
      def builder = new MarkupBuilder(writer)
      builder.setDoubleQuotes(true) // Use double quotes on attributes

      
      
      // Destination html
      walk.memory['html'] = builder
      
      walk.observeCCO { node, parchetype, walkt, parent ->

         //println "complex "+ node.rmTypeName
         
         // datavalue not supported
         if (!datavalues.contains(node.rmTypeName))
         {
            //println node.rmTypeName + " not supported "
            return
         }
         
         /*
         println "parent " + node.path()
         println "parent " + node.parent.parentNodePath()
         println "parent " + parchetype.node(node.parent.parentNodePath()).rmTypeName
         */
         
         def parentObject = parchetype.node(node.parent.parentNodePath())
         
         // get nodeid to get term
         if (parentObject.rmTypeName == 'ELEMENT')
         {
         }
         else if (parentObject.rmTypeName.startsWith('DV_INTERVAL')) // DV_INTERVAL<DV_COUNT>
         {
            parentObject = parchetype.node(parentObject.parent.parentNodePath())
            return // interval not supported yet
         }
         
         walkt.memory['html'].div() {
         
           label( parchetype.ontology.termDefinition(locale, parentObject.nodeID)?.text )
           
           switch (node.rmTypeName)
           {
              case 'DV_TEXT':
                 input(type:'text', name:node.path())
              break
              case 'DV_CODED_TEXT':
                 def constraint = node.attributes.find{ it.rmAttributeName == 'defining_code' }.children[0]
                 if (constraint instanceof CCodePhrase)
                 {
                    //println constraint.codeList
                    
                    select(name:node.path()) {
                    
                       constraint.codeList.each { code ->
                       
                          option(value:code, parchetype.ontology.termDefinition(locale, code).text)
                       }
                    }
                 }
                 //if (constraint instanceof ConstraintRef) println constraint // TODO
              break
              case 'DV_QUANTITY':
                 input(type:'text', name:node.path()+'/magnitude')
                 input(type:'text', name:node.path()+'/units')
              break
              case 'DV_COUNT':
                 input(type:'number', name:node.path())
              break
              case 'DV_ORDINAL':
              
              break
              case 'DV_DATE':
                 input(type:'date', name:node.path())
              break
              case 'DV_DATE_TIME':
                 input(type:'datetime-local', name:node.path())
              break
              // TODO: generar campos para los DV_INTERVAL
           }
         }
      }
      
      
      /*
      walk.observeCPO { node, parchetype, walkt, parent ->

         println "primitive "+ node.rmTypeName
         
      }
      */
      
      walk.init(arch)
      
      builder.html {
        head()
        body() {
          walk.start()
        }
      }
      
      //println "memory: "+ writer.toString()
      
      def f = new File( new java.text.SimpleDateFormat("yyyyMMddhhmmss'.html'").format(new Date()) )
      f << writer.toString()
      println "Generated: "+ f.path
   }
}
