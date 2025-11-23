package com.projet_ddc.projet_ddc;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class HybridCVService {

    @Autowired
    private OllamaService ollamaService;

    @Autowired
    private RegexCVExtractor regexCVExtractor;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String extractCV(String cvText) throws Exception {
        System.out.println("🚀 Démarrage extraction LLM uniquement...");
        // 1) Vérification disponibilité du LLM
        if (ollamaService.isAvailable()) {
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
                System.err.println("➡️ Bascule vers extraction Regex en secours.");
                // Fall through to regex fallback
            }
        } else {
            System.err.println("❌ Ollama / LLM non disponible, utilisation du fallback Regex.");
        }

        // Fallback: use regex-based extractor and return JSON
        try {
            Map<String, Object> data = regexCVExtractor.extractCVData(cvText);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
        } catch (Exception ex) {
            System.err.println("⚠️ Erreur extraction Regex fallback : " + ex.getMessage());
            // As a last resort, return an empty JSON structure expected by the frontend
            return "{\"idCandidat\":\"\",\"nom\":\"\",\"prenom\":\"\",\"mail\":\"\",\"telephone\":\"\"}";
        }
    }
}
