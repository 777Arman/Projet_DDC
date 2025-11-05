package com.projet_ddc.projet_ddc;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CandidatureRepository extends JpaRepository<Candidature, Long> {
        List<Candidature> findByEtat(int etat);

}
