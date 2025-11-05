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
}