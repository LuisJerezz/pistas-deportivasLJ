package com.iesvdc.acceso.pistasdeportivas.repos;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iesvdc.acceso.pistasdeportivas.modelos.Reserva;
import com.iesvdc.acceso.pistasdeportivas.modelos.Usuario;


@Repository
public interface RepoReserva extends JpaRepository<Reserva,Long>{
    
    List<Reserva> findByUsuario(Usuario usuario);
    
    Page<Reserva> findByUsuario(Usuario usuario, Pageable pageable);

}
