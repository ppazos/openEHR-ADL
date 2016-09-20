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
      //println "generate " + arch.definition.rmTypeName +" "+ locale
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
          div(class: "container") {
            h1(arch.archetypeId.value)
            form(method:'POST') {
              input(type:'hidden', name:'archetypeId', value:arch.archetypeId.value)
              process(arch.definition, builder, null)
            }
          }
        }
      }
      
      return writer.toString()
   }
   
   def process(CComplexObject o, MarkupBuilder b, ArchetypeInternalRef from_ref)
   {
      // TODO:
      // For instruction, generate the narrative
      // ...
      
      if (o.rmTypeName == 'ELEMENT')
      {
         // TODO: support ELEMENT.name constraint
         def valueObj = o.attributes.find { it.rmAttributeName == 'value' }.children[0]
         
         // override path is used to support ARCHETYPE_REFS
         def path = valueObj.path()
         if (from_ref)
         {
            //println "current path " + path
            //println "from target: " + from_ref.targetPath
            //println "from path: " + from_ref.path()
            
            // Need to add the nodeId of the target because the ref path doesn't have it
            path = path.replace(from_ref.targetPath, from_ref.path() +'['+ this.arch.node( from_ref.targetPath ).nodeId +']') // the parent of the current node is the ref not the referenced node
            //println "result path "+ path
         }
         
         b.div(class:o.rmTypeName) {
         
           label( this.arch.ontology.termDefinition(locale, o.nodeID)?.text )
           
           switch (valueObj.rmTypeName)
           {
              case 'DV_TEXT':
                 textarea(name:path, class:'form-control', '')
              break
              case 'DV_CODED_TEXT':
                 def constraint = valueObj.attributes.find{ it.rmAttributeName == 'defining_code' }.children[0]
                 if (constraint instanceof CCodePhrase)
                 {
                    //println constraint.codeList
                    def cpath = constraint.path()
                    if (from_ref) cpath = cpath.replace(from_ref.targetPath, from_ref.path() +'['+ this.arch.node( from_ref.targetPath ).nodeId +']') // the parent of the current node is the ref not the referenced node
         
                    select(name:cpath, class:'form-control') {
                    
                       constraint.codeList.each { code ->
                       
                          option(value:code, this.arch.ontology.termDefinition(locale, code)?.text)
                       }
                    }
                 }
                 if (constraint instanceof ConstraintRef)
                 {
                    input(type:'text', name:cpath, class:'form-control')
                    i(class:'search', '')
                 }
              break
              case 'DV_QUANTITY':
                 input(type:'number', name:path+'/magnitude', class:'form-control')
                 
                 select(name:path+'/units', class:'form-control') {
                    
                    valueObj.list.units.each { u ->
                    
                       option(value:u, u)
                    }
                 }
              break
              case 'DV_COUNT':
                 input(type:'number', name:path, class:'form-control')
              break
              case 'DV_ORDINAL':
                 select(name:path, class:'form-control') {
                    
                    valueObj.list.each { ord ->
                    
                       option(value:ord.value, this.arch.ontology.termDefinition(locale, ord.symbol.codeString).text)
                    }
                 }
              break
              case 'DV_DATE':
                 input(type:'date', name:path, class:'form-control')
              break
              case 'DV_DATE_TIME':
                 input(type:'datetime-local', name:path, class:'form-control')
              break
              case 'DV_BOOLEAN':
                 input(type:'checkbox', name:path)
              break
              case 'DV_DURATION':
                 label('D') {
                   input(type:'number', name:path+'/D', class:'small form-control')
                 }
                 label('H') {
                   input(type:'number', name:path+'/H', class:'small form-control')
                 }
                 label('M') {
                   input(type:'number', name:path+'/M', class:'small form-control')
                 }
                 label('S') {
                   input(type:'number', name:path+'/S', class:'small form-control')
                 }
              break
              case 'DV_PROPORTION':
                 label('numerator') {
                   input(type:'number', name:path+'/numerator', class:'small form-control')
                 }
                 label('denominator') {
                   input(type:'number', name:path+'/denominator', class:'small form-control')
                 }
              break
              // TODO: DV_IDENTIFIER
              // TODO: generar campos para los DV_INTERVAL
           }
         }
      }
      else
      {
         b.div(class:o.rmTypeName) {
            
            label(this.arch.ontology.termDefinition(locale, o.nodeID)?.text)
            o.attributes.each { a ->
               process(a, b, from_ref)
            }
         }
      }
   }
   def process(CAttribute a, MarkupBuilder b, ArchetypeInternalRef from_ref)
   {
      a.children.each { o ->
      
        //println o.getClass()
        process(o, b, from_ref)
      }
   }
   def process(ArchetypeSlot s, MarkupBuilder b, ArchetypeInternalRef from_ref)
   {
      // Don't support slots yet
   }
   
   // Handles DV_CODED_TEXT constraints that are for IM attributes that are not ELEMENT.value
   def process(CCodePhrase c, MarkupBuilder b, ArchetypeInternalRef from_ref)
   {
      def cpath = c.path()
      if (from_ref) cpath = cpath.replace(from_ref.targetPath, from_ref.path() +'['+ this.arch.node( from_ref.targetPath ).nodeId +']') // the parent of the current node is the ref not the referenced node
         
      b.select(name:cpath, class:'form-control') {
                    
         c.codeList.each { code ->
        
            option(value:code, this.arch.ontology.termDefinition(locale, code)?.text)
         }
      }
   }
   def process(CPrimitiveObject p, MarkupBuilder b, ArchetypeInternalRef from_ref)
   {
      // TODO:
      // Don't support po yet
      /*
      println p
      org.openehr.am.archetype.constraintmodel.CPrimitiveObject@1c74445[
      item=org.openehr.am.archetype.constraintmodel.primitive.CDuration@1530ff3[
      value=<null>
      interval=org.openehr.rm.support.basic.Interval@691263[lower=PT24H,lowerIncluded=true,upper=PT24H,upperIncluded=true]
      assumedValue=<null>
      pattern=<null>
      defaultValue=<null>
     */
      println "This constraint is not supported yet: "+ p.class +" "+ p.path() +" "+ p.rmTypeName // e.g. CDuration
   }
   
   def process(ArchetypeInternalRef o, MarkupBuilder b, ArchetypeInternalRef from_ref)
   {
      def node = this.arch.node( o.targetPath ) // process referenced node
      
      //println "ref node: " + node.getClass()
      
      process(node, b, o) // the processed is the reference but needs to start with the current node path to avoid losin the reference to the current object
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
