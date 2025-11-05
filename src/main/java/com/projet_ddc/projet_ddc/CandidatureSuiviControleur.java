package com.projet_ddc.projet_ddc;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.servlet.http.HttpSession;

@Controller
public class CandidatureSuiviControleur {

    @Autowired
    private CandidatureRepository candidatureRepository;

    @Autowired
    private CVDataRepository cvDataRepository;

    @GetMapping("/CandidaturesSuivi")
    public String suiviCandidatures(Model model, HttpSession session) {
        // Vérifie si une entreprise est connectée
        if (session.getAttribute("entrepriseConnectee") == null) {
            return "redirect:/connexion";
        }

        // Récupère les candidatures validées (état = 1)
        List<Candidature> candidaturesValidees = candidatureRepository.findByEtat(1);
        model.addAttribute("candidaturesValidees", candidaturesValidees);

        return "CandidaturesSuivi"; 
    }

    @GetMapping("/supprimer/{id}")
    public String supprimerCandidature(@PathVariable("id") Long id, HttpSession session) {
        // Vérifie si une entreprise est connectée
        if (session.getAttribute("entrepriseConnectee") == null) {
            return "redirect:/connexion";
        }

        // Récupère la candidature par son id
        Candidature cand = candidatureRepository.findById(id).orElse(null);
        if (cand != null) {
            cand.setEtat(2); // change l'état à 2
            candidatureRepository.save(cand); // sauvegarde la modification
        }

        // Redirige vers la page de suivi
        return "redirect:/CandidaturesSuivi";
    }

}
