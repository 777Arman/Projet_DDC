package com.projet_ddc.projet_ddc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Controller
public class UploadController {

    @Autowired
    private CandidatureRepository candidatureRepository;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @PostMapping("/upload")
    @ResponseBody
    public String uploadToDatabase(
            @RequestParam("cv") MultipartFile cv,
            @RequestParam("lm") MultipartFile lm,
            @RequestParam("poste") String poste,
            @RequestParam("stockage") boolean stockage,
            @RequestParam("partage") boolean partage
    ) {
        try {
            System.out.println("=== Nouvelle candidature reçue ===");
            System.out.println("Poste : " + poste);
            System.out.println("Stockage : " + stockage + ", Partage : " + partage);

            // Créer l'objet Candidature
            Candidature candidature = new Candidature();
            candidature.setNom("NomTest");
            candidature.setPrenom("PrenomTest");
            candidature.setPoste(poste);
            candidature.setEtat(0);
            candidature.setStockage(stockage);
            candidature.setPartage(partage);

            // Sauvegarder les fichiers sur le disque (optionnel)
            Path dirPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(dirPath);
            
            String cvName = "CV_" + System.currentTimeMillis() + ".pdf";
            String lmName = "LM_" + System.currentTimeMillis() + ".pdf";
            
            Path cvPath = dirPath.resolve(cvName);
            Path lmPath = dirPath.resolve(lmName);
            
            Files.copy(cv.getInputStream(), cvPath, StandardCopyOption.REPLACE_EXISTING);
            Files.copy(lm.getInputStream(), lmPath, StandardCopyOption.REPLACE_EXISTING);

            // Sauvegarder les bytes dans la BDD
            if (cv != null && !cv.isEmpty()) {
                candidature.setCv(cv.getBytes());
            }

            if (lm != null && !lm.isEmpty()) {
                candidature.setLm(lm.getBytes());
            }

            // Sauvegarder en BDD
            candidatureRepository.save(candidature);

            System.out.println("Candidature enregistrée avec succès en BDD !");
            
            return "Candidature enregistrée ! Poste : " + poste + 
                   " | Fichiers sauvegardés : " + cvName + ", " + lmName;

        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur lors de l'enregistrement : " + e.getMessage();
        }
    }
}