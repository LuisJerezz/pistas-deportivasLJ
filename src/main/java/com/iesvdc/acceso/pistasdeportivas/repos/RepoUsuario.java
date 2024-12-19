package com.iesvdc.acceso.pistasdeportivas.repos;

import org.springframework.data.jpa.repository.JpaRepository;

import com.iesvdc.acceso.pistasdeportivas.modelos.Usuario;
import java.util.List;


public interface RepoUsuario extends JpaRepository <Usuario, Long>{
    List<Usuario> findByUsername(String username);
}
