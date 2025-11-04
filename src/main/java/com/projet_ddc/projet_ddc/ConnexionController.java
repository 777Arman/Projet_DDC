package com.projet_ddc.projet_ddc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

@Controller
public class ConnexionController {

    @Autowired
    private EntrepriseRepository entrepriseRepository;

    @GetMapping("/connexion")
    public String connexion() {
        return "connexion";
    }

    @PostMapping("/connexion")
    public ResponseEntity<String> verifierConnexion(@RequestBody Entreprise entreprise, HttpSession session) {
        Entreprise e = entrepriseRepository.findByEmailAndMotDePasse(
            entreprise.getEmail(), entreprise.getMotDePasse()
        );

        if (e != null) {
            // Stocker les infos dans la session
            session.setAttribute("entrepriseConnectee", e);
            return ResponseEntity.ok("OK");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body("Email ou mot de passe incorrect");
        }
    }

    @GetMapping("/deconnexion")
    public String deconnexion(HttpSession session) {
        session.invalidate();
        return "redirect:/connexion";
    }
}