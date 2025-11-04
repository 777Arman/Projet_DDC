package com.projet_ddc.projet_ddc;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

@Controller
public class CandidaturesEnCoursControleur {

    @Autowired
    private CandidatureRepository candidatureRepository;

    @GetMapping("/CandidaturesEnCours")
    public String afficherCandidaturesEnCours(Model model, HttpSession session) {
        if (session.getAttribute("entrepriseConnectee") == null) {
            return "redirect:/connexion";
        }

        List<Candidature> candidaturesEnCours = candidatureRepository.findAll().stream()
            .filter(c -> c.getEtat() == 0) // Etat = 0 => en traitement
            .collect(Collectors.toList());

        model.addAttribute("candidaturesEnCours", candidaturesEnCours);
        model.addAttribute("totalEnCours", candidaturesEnCours.size());

        return "CandidaturesEnCours";
    }

    @PostMapping("/CandidaturesEnCours/ChangementEtat")
    public String changementEtatCandidature(
        @RequestParam Long id,
        @RequestParam int etatVoulu,
        HttpSession session
    ) {
        if (session.getAttribute("entrepriseConnectee") == null) {
            return "redirect:/connexion";
        }

        Candidature c = candidatureRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Candidature introuvable"));
        c.setEtat(etatVoulu);
        candidatureRepository.save(c);

        return "redirect:/CandidaturesEnCours";
    }
}
