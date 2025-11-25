package com.projet_ddc.projet_ddc;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ContactCandidatureControleur {

    @Autowired
    private CandidatureRepository candidatureRepository;

    @Autowired
    private EmailService emailService;

    @GetMapping("/contacter/{id}")
    public String afficherFormulaireContact(@PathVariable("id") Long id, Model model) {
        model.addAttribute("candidatureId", id);
        return "FormulaireContact"; 
    }

    @PostMapping("/envoyerEmail")
    public String envoyerEmailAuCandidat(@RequestParam Long candidatureId,
                                         @RequestParam String objet,
                                         @RequestParam String contenu) {
        Optional<Candidature> opt = candidatureRepository.findById(candidatureId);
        if (opt.isEmpty()) {
            System.err.println("Candidature introuvable pour envoi de mail: " + candidatureId);
            return "redirect:/CandidaturesSuivi";
        }

        Candidature cand = opt.get();
        String destinataire = cand.getEmail();
        if (destinataire == null || destinataire.isBlank()) {
            System.err.println("Email du candidat manquant pour candidature: " + candidatureId);
            return "redirect:/CandidaturesSuivi";
        }

        try {
            // Utiliser JavaMail pour envoyer un message simple personnalisé
            emailService.envoyerEmailPersonnalise(destinataire, objet, contenu);
            System.out.println("Email envoyé au candidat " + destinataire);
        } catch (Exception e) {
            System.err.println("Erreur envoi email au candidat: " + e.getMessage());
        }

        return "redirect:/CandidaturesSuivi";
    }
}
