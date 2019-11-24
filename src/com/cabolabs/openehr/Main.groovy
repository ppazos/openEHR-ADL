package com.cabolabs.openehr

import com.cabolabs.openehr.adl.ArchetypeManager
import org.openehr.am.archetype.constraintmodel.CObject
import org.openehr.am.archetype.ontology.*

class Main {

   private static String PS = File.separator

   /*
    * uigen // generador de ui
    * ingen // generador de instancias
    * inval // validador de instancias con XSD
    */
   static void main(String[] args)
   {
      println args
      if (args.size() == 0 || args[0] == 'help')
      {
         println 'usage: adl command [options]'
         println 'command: [csv]'
         println 'csv: generates a CSV file with path information from set of ADL files'
         // println 'ingen: XML instance generation from an OPT'
         // println 'inval: XML instance validator'
         System.exit(0)
      }

      switch (args[0])
      {
         case 'csv':

            if (args.size() < 2)
            {
               println 'usage: adl csv path_to_folder'
               System.exit(-1)
            }

            def fpath = args[1]
            def folder = new File(fpath)

            if (!folder.exists())
            {
               println "folder doesn't exists"
               System.exit(-1)
            }

            def loader = ArchetypeManager.getInstance(fpath)
            loader.loadAll()

            // archetype_id | path | rm_type | aom_type | node_id | name_lang1 | name_lang2 | ...
            def header, rows
            ArchetypeTerm term
            def out

            loader.getLoadedArchetypes().each { archId, archetype ->

               header = 'archetype_id,path,rm_type,aom_type,node_id'
               rows = ''

               archetype.ontology.languages.each { lang ->
                  header += ',name_'+ lang
               }

               header += "\n"

               // archetype.physicalPaths().sort().each { path ->
               //    rows += archId + ',' + path
               // }

               archetype.pathNodeMap.sort{it.key}.each { path, node ->

                  assert node instanceof CObject

                  rows += archId + ',' + path +','+ node.rmTypeName +','+  node.getClass().getSimpleName()

                  if (node.nodeId)
                  {
                     rows += ','+ node.nodeId // to avoid printing "null"

                     archetype.ontology.languages.each { lang ->
                        term = archetype.ontology.termDefinition(lang, node.nodeId)
                        rows += ','+ term.text
                     }
                  }

                  // next row
                  rows += "\n"
               }

               out = new File(fpath + PS + archId +'.csv')
               out << header + rows
            }

            println "Generated "+ loader.getLoadedArchetypes().size() +" CSV files"

         break
         default:
            println "command "+ args[0] +" not recognized"
      }
   }
}
