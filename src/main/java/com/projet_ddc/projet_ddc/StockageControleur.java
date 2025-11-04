package com.projet_ddc.projet_ddc;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class StockageControleur {
    
    @Autowired
    private CandidatureRepository candidatureRepository;

    @GetMapping("/Stockage")
    public String afficherStockage(Model model) {
        // Récupérer toutes les candidatures
        List<Candidature> toutesLesCandidatures = candidatureRepository.findAll();
        
        // Filtrer les candidatures refusées avec stockage autorisé
        List<Candidature> candidaturesStockees = toutesLesCandidatures.stream()
            .filter(c -> c.getEtat() == 2 && c.isStockage())
            .collect(Collectors.toList());
        
        // Filtrer les candidatures refusées SANS stockage (pour alertes RGPD)
        List<Candidature> candidaturesSansStockage = toutesLesCandidatures.stream()
            .filter(c -> c.getEtat() == 2 && !c.isStockage())
            .collect(Collectors.toList());
        
        // Ajouter les données au modèle
        model.addAttribute("name", "Admin");
        model.addAttribute("liste_Candidats", toutesLesCandidatures);
        model.addAttribute("candidaturesStockees", candidaturesStockees);
        model.addAttribute("candidaturesSansStockage", candidaturesSansStockage);
        
        // Statistiques
        model.addAttribute("totalRefusees", 
            toutesLesCandidatures.stream().filter(c -> c.getEtat() == 2).count());
        model.addAttribute("avecStockage", candidaturesStockees.size());
        model.addAttribute("avecPartage", 
            candidaturesStockees.stream().filter(Candidature::isPartage).count());
        
        return "Stockage";
    }
    
    /**
     * Endpoint pour supprimer une candidature (RGPD)
     * À utiliser pour les candidatures sans autorisation de stockage
     */
    @PostMapping("/Stockage/supprimerCandidature")
    public String supprimerCandidature(@RequestParam Long id) {
    try {
        Candidature candidature = candidatureRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Candidature introuvable"));

        candidatureRepository.deleteById(id);
    } catch (Exception e) {
        System.err.println("Erreur lors de la suppression : " + e.getMessage());
    }

    return "redirect:/Stockage";
    }

    
    /**
     * Endpoint pour exporter les candidatures stockées (avec partage autorisé)
     * Utile pour partager avec d'autres entreprises
     */
    @GetMapping("/Stockage/exporterPartageables")
    public String exporterCandidaturesPartageables(Model model) {
        List<Candidature> candidaturesPartageables = candidatureRepository.findAll().stream()
            .filter(c -> c.getEtat() == 2 && c.isStockage() && c.isPartage())
            .collect(Collectors.toList());
        
        model.addAttribute("candidatures", candidaturesPartageables);
        
        // TODO: Implémenter l'export en CSV ou JSON
        return "redirect:/Stockage";
    }
}