package com.iesvdc.acceso.pistasdeportivas.controladores;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
    

    @GetMapping("add")
    public String addReserva(Model model) {
        model.addAttribute("reserva", new Reserva());
        model.addAttribute("operacion", "ADD");
        model.addAttribute("usuarios", repoUsuario.findAll());
        return "/reserva/add";
    }
    
    
    @PostMapping("/add")
    public String addReserva(@ModelAttribute("reserva") Reserva reserva) {
        repoReserva.save(reserva);
        
        return "redirect:/reserva";
    }
    
}
