package com.projet_ddc.projet_ddc;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

@Service
public class RegexCVExtractor {

    public Map<String, Object> extractCVData(String cvText) {
        Map<String, Object> data = new HashMap<>();
        
        // Nettoyage du texte pour une meilleure extraction
        String cleanedText = cvText.replaceAll("\\s+", " ").trim();
        
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
        // Pattern plus robuste pour les emails
        Pattern pattern = Pattern.compile("[a-zA-Z0-9][a-zA-Z0-9._+-]*@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group().trim() : "";
    }

    private String extractPhone(String text) {
        // Formats variés: +33 7 69 81 57 43, 06 12 34 56 78, 0612345678
        Pattern pattern = Pattern.compile("(?:\\+33|0)\\s*[1-9](?:[\\s.-]?\\d{2}){4}");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group().replaceAll("[.-]", " ").trim();
        }
        return "";
    }

    private String extractNom(String text) {
        String[] lines = text.split("\\n");
        
        // Stratégie 1: Chercher deux lignes consécutives en MAJUSCULES (format CV moderne)
        for (int i = 0; i < Math.min(15, lines.length - 1); i++) {
            String line1 = lines[i].trim();
            String line2 = lines[i + 1].trim();
            
            // Nettoyer line2 si elle contient un numéro de téléphone collé
            String line2Clean = line2.replaceAll("\\+?\\d[\\d\\s]{8,}", "").trim();
            
            // Ignorer les lignes avec email
            if (line1.contains("@") || line2.contains("@")) continue;
            
            // Si ligne1 est en MAJUSCULES
            if (line1.matches("^[A-ZÀÂÄÆÇÉÈÊËÏÎÔŒÙÛÜ\\s'-]{2,30}$")) {
                // Si ligne2 (nettoyée) est aussi en MAJUSCULES
                if (!line2Clean.isEmpty() && line2Clean.matches("^[A-ZÀÂÄÆÇÉÈÊËÏÎÔŒÙÛÜ\\s'-]{2,30}$")) {
                    // Ligne 2 = NOM (convention CV moderne)
                    return line2Clean;
                }
            }
        }
        
        // Stratégie 2: Chercher une seule ligne avec prénom + nom
        for (int i = 0; i < Math.min(15, lines.length); i++) {
            String line = lines[i].trim();
            
            // Ignorer les lignes problématiques
            if (line.contains("@") || line.matches(".*\\+?\\d{2}.*\\d{2}.*\\d{2}.*")) continue;
            
            // Ligne tout en majuscules avec 2+ mots (ex: JEAN DUPONT)
            if (line.matches("^[A-ZÀÂÄÆÇÉÈÊËÏÎÔŒÙÛÜ\\s'-]{3,50}$") && 
                line.split("\\s+").length >= 2) {
                String[] parts = line.split("\\s+");
                // Le dernier mot est généralement le nom
                return parts[parts.length - 1];
            }
        }
        
        return "";
    }

    private String extractPrenom(String text) {
        String[] lines = text.split("\\n");
        
        // Stratégie 1: Chercher deux lignes consécutives en MAJUSCULES (format CV moderne)
        for (int i = 0; i < Math.min(15, lines.length - 1); i++) {
            String line1 = lines[i].trim();
            String line2 = lines[i + 1].trim();
            
            // Nettoyer line2 si elle contient un numéro de téléphone collé
            String line2Clean = line2.replaceAll("\\+?\\d[\\d\\s]{8,}", "").trim();
            
            // Ignorer les lignes avec email
            if (line1.contains("@") || line2.contains("@")) continue;
            
            // Si ligne1 est en MAJUSCULES
            if (line1.matches("^[A-ZÀÂÄÆÇÉÈÊËÏÎÔŒÙÛÜ\\s'-]{2,30}$")) {
                // Si ligne2 (nettoyée) est aussi en MAJUSCULES
                if (!line2Clean.isEmpty() && line2Clean.matches("^[A-ZÀÂÄÆÇÉÈÊËÏÎÔŒÙÛÜ\\s'-]{2,30}$")) {
                    // Ligne 1 = PRÉNOM (convention CV moderne)
                    return line1;
                }
            }
        }
        
        // Stratégie 2: Chercher une seule ligne avec prénom + nom
        for (int i = 0; i < Math.min(15, lines.length); i++) {
            String line = lines[i].trim();
            
            // Ignorer les lignes problématiques
            if (line.contains("@") || line.matches(".*\\+?\\d{2}.*\\d{2}.*\\d{2}.*")) continue;
            
            // Ligne tout en majuscules avec 2+ mots (ex: JEAN DUPONT)
            if (line.matches("^[A-ZÀÂÄÆÇÉÈÊËÏÎÔŒÙÛÜ\\s'-]{3,50}$") && 
                line.split("\\s+").length >= 2) {
                String[] parts = line.split("\\s+");
                // Le premier mot est généralement le prénom
                return parts[0];
            }
        }
        
        return "";
    }

    private List<Map<String, String>> extractDiplomes(String text) {
        List<Map<String, String>> diplomes = new ArrayList<>();
        Set<String> diplomesVus = new HashSet<>();
        
        // Patterns pour les diplômes courants
        String[] diplomeKeywords = {
            "licence", "master", "doctorat", "bac", "baccalauréat", "bts", "dut", 
            "bachelor", "mba", "ingénieur", "école", "université", "diplôme"
        };
        
        String[] lines = text.split("\\n");
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim().toLowerCase();
            
            // Chercher les lignes contenant un mot-clé de diplôme
            boolean hasDiplomeKeyword = false;
            for (String keyword : diplomeKeywords) {
                if (line.contains(keyword)) {
                    hasDiplomeKeyword = true;
                    break;
                }
            }
            
            if (hasDiplomeKeyword) {
                // Chercher une année (format YYYY ou YYYY - YYYY)
                Pattern anneePattern = Pattern.compile("(20\\d{2}|19\\d{2})(?:\\s*-\\s*(20\\d{2}|19\\d{2}))?");
                
                // Chercher dans cette ligne ou les 2 lignes suivantes
                for (int j = i; j < Math.min(i + 3, lines.length); j++) {
                    Matcher anneeMatcher = anneePattern.matcher(lines[j]);
                    
                    if (anneeMatcher.find()) {
                        String anneeComplete = anneeMatcher.group();
                        String anneeObtention = anneeMatcher.group(2) != null ? anneeMatcher.group(2) : anneeMatcher.group(1);
                        
                        // Construire le nom du diplôme (prendre la ligne avec le keyword)
                        String nomDiplome = lines[i].trim()
                            .replaceAll("^[•\\-*–—]\\s*", "")
                            .replaceAll("\\d{4}\\s*-?\\s*\\d{0,4}", "") // Retirer les années
                            .trim();
                        
                        // Nettoyer le nom du diplôme
                        if (!nomDiplome.isEmpty() && nomDiplome.length() > 3) {
                            String key = nomDiplome + anneeObtention;
                            if (!diplomesVus.contains(key)) {
                                Map<String, String> diplome = new HashMap<>();
                                diplome.put("nomDiplome", nomDiplome);
                                diplome.put("anneeObtention", anneeObtention);
                                diplomes.add(diplome);
                                diplomesVus.add(key);
                            }
                        }
                        
                        break; // Passer au diplôme suivant
                    }
                }
            }
        }
        
        return diplomes;
    }

    private List<Map<String, String>> extractExperiences(String text) {
        List<Map<String, String>> experiences = new ArrayList<>();
        
        // Pour ce CV d'étudiant, on cherche les projets plutôt que les expériences pro
        Pattern projetPattern = Pattern.compile(
            "(?i)(projet[s]?|création|développement|conception|réalisation).*?(?:en|avec)\\s+([A-Z][a-zA-Z/+#]+)",
            Pattern.CASE_INSENSITIVE
        );
        
        String[] lines = text.split("\\n");
        Set<String> projetsVus = new HashSet<>();
        
        for (String line : lines) {
            line = line.trim();
            
            // Détecter les projets techniques
            if (line.length() > 15 && line.length() < 150) {
                Matcher m = projetPattern.matcher(line);
                
                if (m.find() || 
                    line.matches("(?i).*\\b(jeu|site|application|algorithme|compilateur|base de données)\\b.*")) {
                    
                    // Extraire les technologies mentionnées
                    Pattern techPattern = Pattern.compile("\\b(Java|C|Python|PHP|HTML|CSS|JavaScript|MySQL|SQL)\\b");
                    Matcher techMatcher = techPattern.matcher(line);
                    StringBuilder techs = new StringBuilder();
                    while (techMatcher.find()) {
                        if (techs.length() > 0) techs.append(", ");
                        techs.append(techMatcher.group());
                    }
                    
                    String projet = line.replaceAll("^[•\\-*–—]\\s*", "").trim();
                    
                    if (!projetsVus.contains(projet) && projet.length() > 10) {
                        Map<String, String> exp = new HashMap<>();
                        exp.put("posteOccupe", "Projet étudiant");
                        exp.put("nomEntreprise", projet);
                        exp.put("dureeExperience", techs.length() > 0 ? "Technologies: " + techs : "");
                        experiences.add(exp);
                        projetsVus.add(projet);
                    }
                }
            }
        }
        
        return experiences;
    }

    private List<String> extractCompetences(String text) {
        Set<String> competences = new LinkedHashSet<>();
        
        // Liste étendue de compétences techniques
        String[] techCompetences = {
            "Java", "HTML", "CSS", "JavaScript", "Python", "PHP", "C", "C\\+\\+", "SQL", "MySQL",
            "React", "Angular", "Vue", "Node\\.js", "Spring", "Django", "Laravel",
            "Git", "Docker", "Kubernetes", "AWS", "Azure", "Linux", "MongoDB",
            "TypeScript", "Ruby", "Go", "Rust", "Swift", "Kotlin", "Scala"
        };
        
        // Chercher chaque compétence dans le texte
        for (String tech : techCompetences) {
            Pattern pattern = Pattern.compile("\\b" + tech + "\\b", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                // Utiliser la casse originale du pattern
                competences.add(tech.replace("\\+\\+", "++").replace("\\.", "."));
            }
        }
        
        // Extraire les langues
        Pattern languePattern = Pattern.compile(
            "(Français|Anglais|Espagnol|Allemand|Italien|Russe|Arabe|Chinois)\\s*[:-]?\\s*(Maternel|Paternel|natif|courant|bilingue|B1|B2|C1|C2)?",
            Pattern.CASE_INSENSITIVE
        );
        
        Matcher langueMatcher = languePattern.matcher(text);
        while (langueMatcher.find()) {
            String langue = langueMatcher.group(1);
            String niveau = langueMatcher.group(2);
            if (niveau != null && !niveau.isEmpty()) {
                competences.add(langue + " (" + niveau + ")");
            } else {
                competences.add(langue);
            }
        }
        
        // Soft skills courants
        String[] softSkills = {
            "autonome", "responsable", "capacité d'adaptation", "travail d'équipe",
            "leadership", "communication", "créatif", "rigoureux", "organisé"
        };
        
        for (String skill : softSkills) {
            if (text.toLowerCase().contains(skill.toLowerCase())) {
                // Capitaliser la première lettre
                competences.add(Character.toUpperCase(skill.charAt(0)) + skill.substring(1));
            }
        }
        
        return new ArrayList<>(competences);
    }

    private String extractPermis(String text) {
        Pattern pattern = Pattern.compile("(?i)permis\\s*[:-]?\\s*([ABCDabcd](?:[\\s,&et]+[ABCDabcd])*)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group().trim() : "Non mentionné";
    }

    private String extractPosteVise(String text) {
        // Chercher dans la section PROFILE ou OBJECTIF
        Pattern profilePattern = Pattern.compile(
            "(?i)(profile|profil|objectif|recherche)[\\s:]*\\n?([\\s\\S]{20,300}?)(?=\\n\\s*[A-Z]{4,}|$)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );
        
        Matcher matcher = profilePattern.matcher(text);
        if (matcher.find()) {
            String profil = matcher.group(2).trim();
            // Prendre seulement la première phrase ou les 100 premiers caractères
            if (profil.contains(".")) {
                profil = profil.substring(0, profil.indexOf(".")).trim();
            }
            if (profil.length() > 100) {
                profil = profil.substring(0, 100) + "...";
            }
            return profil;
        }
        
        // Si pas trouvé, chercher "Étudiant en ..." ou "Développeur ..."
        Pattern rolePattern = Pattern.compile(
            "(?i)(Étudiant en|Développeur|Ingénieur|Consultant|Analyste)\\s+([\\w\\s]{5,50})",
            Pattern.CASE_INSENSITIVE
        );
        Matcher roleMatcher = rolePattern.matcher(text);
        if (roleMatcher.find()) {
            return roleMatcher.group().trim();
        }
        
        return "";
    }

    private String extractDisponibilite(String text) {
        Pattern pattern = Pattern.compile(
            "(?i)disponib(?:le|ilité)\\s*[:-]?\\s*([\\w\\s,]{3,50})(?=\\n|$)",
            Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(1).trim() : "";
    }

    public boolean isDataComplete(Map<String, Object> data) {
        // Vérifier si les champs critiques sont remplis
        boolean nomOk = data.get("nom") != null && !data.get("nom").toString().isEmpty();
        boolean prenomOk = data.get("prenom") != null && !data.get("prenom").toString().isEmpty();
        boolean mailOk = data.get("mail") != null && !data.get("mail").toString().isEmpty();
        boolean telephoneOk = data.get("telephone") != null && !data.get("telephone").toString().isEmpty();
        
        @SuppressWarnings("unchecked")
        List<Map<String, String>> experiences = (List<Map<String, String>>) data.get("experiences");
        @SuppressWarnings("unchecked")
        List<String> competences = (List<String>) data.get("competences");
        
        boolean experiencesOk = experiences != null && !experiences.isEmpty();
        boolean competencesOk = competences != null && competences.size() >= 3; // Au moins 3 compétences
        
        return nomOk && prenomOk && mailOk && telephoneOk && experiencesOk && competencesOk;
    }
}