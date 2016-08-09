package com.cabolabs.openehr.ui

import com.cabolabs.openehr.adl.ArchetypeManager
import com.cabolabs.openehr.adl.ArchetypeWalkthrough
import groovy.xml.MarkupBuilder
import org.openehr.am.archetype.constraintmodel.ConstraintRef
import org.openehr.am.openehrprofile.datatypes.text.CCodePhrase
import org.openehr.rm.common.resource.TranslationDetails

class Main {

   private static String PS = File.separator
   private static String path = "."+ PS +"resources"+ PS +"archetypes"
   private static List datavalues = ['DV_TEXT', 'DV_CODED_TEXT', 'DV_QUANTITY', 'DV_COUNT',
                                     'DV_ORDINAL', 'DV_DATE', 'DV_DATE_TIME']

   static void main(String[] args)
   {
      if (args.size() < 3 || args[0] == 'help')
      {
         println 'usage: uigen archetypeId generatorName language'
         println 'generatorName: [walk, archetype]'
         println 'language: [es, en, es-ar, ...]'
         System.exit(0)
      }
      
      def gen = args[1]
      if (!['walk', 'archetype'].contains(gen))
      {
         println 'wrong generator name '+ gen
         println 'usage: uigen archetypeId generatorName'
         println 'generatorName: [walk, archetype]'
         System.exit(0)
      }
      

      def archetypeId = args[0]
      def loader = ArchetypeManager.getInstance(path)
      loader.loadAll()
      def arch
      

      try
      {
         arch = loader.getArchetype( archetypeId )
         println "Loaded: "+ arch.archetypeId.value
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
      
      
      // Verify the archetype support the selected locale
      def locale = args[2]
      def translations = arch.translations // map<string, translationDetails>
      def supportedLangs = translations.keySet() + [arch.originalLanguage.codeString]
      if (!supportedLangs.contains(locale))
      {
         println "The archetype doesn't support the language $locale, should be one of: "+ supportedLangs
         System.exit(0)
      }
      
      
      
      def generator
      switch (gen)
      {
         case 'walk':
           generator = new FormGeneratorWalkthrough()
         break
         case 'archetype':
           generator = new FormGeneratorArchetype()
         break
      }

      
      def f = new File( 'html'+ PS + new java.text.SimpleDateFormat("yyyyMMddhhmmss'.html'").format(new Date()) )
      f << generator.generate(arch, locale)
      println "Generated: "+ f.path
   }
}
