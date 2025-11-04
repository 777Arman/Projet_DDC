package com.projet_ddc.projet_ddc;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/Compagnies")
public class CompagnieController {

    // Liste pour stocker les compagnies en mémoire
    private List<Compagnie> compagnies = new ArrayList<>();

    @GetMapping
    public String listCompagnies(Model model) {
        model.addAttribute("compagnies", compagnies);
        return "Compagnies"; // Nom du template HTML
    }

    @PostMapping("/Ajouter")
    public String ajouterCompagnie(@RequestParam String nom,
                                   @RequestParam String email) {
        compagnies.add(new Compagnie(nom, email));
        return "redirect:/Compagnies"; // Retourne à la page liste
    }
}

