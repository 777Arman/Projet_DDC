package com.projet_ddc.projet_ddc;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;

@Entity
public class Candidature {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String nom;
    private String prenom;
    private String poste;
    private String email;
    private String telephone;
    private int etat = -1; // 0 = en cours, 1 = accepté, 2 = refusé

    private boolean stockage;
    private boolean partage;

    @Lob
    private byte[] cv;

    @Lob
    private byte[] lm;

    @OneToOne(mappedBy = "candidature", cascade = CascadeType.ALL, orphanRemoval = true)
    private CVData cvData;

    public Candidature() { /* A ne pas oublier sinon ça va pas marcher*/
    }

    public Candidature(String nom, String prenom, String poste, int etat, boolean stockage, boolean partage, byte[] cv, byte[] lm) {
        this.nom = nom;
        this.prenom = prenom;
        this.poste = poste;
        this.etat = etat;
        this.stockage = stockage;
        this.partage = partage;
        this.cv = cv;
        this.lm = lm;
    }

    public Long getId(){ 
        return id; 
    }

    public String getNom(){ 
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

    public String getEmail() { 
        return email; 
    }
    public void setEmail(String email) { 
        this.email = email; 
    }

    public String getTelephone() { 
        return telephone; 
    }

    public void setTelephone(String telephone) { 
        this.telephone = telephone; 
    }

    public int getEtat() { 
        return etat; 
    }

    public void setEtat(int etat) { 
        this.etat = etat; 
    }

    public boolean isStockage() { 
        return stockage; 
    }

    public void setStockage(boolean stockage) { 
        this.stockage = stockage; 
    }

    public boolean isPartage() { 
        return partage; 
    }

    public void setPartage(boolean partage) { 
        this.partage = partage; 
    }

    public byte[] getCv() { 
        return cv; 
    }

    public void setCv(byte[] cv) { 
        this.cv = cv;
    }

    public byte[] getLm() { 
        return lm; 
    }

    public void setLm(byte[] lm) { 
        this.lm = lm; 
    }
}