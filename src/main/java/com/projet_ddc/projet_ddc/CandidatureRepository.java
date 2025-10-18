package com.projet_ddc.projet_ddc;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CandidatureRepository extends CrudRepository<Candidature, Long> {
    
}
