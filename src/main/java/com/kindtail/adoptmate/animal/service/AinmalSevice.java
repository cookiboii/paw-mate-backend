package com.kindtail.adoptmate.animal.service;

import com.kindtail.adoptmate.animal.repository.AinmalRepository;
import org.springframework.stereotype.Service;

@Service
public class AinmalSevice {

   private final AinmalRepository ainmalRepository;

    public AinmalSevice(AinmalRepository ainmalRepository) {
        this.ainmalRepository = ainmalRepository;
    }

}
