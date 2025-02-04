package com.iesvdc.acceso.pistasdeportivas.controladores;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
import org.springframework.web.bind.annotation.RequestParam;

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
public String misReservas(
    @RequestParam(name = "instalacionId", required = false) Long instalacionId,
    Model modelo, 
    @PageableDefault(size = 10, sort = "id") Pageable pageable) {

    Usuario usuario = getLoggedUser();
    Page<Reserva> page;

    // Obtener todas las reservas del usuario
    List<Reserva> reservas = repoReserva.findByUsuario(usuario);

    // Filtrar por instalación si se proporciona un instalacionId
    if (instalacionId != null) {
        reservas = reservas.stream()
            .filter(reserva -> reserva.getHorario().getInstalacion().getId().equals(instalacionId))
            .collect(Collectors.toList());
    }

    // Convertir la lista filtrada a una página
    int start = (int) pageable.getOffset();
    int end = Math.min((start + pageable.getPageSize()), reservas.size());
    page = new PageImpl<>(reservas.subList(start, end), pageable, reservas.size());

    // Obtener todas las instalaciones para el selector
    List<Instalacion> instalaciones = repoInstalacion.findAll();

    modelo.addAttribute("page", page);
    modelo.addAttribute("reservas", page.getContent());
    modelo.addAttribute("instalaciones", instalaciones);
    modelo.addAttribute("instalacionSeleccionada", instalacionId);

    return "mis-datos/mis-reservas";
}

    @GetMapping("/mis-reservas/edit/{id}")
    public String editReserva( 
        @PathVariable @NonNull Long id,
        Model modelo) {

        Optional<Reserva> oReserva = repoReserva.findById(id);
        if (oReserva.isPresent()) {
            Reserva reserva = oReserva.get();
            modelo.addAttribute("reserva", reserva);
            modelo.addAttribute("usuario", repoUsuario.findAll());

            // Obtener los horarios disponibles para la instalación de la reserva
            List<Horario> horariosDisponibles = repoHorario.findByInstalacion(reserva.getHorario().getInstalacion());
            modelo.addAttribute("horariosDisponibles", horariosDisponibles);

            return "/mis-datos/edit";
        } else {
            modelo.addAttribute("mensaje", "La reserva no existe");
            modelo.addAttribute("titulo", "Error editando reserva.");
            return "/error";
        }
    }

    @PostMapping("/mis-reservas/edit/{id}")
public String editReserva(
        @PathVariable Long id,
        @RequestParam("horarioId") Long horarioId,
        @ModelAttribute("reserva") Reserva reserva) {
    
    Optional<Reserva> optReserva = repoReserva.findById(id);
    Optional<Horario> optHorario = repoHorario.findById(horarioId);
    
    if (optReserva.isPresent() && optHorario.isPresent()) {
        Reserva reservaActualizada = optReserva.get();
        Horario nuevoHorario = optHorario.get();

        // Actualizar el horario y la fecha
        reservaActualizada.setHorario(nuevoHorario);
        reservaActualizada.setFecha(reserva.getFecha());  // Asegúrate de actualizar la fecha

        repoReserva.save(reservaActualizada);  // Guardar la reserva actualizada
        return "redirect:/mis-datos/mis-reservas";
    }

    return "error";
}


    @GetMapping("/mis-reservas/add")
    public String addReserva(@RequestParam(name = "instalacionId", required = false) Long instalacionId, Model model) {
        // Obtener todas las instalaciones disponibles
        List<Instalacion> instalaciones = repoInstalacion.findAll();
        
        // Crear una nueva reserva vacía
        Reserva reserva = new Reserva();
        reserva.setUsuario(getLoggedUser());
    
        // Obtener los horarios disponibles según la instalación seleccionada
        List<Horario> horariosDisponibles = (instalacionId != null) ? 
            repoHorario.findByInstalacion(repoInstalacion.findById(instalacionId).orElse(null)) :
            List.of(); // Lista vacía si no se ha seleccionado una instalación.
    
        // Agregar atributos al modelo
        model.addAttribute("reserva", reserva);
        model.addAttribute("instalaciones", instalaciones);
        model.addAttribute("instalacionSeleccionada", instalacionId);
        model.addAttribute("horariosDisponibles", horariosDisponibles);
    
        return "mis-datos/add";
    }

    @PostMapping("/mis-reservas/add")
    public String addReserva(
        @ModelAttribute("reserva") Reserva reserva,
        @RequestParam("instalacionId") Long instalacionId,
        @RequestParam("horarioId") Long horarioId,
        Model model) {
        
        Usuario usuario = getLoggedUser();
        Instalacion instalacion = repoInstalacion.findById(instalacionId).orElse(null);
        Horario horario = repoHorario.findById(horarioId).orElse(null);
        
        if (instalacion == null || horario == null) {
            model.addAttribute("mensaje", "Error al crear la reserva. Verifique los datos seleccionados.");
            return "error";
        }

        reserva.setUsuario(usuario);
        reserva.setHorario(horario);

        repoReserva.save(reserva);

        return "redirect:/mis-datos/mis-reservas";
    }



    @GetMapping("/mis-reservas/del/{id}")
    public String mostrarConfirmacionEliminacion(@PathVariable Long id, Model model) {
        try {
            Reserva reserva = repoReserva.findById(id).orElse(null);
            if (reserva != null) {
                model.addAttribute("reserva", reserva);
                return "mis-datos/del";
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
    
}
