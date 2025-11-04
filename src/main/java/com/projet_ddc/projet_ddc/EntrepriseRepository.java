package com.projet_ddc.projet_ddc;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EntrepriseRepository extends JpaRepository<Entreprise, Long> {
    Entreprise findByEmailAndMotDePasse(String email, String motDePasse);
}