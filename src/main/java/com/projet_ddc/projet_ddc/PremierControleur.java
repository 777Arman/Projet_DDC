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

    @Autowired
    private CVDataRepository cvDataRepository;

    @Autowired
    private EmailService emailService;  

    @GetMapping("/AccueilRH")
    public String accueilRH(Model model, HttpSession session) {
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
        if (session.getAttribute("entrepriseConnectee") == null) {
            return "redirect:/connexion";
        }

        Candidature c = candidatureRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Candidature introuvable"));
        
        c.setEtat(etatVoulu);
        candidatureRepository.save(c);

        // ========== BLOC AJOUTÉ : Envoi d'email ==========
        if (etatVoulu == 1 || etatVoulu == 2) {
            cvDataRepository.findByCandidatureId(id).ifPresent(cvData -> {
                String email = cvData.getEmail();
                if (email != null && !email.isEmpty()) {
                    if (etatVoulu == 1) {
                        emailService.envoyerEmailAcceptation(email, c.getPoste());
                    } else {
                        emailService.envoyerEmailRefus(email, c.getPoste());
                    }
                } else {
                    System.err.println("⚠️ Pas d'email trouvé pour la candidature " + id);
                }
            });
        }
        // ================================================

        return "redirect:/AccueilRH";
    }
}