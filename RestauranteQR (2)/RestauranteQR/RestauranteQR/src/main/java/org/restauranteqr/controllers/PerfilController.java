package org.restauranteqr.controllers;

import org.restauranteqr.entity.Perfil;
import org.restauranteqr.entity.Usuario;
import org.restauranteqr.usecase.TransactionUseCase;
import org.restauranteqr.utility.Autenticar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/perfil")
public class PerfilController {

	@Autowired
	private TransactionUseCase transactionUseCase;

	@GetMapping
	public String redirigirAMostrarPerfil() {
		return "redirect:/perfil/mostrar";
	}

	@GetMapping("/mostrar")
	public String mostrarPerfil(HttpSession session, Model model) {

		if (!Autenticar.getInstancia().autenticado(session))
			return "redirect:/usuario/login";

		Usuario usuario = (Usuario) session.getAttribute("usuario");

		model.addAttribute("usuario", usuario);
		model.addAttribute("perfil", usuario.getPerfil());
		return "perfil";
	}

	@PostMapping("/actualizar")
	public String actualizarPerfil(@RequestParam Integer idUsuario, @RequestParam String nombre,
			@RequestParam String apellido, @RequestParam String correo, HttpSession session,
			RedirectAttributes redirectAttributes) {
		Usuario usuario = (Usuario) session.getAttribute("usuario");

		Perfil perfil = usuario.getPerfil();
		perfil.setNombre(nombre);
		perfil.setApellido(apellido);
		perfil.setCorreo(correo);

		transactionUseCase.actualizarPerfil(usuario, perfil);
		session.setAttribute("usuario", usuario);
		redirectAttributes.addFlashAttribute("mensajeExito", "El perfil se ha actualizado");
		return "redirect:/perfil/mostrar";
	}

}
