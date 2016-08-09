package com.cabolabs.openehr.ui

import com.cabolabs.openehr.adl.ArchetypeManager
import com.cabolabs.openehr.adl.ArchetypeWalkthrough
import groovy.xml.MarkupBuilder
import org.openehr.am.archetype.constraintmodel.ConstraintRef
import org.openehr.am.openehrprofile.datatypes.text.CCodePhrase

class FormGeneratorWalkthrough {

   private static String PS = File.separator
   private static String path = "."+ PS +"resources"+ PS +"archetypes"
   private static List datavalues = ['DV_TEXT', 'DV_CODED_TEXT', 'DV_QUANTITY', 'DV_COUNT',
                                     'DV_ORDINAL', 'DV_DATE', 'DV_DATE_TIME', 'DV_PROPORTION',
                                     'DV_DURATION']

   String generate(arch, locale)
   {
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
                       
                          option(value:code, parchetype.ontology.termDefinition(locale, code)?.text)
                       }
                    }
                 }
                 if (constraint instanceof ConstraintRef)
                 {
                    println constraint // TODO
                    def term = parchetype.ontology.constraintDefinition(locale, constraint.reference)
                    println term.text
                    println term.description
                    
                    input(type:'text', name:node.path())
                    i(class:'search', '')
                 }
              break
              case 'DV_QUANTITY':
                 input(type:'text', name:node.path()+'/magnitude')
                 
                 select(name:node.path()+'/units') {
                    
                    node.list.units.each { u ->
                    
                       option(value:u, u)
                    }
                 }
              break
              case 'DV_COUNT':
                 input(type:'number', name:node.path())
              break
              case 'DV_ORDINAL':
                 
                 // ordinal.value // int
                 // ordinal.symbol // DvCodedText
                 // ordinal.symbol.codeString
                 // ordinal.symbol.terminologyId
                 
                 select(name:node.path()) {
                    
                    node.list.each { ord ->
                    
                       option(value:ord.value, this.arch.ontology.termDefinition(locale, ord.symbol.codeString).text)
                    }
                 }
              break
              case 'DV_DATE':
                 input(type:'date', name:node.path())
              break
              case 'DV_DATE_TIME':
                 input(type:'datetime-local', name:node.path())
              break
              case 'DV_BOOLEAN':
                 input(type:'checkbox', name:node.path())
              break
              case 'DV_DURATION':
                 label('D') {
                   input(type:'number', name:node.path()+'/D', class:'small')
                 }
                 label('H') {
                   input(type:'number', name:node.path()+'/H', class:'small')
                 }
                 label('M') {
                   input(type:'number', name:node.path()+'/M', class:'small')
                 }
                 label('S') {
                   input(type:'number', name:node.path()+'/S', class:'small')
                 }
              break
              case 'DV_PROPORTION':
                 label('numerator') {
                   input(type:'number', name:node.path()+'/numerator', class:'small')
                 }
                 label('denominator') {
                   input(type:'number', name:node.path()+'/denominator', class:'small')
                 }
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
        head() {
          link(rel:"stylesheet", href:"style.css")
        }
        body() {
          h1(arch.archetypeId.value)
          walk.start()
        }
      }
      
      return writer.toString()
   }
}
