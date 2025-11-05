package com.projet_ddc.projet_ddc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void envoyerEmailAcceptation(String destinataire, String poste) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(destinataire);
            message.setSubject("Candidature du poste " + poste);
            message.setText("Accepté");
            
            mailSender.send(message);
            System.out.println("✅ Email d'acceptation envoyé à : " + destinataire);
        } catch (Exception e) {
            System.err.println("❌ Erreur envoi email : " + e.getMessage());
        }
    }

    public void envoyerEmailRefus(String destinataire, String poste) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(destinataire);
            message.setSubject("Candidature du poste " + poste);
            message.setText("Refusé");
            
            mailSender.send(message);
            System.out.println("✅ Email de refus envoyé à : " + destinataire);
        } catch (Exception e) {
            System.err.println("❌ Erreur envoi email : " + e.getMessage());
        }
    }
}