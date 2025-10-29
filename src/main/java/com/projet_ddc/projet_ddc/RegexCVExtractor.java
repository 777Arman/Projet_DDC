package com.projet_ddc.projet_ddc;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

@Service
public class RegexCVExtractor {

    public Map<String, Object> extractCVData(String cvText) {
        Map<String, Object> data = new HashMap<>();
        
        data.put("idCandidat", UUID.randomUUID().toString());
        data.put("nom", extractNom(cvText));
        data.put("prenom", extractPrenom(cvText));
        data.put("mail", extractEmail(cvText));
        data.put("telephone", extractPhone(cvText));
        data.put("diplomes", extractDiplomes(cvText));
        data.put("experiences", extractExperiences(cvText));
        data.put("competences", extractCompetences(cvText));
        data.put("permis", extractPermis(cvText));
        data.put("posteVise", extractPosteVise(cvText));
        data.put("dateDisponibilite", extractDisponibilite(cvText));
        
        return data;
    }

    private String extractEmail(String text) {
        Pattern pattern = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group() : "";
    }

    private String extractPhone(String text) {
        // Formats: 06 12 34 56 78, 06.12.34.56.78, +33 6 12 34 56 78, 0612345678
        Pattern pattern = Pattern.compile("(?:\\+33|0)[1-9](?:[\\s.-]?\\d{2}){4}");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group().replaceAll("[.-]", " ").trim();
        }
        return "";
    }

    private String extractNom(String text) {
        // Chercher ligne avec nom (souvent en MAJUSCULES dans les 10 premières lignes)
        String[] lines = text.split("\\n");
        for (int i = 0; i < Math.min(10, lines.length); i++) {
            String line = lines[i].trim();
            
            // Ligne tout en majuscules avec 2+ mots (ex: JEAN DUPONT)
            if (line.matches("^[A-ZÀÂÄÆÇÉÈÊËÏÎÔŒÙÛÜ\\s'-]{3,50}$") && line.split("\\s+").length >= 2) {
                String[] parts = line.split("\\s+");
                // Le dernier mot est généralement le nom
                return parts[parts.length - 1];
            }
        }
        return "";
    }

    private String extractPrenom(String text) {
        String[] lines = text.split("\\n");
        for (int i = 0; i < Math.min(10, lines.length); i++) {
            String line = lines[i].trim();
            
            // Ligne tout en majuscules avec 2+ mots (ex: JEAN DUPONT)
            if (line.matches("^[A-ZÀÂÄÆÇÉÈÊËÏÎÔŒÙÛÜ\\s'-]{3,50}$") && line.split("\\s+").length >= 2) {
                String[] parts = line.split("\\s+");
                // Le premier mot est généralement le prénom
                return parts[0];
            }
        }
        return "";
    }

    private List<Map<String, String>> extractDiplomes(String text) {
        List<Map<String, String>> diplomes = new ArrayList<>();
        
        // Trouver section formation/diplômes
        Pattern sectionPattern = Pattern.compile(
            "(?i)(formations?|diplômes?|éducation|cursus|parcours\\s+scolaire)\\s*:?\\s*\\n([\\s\\S]{50,2500}?)(?=\\n\\s*\\n\\s*[A-ZÉÈÊ]|expériences?|compétences?|$)",
            Pattern.CASE_INSENSITIVE
        );
        Matcher sectionMatcher = sectionPattern.matcher(text);
        
        if (sectionMatcher.find()) {
            String section = sectionMatcher.group(2);
            
            // Pattern pour extraire : Titre diplôme AVANT l'année
            // On cherche des lignes qui contiennent un diplôme suivi d'une année à 4 chiffres
            String[] lignes = section.split("\\n");
            
            for (int i = 0; i < lignes.length; i++) {
                String ligne = lignes[i].trim();
                
                // Chercher une année (YYYY) dans cette ligne ou la suivante
                Pattern anneePattern = Pattern.compile("(20\\d{2}|19\\d{2})");
                Matcher anneeMatcher = anneePattern.matcher(ligne);
                
                if (anneeMatcher.find()) {
                    String annee = anneeMatcher.group(1);
                    
                    // Le diplôme est soit sur cette ligne, soit sur la ligne précédente
                    String diplome = "";
                    
                    // Vérifier si le diplôme est sur la même ligne (avant l'année)
                    String avantAnnee = ligne.substring(0, ligne.indexOf(annee)).trim();
                    if (avantAnnee.length() > 5 && 
                        !avantAnnee.matches("^(Septembre|Janvier|Février|Mars|Avril|Mai|Juin|Juillet|Août|Octobre|Novembre|Décembre).*")) {
                        diplome = avantAnnee.replaceAll("^[•\\-*–—]\\s*", "").trim();
                    }
                    // Sinon regarder la ligne précédente
                    else if (i > 0) {
                        String lignePrecedente = lignes[i - 1].trim();
                        if (lignePrecedente.length() > 5 &&
                            !lignePrecedente.matches("^(Septembre|Janvier|Février|Mars|Avril|Mai|Juin|Juillet|Août|Octobre|Novembre|Décembre).*")) {
                            diplome = lignePrecedente.replaceAll("^[•\\-*–—]\\s*", "").trim();
                        }
                    }
                    
                    // Vérifier que c'est bien un diplôme (pas une date seule, pas un mois)
                    if (!diplome.isEmpty() && 
                        diplome.length() > 5 &&
                        !diplome.matches("^\\d+.*") &&
                        !diplome.matches("^(Septembre|Janvier|Février|Mars|Avril|Mai|Juin|Juillet|Août|Octobre|Novembre|Décembre).*") &&
                        !diplome.matches(".*Certified.*Architect.*") &&
                        !diplome.matches(".*Certification.*")) {
                        
                        // Utiliser une variable final pour la lambda
                        final String diplomeFinal = diplome;
                        final String anneeFinal = annee;
                        
                        Map<String, String> diplomeMap = new HashMap<>();
                        diplomeMap.put("nomDiplome", diplomeFinal);
                        diplomeMap.put("anneeObtention", anneeFinal);
                        
                        // Éviter les doublons
                        boolean existe = diplomes.stream()
                            .anyMatch(d -> d.get("nomDiplome").equals(diplomeFinal) && d.get("anneeObtention").equals(anneeFinal));
                        
                        if (!existe) {
                            diplomes.add(diplomeMap);
                        }
                    }
                }
            }
        }
        
        return diplomes;
    }

    private List<Map<String, String>> extractExperiences(String text) {
        List<Map<String, String>> experiences = new ArrayList<>();
        
        // Trouver section expériences
        Pattern sectionPattern = Pattern.compile(
            "(?i)(expériences?\\s+professionnelles?|parcours\\s+professionnel|emplois?)\\s*:?\\s*\\n([\\s\\S]{50,3500}?)(?=\\n\\s*\\n\\s*[A-ZÉÈÊ]|formations?|compétences?|$)",
            Pattern.CASE_INSENSITIVE
        );
        Matcher sectionMatcher = sectionPattern.matcher(text);
        
        if (sectionMatcher.find()) {
            String section = sectionMatcher.group(2);
            
            String[] lignes = section.split("\\n");
            
            for (int i = 0; i < lignes.length; i++) {
                String ligne = lignes[i].trim();
                
                // Détecter les lignes de dates (ex: "Septembre 2021 - Présent")
                Pattern datePattern = Pattern.compile("((?:Janvier|Février|Mars|Avril|Mai|Juin|Juillet|Août|Septembre|Octobre|Novembre|Décembre)\\s+\\d{4})\\s*[-–—]\\s*((?:Janvier|Février|Mars|Avril|Mai|Juin|Juillet|Août|Septembre|Octobre|Novembre|Décembre)\\s+\\d{4}|Présent|Aujourd'hui|Actuel)", Pattern.CASE_INSENSITIVE);
                Matcher dateMatcher = datePattern.matcher(ligne);
                
                if (dateMatcher.find()) {
                    String dates = dateMatcher.group();
                    
                    // Le poste est généralement 2 lignes avant
                    // L'entreprise est 1 ligne avant
                    String poste = "";
                    String entreprise = "";
                    
                    if (i >= 2) {
                        poste = lignes[i - 2].trim().replaceAll("^[•\\-*–—]\\s*", "");
                    }
                    if (i >= 1) {
                        entreprise = lignes[i - 1].trim().replaceAll("^[•\\-*–—]\\s*", "");
                    }
                    
                    // Nettoyer l'entreprise (retirer "- Paris" par exemple)
                    if (entreprise.contains(" - ")) {
                        entreprise = entreprise.substring(0, entreprise.indexOf(" - ")).trim();
                    }
                    
                    if (!poste.isEmpty() && !entreprise.isEmpty() &&
                        poste.length() > 3 && entreprise.length() > 3 &&
                        !poste.matches("^\\d{4}$") && !entreprise.matches("^\\d{4}$")) {
                        
                        Map<String, String> exp = new HashMap<>();
                        exp.put("posteOccupe", poste);
                        exp.put("nomEntreprise", entreprise);
                        exp.put("dureeExperience", dates);
                        
                        experiences.add(exp);
                    }
                }
            }
        }
        
        return experiences;
    }

    private List<String> extractCompetences(String text) {
        Set<String> competences = new LinkedHashSet<>();
        
        // Trouver section compétences TECHNIQUES uniquement
        Pattern sectionPattern = Pattern.compile(
            "(?i)(compétences?\\s+techniques?|technologies?|langages?)\\s*:?\\s*\\n([\\s\\S]{20,2000}?)(?=\\n\\s*\\n\\s*(?:compétences?\\s+linguistiques?|langues?|certifications?|projets?|centres?|$))",
            Pattern.CASE_INSENSITIVE
        );
        Matcher sectionMatcher = sectionPattern.matcher(text);
        
        if (sectionMatcher.find()) {
            String section = sectionMatcher.group(2);
            
            // Extraire items séparés par virgules, puces, retours à la ligne
            String[] items = section.split("[,;•\\-–—\\n]+");
            for (String item : items) {
                String comp = item.trim()
                    .replaceAll("^[\\s:]+", "")
                    .replaceAll("[\\s:]+$", "")
                    .replaceAll("^(Langages de programmation|Frameworks et bibliothèques|Bases de données|Outils et technologies|Méthodologies)$", "");
                
                // Filtrer les titres de sections et items trop courts
                if (!comp.isEmpty() && 
                    comp.length() > 2 && 
                    comp.length() < 50 &&
                    !comp.matches("(?i)^(langages?|frameworks?|bases?|outils?|technologies?|méthodologies?).*") &&
                    !comp.matches("^[A-ZÉÈÊ\\s]{5,}$")) { // Pas de texte tout en majuscules
                    
                    competences.add(comp);
                }
            }
        }
        
        // Ajouter les langues séparément
        Pattern languePattern = Pattern.compile(
            "(?i)(langues?|compétences?\\s+linguistiques?)\\s*:?\\s*\\n([\\s\\S]{10,500}?)(?=\\n\\s*\\n|certifications?|projets?|$)",
            Pattern.CASE_INSENSITIVE
        );
        Matcher langueMatcher = languePattern.matcher(text);
        
        if (langueMatcher.find()) {
            String section = langueMatcher.group(2);
            String[] lignes = section.split("\\n");
            
            for (int i = 0; i < lignes.length; i++) {
                String ligne = lignes[i].trim();
                
                // Chercher pattern "Langue" suivi de "Niveau"
                if (ligne.matches("(?i)^(Français|Anglais|Espagnol|Allemand|Italien|Arabe|Chinois|Japonais|Portugais|Russe).*")) {
                    String langue = ligne;
                    // Si la ligne suivante contient le niveau, combiner
                    if (i + 1 < lignes.length) {
                        String ligneSuivante = lignes[i + 1].trim();
                        if (ligneSuivante.matches("(?i).*(maternelle|courant|bilingue|intermédiaire|débutant|notions|[ABC][12]).*")) {
                            langue = langue + " - " + ligneSuivante;
                            i++; // Sauter la ligne suivante
                        }
                    }
                    competences.add(langue);
                }
            }
        }
        
        return new ArrayList<>(competences);
    }

    private String extractPermis(String text) {
        Pattern pattern = Pattern.compile("(?i)permis\\s+([ABCDabcd](?:[\\s,&et]+[ABCDabcd])*)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group().trim() : "Non mentionné";
    }

    private String extractPosteVise(String text) {
        Pattern pattern = Pattern.compile(
            "(?i)(objectif|poste\\s+(?:visé|recherché)|recherche|souhaite)\\s*:?\\s*(.+?)(?=\\n\\n|disponibilité|formations?|$)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String poste = matcher.group(2).trim();
            // Prendre seulement la première phrase
            if (poste.contains(".")) {
                poste = poste.substring(0, poste.indexOf(".")).trim();
            }
            return poste;
        }
        return "";
    }

    private String extractDisponibilite(String text) {
        Pattern pattern = Pattern.compile(
            "(?i)disponib(?:le|ilité)\\s*:?\\s*(.+?)(?=\\n\\n|formations?|$)",
            Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "";
    }

    public boolean isDataComplete(Map<String, Object> data) {
        // Vérifier si les champs critiques sont remplis
        boolean nomOk = !data.get("nom").toString().isEmpty();
        boolean prenomOk = !data.get("prenom").toString().isEmpty();
        boolean mailOk = !data.get("mail").toString().isEmpty();
        boolean telephoneOk = !data.get("telephone").toString().isEmpty();
        
        @SuppressWarnings("unchecked")
        List<Map<String, String>> experiences = (List<Map<String, String>>) data.get("experiences");
        @SuppressWarnings("unchecked")
        List<String> competences = (List<String>) data.get("competences");
        
        boolean experiencesOk = !experiences.isEmpty();
        boolean competencesOk = competences.size() >= 5; // Au moins 5 compétences
        
        return nomOk && prenomOk && mailOk && telephoneOk && experiencesOk && competencesOk;
    }
}