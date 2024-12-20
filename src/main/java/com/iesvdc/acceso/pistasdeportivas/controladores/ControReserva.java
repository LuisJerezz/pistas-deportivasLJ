package com.iesvdc.acceso.pistasdeportivas.controladores;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.iesvdc.acceso.pistasdeportivas.modelos.Reserva;
import com.iesvdc.acceso.pistasdeportivas.repos.RepoReserva;
import com.iesvdc.acceso.pistasdeportivas.repos.RepoUsuario;




@Controller
@RequestMapping("/reserva")
public class ControReserva {
    
    @Autowired
    RepoReserva repoReserva;

    @Autowired
    RepoUsuario repoUsuario;

    

    @GetMapping("")
    public String getReservas(
        Model model, 
        @PageableDefault(size = 10, sort = "id") Pageable pageable ) {
        
        Page<Reserva> page = repoReserva.findAll(pageable);
        model.addAttribute("page", page);
        model.addAttribute("reservas", page.getContent());
        model.addAttribute("usuarios", repoUsuario.findAll());

        return "reservas/reservas";
    }
    

    
    

    //@GetMapping("/add")
    //public String addReserva(Model model) {
    //    model.addAttribute("reserva", new Reserva());
    //    model.addAttribute("operacion", "ADD");
    //    model.addAttribute("usuarios", repoUsuario.findAll());
    //    return "/reservas/add";
    //}
    //
    //
    //@PostMapping("/add")
    //public String addReserva(@ModelAttribute("reserva") Reserva reserva) {
    //    repoReserva.save(reserva);
    //    
    //    return "redirect:/reservas";
    //}


    @GetMapping("/edit/{id}")
    public String editReserva(
        @PathVariable @NonNull Long id,
        Model model){

        Optional<Reserva> opReserva = repoReserva.findById(id);
        if (opReserva.isPresent()) {
            model.addAttribute("reserva", opReserva.get());
            model.addAttribute("operacion", "EDIT");
            model.addAttribute("instalaciones", repoUsuario.findAll());
            return "reservas/add";
        } else {
            model.addAttribute("mensaje", "LA INSTALACIÓN NO EXISTE");
            model.addAttribute("titulo", "ERROR EN LA EDICIÓN DE LA INSTALACIÓN");
            return "error";
        }
        
    }
    

    @PostMapping("/edit/{id}")
    public String editHorario(
        @ModelAttribute("reserva") Reserva reserva) {
            repoReserva.save(reserva);
            return "redirect:/reservas";
        }
}
