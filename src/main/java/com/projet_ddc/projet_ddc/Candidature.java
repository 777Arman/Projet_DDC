package com.projet_ddc.projet_ddc;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Candidature {
    String nom;
    String prenom;
    String poste;
    int etat = -1; // 0 = en cours, 1 = accepté, 2 = refusé
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    public Candidature() { /* A ne pas oublier sinon ça va pas marcher*/
    } 

    public Candidature(String nom, String prenom, String poste, int etat,Long id) {
        this.nom = nom;
        this.prenom = prenom;
        this.poste = poste;
        this.etat = etat;
        this.id = id;
    }
    public Candidature(String nom, String prenom, String poste, int etat) {
        this.nom = nom;
        this.prenom = prenom;
        this.poste = poste;
        this.etat = etat;
    }

    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }
    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }
    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getPoste() {
        return poste;
    }
    public void setPoste(String poste) {
        this.poste = poste;
    }

    public int getEtat() {
        return etat;
    }  
    public void setEtat(int etat) {
        this.etat = etat;
    }
}
