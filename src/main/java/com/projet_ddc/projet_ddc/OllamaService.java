package com.projet_ddc.projet_ddc;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class OllamaService {

    private static final String OLLAMA_API_URL = "http://localhost:11434/api/generate";
    private static final String MODEL = "mistral:latest";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private boolean ollamaAvailable = false;

    public OllamaService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = new ObjectMapper();
        checkOllamaAvailability();
    }
    
    private void checkOllamaAvailability() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:11434/api/tags"))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            ollamaAvailable = (response.statusCode() == 200);
            
            if (ollamaAvailable) {
                System.out.println("✅ Ollama est disponible et sera utilisé pour l'extraction avancée");
            } else {
                System.out.println("⚠️ Ollama n'est pas disponible. Seule l'extraction Regex sera utilisée.");
            }
        } catch (Exception e) {
            ollamaAvailable = false;
            System.out.println("⚠️ Ollama n'est pas disponible. Seule l'extraction Regex sera utilisée.");
        }
    }
    
    public boolean isAvailable() {
        return ollamaAvailable;
    }

    public String summarizeCV(String cvText) throws IOException, InterruptedException {
        if (!ollamaAvailable) {
            throw new IOException("Ollama n'est pas disponible");
        }
        
        String prompt = buildPrompt(cvText);

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", MODEL);
        requestBody.put("prompt", prompt);
        requestBody.put("stream", true);
        requestBody.put("temperature", 0.1);
        requestBody.put("format", "json");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OLLAMA_API_URL))
                .timeout(Duration.ofMinutes(5))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                .build();

        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() == 200) {
            StringBuilder result = new StringBuilder();
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        ObjectNode chunk = (ObjectNode) objectMapper.readTree(line);
                        if (chunk.has("response")) {
                            result.append(chunk.get("response").asText());
                        }
                        System.out.print(".");  // Progression visuelle
                    }
                }
            }
            
            System.out.println(" ✓");
            return addIdIfMissing(result.toString());
        } else {
            throw new IOException("Ollama API error: " + response.statusCode());
        }
    }

    private String buildPrompt(String cvText) {
    return """
Extrait les informations du CV ci-dessous et renvoie UNIQUEMENT du JSON avec les champs suivants :

{
  "idCandidature": "",
  "nom": "",
  "prenom": "",
  "mail": "",
  "telephone": "",
  "diplomes": [
    {
      "nomDiplome": "",
      "anneeObtention": "",
      "domaine": ""
    }
  ],
  "experiences": [
    {
      "nomEntreprise": "",
      "dureeExperience": "",
      "dateDebut": ""
    }
  ],
  "posteVise": "",
  "dateDisponibilite": "",
  "competences": [],
  "softSkills": [],
  "permisDeConduite": [],
  "langues": [
    {
      "langue": "",
      "niveau": ""
    }
  ],
  "dateCandidature": "",
  "fichiers": {
    "cv_filename": "",
    "lm_filename": ""
  }
}

Règles :
- Si une donnée est absente du CV : renvoyer "" ou [] selon le type.
- Aucune phrase, aucune explication. Juste le JSON.
- Toujours un JSON valide.
- Les dates doivent être sous format ISO lorsque possible (AAAA-MM-JJ).
- Ne jamais inventer de diplômes ou entreprises inexistants, seulement ce qui est présent dans le texte.

Maintenant, extrais les informations du CV suivant :

""" + cvText;
}


    private String addIdIfMissing(String jsonString) throws IOException {
        try {
            ObjectNode jsonNode = (ObjectNode) objectMapper.readTree(jsonString);
            
            if (!jsonNode.has("idCandidat") || jsonNode.get("idCandidat").asText().isEmpty() 
                || jsonNode.get("idCandidat").asText().contains("<") || jsonNode.get("idCandidat").asText().equals("UUID")) {
                jsonNode.put("idCandidat", UUID.randomUUID().toString());
            }
            
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
        } catch (Exception e) {
            System.err.println("Erreur parsing JSON: " + e.getMessage());
            System.err.println("Réponse brute: " + jsonString);
            return jsonString;
        }
    }
}