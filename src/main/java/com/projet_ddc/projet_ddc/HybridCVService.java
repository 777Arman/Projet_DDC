package com.projet_ddc.projet_ddc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.*;

@Service
public class HybridCVService {

    @Autowired
    private RegexCVExtractor regexExtractor;

    @Autowired
    private OllamaService ollamaService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String extractCV(String cvText) throws Exception {
        System.out.println("Démarrage extraction hybride...");
        
        // ÉTAPE 1 : Extraction rapide par regex (0.1s)
        System.out.println("Extraction regex...");
        long startRegex = System.currentTimeMillis();
        Map<String, Object> regexData = regexExtractor.extractCVData(cvText);
        long regexTime = System.currentTimeMillis() - startRegex;
        System.out.println("Regex terminée en " + regexTime + "ms");
        
        // ÉTAPE 2 : Vérifier complétude
        boolean isComplete = regexExtractor.isDataComplete(regexData);
        
        // Si Ollama n'est pas disponible ou si les données sont complètes, retourner regex
        if (!ollamaService.isAvailable() || isComplete) {
            if (isComplete) {
                System.out.println("✅ Extraction complète par regex uniquement !");
            } else {
                System.out.println("⚠️ Données partielles (Ollama non disponible)");
            }
            return objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(regexData);
        }
        
        // ÉTAPE 3 : Compléter avec LLM si nécessaire
        System.out.println("⚠️ Données incomplètes, utilisation du LLM...");
        System.out.println("📊 État actuel:");
        System.out.println("   - Nom: " + (regexData.get("nom").toString().isEmpty() ? "❌" : "✓"));
        System.out.println("   - Prénom: " + (regexData.get("prenom").toString().isEmpty() ? "❌" : "✓"));
        System.out.println("   - Email: " + (regexData.get("mail").toString().isEmpty() ? "❌" : "✓"));
        System.out.println("   - Téléphone: " + (regexData.get("telephone").toString().isEmpty() ? "❌" : "✓"));
        
        try {
            long startLLM = System.currentTimeMillis();
            String llmResult = ollamaService.summarizeCV(cvText);
            long llmTime = System.currentTimeMillis() - startLLM;
            System.out.println("✓ LLM terminé en " + llmTime + "ms");
            
            // ÉTAPE 4 : Merger les résultats (regex prioritaire, LLM en fallback)
            Map<String, Object> mergedData = mergeResults(regexData, llmResult);
            
            System.out.println("✅ Extraction hybride terminée ! (Regex: " + regexTime + "ms + LLM: " + llmTime + "ms = Total: " + (regexTime + llmTime) + "ms)");
            
            return objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(mergedData);
        } catch (Exception e) {
            System.err.println("⚠️ Erreur LLM, fallback sur regex: " + e.getMessage());
            return objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(regexData);
        }
    }

    private Map<String, Object> mergeResults(Map<String, Object> regexData, String llmJson) {
        try {
            ObjectNode llmData = (ObjectNode) objectMapper.readTree(llmJson);
            ObjectNode finalData = objectMapper.createObjectNode();
            
            // Merger chaque champ : préférer regex si non vide, sinon LLM
            finalData.put("idCandidat", regexData.get("idCandidat").toString());
            
            finalData.put("nom", 
                !regexData.get("nom").toString().isEmpty() 
                    ? regexData.get("nom").toString() 
                    : llmData.has("nom") ? llmData.get("nom").asText() : "");
            
            finalData.put("prenom", 
                !regexData.get("prenom").toString().isEmpty() 
                    ? regexData.get("prenom").toString() 
                    : llmData.has("prenom") ? llmData.get("prenom").asText() : "");
            
            finalData.put("mail", 
                !regexData.get("mail").toString().isEmpty() 
                    ? regexData.get("mail").toString() 
                    : llmData.has("mail") ? llmData.get("mail").asText() : "");
            
            finalData.put("telephone", 
                !regexData.get("telephone").toString().isEmpty() 
                    ? regexData.get("telephone").toString() 
                    : llmData.has("telephone") ? llmData.get("telephone").asText() : "");
            
            // Diplômes : combiner les deux sources
            @SuppressWarnings("unchecked")
            List<Map<String, String>> regexDiplomes = (List<Map<String, String>>) regexData.get("diplomes");
            ArrayNode diplomesNode = objectMapper.createArrayNode();
            if (!regexDiplomes.isEmpty()) {
                regexDiplomes.forEach(d -> {
                    ObjectNode diplomeNode = objectMapper.createObjectNode();
                    diplomeNode.put("nomDiplome", d.get("nomDiplome"));
                    diplomeNode.put("anneeObtention", d.get("anneeObtention"));
                    diplomesNode.add(diplomeNode);
                });
            } else if (llmData.has("diplomes") && llmData.get("diplomes").isArray()) {
                llmData.get("diplomes").forEach(diplomesNode::add);
            }
            finalData.set("diplomes", diplomesNode);
            
            // Expériences : préférer regex
            @SuppressWarnings("unchecked")
            List<Map<String, String>> regexExp = (List<Map<String, String>>) regexData.get("experiences");
            ArrayNode expNode = objectMapper.createArrayNode();
            if (!regexExp.isEmpty()) {
                regexExp.forEach(e -> {
                    ObjectNode expNodeItem = objectMapper.createObjectNode();
                    expNodeItem.put("nomEntreprise", e.get("nomEntreprise"));
                    expNodeItem.put("dureeExperience", e.get("dureeExperience"));
                    expNodeItem.put("posteOccupe", e.get("posteOccupe"));
                    expNode.add(expNodeItem);
                });
            } else if (llmData.has("experiences") && llmData.get("experiences").isArray()) {
                llmData.get("experiences").forEach(expNode::add);
            }
            finalData.set("experiences", expNode);
            
            // Compétences : combiner
            @SuppressWarnings("unchecked")
            List<String> regexComp = (List<String>) regexData.get("competences");
            ArrayNode compNode = objectMapper.createArrayNode();
            Set<String> allComp = new LinkedHashSet<>(regexComp);
            if (llmData.has("competences") && llmData.get("competences").isArray()) {
                llmData.get("competences").forEach(c -> allComp.add(c.asText()));
            }
            allComp.forEach(compNode::add);
            finalData.set("competences", compNode);
            
            // Autres champs
            finalData.put("posteVise", 
                !regexData.get("posteVise").toString().isEmpty() 
                    ? regexData.get("posteVise").toString() 
                    : llmData.has("posteVise") ? llmData.get("posteVise").asText() : "");
            
            finalData.put("dateDisponibilite", 
                !regexData.get("dateDisponibilite").toString().isEmpty() 
                    ? regexData.get("dateDisponibilite").toString() 
                    : llmData.has("dateDisponibilite") ? llmData.get("dateDisponibilite").asText() : "");
            
            finalData.put("permis", 
                !regexData.get("permis").toString().equals("Non mentionné") 
                    ? regexData.get("permis").toString() 
                    : llmData.has("permis") ? llmData.get("permis").asText() : "Non mentionné");
            
            return objectMapper.convertValue(finalData, Map.class);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur merge : " + e.getMessage());
            return regexData; // Fallback sur regex
        }
    }
}