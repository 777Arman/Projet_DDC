package com.projet_ddc.projet_ddc;


import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class TempCandidatureRep implements CandidatureRep {
    public static final List<Candidature> ListeCandidature = new ArrayList<>();

    static {
        ListeCandidature.add(new Candidature("Bouabdallah", "Amine", "Développeur",-1,0L));
        ListeCandidature.add(new Candidature("Hbiba", "Bellisima",  "Designer",-1,1L));
        ListeCandidature.add(new Candidature("Agartha", "Yakub", "Chef de projet",0,2L));
        ListeCandidature.add(new Candidature("Dimitri", "Ivanov", "Analyste",1,3L));
        ListeCandidature.add(new Candidature("Svetlana", "Petrova", "Consultant",2,4L));

    }


    @Override
    public List<Candidature> findAll() {
        return ListeCandidature;
    }

    @Override
    public void addCandidature(Candidature legume){
        ListeCandidature.add(legume);   

    }

    @Override
    public void suppCandidature(Long id){
        ListeCandidature.removeIf(c -> c.getId().equals(id));
    }

}

    
