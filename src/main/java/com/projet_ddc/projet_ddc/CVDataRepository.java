package com.projet_ddc.projet_ddc;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CVDataRepository extends JpaRepository<CVData, Long> {
    
    Optional<CVData> findByCandidatureId(Long candidatureId);
    
}