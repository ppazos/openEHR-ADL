import com.cabolabs.openehr.adl.ArchetypeManager

def path = "C:\\Documents and Settings\\pab\\My Documents\\GitHub\\openEHR-ADL\\resources\\archetypes"
def loader = ArchetypeManager.getInstance(path)

// Carga todos los arquetipos de un directorio dado y los cachea
loader.loadAll()

// Acceso r�pido a arquetipos en memoria
def arch = loader.getArchetype("openEHR-EHR-OBSERVATION.blood_pressure.v1")

// Acceso a nodos de arquetipos por rutas absolutas
def c = loader.getArchetypeNode("openEHR-EHR-OBSERVATION.blood_pressure.v1"+
                                "/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value")

// Restricci�n sobre DV_QUANTITY de PA Sist�lica!
assert c.getClass().getSimpleName() == "CDvQuantity"

// Idem usando la interfaz de Archetype y ruta relativa a la ra�z del arquetipo
c = arch.node("/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value")

/** Validacion de datos contra restriccion
https://github.com/openEHR/java-libs/blob/master/openehr-ap/src/main/java/org/openehr/am/
openehrprofile/datatypes/quantity/CDvQuantity.java
**/
def datos = [magnitude: 134d, units: "mm[Hg]"]
def valid = false
def validador = c.list.find{ it.units == datos.units } // soporta multiples unidades
if (validador) valid = validador.magnitude.has(datos.magnitude)

println (valid ? "Valida" : "No valida")
