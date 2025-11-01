package com.projet_ddc.projet_ddc;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class PremierControleur {

    @Autowired
    private CandidatureRepository candidatureRepository;

    @GetMapping("/AcceuilRH")
    public String hello(Model model) {
        model.addAttribute("name", "Amine");
        model.addAttribute("liste_Candidats", candidatureRepository.findAll());
        return "AcceuilRH";    
    }

    @PostMapping("/ChangementEtatCandidature")
    public String ChangementEtatCandidature(@RequestParam Long id, @RequestParam int etatVoulu){
        Candidature c = candidatureRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Candidature introuvable"));
        c.setEtat(etatVoulu);
        candidatureRepository.save(c);  
        return "redirect:/AcceuilRH";
    }
    

}


