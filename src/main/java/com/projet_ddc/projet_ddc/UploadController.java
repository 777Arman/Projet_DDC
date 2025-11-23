package com.projet_ddc.projet_ddc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
public class UploadController {

    @Autowired
    private CandidatureRepository candidatureRepository;
    
    @Autowired
    private CVDataRepository cvDataRepository;
    
    @Autowired
    private HybridCVService hybridCVService;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/upload")
    public Object uploadToDatabase(
            @RequestParam("cv") MultipartFile cv,
            @RequestParam("lm") MultipartFile lm,
            @RequestParam("poste") String poste,
            @RequestParam("stockage") boolean stockage,
            @RequestParam("partage") boolean partage
    ) {
        try {
            System.out.println("=== Nouvelle candidature reçue ===");
            System.out.println("Poste : " + poste);
            System.out.println("Stockage : " + stockage + ", Partage : " + partage);

            // ÉTAPE 1 : Extraire le texte du CV PDF
            String cvText = "";
            try {
                System.out.println("📄 Extraction du texte du CV...");
                cvText = PDFReader.extractText(cv.getBytes());
                System.out.println("✅ Texte extrait : " + cvText.length() + " caractères");
            } catch (Exception e) {
                System.err.println("⚠️ Erreur extraction PDF : " + e.getMessage());
                cvText = ""; // Continuer même si l'extraction échoue
            }

            // ÉTAPE 2 : Extraire les données structurées du CV (Regex + LLM optionnel)
            String extractedJson = "";
            String nomCandidat = "NomTest";
            String prenomCandidat = "PrenomTest";
            String emailCandidat = "";
            String telephoneCandidat = "";
            String posteViseCandidat = "";
            
            if (!cvText.isEmpty()) {
                try {
                    System.out.println("🤖 Extraction des données du CV...");
                    extractedJson = hybridCVService.extractCV(cvText);
                    System.out.println("✅ Données extraites avec succès");
                    
                    // Parser le JSON pour extraire les infos principales
                    JsonNode jsonNode = objectMapper.readTree(extractedJson);
                    
                    if (jsonNode.has("nom") && !jsonNode.get("nom").asText().isEmpty()) {
                        nomCandidat = jsonNode.get("nom").asText();
                    }
                    if (jsonNode.has("prenom") && !jsonNode.get("prenom").asText().isEmpty()) {
                        prenomCandidat = jsonNode.get("prenom").asText();
                    }
                    if (jsonNode.has("mail")) {
                        emailCandidat = jsonNode.get("mail").asText();
                    }
                    if (jsonNode.has("telephone")) {
                        telephoneCandidat = jsonNode.get("telephone").asText();
                    }
                    if (jsonNode.has("posteVise")) {
                        posteViseCandidat = jsonNode.get("posteVise").asText();
                    }
                    
                    System.out.println(" Candidat identifié : " + prenomCandidat + " " + nomCandidat);
                    
                } catch (Exception e) {
                    System.err.println("⚠️ Erreur extraction données : " + e.getMessage());
                    e.printStackTrace();
                }
            }

            // ÉTAPE 3 : Créer l'objet Candidature
            Candidature candidature = new Candidature();
            candidature.setNom(nomCandidat);
            candidature.setPrenom(prenomCandidat);
            candidature.setPoste(poste);
            candidature.setEtat(-1); // -1 = nouvelle candidature
            candidature.setStockage(stockage);
            candidature.setPartage(partage);

            // ÉTAPE 4 : Sauvegarder les fichiers sur le disque (optionnel)
            Path dirPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(dirPath);
            
            String cvName = "CV_" + System.currentTimeMillis() + ".pdf";
            String lmName = "LM_" + System.currentTimeMillis() + ".pdf";
            
            Path cvPath = dirPath.resolve(cvName);
            Path lmPath = dirPath.resolve(lmName);
            
            Files.copy(cv.getInputStream(), cvPath, StandardCopyOption.REPLACE_EXISTING);
            Files.copy(lm.getInputStream(), lmPath, StandardCopyOption.REPLACE_EXISTING);

            // ÉTAPE 5 : Sauvegarder les bytes dans la BDD
            if (cv != null && !cv.isEmpty()) {
                candidature.setCv(cv.getBytes());
            }

            if (lm != null && !lm.isEmpty()) {
                candidature.setLm(lm.getBytes());
            }

            // ÉTAPE 6 : Sauvegarder la candidature en BDD
            candidature = candidatureRepository.save(candidature);
            System.out.println("Candidature enregistrée avec ID : " + candidature.getId());
            
            // ÉTAPE 7 : Sauvegarder les données extraites du CV dans CVData
            if (!cvText.isEmpty() || !extractedJson.isEmpty()) {
                try {
                    CVData cvData = new CVData();
                    cvData.setCandidature(candidature);
                    cvData.setCvTextContent(cvText);
                    cvData.setExtractedDataJson(extractedJson);
                    cvData.setNom(nomCandidat);
                    cvData.setPrenom(prenomCandidat);
                    cvData.setEmail(emailCandidat);
                    cvData.setTelephone(telephoneCandidat);
                    cvData.setPosteVise(posteViseCandidat);
                    
                    cvDataRepository.save(cvData);
                    System.out.println(" Données CV enregistrées en BDD !");
                } catch (Exception e) {
                    System.err.println("Erreur sauvegarde CVData : " + e.getMessage());
                }
            }
            
                 System.out.println("=== Traitement terminé avec succès ===");

                 // Redirect the browser to the confirmation page (form submit will follow)
                 return "redirect:/ConfirmationCadidatures";

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erreur lors de l'enregistrement : " + e.getMessage());
        }
    }

    @GetMapping("/ConfirmationCadidatures")
    public String confirmationPage() {
        return "ConfirmationCadidatures";
    }
}
