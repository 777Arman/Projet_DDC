package com.projet_ddc.projet_ddc;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ContactCandidatureControleur {

    @GetMapping("/contacter/{id}")
    public String afficherFormulaireContact(@PathVariable("id") Long id, Model model) {
        model.addAttribute("candidatureId", id);
        return "FormulaireContact"; 
    }
}
