package com.iesvdc.acceso.pistasdeportivas.controladores;

import java.time.LocalDate;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

        List<Reserva> reservas = repoReserva.findByUsuario(usuario);

        if (instalacionId != null) {
            reservas = reservas.stream()
                .filter(reserva -> reserva.getHorario().getInstalacion().getId().equals(instalacionId))
                .collect(Collectors.toList());
        }

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), reservas.size());
        page = new PageImpl<>(reservas.subList(start, end), pageable, reservas.size());

        List<Instalacion> instalaciones = repoInstalacion.findAll();

        modelo.addAttribute("page", page);
        modelo.addAttribute("reservas", page.getContent());
        modelo.addAttribute("instalaciones", instalaciones);
        modelo.addAttribute("instalacionSeleccionada", instalacionId);

        return "mis-datos/mis-reservas";
    }

    @GetMapping("/mis-reservas/edit/{id}")
    public String editReserva(@PathVariable @NonNull Long id, Model modelo, RedirectAttributes redirectAttributes) {
        Optional<Reserva> oReserva = repoReserva.findById(id);
        if (oReserva.isPresent()) {
            Reserva reserva = oReserva.get();
            LocalDate fechaActual = LocalDate.now();

            if (reserva.getFecha().isBefore(fechaActual) || reserva.getFecha().isEqual(fechaActual)) {
                redirectAttributes.addFlashAttribute("mensajeError", "No se puede editar una reserva que ya ha pasado o está programada para hoy.");
                return "redirect:/mis-datos/mis-reservas";
            }

            modelo.addAttribute("reserva", reserva);
            modelo.addAttribute("usuario", repoUsuario.findAll());

            List<Horario> horariosDisponibles = repoHorario.findByInstalacion(reserva.getHorario().getInstalacion());
            modelo.addAttribute("horariosDisponibles", horariosDisponibles);

            return "/mis-datos/edit";
        } else {
            redirectAttributes.addFlashAttribute("mensajeError", "La reserva no existe.");
            return "redirect:/mis-datos/mis-reservas";
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

            reservaActualizada.setHorario(nuevoHorario);
            reservaActualizada.setFecha(reserva.getFecha());

            repoReserva.save(reservaActualizada);
            return "redirect:/mis-datos/mis-reservas";
        }

        return "error";
    }

    @GetMapping("/mis-reservas/add")
    public String addReserva(@RequestParam(name = "instalacionId", required = false) Long instalacionId, Model model) {
        List<Instalacion> instalaciones = repoInstalacion.findAll();
        
        Reserva reserva = new Reserva();
        reserva.setUsuario(getLoggedUser());
    
        List<Horario> horariosDisponibles = (instalacionId != null) ? 
            repoHorario.findByInstalacion(repoInstalacion.findById(instalacionId).orElse(null)) :
            List.of();
    
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
        RedirectAttributes redirectAttributes) {
        
        Usuario usuario = getLoggedUser();
        Instalacion instalacion = repoInstalacion.findById(instalacionId).orElse(null);
        Horario horario = repoHorario.findById(horarioId).orElse(null);
        
        if (instalacion == null || horario == null) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al crear la reserva. Verifique los datos seleccionados.");
            return "redirect:/mis-datos/mis-reservas";
        }

        LocalDate fechaReserva = reserva.getFecha();
        LocalDate hoy = LocalDate.now();
        LocalDate maxFechaPermitida = hoy.plusWeeks(1);

        if (fechaReserva.isBefore(hoy)) {
            redirectAttributes.addFlashAttribute("mensajeError", "No se pueden hacer reservas en fechas anteriores a la actual.");
            return "redirect:/mis-datos/mis-reservas";
        }

        if (fechaReserva.isAfter(maxFechaPermitida)) {
            redirectAttributes.addFlashAttribute("mensajeError", "No se pueden hacer reservas con más de una semana de antelación.");
            return "redirect:/mis-datos/mis-reservas";
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
                return "redirect:/mis-datos/mis-reservas";
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
