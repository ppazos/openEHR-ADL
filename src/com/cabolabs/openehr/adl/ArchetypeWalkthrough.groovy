package com.cabolabs.openehr.adl

import org.openehr.am.archetype.Archetype
import org.openehr.am.archetype.constraintmodel.ArchetypeInternalRef
import org.openehr.am.archetype.constraintmodel.ArchetypeSlot
import org.openehr.am.archetype.constraintmodel.CAttribute
import org.openehr.am.archetype.constraintmodel.CComplexObject
import org.openehr.am.archetype.constraintmodel.CDomainType
import org.openehr.am.archetype.constraintmodel.CObject
import org.openehr.am.archetype.constraintmodel.CPrimitiveObject
import org.openehr.am.archetype.constraintmodel.CSingleAttribute
import org.openehr.am.archetype.constraintmodel.ConstraintRef
import org.openehr.am.archetype.constraintmodel.primitive.CPrimitive
import org.openehr.am.archetype.constraintmodel.primitive.CString
import org.openehr.am.archetype.ontology.ArchetypeOntology
import org.openehr.am.archetype.ontology.ArchetypeTerm
import org.openehr.am.archetype.ontology.OntologyBinding
import org.openehr.am.archetype.ontology.OntologyBindingItem
import org.openehr.am.archetype.ontology.OntologyDefinitions
import org.openehr.am.archetype.ontology.TermBindingItem
import org.openehr.am.openehrprofile.datatypes.quantity.CDvOrdinal

public class ArchetypeWalkthrough {

   /**
    * Eventos que se disparan cuando se encuentran distintos tipos de nodos.
    * TODO: Deberian ser un enum.
    */
   static String EVENT_COBJECT = "co"
   static String EVENT_CCOMPLEXOBJECT = "cco"
   static String EVENT_CDOMAIN = "cdo"           // ConstraintRef
   static String EVENT_CPRIMITIVE_OBJECT = "cpo" // org/openehr/am/archetype/constraintmodel/CPrimitiveObject
   static String EVENT_CPRIMITIVE = "cpr"        // org/openehr/am/archetype/constraintmodel/primitive/CPrimitive
   static String EVENT_SLOT = "sl"               // ArchetypeSlot
   static String EVENT_CATTRIBUTE = "cat"
   
   static String EVENT_CREF = "ref"
   static String EVENT_BEFORE = "bef"            // Antes de comenzar la recorrida ejecuta este
   
   static String EVENT_ONT_DEFINITIONS = "ontdefs" // OntologyDefinitions
   static String EVENT_ONT_BINDING     = "ontbind" // OntologyBinding
   
   static String EVENT_ONT_ARCHETYPE_TERM = "ont_at"
   static String EVENT_ONT_BINDING_ITEM = "ont_bit"
   static String EVENT_ONT_TERM_BINDING_ITEM = "ont_tbi" // TermBindingItem extends OntologyBindingItem
   
   // Map event->lista de acciones que se ejecutan cuando se encuentra cada nodo
   Map observers = [:]
   
   /**
    * Arquetipo por el que se empieza la recorrida.
    */
   Archetype root
   
   ArchetypeCodes codeMapping
   
   // Memoria interna disponible para que los observers guarden estructuras durante la recorrida
   Map memory = [:]
   
   
   /**
    * Registra una accion a un evento.
    */
   //public void observe(String event, AbstractAction action)
   public void observeCCO(Closure action)
   {
      if (!observers[EVENT_CCOMPLEXOBJECT]) observers[EVENT_CCOMPLEXOBJECT] = []
      observers[EVENT_CCOMPLEXOBJECT] << action
   }
   public void observeDM(Closure action) // CDomainType es abstract, subclases definidas en profile.
   {
      if (!observers[EVENT_CDOMAIN]) observers[EVENT_CDOMAIN] = []
      observers[EVENT_CDOMAIN] << action
   }
   public void observeCR(Closure action)
   {
      if (!observers[EVENT_CREF]) observers[EVENT_CREF] = []
      observers[EVENT_CREF] << action
   }
   public void observeAS(Closure action)
   {
      if (!observers[EVENT_SLOT]) observers[EVENT_SLOT] = []
      observers[EVENT_SLOT] << action
   }
   public void observeCPO(Closure action)
   {
      if (!observers[EVENT_CPRIMITIVE_OBJECT]) observers[EVENT_CPRIMITIVE_OBJECT] = []
      observers[EVENT_CPRIMITIVE_OBJECT] << action
   }
   public void observeCPrimitive(Closure action)
   {
      if (!observers[EVENT_CPRIMITIVE]) observers[EVENT_CPRIMITIVE] = []
      observers[EVENT_CPRIMITIVE] << action
   }
   
   
   public void observeOntDefs(Closure action)
   {
      if (!observers[EVENT_ONT_DEFINITIONS]) observers[EVENT_ONT_DEFINITIONS] = []
      observers[EVENT_ONT_DEFINITIONS] << action
   }
   public void observeOntBind(Closure action)
   {
      if (!observers[EVENT_ONT_BINDING]) observers[EVENT_ONT_BINDING] = []
      observers[EVENT_ONT_BINDING] << action
   }
   
   public void observeOAT(Closure action)
   {
      if (!observers[EVENT_ONT_ARCHETYPE_TERM]) observers[EVENT_ONT_ARCHETYPE_TERM] = []
      observers[EVENT_ONT_ARCHETYPE_TERM] << action
   }
   public void observeOBI(Closure action)
   {
      if (!observers[EVENT_ONT_BINDING_ITEM]) observers[EVENT_ONT_BINDING_ITEM] = []
      observers[EVENT_ONT_BINDING_ITEM] << action
   }
   public void observeOTBI(Closure action)
   {
      if (!observers[EVENT_ONT_TERM_BINDING_ITEM]) observers[EVENT_ONT_TERM_BINDING_ITEM] = []
      observers[EVENT_ONT_TERM_BINDING_ITEM] << action
   }
   
   
   public void init(Archetype root)
   {
      this.root = root
      this.codeMapping = new ArchetypeCodes(root.archetypeId.value)
   }
   
   /**
    * Comienza recorrida de raiz a hojas.
    */
   public void start()
   {
      // walkthrough definition
      wt(this.root.definition, null, this.root)
      
      // walkthrough ontology
      wt(this.root.ontology, null, this.root)
      
      // debug
      //println "---------------------------------------"
      //println "----------  CODE MAPPING  -------------"
      //println "---------------------------------------"
      //println this.codeMapping.atCodesMapping
   }
   
   /**
    * Elimina los terminos de la ontologia con el mismo codigo
    * que el slot. Se usa luego de que un slot es resuelto y
    * eliminado de los nodos del arquetipo resultante del flattening.
    * @param slot
    */
   public void removeTermsForSlot(ArchetypeSlot slot)
   {
      // ontology.languages es null
      // https://github.com/openEHR/java-libs/issues/2
      /*
      println "..."+ this.root.ontology.languages
      this.root.ontology.languages.each { lang ->
         println "...remove $lang "+ slot.nodeId
         this.root.ontology.termDefinitionsList.remove(
            this.root.ontology.termDefinition(lang, slot.nodeId)
         )
      }
      */
      def langs = []
      def termsToRemove = []
      this.root.ontology.termDefinitionsList.each { ontoDefs -> // uno por lenguaje
         langs << ontoDefs.language
         ontoDefs.definitions.each { archTerm ->
            if (archTerm.code == slot.nodeId)
            {
               termsToRemove << archTerm
            }
         }
      }
      def ontDefs
      termsToRemove.each { term ->
         langs.each { lang ->
            //println "...removeTermsForSlot $lang "+ term.code
            ontDefs = this.root.ontology.termDefinitionsList.find { it.language == lang }
            ontDefs.definitions.remove( term )
         }
      }
   }
   
   
   // Recorrida por ontology
   
   // Comienza recorrida
   def wt(ArchetypeOntology ontology, Object parent, Archetype archetype)
   {
      // ArchetypeOntology:
      // - List<OntologyDefinitions> termDefinitionsList
      //   - ArchetypeTerm
      // - List<OntologyDefinitions> constDefinitionsList
      //   - ArchetypeTerm
      // - List<OntologyBinding> termBindingList
      //   - OntologyBindingItem
      // - List<OntologyBinding> constraintBindingList
      //   - OntologyBindingItem
      
      wtdefs(ontology.termDefinitionsList, archetype, 'term_definitions')
      wtdefs(ontology.constraintDefinitionsList, archetype, 'constraint_definitions')
      wtbinds(ontology.termBindingList, archetype, 'term_bindings')
      wtbinds(ontology.constraintBindingList, archetype, 'constraint_bindings')
   }
   // term definitions
   // constraint definitions
   // attr es necesario para que las acciones sepan donde agregar
   // los terminos de arquetipos referenciados en el root.
   def wtdefs(List<OntologyDefinitions> definitions, Archetype archetype, String attr)
   {
      this.observers[EVENT_ONT_DEFINITIONS].each { actions ->
         actions.each { action ->
            action(definitions, archetype, this, attr)
         }
      }
   }
   // term binding
   // constraint binding
   def wtbinds(List<OntologyBinding> bindings, Archetype archetype, String attr)
   {
      this.observers[EVENT_ONT_BINDING].each { actions ->
         actions.each { action ->
            action(bindings, archetype, this, attr)
         }
      }
   }
   
   /**
   def wto(OntologyDefinitions defs, Archetype archetype, String attr)
   {
      //defs.language
      defs.definitions.each { // ArchetypeTerm
         wto(it, archetype, defs.definitions, attr)
      }
   }
   def wto(OntologyBinding binding, Archetype archetype, String attr)
   {
      //binding.terminology
      binding.bindingList.each { // OntologyBindingItem
         wto(it, archetype, binding.bindingList, attr)
      }
   }
   def wto(ArchetypeTerm term, Archetype archetype, Object parent, String attr)
   {
      //term.code         << el codigo que hay que cambiar en el rewrite de codigos
      //term.description
      //term.text
      this.observers[EVENT_ONT_ARCHETYPE_TERM].each { actions ->
         actions.each { action ->
            //action.execute([archetype:archetype, node:term, result:this.result, walk:this]) // result es in/out
            action(term, archetype, this, parent, attr)
         }
      }
   }
   
   // Esta no creo que se llame, seguro se llama a la de su hija TermBindingItem
   def wto(OntologyBindingItem item, Archetype archetype, Object parent, String attr)
   {
      //item.code
      this.observers[EVENT_ONT_BINDING_ITEM].each { actions ->
         actions.each { action ->
            //action.execute([archetype:archetype, node:item, result:this.result, walk:this]) // result es in/out
            action(item, archetype, this, parent, attr)
         }
      }
   }
   
   // Subclase de OntologyBindingItem
   def wto(TermBindingItem item, Archetype archetype, Object parent, String attr)
   {
      //item.code  // String
      //item.terms // List<String>
      // esta es subclase, por eso el evento es el mismo
      this.observers[EVENT_ONT_TERM_BINDING_ITEM].each { actions ->
         actions.each { action ->
            //action.execute([archetype:archetype, node:item, result:this.result, walk:this]) // result es in/out
            action(item, archetype, this, parent, attr)
         }
      }
   }
   */
   // /Recorrida por ontology
   // =================================================================================
   
   // =================================================================================
   // Inicio de la recorrida definition
   def wt(CComplexObject c, Object parent, Archetype archetype)
   {  
      this.observers[EVENT_CCOMPLEXOBJECT].each { actions ->
         actions.each { action ->
            //action.execute([archetype:archetype, node:c, result:this.result, walk:this]) // result es in/out
            action(c, archetype, this, parent)
         }
      }
      
      //println "XX" + c.attributes.find{ it instanceof CSingleAttribute }
      
      //println "CComplexObject"
      // List<CAttributes>
      c.attributes.each{ attr -> wt(attr, c, archetype) }
   }
   // Para CMultipleAttribute y CSingleAttribute
   def wt(CAttribute c, Object parent, Archetype archetype)
   {
      //println "CAttribute " + c.getClass().getSimpleName()
      
      this.observers[EVENT_CATTRIBUTE].each { actions ->
         actions.each { action ->
            //action.execute([archetype:archetype, node:c, result:this.result, walk:this]) // result es in/out
            action(c, archetype, this, parent)
         }
      }
      
      // List<CObject>
      //c.children.each { co -> findSlots(co, c) } // sin contiene slots, estoy modificando el c.children por el que estoy iterando y tira una except
      
      // Coleccion aparte para poder iterar y no modificar la coleccion por la que itero
      def loopPorAfuera = []
      c.children.each { co -> loopPorAfuera << co }
      loopPorAfuera.each { co ->
         wt(co, c, archetype)
      }
   }
   
   // Muestra el slot encontrado y carga arquetipos referenciados
   def wt(ArchetypeSlot c, Object parent, Archetype archetype)
   {
      // nodeId dice que es null para slot
      //println "Slot>> " + c
      //println "Slot>> " + c.rmTypeName  // + ' ' + c.nodeId // + ' ' + c.includes
      
      this.observers[EVENT_SLOT].each { actions ->
         actions.each { action ->
            //action.execute([archetype:archetype, node:c, result:this.result, walk:this]) // result es in/out
            action(c, archetype, this, parent)
         }
      }
   }
   
   def wt(ArchetypeInternalRef c, Object parent, Archetype archetype)
   {
      def node = archetype.node( c.targetPath )
      wt(node, c.parent, archetype)
   }
   
   // No hacen nada porque no tiene hijos que puedan ser slots, solo el CComplexObject puede tener hijos slots
   def wt(CPrimitiveObject c, Object parent, Archetype archetype)
   {
      //println "CPrimitiveObject"
      this.observers[EVENT_CPRIMITIVE_OBJECT].each { actions ->
         actions.each { action ->
            action(c, archetype, this, parent)
         }
      }
      
      wt(c.item, c, archetype)
   }
   
   def wt(CPrimitive c, Object parent, Archetype archetype)
   {
//      if (c instanceof CString)
//         println "wt primitive " + c.list
      this.observers[EVENT_CPRIMITIVE].each { actions ->
         actions.each { action ->
            action(c, archetype, this, parent)
         }
      }
   }
   
   def wt(CDomainType c, Object parent, Archetype archetype) // CCodePhrase, ...
   {
      //println "CDomainType " + c.getClass().getSimpleName()
      // CDvState, CCodePhrase, CDvOrdinal, CDvQuantity
      
      //println c
      //if (c instanceof CDvOrdinal) println c
      
      this.observers[EVENT_CDOMAIN].each { actions ->
         actions.each { action ->
            //action.execute([archetype:archetype, node:c, result:this.result, walk:this]) // result es in/out
            action(c, archetype, this, parent)
         }
      }
   }
   
   // Nodos que tienen codigos acNNNN
   def wt(ConstraintRef c, Object parent, Archetype archetype) // CCodePhrase, ...
   {
      //println "CDomainType " + c.getClass().getSimpleName()
      
      this.observers[EVENT_CREF].each { actions ->
         actions.each { action ->
            //action.execute([archetype:archetype, node:c, result:this.result, walk:this]) // result es in/out
            action(c, archetype, this, parent)
         }
      }
   }
   
   /**
    * Metodos auxiliares para recorrer desed nodos hijos a nodos padres.
    */
   def parent(CObject co)
   {
      // WARNING: por como esta implementado node(), solo se obtienen CObjects,
      //          entonces el parent de CObject no puede ser CAttribute.
      
      // la path del co es /a[atNNNN]/b[atNNNN], puede ser /
      if (co.path() == "/") return null
      
      // FIXME: si co no tienen nodeID su ruta es igual a la de su parent. Y no se que va a devolver
      //        el archetype.node con esta path, el mismo CObject o el padre CAttribute.
      
      // quiero la path al atibuto padre, tengo que sacar el nodeId de la path del co
      if (!co.nodeID) throw new Exception("CObject no tiene nodeID "+ co.toString()) 
      
      String parentPath = co.path() - "["+ co.nodeID +"]" // path al CAttribute padre
      return this.root.node(parentPath)
      
      /* esto era para sacarle la ultima parte a la path pero da la path a otro CObject, el abuelo.
      def parts = co.path().split("/") // tiene por lo menos 2 elementos ej. /a[atNNNN].split = /, a[atNNNN]
      
      // si tiene 2 elementos, el padre es root.
      // se verifica aqui porque el join sacandole el ultimo elemento da "" en lugar de "/"
      if (parts.size() == 2) return this.root.node("/")
      
      // saca el ultimo elemento de la lista ej. b[atNNNN]
      // y une los restantes concatenandolos usando /, lo que deja una path valida
      parts[0..parts.size()-2].join("/")
      */
      
   }
   
   def parent(CAttribute ca)
   {
      // la path de ca termina en el nombre del atributo /a[atNNNN]/b[atNNNN]/c
      // ca ya tiene el metodo que le saca el nombre del atributo del propio ca a la path
      String parentPath = ca.parentNodePath()
      return this.root.node(parentPath)
   }
}
