package com.cabolabs.openehr.adl

class ArchetypeCodes {

   String toArchId
   
   // [
   //  archId->[atNNNN, atNNNN, ...],
   //  archId->[...]
   //  ...
   // ]
   Map archetypesAtCodes = [:] // No se si lo voy a usar
   
   // [
   //  archId1::atNNNN -> archId2::atMMMM
   //  ...
   // ]
   Map atCodesMapping = [:]
   
   //  0 -> at0000
   //  1 -> at0001
   // 15 -> at0015
   int currentAtCount = 0
   
   // acNNNN
   int currentAcCount = 1 // ac empiezan en 1
   
   
   /**
    * El mapeo de codigos se hace contra el arquetipo que resulta del flattening.
    * 
    * @param toArchId
    * @return
    */
   public ArchetypeCodes(String toArchId)
   {
      this.toArchId = toArchId
   }
   
   /**
    * 
    * @return
    */
   public boolean hasFlatArchetypeId()
   {
      return this.toArchId != null
   }
   
   public String transformCode(String archId, String code)
   {
      //println "  transform code: " + archId + " " + code
      
      if (code.startsWith("at")) return transformAtCode(archId, code)
      return transformAcCode(archId, code)
   }
   
   
   private String transformAtCode(String archId, String atCode)
   {
      String toAtCode = "at"
      
      if (currentAtCount < 10) toAtCode += "000" + currentAtCount
      else if (currentAtCount < 100) toAtCode += "00" + currentAtCount
      else if (currentAtCount < 1000) toAtCode += "0" + currentAtCount
      else toAtCode += currentAtCount
      
      this.map(archId, atCode, this.toArchId, toAtCode)
      
      currentAtCount++
      
      return toAtCode
   }
   
   private String transformAcCode(String archId, String acCode)
   {
      String toAcCode = "ac"
      
      if (currentAcCount < 10) toAcCode += "000" + currentAcCount
      else if (currentAcCount < 100) toAcCode += "00" + currentAcCount
      else if (currentAcCount < 1000) toAcCode += "0" + currentAcCount
      else acCode += currentAcCount
      
      this.map(archId, acCode, this.toArchId, toAcCode)
      
      currentAcCount++
      
      return toAcCode
   }
   
   private void map(String archId, String atCode, String toArchId, String toAtCode)
   {
      atCodesMapping[archId+"::"+atCode] = toArchId+"::"+toAtCode
   }
   
   
   /**
    * Devuelve el codigo para la ontologia al que se mapeo un codigo
    * de la definicion del arquetipo (para que la ontologia del plano
    * tenga los mismos codigos que los nodos que su nodeID fue transformado).
    * @param archId
    * @param code
    * @return
    */
   public String getMappedCode(String archId, String code)
   {
      //println "ontologyCode: "+ archId +"::"+ code + " " + atCodesMapping[archId+"::"+code]
      
      def mappedCode = atCodesMapping[archId+"::"+code]
      
      // Que pasa sino esta? El arquetipo esta mal definido.
      //if (!mappedCode) throw new Exception("The archetype $archId doesn't have the code $code in it's definition")
      
      // Sino esta el mapeo es porque el nodo que tiene el code fue removido del arquetipo
      // por ejemplo un slot que es resuelto al arquetipo referenciado.
      if (!mappedCode) return null
      
      return mappedCode.split("::")[1]
   }
   
   /**
    * Inverso a getMappedCode: busca la clave por el valor.
    * @param toArchId
    * @param toCode
    * @return
    */
   public String getKeyCode(String toArchId, String toCode)
   {
      def code
      atCodesMapping.each { key, value ->
         
         if (value == (toArchId +"::"+ toCode))
         {
            code = key.split("::")[1]
            return
         }
      }
      
      return code
   }
   
   /**
    * Como es singleton, necesito reiniciarla cuando termino el flattening de un arquetipo y quiero hacer otro.
    */
   public void reset()
   {
      archetypesAtCodes = [:] // No se si lo voy a usar
      atCodesMapping = [:]

      currentAtCount = 0
      currentAcCount = 0
   }
   
   // TODO: metodo para guardar el mapping a un xml en disco
}