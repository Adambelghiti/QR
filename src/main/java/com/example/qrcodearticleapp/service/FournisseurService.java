package com.example.qrcodearticleapp.service;

import com.example.qrcodearticleapp.entity.Fournisseur;
import com.example.qrcodearticleapp.repository.FournisseurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FournisseurService {

    @Autowired
    private FournisseurRepository fournisseurRepository;

    public List<Fournisseur> getAllFournisseurs() {
        return fournisseurRepository.findAll();
    }

    public Fournisseur getFournisseurById(Long id) {
        return fournisseurRepository.findById(id).orElse(null);
    }

    public Fournisseur saveFournisseur(Fournisseur fournisseur) {
        return fournisseurRepository.save(fournisseur);
    }

    public void deleteFournisseur(Long id) {
        fournisseurRepository.deleteById(id);
    }

    /*public Fournisseur getFournisseurByName(String name) {
        List<Fournisseur> fournisseurs = getAllFournisseurs();
        return fournisseurRepository.findByName(name,fournisseurs);
    }*/
    public Fournisseur getFournisseurByName(String name){
        return fournisseurRepository.findByName(name);
    }
}
