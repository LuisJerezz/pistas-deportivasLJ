package com.iesvdc.acceso.pistasdeportivas.controladores;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;

import com.iesvdc.acceso.pistasdeportivas.modelos.Horario;
import com.iesvdc.acceso.pistasdeportivas.modelos.Instalacion;
import com.iesvdc.acceso.pistasdeportivas.modelos.Reserva;
import com.iesvdc.acceso.pistasdeportivas.modelos.Usuario;
import com.iesvdc.acceso.pistasdeportivas.repos.RepoHorario;
import com.iesvdc.acceso.pistasdeportivas.repos.RepoInstalacion;
import com.iesvdc.acceso.pistasdeportivas.repos.RepoReserva;
import com.iesvdc.acceso.pistasdeportivas.repos.RepoUsuario;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping("/mis-datos")
public class ControDatos {

    @Autowired
    RepoReserva repoReserva;

    @Autowired
    RepoUsuario repoUsuario;

    @Autowired 
    RepoHorario repoHorario;

    @Autowired
    RepoInstalacion repoInstalacion;

    private Usuario getLoggedUser(){
        Authentication authentication = 
            SecurityContextHolder.getContext().getAuthentication();
        return repoUsuario.findByUsername(
            authentication.getName()).get(0);
    }
    
    @GetMapping("")
    public String misDatos(Model modelo) {
        modelo.addAttribute("usuario", getLoggedUser());

        return "mis-datos/mis-datos";
    }

    
    @GetMapping("/edit")
    public String getMisDatos(Model modelo) {
        modelo.addAttribute("usuario", getLoggedUser());

        return "mis-datos/mis-datos";
    }

    @PostMapping("/edit")
    public String postMisDatos(
        @ModelAttribute("usuario") Usuario u, Model modelo) {
        Usuario loggedUser = getLoggedUser();
        if (loggedUser.getId() == u.getId()) {

            u.setTipo(loggedUser.getTipo());
            u.setPassword(loggedUser.getPassword());
            u.setEnabled(loggedUser.isEnabled());

            repoUsuario.save(u);
            return "redirect:/mis-datos";
        } else {
            modelo.addAttribute("mensaje", "Error editando datos de usuario");
            modelo.addAttribute("titulo", "No ha sido posible editar sus datos.");
            return "/error";
        }
        
    }

    @GetMapping("/mis-reservas")
    public String misReservas(Model modelo, 
        @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        Usuario usuario = getLoggedUser();
        Page<Reserva> page = repoReserva.findByUsuario(usuario, pageable); 
        modelo.addAttribute("page", page);
        modelo.addAttribute("reservas", page.getContent());
        return "mis-datos/mis-reservas";
    }

    @GetMapping("/mis-reservas/edit/{id}")
    public String editReserva( 
        @PathVariable @NonNull Long id,
        Model modelo) {

        Optional<Reserva> oReserva = repoReserva.findById(id);
        if (oReserva.isPresent()) {
            modelo.addAttribute("reserva", oReserva.get());
            return "/mis-datos/edit";
        } else {
            modelo.addAttribute("mensaje", "La reserva no exsiste");
            modelo.addAttribute("titulo", "Error editando reserva.");
            return "/error";
        }
    }

    @PostMapping("/mis-reservas/edit/{id}")
    public String editReserva(
        @PathVariable @NonNull Long id,
        @ModelAttribute("reserva") Reserva reserva)  {
        repoReserva.save(reserva);
        return "redirect:/mis-datos/mis-reservas"; 
    }

    /* @GetMapping("/mis-reservas/add")
    public String addReserva(Model model) {
        List<Instalacion> instalacionesLibres = repoInstalacion.encontrarInstalaciones();
        model.addAttribute("reserva", new Reserva());
        model.addAttribute("instalaciones", instalacionesLibres);
        return "/mis-datos/add";
    }
    
    @PostMapping("/mis-reservas/add")
    public String addReserva(
        @ModelAttribute("reserva") Reserva reserva)  {
        repoReserva.save(reserva);
        return "redirect:/mis-reservas";
    } */

    @GetMapping("/mis-reservas/del/{id}")
    public String mostrarConfirmacionEliminacion(@PathVariable Long id, Model model) {
        try {
            Reserva reserva = repoReserva.findById(id).orElse(null);
            if (reserva != null) {
                model.addAttribute("reserva", reserva);
                return "mis-datos/del";  // Aquí cargamos la vista con el formulario de confirmación
            } else {
                model.addAttribute("mensaje", "Reserva no encontrada.");
                return "error";
            }
        } catch (Exception e) {
            model.addAttribute("mensaje", "Error al cargar la reserva: " + e.getMessage());
            e.printStackTrace();
            return "error";
        }
    }

    @PostMapping("/mis-reservas/del/{id}")
    public String eliminarReserva(@PathVariable Long id, Model model) {
        try {
            Reserva reserva = repoReserva.findById(id).orElse(null);
            if (reserva != null) {
                repoReserva.delete(reserva);
                return "redirect:/mis-datos/mis-reservas";  // Redirigir a la lista de reservas
            } else {
                model.addAttribute("mensaje", "Reserva no encontrada.");
                return "error";
            }
        } catch (Exception e) {
            model.addAttribute("mensaje", "Error al eliminar la reserva: " + e.getMessage());
            e.printStackTrace();
            return "error";
        }
    }

    /* @GetMapping("/mis-reservas/edit/{id}")
    public String editReserva(@PathVariable @NonNull Long id, Model modelo) {
        Optional<Reserva> oReserva = repoReserva.findById(id);
        if (oReserva.isPresent()) {
            modelo.addAttribute("reserva", oReserva.get());
            modelo.addAttribute("instalaciones", repoInstalacion.findAll());
            modelo.addAttribute("horarios", repoHorario.findAll().stream()
                .sorted(Comparator.comparing(Horario::getHoraInicio))
                .collect(Collectors.toList()));
            return "mis-datos/edit";
        } else {
            modelo.addAttribute("mensaje", "La reserva no existe.");
            modelo.addAttribute("titulo", "Error editando reserva.");
            return "error"; 
        }
    }


    @PostMapping("/mis-reservas/edit/{id}")
    public String editReserva(@PathVariable @NonNull Long id, 
    @Validated @ModelAttribute("reserva") Reserva reserva, 
    BindingResult result, 
    Model modelo) {

        if (result.hasErrors()) {
            modelo.addAttribute("mensaje", "Por favor corrija los errores en el formulario.");
            modelo.addAttribute("instalaciones", repoInstalacion.findAll());
            modelo.addAttribute("horarios", repoHorario.findAll().stream()
                .sorted(Comparator.comparing(Horario::getHoraInicio))
                .collect(Collectors.toList()));
            return "mis-datos/edit"; 
        }

        try {
            if (reserva.getHorario() == null || reserva.getHorario().getId() == null) {
                result.rejectValue("horario", "error.reserva", "Horario es obligatorio");
                modelo.addAttribute("mensaje", "El horario es obligatorio.");
                modelo.addAttribute("instalaciones", repoInstalacion.findAll());
                modelo.addAttribute("horarios", repoHorario.findAll().stream()
                    .sorted(Comparator.comparing(Horario::getHoraInicio))
                    .collect(Collectors.toList()));
                return "mis-datos/edit"; 
            }

            Horario horarioSeleccionado = repoHorario.findById(reserva.getHorario().getId())
                .orElse(null);
            if (horarioSeleccionado == null) {
                result.rejectValue("horario", "error.reserva", "Horario inválido");
                modelo.addAttribute("mensaje", "Horario inválido.");
                modelo.addAttribute("instalaciones", repoInstalacion.findAll());
                modelo.addAttribute("horarios", repoHorario.findAll().stream()
                    .sorted(Comparator.comparing(Horario::getHoraInicio))
                    .collect(Collectors.toList()));
            return "mis-datos/edit";
            }

            reserva.setHorario(horarioSeleccionado);
        
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuario = repoUsuario.findByUsername(authentication.getName()).get(0);
            reserva.setUsuario(usuario);

            repoReserva.save(reserva);
        } catch (Exception e) {
            modelo.addAttribute("mensaje", e.getMessage());
            modelo.addAttribute("instalaciones", repoInstalacion.findAll());
            modelo.addAttribute("horarios", repoHorario.findAll().stream()
                .sorted(Comparator.comparing(Horario::getHoraInicio))
                .collect(Collectors.toList()));
            e.printStackTrace();
        return "mis-datos/edit";
        }

        return "redirect:/mis-datos/mis-reservas"; 
    } */
    

    
}
