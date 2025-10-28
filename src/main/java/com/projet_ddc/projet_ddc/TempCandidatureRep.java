package com.projet_ddc.projet_ddc;


import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class TempCandidatureRep implements CandidatureRep {
    public static final List<Candidature> ListeCandidature = new ArrayList<>();

    static {

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

    
