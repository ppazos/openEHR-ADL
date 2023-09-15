import com.cabolabs.openehr.adl.ArchetypeManager

def path = "C:\\Documents and Settings\\pab\\My Documents\\GitHub\\openEHR-ADL\\resources\\archetypes"
def loader = ArchetypeManager.getInstance( path )
loader.loadAll()

// Pedir textos usando el ArchetypeManager
assert 'Blood Pressure' == loader.getText('openEHR-EHR-OBSERVATION.blood_pressure.v1', 'en', 'at0000')
assert 'Systolic' == loader.getText('openEHR-EHR-OBSERVATION.blood_pressure.v1', 'en', 'at0004')
assert 'Diastolic' == loader.getText('openEHR-EHR-OBSERVATION.blood_pressure.v1', 'en', 'at0005')
assert 'Cuff size' == loader.getText('openEHR-EHR-OBSERVATION.blood_pressure.v1', 'en', 'at0013')

// Utilizando Achertype
def arch = loader.getArchetype('openEHR-EHR-OBSERVATION.blood_pressure.v1')
assert 'Blood Pressure' == arch.ontology.termDefinition('en', 'at0000')?.text
