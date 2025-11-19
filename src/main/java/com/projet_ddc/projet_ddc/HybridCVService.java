package com.projet_ddc.projet_ddc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class HybridCVService {

    @Autowired
    private OllamaService ollamaService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String extractCV(String cvText) throws Exception {
        System.out.println("🚀 Démarrage extraction LLM uniquement...");

        // 1) Vérification disponibilité du LLM
        if (!ollamaService.isAvailable()) {
            System.err.println("❌ Erreur : Ollama / LLM non disponible !");
            throw new RuntimeException("Ollama indisponible : impossible d'extraire le CV");
        }

        // 2) Appel au LLM
        try {
            long start = System.currentTimeMillis();
            System.out.println("🧠 Extraction en cours via Ollama...");
            
            String llmResult = ollamaService.summarizeCV(cvText);
            
            long totalTime = System.currentTimeMillis() - start;
            System.out.println("📌 Extraction LLM terminée en " + totalTime + "ms");
            
            // Format JSON (pretty print)
            return objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(objectMapper.readTree(llmResult));

        } catch (Exception e) {
            System.err.println("⚠️ Erreur extraction LLM : " + e.getMessage());
            throw new RuntimeException("Erreur pendant l'extraction du CV via Ollama");
        }
    }
}
