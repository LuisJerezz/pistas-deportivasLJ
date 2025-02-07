package com.iesvdc.acceso.pistasdeportivas.controladores;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.iesvdc.acceso.pistasdeportivas.modelos.Horario;
import com.iesvdc.acceso.pistasdeportivas.modelos.Instalacion;
import com.iesvdc.acceso.pistasdeportivas.modelos.Reserva;
import com.iesvdc.acceso.pistasdeportivas.modelos.Usuario;
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
        @PageableDefault(size = 10, sort = "id") Pageable pageable,
        @RequestParam(required = false) Long usuarioId) {

        Page<Reserva> page;
        
        if (usuarioId != null) {
            page = repoReserva.findByUsuarioId(usuarioId, pageable);
        } else {
            page = repoReserva.findAll(pageable);
        }

        List<Usuario> usuarios = repoUsuario.findAll();

        model.addAttribute("page", page);
        model.addAttribute("reservas", page.getContent());
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("usuarioSeleccionado", usuarioId);

        return "reservas/reservas";
        }

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
                return "reservas/edit";
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

        reservaActualizada.setHorario(nuevoHorario);
        reservaActualizada.setFecha(reserva.getFecha());  

        repoReserva.save(reservaActualizada);  
        return "redirect:/reserva";
    }

    @GetMapping("/del/{id}")
    public String delReserva(
        @PathVariable Long id,
        Model model) {
    
            Optional<Reserva> opReserva = repoReserva.findById(id);
            if (opReserva.isPresent()) {
                Reserva reserva = opReserva.get();
                model.addAttribute("reserva", opReserva.get());
                model.addAttribute("instalaciones", repoInstalacion.findAll());
                List<Horario> horarios = repoHorario.findByInstalacion(reserva.getHorario().getInstalacion());
                model.addAttribute("horario", horarios);
                return "reservas/del";
            } else {
                model.addAttribute("mensaje", "LA INSTALACIÓN NO EXISTE");
                model.addAttribute("titulo", "ERROR EN LA EDICIÓN DE LA INSTALACIÓN");
                return "/error";
            }
    }

    @PostMapping("/del/{id}")
    public String delReservaPost(
        @PathVariable Long id,
        Model model){
    
            Optional<Reserva> opReserva = repoReserva.findById(id);
            if (opReserva.isPresent()){
                Reserva reserva = opReserva.get();
                repoReserva.delete(reserva);
                return "redirect:/reserva";
            } else{
                model.addAttribute("mensaje", "LA INSTALACIÓN NO EXISTE");
                model.addAttribute("titulo", "ERROR EN LA EDICIÓN DE LA INSTALACIÓN");
                return "/error";
            }
    }
        

    

    @GetMapping("/add/{horarioId}")
    public String addReserva(@PathVariable("horarioId") Long horarioId, Model model) {
        Optional<Horario> optHorario = repoHorario.findById(horarioId);
        if (optHorario.isEmpty()){
            model.addAttribute("mensaje", "El horario no existe");

            return "/error"; 
        }
        Horario horario = optHorario.get();
        Instalacion instalacion = horario.getInstalacion();
        if (instalacion==null){
            model.addAttribute("mensaje", "La instalación no existe");
            return "/error"; 
        }

        List<Usuario> usuarios = repoUsuario.findAll();
        if (usuarios.isEmpty()){
            model.addAttribute("mensaje", "No hay usuarios registrados");
            return "/error"; 
        }

        Reserva reserva = new Reserva();

        model.addAttribute("reserva", reserva);
        model.addAttribute("instalacion", instalacion);
        model.addAttribute("horario", horario);
        model.addAttribute("usuarios", usuarios);

        return "reservas/add"; 
}

    
@PostMapping("/add/{horarioId}")
public String addReserva(
    @PathVariable("horarioId") Long horarioId,
    @RequestParam("usuarioId") Long usuarioId,
    @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
    RedirectAttributes redirectAttributes){

    LocalDate hoy = LocalDate.now();
    LocalDate limiteInferior = hoy.minusDays(0);
    LocalDate limiteSuperior = hoy.plusDays(7);

    if (fecha.isBefore(limiteInferior) || fecha.isAfter(limiteSuperior)) {
        redirectAttributes.addFlashAttribute("mensaje", "La reserva solo puede hacerse dentro de los 7 días anteriores o posteriores al día de hoy.");
        return "redirect:/error";
    }

    Optional<Horario> optHorario = repoHorario.findById(horarioId);
    if (optHorario.isEmpty()){
        redirectAttributes.addFlashAttribute("mensaje", "El horario no existe.");
        return "redirect:/error";
    }
    Horario horario = optHorario.get();

    Optional<Usuario> optUsuario = repoUsuario.findById(usuarioId);
    if (optUsuario.isEmpty()){
        redirectAttributes.addFlashAttribute("mensaje", "El usuario no existe.");
        return "redirect:/error";
    }
    Usuario usuario = optUsuario.get();

    // Verificar si el usuario ya tiene una reserva para ese día
    boolean reservaExistente = repoReserva.findByUsuarioAndFecha(usuario, fecha).isPresent();
    if (reservaExistente) {
        redirectAttributes.addFlashAttribute("mensaje", "No puedes hacer más de una reserva por día.");
        return "redirect:/error";
    }

    if (repoReserva.existsByUsuarioAndHorario(usuario, horario)){
        redirectAttributes.addFlashAttribute("mensaje", "El usuario ya tiene una reserva para este horario.");
        return "redirect:/error";
    }

    Reserva reserva = new Reserva();
    reserva.setUsuario(usuario);
    reserva.setHorario(horario);
    reserva.setFecha(fecha);

    repoReserva.save(reserva);

    return "redirect:/reserva";
}









}
    


