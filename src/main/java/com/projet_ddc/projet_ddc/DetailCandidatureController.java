package com.projet_ddc.projet_ddc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;

@Controller
public class DetailCandidatureController {

    @Autowired
    private CandidatureRepository candidatureRepository;

    @Autowired
    private CVDataRepository cvDataRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/candidature/{id}")
    public String detailCandidature(@PathVariable Long id, Model model) {
        try {
            // Récupérer la candidature
            Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidature introuvable avec l'ID : " + id));
            
            // Récupérer les données extraites du CV
            CVData cvData = cvDataRepository.findByCandidatureId(id).orElse(null);
            
            // Ajouter les données de base au modèle
            model.addAttribute("candidature", candidature);
            model.addAttribute("cvData", cvData);
            
            // Parser le JSON et préparer les listes pour l'affichage
            if (cvData != null && cvData.getExtractedDataJson() != null && !cvData.getExtractedDataJson().isEmpty()) {
                try {
                    JsonNode jsonData = objectMapper.readTree(cvData.getExtractedDataJson());
                    
                    // Extraire les diplômes
                    List<Diplome> diplomes = new ArrayList<>();
                    if (jsonData.has("diplomes") && jsonData.get("diplomes").isArray()) {
                        jsonData.get("diplomes").forEach(d -> {
                            diplomes.add(new Diplome(
                                d.has("nomDiplome") ? d.get("nomDiplome").asText() : "",
                                d.has("anneeObtention") ? d.get("anneeObtention").asText() : ""
                            ));
                        });
                    }
                    model.addAttribute("diplomes", diplomes);
                    
                    // Extraire les expériences
                    List<Experience> experiences = new ArrayList<>();
                    if (jsonData.has("experiences") && jsonData.get("experiences").isArray()) {
                        jsonData.get("experiences").forEach(e -> {
                            experiences.add(new Experience(
                                e.has("posteOccupe") ? e.get("posteOccupe").asText() : "",
                                e.has("nomEntreprise") ? e.get("nomEntreprise").asText() : "",
                                e.has("dureeExperience") ? e.get("dureeExperience").asText() : ""
                            ));
                        });
                    }
                    model.addAttribute("experiences", experiences);
                    
                    // Extraire les compétences
                    List<String> competences = new ArrayList<>();
                    if (jsonData.has("competences") && jsonData.get("competences").isArray()) {
                        jsonData.get("competences").forEach(c -> {
                            competences.add(c.asText());
                        });
                    }
                    model.addAttribute("competences", competences);
                    
                    // Extraire le permis
                    String permis = jsonData.has("permis") ? jsonData.get("permis").asText() : "Non mentionné";
                    model.addAttribute("permis", permis);
                    
                } catch (Exception e) {
                    System.err.println("Erreur parsing JSON: " + e.getMessage());
                    e.printStackTrace();
                    model.addAttribute("diplomes", new ArrayList<>());
                    model.addAttribute("experiences", new ArrayList<>());
                    model.addAttribute("competences", new ArrayList<>());
                    model.addAttribute("permis", "Non mentionné");
                }
            } else {
                model.addAttribute("diplomes", new ArrayList<>());
                model.addAttribute("experiences", new ArrayList<>());
                model.addAttribute("competences", new ArrayList<>());
                model.addAttribute("permis", "Non mentionné");
            }
            
            return "detailCandidature";
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'affichage des détails: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Erreur lors du chargement des détails de la candidature");
            return "error";
        }
    }
    
    @GetMapping("/candidature/{id}/download/cv")
    public ResponseEntity<Resource> downloadCV(@PathVariable Long id) {
        try {
            Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidature introuvable"));
            
            byte[] cv = candidature.getCv();
            if (cv == null || cv.length == 0) {
                return ResponseEntity.notFound().build();
            }
            
            ByteArrayResource resource = new ByteArrayResource(cv);
            
            String filename = "CV_" + candidature.getPrenom() + "_" + candidature.getNom() + ".pdf";
            
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
                
        } catch (Exception e) {
            System.err.println("Erreur téléchargement CV: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/candidature/{id}/download/lm")
    public ResponseEntity<Resource> downloadLM(@PathVariable Long id) {
        try {
            Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidature introuvable"));
            
            byte[] lm = candidature.getLm();
            if (lm == null || lm.length == 0) {
                return ResponseEntity.notFound().build();
            }
            
            ByteArrayResource resource = new ByteArrayResource(lm);
            
            String filename = "LM_" + candidature.getPrenom() + "_" + candidature.getNom() + ".pdf";
            
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
                
        } catch (Exception e) {
            System.err.println("Erreur téléchargement LM: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    @GetMapping("/candidature/{id}/export/json")
    public ResponseEntity<String> exportJSON(@PathVariable Long id) {
        try {
            // Récupérer la candidature
            Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidature introuvable"));
            
            // Récupérer les données extraites du CV
            CVData cvData = cvDataRepository.findByCandidatureId(id).orElse(null);
            
            // Créer l'objet JSON selon le schéma
            ObjectNode jsonExport = objectMapper.createObjectNode();
            
            // Informations de base
            jsonExport.put("idCandidature", candidature.getId().toString());
            jsonExport.put("nom", candidature.getNom() != null ? candidature.getNom() : "");
            jsonExport.put("prenom", candidature.getPrenom() != null ? candidature.getPrenom() : "");
            
            // Informations extraites du CV
            if (cvData != null) {
                jsonExport.put("mail", cvData.getEmail() != null ? cvData.getEmail() : "");
                jsonExport.put("telephone", cvData.getTelephone() != null ? cvData.getTelephone() : "");
                jsonExport.put("posteVise", cvData.getPosteVise() != null ? cvData.getPosteVise() : candidature.getPoste());
            } else {
                jsonExport.put("mail", "");
                jsonExport.put("telephone", "");
                jsonExport.put("posteVise", candidature.getPoste());
            }
            
            // Diplômes
            ArrayNode diplomesArray = objectMapper.createArrayNode();
            if (cvData != null && cvData.getExtractedDataJson() != null && !cvData.getExtractedDataJson().isEmpty()) {
                try {
                    JsonNode extractedData = objectMapper.readTree(cvData.getExtractedDataJson());
                    if (extractedData.has("diplomes") && extractedData.get("diplomes").isArray()) {
                        extractedData.get("diplomes").forEach(d -> {
                            ObjectNode diplome = objectMapper.createObjectNode();
                            diplome.put("nomDiplome", d.has("nomDiplome") ? d.get("nomDiplome").asText() : "");
                            diplome.put("anneeObtention", d.has("anneeObtention") ? d.get("anneeObtention").asText() : "");
                            diplome.put("domaine", ""); // Non extrait pour le moment
                            diplomesArray.add(diplome);
                        });
                    }
                } catch (Exception e) {
                    System.err.println("Erreur extraction diplômes: " + e.getMessage());
                }
            }
            jsonExport.set("diplomes", diplomesArray);
            
            // Expériences
            ArrayNode experiencesArray = objectMapper.createArrayNode();
            if (cvData != null && cvData.getExtractedDataJson() != null && !cvData.getExtractedDataJson().isEmpty()) {
                try {
                    JsonNode extractedData = objectMapper.readTree(cvData.getExtractedDataJson());
                    if (extractedData.has("experiences") && extractedData.get("experiences").isArray()) {
                        extractedData.get("experiences").forEach(e -> {
                            ObjectNode experience = objectMapper.createObjectNode();
                            experience.put("nomEntreprise", e.has("nomEntreprise") ? e.get("nomEntreprise").asText() : "");
                            experience.put("dureeExperience", e.has("dureeExperience") ? e.get("dureeExperience").asText() : "");
                            experience.put("posteOccupe", e.has("posteOccupe") ? e.get("posteOccupe").asText() : "");
                            experience.put("dateDebut", ""); // Non extrait pour le moment
                            experiencesArray.add(experience);
                        });
                    }
                } catch (Exception e) {
                    System.err.println("Erreur extraction expériences: " + e.getMessage());
                }
            }
            jsonExport.set("experiences", experiencesArray);
            
            // Compétences
            ArrayNode competencesArray = objectMapper.createArrayNode();
            if (cvData != null && cvData.getExtractedDataJson() != null && !cvData.getExtractedDataJson().isEmpty()) {
                try {
                    JsonNode extractedData = objectMapper.readTree(cvData.getExtractedDataJson());
                    if (extractedData.has("competences") && extractedData.get("competences").isArray()) {
                        extractedData.get("competences").forEach(competencesArray::add);
                    }
                } catch (Exception e) {
                    System.err.println("Erreur extraction compétences: " + e.getMessage());
                }
            }
            jsonExport.set("competences", competencesArray);
            
            // Autres champs
            jsonExport.put("dateDisponibilite", "");
            jsonExport.set("softSkills", objectMapper.createArrayNode());
            
            // Permis
            ArrayNode permisArray = objectMapper.createArrayNode();
            if (cvData != null && cvData.getExtractedDataJson() != null && !cvData.getExtractedDataJson().isEmpty()) {
                try {
                    JsonNode extractedData = objectMapper.readTree(cvData.getExtractedDataJson());
                    if (extractedData.has("permis")) {
                        String permis = extractedData.get("permis").asText();
                        if (permis != null && !permis.equals("Non mentionné")) {
                            // Extraire les lettres du permis (ex: "Permis B" -> ["B"])
                            if (permis.contains("B")) permisArray.add("B");
                            if (permis.contains("A")) permisArray.add("A");
                            if (permis.contains("C")) permisArray.add("C");
                            if (permis.contains("D")) permisArray.add("D");
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Erreur extraction permis: " + e.getMessage());
                }
            }
            jsonExport.set("permisDeConduite", permisArray);
            
            // Langues
            jsonExport.set("langues", objectMapper.createArrayNode());
            
            // Date de candidature
            jsonExport.put("dateCandidature", java.time.LocalDate.now().toString());
            
            // Fichiers
            ObjectNode fichiers = objectMapper.createObjectNode();
            fichiers.put("cv_filename", "CV_" + candidature.getPrenom() + "_" + candidature.getNom() + ".pdf");
            fichiers.put("lm_filename", "LM_" + candidature.getPrenom() + "_" + candidature.getNom() + ".pdf");
            jsonExport.set("fichiers", fichiers);
            
            // Autorisations
            jsonExport.put("autorisationStockage", candidature.isStockage() ? "O" : "N");
            jsonExport.put("autorisationPartage", candidature.isPartage() ? "O" : "N");
            
            // Générer le JSON formaté
            String jsonString = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(jsonExport);
            
            // Créer le nom du fichier
            String filename = "candidature_" + candidature.getId() + "_" + 
                            candidature.getPrenom() + "_" + candidature.getNom() + ".json";
            
            // Retourner le JSON en téléchargement
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(jsonString);
                
        } catch (Exception e) {
            System.err.println("Erreur export JSON: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\": \"Erreur lors de l'export JSON\"}");
        }
    }
    
    // Classes internes pour faciliter l'affichage dans Thymeleaf
    public static class Diplome {
        private String nomDiplome;
        private String anneeObtention;
        
        public Diplome(String nomDiplome, String anneeObtention) {
            this.nomDiplome = nomDiplome;
            this.anneeObtention = anneeObtention;
        }
        
        public String getNomDiplome() { return nomDiplome; }
        public String getAnneeObtention() { return anneeObtention; }
    }
    
    public static class Experience {
        private String posteOccupe;
        private String nomEntreprise;
        private String dureeExperience;
        
        public Experience(String posteOccupe, String nomEntreprise, String dureeExperience) {
            this.posteOccupe = posteOccupe;
            this.nomEntreprise = nomEntreprise;
            this.dureeExperience = dureeExperience;
        }
        
        public String getPosteOccupe() { return posteOccupe; }
        public String getNomEntreprise() { return nomEntreprise; }
        public String getDureeExperience() { return dureeExperience; }
    }
}