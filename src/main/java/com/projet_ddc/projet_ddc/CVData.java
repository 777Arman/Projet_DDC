package com.projet_ddc.projet_ddc;

import jakarta.persistence.*;

@Entity
@Table(name = "cv_data")
public class CVData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "candidature_id", unique = true)
    private Candidature candidature;
    
    @Column(columnDefinition = "TEXT")
    private String cvTextContent; // Texte extrait du PDF
    
    @Column(columnDefinition = "TEXT")
    private String extractedDataJson; // Données JSON extraites
    
    @Column(length = 255)
    private String nom;
    
    @Column(length = 255)
    private String prenom;
    
    @Column(length = 255)
    private String email;
    
    @Column(length = 50)
    private String telephone;
    
    @Column(length = 500)
    private String posteVise;
    
    public CVData() {
    }
    
    public CVData(Candidature candidature, String cvTextContent, String extractedDataJson) {
        this.candidature = candidature;
        this.cvTextContent = cvTextContent;
        this.extractedDataJson = extractedDataJson;
    }
    
    // Getters et Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Candidature getCandidature() {
        return candidature;
    }
    
    public void setCandidature(Candidature candidature) {
        this.candidature = candidature;
    }
    
    public String getCvTextContent() {
        return cvTextContent;
    }
    
    public void setCvTextContent(String cvTextContent) {
        this.cvTextContent = cvTextContent;
    }
    
    public String getExtractedDataJson() {
        return extractedDataJson;
    }
    
    public void setExtractedDataJson(String extractedDataJson) {
        this.extractedDataJson = extractedDataJson;
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
    
    public String getPosteVise() {
        return posteVise;
    }
    
    public void setPosteVise(String posteVise) {
        this.posteVise = posteVise;
    }
}