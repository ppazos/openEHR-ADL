package com.cabolabs.openehr.ui

import com.cabolabs.openehr.adl.ArchetypeManager
import com.cabolabs.openehr.adl.ArchetypeWalkthrough
import groovy.xml.MarkupBuilder
import org.openehr.am.archetype.*
import org.openehr.am.archetype.constraintmodel.*
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
         
         // process just elements for simplicity...
         if (node.rmTypeName != 'ELEMENT') return
         
         // constraints for ELEMENT.name and ELEMENT.value, can be null
         def name = node.attributes.find { it.rmAttributeName == 'name' }?.children?.getAt(0)
         def value = node.attributes.find { it.rmAttributeName == 'value' }?.children?.getAt(0)
         /*
         println name?.class
         println value?.class
         println parchetype?.class
         println node?.class
         */
         walkt.memory['html'].div() {
        
            if (name) generateFields(name, node, parchetype, walkt.memory['html'], locale)
            else
            {
               //def parentObject = parchetype.node(node.parent.parentNodePath())
               label( parchetype.ontology.termDefinition(locale, node.nodeID)?.text )
            }
            
            if (value) generateFields(value, node, parchetype, walkt.memory['html'], locale)
         }
         
         /*
         println "parent " + node.path()
         println "parent " + node.parent.parentNodePath()
         println "parent " + parchetype.node(node.parent.parentNodePath()).rmTypeName
         */
         
         /*
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
         
         */
      }
      
      
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
   
   
   def generateFields(CObject node, CObject parent, Archetype a, MarkupBuilder builder, String locale)
   {
      // generate for supported datatypes, other datatypes can be added later
      if (!datavalues.contains(node.rmTypeName))
      {
         return
      }
      
      switch (node.rmTypeName)
      {
        case 'DV_TEXT':
           builder.input(type:'text', name:node.path())
        break
        case 'DV_CODED_TEXT':

           def constraint = node.attributes.find{ it.rmAttributeName == 'defining_code' }.children[0]
           if (constraint instanceof CCodePhrase)
           {
              builder.select(name:constraint.path()) {
              
                 constraint.codeList.each { code ->
                 
                    option(value:code, a.ontology.termDefinition(locale, code)?.text)
                 }
              }
           }
           if (constraint instanceof ConstraintRef)
           {
              /*
              println constraint // TODO
              println term.text
              println term.description
              */
              def term = a.ontology.constraintDefinition(locale, constraint.reference)
              builder.input(type:'text', name:constraint.path())
              i(class:'search', '')
           }
           
        break
        case 'DV_QUANTITY':
           builder.input(type:'text', name:node.path()+'/magnitude')
           
           builder.select(name:node.path()+'/units') {
              
              node.list.units.each { u ->
              
                 option(value:u, u)
              }
           }
        break
        case 'DV_COUNT':
           builder.input(type:'number', name:node.path())
        break
        case 'DV_ORDINAL':
           
           // ordinal.value // int
           // ordinal.symbol // DvCodedText
           // ordinal.symbol.codeString
           // ordinal.symbol.terminologyId
           
           builder.select(name:node.path()) {
              
              node.list.each { ord ->
              
                 option(value:ord.value, a.ontology.termDefinition(locale, ord.symbol.codeString).text)
              }
           }
        break
        case 'DV_DATE':
           builder.input(type:'date', name:node.path())
        break
        case 'DV_DATE_TIME':
           builder.input(type:'datetime-local', name:node.path())
        break
        case 'DV_BOOLEAN':
           builder.input(type:'checkbox', name:node.path())
        break
        case 'DV_DURATION':
           builder.label('D') {
             input(type:'number', name:node.path()+'/D', class:'small')
           }
           builder.label('H') {
             input(type:'number', name:node.path()+'/H', class:'small')
           }
           builder.label('M') {
             input(type:'number', name:node.path()+'/M', class:'small')
           }
           builder.label('S') {
             input(type:'number', name:node.path()+'/S', class:'small')
           }
        break
        case 'DV_PROPORTION':
           builder.label('numerator') {
             input(type:'number', name:node.path()+'/numerator', class:'small')
           }
           builder.label('denominator') {
             input(type:'number', name:node.path()+'/denominator', class:'small')
           }
        break
        // TODO: generar campos para los DV_INTERVAL
      }
   }
}
