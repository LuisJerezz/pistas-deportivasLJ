package com.iesvdc.acceso.pistasdeportivas.repos;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iesvdc.acceso.pistasdeportivas.modelos.Instalacion;

@Repository
public interface RepoInstalacion extends JpaRepository<Instalacion,Long> {
    
}
