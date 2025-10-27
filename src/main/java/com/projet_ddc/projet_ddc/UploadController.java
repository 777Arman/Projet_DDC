package com.projet_ddc.projet_ddc;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.io.IOException;

@Controller
public class UploadController {

    private static final String UPLOAD_DIR = "uploads";

    @PostMapping("/upload")
    @ResponseBody
    public String handleUpload(
            @RequestParam("cv") MultipartFile cv,
            @RequestParam("lm") MultipartFile lm,
            @RequestParam("poste") String poste,
            @RequestParam("stockage") boolean stockage,
            @RequestParam("partage") boolean partage
    ) {
        try {
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            File cvFile = new File(uploadDir, cv.getOriginalFilename());
            cv.transferTo(cvFile);

            File lmFile = new File(uploadDir, lm.getOriginalFilename());
            lm.transferTo(lmFile);

            System.out.println("CV reçu : " + cvFile.getAbsolutePath());
            System.out.println("LM reçue : " + lmFile.getAbsolutePath());
            System.out.println("Poste : " + poste);
            System.out.println("Stockage : " + stockage);
            System.out.println("Partage : " + partage);

            return "Fichiers reçus avec succès !";

        } catch (IOException e) {
            e.printStackTrace();
            return "Erreur lors de l’envoi : " + e.getMessage();
        }
    }
}