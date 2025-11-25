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
            // Prefer CVData email, fallback to Candidature.email
            String emailToUse = null;
            try {
                emailToUse = cvDataRepository.findByCandidatureId(id)
                        .map(CVData::getEmail)
                        .orElse(null);
            } catch (Exception ex) {
                System.err.println("Erreur récupération CVData pour " + id + " : " + ex.getMessage());
            }

            if (emailToUse == null || emailToUse.isEmpty()) {
                // use the email stored on the candidature as fallback
                emailToUse = c.getEmail();
            }

            if (emailToUse != null && !emailToUse.isEmpty()) {
                if (etatVoulu == 1) {
                    System.out.println("Envoi email acceptation à : " + emailToUse);
                    emailService.envoyerEmailAcceptation(emailToUse, c.getPoste());
                } else {
                    System.out.println("Envoi email refus à : " + emailToUse);
                    emailService.envoyerEmailRefus(emailToUse, c.getPoste());
                }
            } else {
                System.err.println("⚠️ Pas d'email trouvé pour la candidature " + id + " (ni CVData ni Candidature)");
            }
        }
        // ================================================

        return "redirect:/AccueilRH";
    }
}