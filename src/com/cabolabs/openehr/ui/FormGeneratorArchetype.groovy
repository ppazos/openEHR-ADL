package com.cabolabs.openehr.ui

import com.cabolabs.openehr.adl.ArchetypeManager
import groovy.xml.MarkupBuilder
import org.openehr.am.archetype.constraintmodel.*
import org.openehr.am.openehrprofile.datatypes.text.CCodePhrase

class FormGeneratorArchetype {
                           
   def arch
   def locale

   String generate(arch, locale)
   {
      println "generate " + arch.definition.rmTypeName +" "+ locale
      this.arch = arch
      this.locale = locale
      
      def writer = new StringWriter()
      def builder = new MarkupBuilder(writer)
      builder.setDoubleQuotes(true) // Use double quotes on attributes
      
      // Generates HTML while traversing the archetype tree
      builder.html {
        head() {
          link(rel:"stylesheet", href:"style.css")
        }
        body() {
          h1(arch.archetypeId.value)
          process(arch.definition, builder)
        }
      }
      
      return writer.toString()
   }
   
   def process(CComplexObject o, MarkupBuilder b)
   {
      // TODO:
      // For instruction, generate the narrative
      // ...
      
      if (o.rmTypeName == 'ELEMENT')
      {
         def valueObj = o.attributes.find { it.rmAttributeName == 'value' }.children[0]
         
         b.div(class:o.rmTypeName) {
         
           label( this.arch.ontology.termDefinition(locale, o.nodeID)?.text )
           
           switch (valueObj.rmTypeName)
           {
              case 'DV_TEXT':
                 textarea(name:valueObj.path(), '')
              break
              case 'DV_CODED_TEXT':
                 def constraint = valueObj.attributes.find{ it.rmAttributeName == 'defining_code' }.children[0]
                 if (constraint instanceof CCodePhrase)
                 {
                    //println constraint.codeList
                    
                    select(name:valueObj.path()) {
                    
                       constraint.codeList.each { code ->
                       
                          option(value:code, this.arch.ontology.termDefinition(locale, code)?.text)
                       }
                    }
                 }
                 if (constraint instanceof ConstraintRef)
                 {
                    input(type:'text', name:valueObj.path())
                    i(class:'search', '')
                 }
              break
              case 'DV_QUANTITY':
                 input(type:'text', name:valueObj.path()+'/magnitude')
                 
                 select(name:valueObj.path()+'/units') {
                    
                    valueObj.list.units.each { u ->
                    
                       option(value:u, u)
                    }
                 }
              break
              case 'DV_COUNT':
                 input(type:'number', name:valueObj.path())
              break
              case 'DV_ORDINAL':
                 select(name:valueObj.path()) {
                    
                    valueObj.list.each { ord ->
                    
                       option(value:ord.value, this.arch.ontology.termDefinition(locale, ord.symbol.codeString).text)
                    }
                 }
              break
              case 'DV_DATE':
                 input(type:'date', name:valueObj.path())
              break
              case 'DV_DATE_TIME':
                 input(type:'datetime-local', name:valueObj.path())
              break
              case 'DV_BOOLEAN':
                 input(type:'checkbox', name:valueObj.path())
              break
              case 'DV_DURATION':
                 label('D') {
                   input(type:'number', name:valueObj.path()+'/D', class:'small')
                 }
                 label('H') {
                   input(type:'number', name:valueObj.path()+'/H', class:'small')
                 }
                 label('M') {
                   input(type:'number', name:valueObj.path()+'/M', class:'small')
                 }
                 label('S') {
                   input(type:'number', name:valueObj.path()+'/S', class:'small')
                 }
              break
              case 'DV_PROPORTION':
                 label('numerator') {
                   input(type:'number', name:valueObj.path()+'/numerator', class:'small')
                 }
                 label('denominator') {
                   input(type:'number', name:valueObj.path()+'/denominator', class:'small')
                 }
              break
              // TODO: generar campos para los DV_INTERVAL
           }
         }
      }
      else
      {
         b.div(class:o.rmTypeName) {
            
            label(this.arch.ontology.termDefinition(locale, o.nodeID)?.text)
            o.attributes.each { a ->
               process(a, b)
            }
         }
      }
   }
   def process(CAttribute a, MarkupBuilder b)
   {
      a.children.each { o ->
      
        println o.getClass()
        process(o, b)
      }
   }
   def process(ArchetypeSlot s, MarkupBuilder b)
   {
      // Don't support slots yet
   }
   
   // Handles DV_CODED_TEXT constraints that are for IM attributes that are not ELEMENT.value
   def process(CCodePhrase c, MarkupBuilder b)
   {
      // TODO
      println c
   }
   def process(CPrimitiveObject p, MarkupBuilder b)
   {
      // Don't support po yet
      println p // e.g. CDuration
   }
   
   def process(ArchetypeInternalRef o, MarkupBuilder b)
   {
      // Don't support iref yet
      println o
      
      // TODO get the node and process it
      /*
      class org.openehr.am.archetype.constraintmodel.ArchetypeInternalRef
         org.openehr.am.archetype.constraintmodel.ArchetypeInternalRef@11b16[
           targetPath=/data[at0001]/events[at0006]/state[at0007]
           rmTypeName=ITEM_TREE
           occurrences=org.openehr.rm.support.basic.Interval@9957eb[lower=1,lowerIncluded=true,upper=1,upperIncluded=true]
           nodeID=<null>
           parent=org.openehr.am.archetype.constraintmodel.CSingleAttribute@15b4535[
           rmAttributeName=state
           existence=REQUIRED
           children=[org.openehr.am.archetype.constraintmodel.ArchetypeInternalRef@11b16[
           targetPath=/data[at0001]/events[at0006]/state[at0007]
           rmTypeName=ITEM_TREE
           occurrences=org.openehr.rm.support.basic.Interval@9957eb[lower=1,lowerIncluded=true,upper=1,upperIncluded=true]
        nodeID=<null>
        parent=org.openehr.am.archetype.constraintmodel.CSingleAttribute@15b4535
        anyAllowed=false
        path=/data[at0001]/events[at1042]/state
        hiddenOnForm=false
        annotation=<null>
      ]]
        anyAllowed=false
        path=/data[at0001]/events[at1042]/state
        hiddenOnForm=false
        annotation=<null>
      ]
        anyAllowed=false
        path=/data[at0001]/events[at1042]/state
        hiddenOnForm=false
        annotation=<null>
      ]

      */
   }
   
   /*
   def process(CComplexObject o, MarkupBuilder b)
   {
   }
   def process(CComplexObject o, MarkupBuilder b)
   {
   }
   */
}
