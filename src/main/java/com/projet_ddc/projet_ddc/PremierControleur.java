package com.projet_ddc.projet_ddc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;

@Controller
public class PremierControleur {

    @Autowired
    private CandidatureRepository candidatureRepository;

    @GetMapping("/AccueilRH")
    public String accueilRH(Model model, HttpSession session) {
        // Vérifie si quelqu’un est connecté
        if (session.getAttribute("entrepriseConnectee") == null) {
            return "redirect:/connexion";
        }

        model.addAttribute("liste_Candidats", candidatureRepository.findAll());
        return "AccueilRH";
    }

    @PostMapping("/ChangementEtatCandidature")
    public String changementEtatCandidature(
        @RequestParam Long id,
        @RequestParam int etatVoulu,
        HttpSession session
    ) {
        // Bloque si pas connecté
        if (session.getAttribute("entrepriseConnectee") == null) {
            return "redirect:/connexion";
        }

        Candidature c = candidatureRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Candidature introuvable"));
        c.setEtat(etatVoulu);
        candidatureRepository.save(c);  

        return "redirect:/AccueilRH";
    }
}