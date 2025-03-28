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

import com.iesvdc.acceso.pistasdeportivas.modelos.Horario;
import com.iesvdc.acceso.pistasdeportivas.modelos.Instalacion;
import com.iesvdc.acceso.pistasdeportivas.repos.RepoHorario;
import com.iesvdc.acceso.pistasdeportivas.repos.RepoInstalacion;


@Controller
@RequestMapping("/horario")
public class ControHorario {
    
    @Autowired 
    RepoHorario repoHorario;

    @Autowired
    RepoInstalacion repoInstalacion;

    @GetMapping("")
    public String getHorarios(
        Model model,
        @PageableDefault(size = 10, sort = "id") Pageable pageable) {

        Page<Horario> page = repoHorario.findAll(pageable);
        model.addAttribute("page", page);
        model.addAttribute("horarios", page.getContent());
        model.addAttribute("instalaciones", repoInstalacion.findAll());
        return "horarios/horarios";
    }

    @GetMapping("/instalacion/{id}")
    public String getHorariosByInstalacion(
        @PathVariable @NonNull Long id,
        Model model,
        @PageableDefault(size = 10, sort = "id") Pageable pageable) {

        Optional<Instalacion> insOptional = repoInstalacion.findById(id);

        if (insOptional.isPresent()) {
            Page<Horario> page = repoHorario.findByInstalacion(insOptional.get(), pageable);
            model.addAttribute("page", page);
            model.addAttribute("horarios", page.getContent());
            model.addAttribute("instalaciones", repoInstalacion.findAll());
            model.addAttribute("instalacion", insOptional.get());
            return "horarios/horarios";
        } else {
            model.addAttribute("mensaje", "La instalación no existe");
            model.addAttribute("titulo", "Error filtrando por instalación.");
            return "/error";
        }

    }

    // formulario añadir un horario a una instalación
    @GetMapping("/add")
    public String addHorario(Model modelo) {
        modelo.addAttribute("horario", new Horario());
        modelo.addAttribute("operacion", "ADD");
        modelo.addAttribute("instalaciones", repoInstalacion.findAll());
        return "/horarios/add";
    }

    @PostMapping("/add")
    public String addHorario(
        @ModelAttribute("horario") Horario horario)  {
        repoHorario.save(horario);
        return "redirect:/horario";
    }

    // formulario editar un horario
    @GetMapping("/edit/{id}")
    public String editHorario( 
        @PathVariable @NonNull Long id,
        Model modelo) {

        Optional<Horario> oHorario = repoHorario.findById(id);
        if (oHorario.isPresent()) {
            modelo.addAttribute("horario", oHorario.get());
            modelo.addAttribute("operacion", "EDIT");
            modelo.addAttribute("instalaciones", repoInstalacion.findAll());
            return "/horarios/add";
        } else {
            modelo.addAttribute("mensaje", "La instalación no exsiste");
            modelo.addAttribute("titulo", "Error editando instalación.");
            return "/error";
        }
    }

    @PostMapping("/edit/{id}")
    public String editHorario(
        @ModelAttribute("horario") Horario horario)  {
        repoHorario.save(horario);
        return "redirect:/horario";
    }

    // formulario borrar
    @GetMapping("/del/{id}")
    public String delHorario( 
        @PathVariable @NonNull Long id,
        Model modelo) {

        Optional<Horario> oHorario = repoHorario.findById(id);
        if (oHorario.isPresent()) {
            modelo.addAttribute("operacion", "DEL");
            modelo.addAttribute("instalaciones", repoInstalacion.findAll());
            modelo.addAttribute("horario", oHorario.get());
            return "/horarios/add";
        } else {
            modelo.addAttribute("mensaje", "La instalación no exsiste");
            modelo.addAttribute("titulo", "Error borrando instalación.");
            return "/error";
        }
    }

    @PostMapping("/del/{id}")
    public String delHorario(
        @ModelAttribute("horario") Horario horario)  {
        repoHorario.delete(horario);
        return "redirect:/horario";
    }
    
}
