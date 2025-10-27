package com.projet_ddc.projet_ddc;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    //dossier de stockage
    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    // --- endpoint pour l'upload ---
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> handleUpload(
            @RequestParam("poste") String poste,
            @RequestParam("cv") MultipartFile cv,
            @RequestParam("lm") MultipartFile motiv,
            @RequestParam(value = "stockage", required = false) String stockage,
            @RequestParam(value = "partage", required = false) String partage
    ) {

        // Vérifications de base
        if (poste == null || poste.isBlank()) {
            return ResponseEntity.badRequest().body("Veuillez choisir un poste.");
        }

        if (cv == null || cv.isEmpty()) {
            return ResponseEntity.badRequest().body("Veuillez déposer votre CV.");
        }

        if (motiv == null || motiv.isEmpty()) {
            return ResponseEntity.badRequest().body("Veuillez déposer votre lettre de motivation.");
        }
        Path dirPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(dirPath);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Erreur : impossible de créer le dossier de stockage.");
        }

        try {
            // Enregistre les fichiers
            String cvName = "CV_" + System.currentTimeMillis() + ".pdf";
            String motivName = "Motivation_" + System.currentTimeMillis() + ".pdf";

            Path cvPath = dirPath.resolve(cvName);
            Path motivPath = dirPath.resolve(motivName);

            Files.copy(cv.getInputStream(), cvPath, StandardCopyOption.REPLACE_EXISTING);
            Files.copy(motiv.getInputStream(), motivPath, StandardCopyOption.REPLACE_EXISTING);

            // Résumé des consentements
            String stockageInfo = (stockage != null) ? "Oui" : "Non";
            String partageInfo = (partage != null) ? "Oui" : "Non";

            return ResponseEntity.ok(
                    "Candidature reçue ! Poste : " + poste +
                            " | CV : " + cvName +
                            " | Lettre : " + motivName +
                            " | Stockage autorisé : " + stockageInfo +
                            " | Partage autorisé : " + partageInfo
            );

        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Erreur lors de l’enregistrement des fichiers.");
        }
    }
}