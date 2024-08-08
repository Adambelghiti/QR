package com.example.qrcodearticleapp.service;

import com.example.qrcodearticleapp.entity.Entrepot;
import com.example.qrcodearticleapp.repository.EntrepotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EntrepotService {

    @Autowired
    private EntrepotRepository entrepotRepository;

    public List<Entrepot> getAllEntrepots() {
        return entrepotRepository.findAll();
    }

    public Entrepot getEntrepotById(Long id) {
        return entrepotRepository.findById(id).orElse(null);
    }

    public Entrepot saveEntrepot(Entrepot entrepot) {
        return entrepotRepository.save(entrepot);
    }

    public void deleteEntrepot(Long id) {
        entrepotRepository.deleteById(id);
    }
  /*  public Entrepot getEntrepotByName(String nom) {
        List<Entrepot> entrepots = getAllEntrepots();
        return entrepotRepository.findByNom(nom, entrepots);
    }*/
  public Entrepot getEntrepotByName(String nom){
      return entrepotRepository.findByNom(nom);
  }
}
