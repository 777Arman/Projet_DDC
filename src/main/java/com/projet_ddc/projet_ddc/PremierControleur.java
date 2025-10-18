package com.projet_ddc.projet_ddc;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class PremierControleur {

    @Inject
    private CandidatureRep candRepo;

    @GetMapping("/AcceuilRH")
    public String hello(Model model) {
        model.addAttribute("name", "Amine");
        model.addAttribute("liste_Candidats", candRepo.findAll());
        return "AcceuilRH";    
    }

    @PostMapping("/ChangementEtatCandidature")
    public String ChangementEtatCandidature(@RequestParam Long id, @RequestParam int etatVoulu){
        for (Candidature c : candRepo.findAll()) {
            if (c.getId().equals(id)) {
                c.setEtat(etatVoulu);
                break;
            }
        }
        return "redirect:/AcceuilRH";
    }
    

}


