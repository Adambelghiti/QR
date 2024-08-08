package com.example.qrcodearticleapp.repository;

import com.example.qrcodearticleapp.entity.Entrepot;
import com.example.qrcodearticleapp.entity.Fournisseur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FournisseurRepository extends JpaRepository<Fournisseur, Long> {
     //Fournisseur findByName(String name, List<Fournisseur> fournisseurs);
     Fournisseur findByName(String name);

}
