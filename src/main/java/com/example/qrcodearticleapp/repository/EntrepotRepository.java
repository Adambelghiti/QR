package com.example.qrcodearticleapp.repository;

import com.example.qrcodearticleapp.entity.Entrepot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EntrepotRepository extends JpaRepository<Entrepot, Long> {
     //Entrepot findByNom(String nom, List<Entrepot> entrepots);
     Entrepot findByNom(String nom);

}
