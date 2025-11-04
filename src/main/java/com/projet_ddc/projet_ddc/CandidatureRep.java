package com.projet_ddc.projet_ddc;

import java.util.List;

public interface CandidatureRep {
    List<Candidature> findAll();
    void addCandidature(Candidature cand);
    void suppCandidature(Long id);
}