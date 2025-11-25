package com.projet_ddc.projet_ddc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void envoyerEmailAcceptation(String destinataire, String poste) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(destinataire);
            message.setSubject("Candidature du poste " + poste);
            message.setText("Nous vous remercions pour votre candidature au poste de "+ poste +" au sein de notre entreprise. Après examen attentif de votre profil, nous sommes heureux de vous informer que votre candidature a été retenue.\n" + //
                                "\n" + //
                                "Nous souhaiterions organiser un entretien avec vous dans les prochains jours afin de mieux échanger sur votre expérience et vos compétences. Notre équipe vous contactera prochainement pour convenir d’une date et d’un horaire qui vous conviennent.\n" + //
                                "\n" + //
                                "Nous nous réjouissons de cette opportunité de vous rencontrer et d’échanger avec vous.\n" + //
                                "\n" + //
                                "Cordialement");
            
            mailSender.send(message);
            System.out.println(" Email d'acceptation envoyé à : " + destinataire);
        } catch (Exception e) {
            System.err.println(" Erreur envoi email : " + e.getMessage());
        }
    }

    public void envoyerEmailRefus(String destinataire, String poste) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(destinataire);
            message.setSubject("Candidature du poste " + poste);
            message.setText("Nous vous remercions d’avoir postulé pour le poste de [Intitulé du poste] au sein de notre entreprise. Après un examen attentif de votre candidature, nous regrettons de vous informer que nous n’allons pas donner suite à votre candidature.\n" + 
                                "\n" +
                                "Cette décision n’enlève rien à la qualité de votre profil et nous vous encourageons à postuler à nouveau pour de futures opportunités correspondant à votre expérience et vos compétences.\n" + 
                                "\n" +
                                "Nous vous souhaitons le meilleur dans la poursuite de vos projets professionnels.\n" + //
                                "\n" +  
                                "Cordialement");
            
            mailSender.send(message);
            System.out.println(" Email de refus envoyé à : " + destinataire);
        } catch (Exception e) {
            System.err.println(" Erreur envoi email : " + e.getMessage());
        }
    }

    // Envoi d'une candidature (avec pièces jointes CV/LM si présentes)
    public void envoyerCandidatureVersEntreprise(String destinataire, Candidature candidature) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(destinataire);
            helper.setSubject("Partage d'une candidature - " + candidature.getPoste());

            StringBuilder sb = new StringBuilder();
            sb.append("Bonjour,\n\n");
            sb.append("Nous vous transmettons une candidature pour le poste : ").append(candidature.getPoste()).append("\n\n");
            sb.append("Nom : ").append(candidature.getNom() != null ? candidature.getNom() : "").append("\n");
            sb.append("Prénom : ").append(candidature.getPrenom() != null ? candidature.getPrenom() : "").append("\n");
            sb.append("Email candidat : ").append(candidature.getEmail() != null ? candidature.getEmail() : "Non renseigné").append("\n");
            sb.append("Téléphone : ").append(candidature.getTelephone() != null ? candidature.getTelephone() : "Non renseigné").append("\n\n");
            sb.append("Cordialement,\nLe consortium");

            helper.setText(sb.toString());

            // Attach CV and LM if present
            if (candidature.getCv() != null && candidature.getCv().length > 0) {
                String cvName = "CV_" + (candidature.getPrenom() != null ? candidature.getPrenom() : "") + "_" + (candidature.getNom() != null ? candidature.getNom() : "") + ".pdf";
                helper.addAttachment(cvName, new ByteArrayResource(candidature.getCv()));
            }
            if (candidature.getLm() != null && candidature.getLm().length > 0) {
                String lmName = "LM_" + (candidature.getPrenom() != null ? candidature.getPrenom() : "") + "_" + (candidature.getNom() != null ? candidature.getNom() : "") + ".pdf";
                helper.addAttachment(lmName, new ByteArrayResource(candidature.getLm()));
            }

            mailSender.send(mimeMessage);
            System.out.println(" Email de partage envoyé à : " + destinataire);
        } catch (Exception e) {
            System.err.println(" Erreur envoi email partage : " + e.getMessage());
        }
    }

    // Surcharge: envoie la candidature et joint également le JSON extrait si fourni
    public void envoyerCandidatureVersEntreprise(String destinataire, Candidature candidature, String extractedJson) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(destinataire);
            helper.setSubject("Partage d'une candidature - " + candidature.getPoste());

            StringBuilder sb = new StringBuilder();
            sb.append("Bonjour,\n\n");
            sb.append("Nous vous transmettons une candidature pour le poste : ").append(candidature.getPoste()).append("\n\n");
            sb.append("Nom : ").append(candidature.getNom() != null ? candidature.getNom() : "").append("\n");
            sb.append("Prénom : ").append(candidature.getPrenom() != null ? candidature.getPrenom() : "").append("\n");
            sb.append("Email candidat : ").append(candidature.getEmail() != null ? candidature.getEmail() : "Non renseigné").append("\n");
            sb.append("Téléphone : ").append(candidature.getTelephone() != null ? candidature.getTelephone() : "Non renseigné").append("\n\n");
            sb.append("Cordialement,\nLe consortium");

            helper.setText(sb.toString());

            // Attach CV and LM if present
            if (candidature.getCv() != null && candidature.getCv().length > 0) {
                String cvName = "CV_" + (candidature.getPrenom() != null ? candidature.getPrenom() : "") + "_" + (candidature.getNom() != null ? candidature.getNom() : "") + ".pdf";
                helper.addAttachment(cvName, new ByteArrayResource(candidature.getCv()));
            }
            if (candidature.getLm() != null && candidature.getLm().length > 0) {
                String lmName = "LM_" + (candidature.getPrenom() != null ? candidature.getPrenom() : "") + "_" + (candidature.getNom() != null ? candidature.getNom() : "") + ".pdf";
                helper.addAttachment(lmName, new ByteArrayResource(candidature.getLm()));
            }

            // Attach JSON if provided
            if (extractedJson != null && !extractedJson.isBlank()) {
                String jsonName = "candidature_" + (candidature.getId() != null ? candidature.getId() : "unknown") + ".json";
                helper.addAttachment(jsonName, new ByteArrayResource(extractedJson.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
            }

            mailSender.send(mimeMessage);
            System.out.println(" Email de partage (avec JSON) envoyé à : " + destinataire);
        } catch (Exception e) {
            System.err.println(" Erreur envoi email partage avec JSON : " + e.getMessage());
        }
    }
}