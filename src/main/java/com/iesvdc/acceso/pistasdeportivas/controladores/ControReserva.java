package com.iesvdc.acceso.pistasdeportivas.controladores;

import java.util.List;
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
import org.springframework.web.bind.annotation.RequestParam;

import com.iesvdc.acceso.pistasdeportivas.modelos.Horario;
import com.iesvdc.acceso.pistasdeportivas.modelos.Reserva;
import com.iesvdc.acceso.pistasdeportivas.repos.RepoHorario;
import com.iesvdc.acceso.pistasdeportivas.repos.RepoInstalacion;
import com.iesvdc.acceso.pistasdeportivas.repos.RepoReserva;
import com.iesvdc.acceso.pistasdeportivas.repos.RepoUsuario;




@Controller
@RequestMapping("/reserva")
public class ControReserva {
    
    @Autowired
    RepoReserva repoReserva;

    @Autowired
    RepoUsuario repoUsuario;

    @Autowired
    RepoHorario repoHorario;

    @Autowired
    RepoInstalacion repoInstalacion;

    

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
        Model model) {

        Optional<Reserva> opReserva = repoReserva.findById(id);
        if (opReserva.isPresent()) {
            Reserva reserva = opReserva.get();
            model.addAttribute("reserva", opReserva.get());
            model.addAttribute("instalaciones", repoInstalacion.findAll());
            List<Horario> horarios = repoHorario.findByInstalacion(reserva.getHorario().getInstalacion());
            model.addAttribute("horario", horarios);
            return "reservas/add";
        } else {
            model.addAttribute("mensaje", "LA INSTALACIÓN NO EXISTE");
            model.addAttribute("titulo", "ERROR EN LA EDICIÓN DE LA INSTALACIÓN");
            return "/error";
        }
        
    }
    

    @PostMapping("/edit/{id}")
    public String editReserva(
        @PathVariable Long id,
        @RequestParam("horarioId") Long horarioId,
        @ModelAttribute("reserva") Reserva reserva) {
    
        
    Optional<Reserva> optReserva = repoReserva.findById(id);
    Optional<Horario> optHorario = repoHorario.findById(horarioId);
    
    
        Reserva reservaActualizada = optReserva.get();
        Horario nuevoHorario = optHorario.get();

        // Actualizar el horario y la fecha
        reservaActualizada.setHorario(nuevoHorario);
        reservaActualizada.setFecha(reserva.getFecha());  // Asegúrate de actualizar la fecha

        repoReserva.save(reservaActualizada);  // Guardar la reserva actualizada
        return "redirect:/reserva";
    }

    
    
    
}
    


