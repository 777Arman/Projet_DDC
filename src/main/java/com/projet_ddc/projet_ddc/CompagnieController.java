package com.projet_ddc.projet_ddc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/Compagnies")
public class CompagnieController {

    // Liste pour stocker les compagnies en mémoire
    private List<Compagnie> compagnies = new ArrayList<>();

    @Autowired
    private CandidatureRepository candidatureRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private CVDataRepository cvDataRepository;

    @GetMapping
    public String listCompagnies(Model model) {
        model.addAttribute("compagnies", compagnies);
        return "Compagnies"; // Nom du template HTML
    }

    @GetMapping("/partager/{candidatureId}")
    public String partagerCandidature(@PathVariable Long candidatureId, Model model) {
        // Affiche la page permettant de choisir les compagnies du consortium
        model.addAttribute("compagnies", compagnies);
        model.addAttribute("candidatureId", candidatureId);
        return "PartageCompagnies";
    }

    @PostMapping("/envoyer")
    public String envoyerCandidature(@RequestParam Long candidatureId,
                                     @RequestParam(required = false, name = "selected") List<Integer> selectedIndices,
                                     Model model) {
        // Récupérer la candidature
        Optional<Candidature> optCand = candidatureRepository.findById(candidatureId);
        if (optCand.isEmpty()) {
            System.err.println("Candidature introuvable: " + candidatureId);
            return "redirect:/Stockage";
        }

        Candidature candidature = optCand.get();

        if (selectedIndices != null && !selectedIndices.isEmpty()) {
            for (Integer idx : selectedIndices) {
                if (idx >= 0 && idx < compagnies.size()) {
                    Compagnie dest = compagnies.get(idx);
                    try {
                        // Récupérer le JSON extrait (si disponible)
                        String extractedJson = null;
                        try {
                            extractedJson = cvDataRepository.findByCandidatureId(candidatureId)
                                    .map(CVData::getExtractedDataJson)
                                    .orElse(null);
                        } catch (Exception ex) {
                            System.err.println("Impossible de récupérer CVData pour " + candidatureId + " : " + ex.getMessage());
                        }

                        emailService.envoyerCandidatureVersEntreprise(dest.getEmail(), candidature, extractedJson);
                    } catch (Exception e) {
                        System.err.println("Erreur envoi vers " + dest.getEmail() + " : " + e.getMessage());
                    }
                }
            }
        } else {
            System.out.println("Aucune compagnie sélectionnée pour la candidature " + candidatureId);
        }

        // Après envoi, retour à la page Stockage
        return "redirect:/Stockage";
    }

    @PostMapping("/Ajouter")
    public String ajouterCompagnie(@RequestParam String nom,
                                   @RequestParam String email) {
        compagnies.add(new Compagnie(nom, email));
        return "redirect:/Compagnies"; // Retourne à la page liste
    }
}

