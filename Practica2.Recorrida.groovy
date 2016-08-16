import com.cabolabs.openehr.adl.ArchetypeManager
import org.openehr.am.archetype.constraintmodel.*
import org.openehr.am.openehrprofile.datatypes.quantity.CDvQuantity

def path = "C:\\Documents and Settings\\pab\\My Documents\\GitHub\\openEHR-ADL\\resources\\archetypes"
def loader = ArchetypeManager.getInstance(path)

// Carga todos los arquetipos de un directorio dado y los cachea
loader.loadAll()

// Acceso rápido a arquetipos en memoria
def arch = loader.getArchetype("openEHR-EHR-OBSERVATION.blood_pressure.v1")

// ver http://www.openehr.org/releases/1.0.2/architecture/am/aom.pdf pagina 19

traverse(arch.definition)

def traverse(CObject o)
{
  println o.getClass().getSimpleName() +"\t"+ o.rmTypeName.padLeft(15) +"\t"+ o.path()
  
  /*
  println o.getClass()
  org.openehr.am.openehrprofile.datatypes.text.CCodePhrase
  org.openehr.am.archetype.constraintmodel.ArchetypeSlot
  org.openehr.am.archetype.constraintmodel.ArchetypeInternalRef
  org.openehr.am.archetype.constraintmodel.CPrimitiveObject
  org.openehr.am.openehrprofile.datatypes.quantity.CDvQuantity
  */
}

def traverse(CDvQuantity c)
{
  c.list.each {
    println c.getClass().getSimpleName() +"\t"+ c.rmTypeName.padLeft(15) +"\t"+ it.units +" "+ it.magnitude.lower +".."+ it.magnitude.upper
  }
}

def traverse(CComplexObject o)
{
  println o.getClass().getSimpleName() +"\t"+ o.rmTypeName.padLeft(15) +"\t"+ o.path()
  o.attributes.each { traverse(it) } // CComplexObject tiene attributes
}

def traverse(CAttribute a)
{
  println a.getClass().getSimpleName() +"\t"+ a.rmAttributeName.padLeft(15) +"\t"+ a.path()
  a.children.each{ traverse(it) }
}

return