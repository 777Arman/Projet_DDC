package com.projet_ddc.projet_ddc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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