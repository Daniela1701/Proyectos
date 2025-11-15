package org.restauranteqr.controllers;

import org.restauranteqr.entity.Categoria;
import org.restauranteqr.entity.Perfil;
import org.restauranteqr.entity.Usuario;
import org.restauranteqr.usecase.CategoriaUseCase;
import org.restauranteqr.usecase.TransactionUseCase;
import org.restauranteqr.usecase.UsuarioUseCase;
import org.restauranteqr.utility.Autenticar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/usuario")
public class UsuarioController {

	@Autowired
	private UsuarioUseCase usuarioUseCase;

	@Autowired
	private CategoriaUseCase categoriaUseCase;

	@Autowired
	private TransactionUseCase transactionUseCase;

	@GetMapping("/login")
	public String login() {

		return "login";
	}

	@PostMapping("/login")
	public String login(@RequestParam String username, @RequestParam String password, Model model,
			HttpSession session) {
		Usuario usuario = usuarioUseCase.verificarUsuario(username, password);
		if (usuario == null) {

			model.addAttribute("mensajeError", "Digite correctamente sus credenciales.");
			return "login";
		}

		usuario.setPassword(null);
		session.setAttribute("usuario", usuario);

		return "redirect:/aplicacion/home";
	}

	@GetMapping("/logout")
	public String logout(HttpSession session) {
		session.invalidate();
		return "redirect:/usuario/login";
	}

	@GetMapping("/registroUsuario")
	public String registroUsuario() {
		return "registroUsuario";
	}

	@PostMapping("registroUsuario")
	public String registroUsuario(@RequestParam String nombre, @RequestParam String apellido,
			@RequestParam String correo, @RequestParam String username, @RequestParam String password, Model model,
			HttpSession session) {
		if (usuarioUseCase.buscarPorUsernameOCorreo(username, correo) != null) {
			model.addAttribute("mensajeError", "El usuario o correo ya existe, digite otro.");
			return "registroUsuario";
		}

		Perfil perfil = new Perfil();
		perfil.setNombre(nombre);
		perfil.setApellido(apellido);
		perfil.setCorreo(correo);

		Usuario usuario = new Usuario();
		usuario.setUsername(username);
		usuario.setPassword(password);

		usuario = usuarioUseCase.obtenerUsuario(transactionUseCase.registrarUsuario(usuario, perfil));

		usuario.setPassword(null);
		session.setAttribute("usuario", usuario);
		return "redirect:/aplicacion/home";
	}

	@GetMapping("/gestionarUsuarios")
	public String gestionarUsuarios(Model model, HttpSession session) {

		if (!Autenticar.getInstancia().autenticado(session))
			return "redirect:/usuario/login";
		if (!Autenticar.getInstancia().acceso(session, 3))
			return "redirect:/aplicacion/home";

		model.addAttribute("listaUsuarios", usuarioUseCase.listarUsuario());
		return "gestionarUsuarios";
	}

	@GetMapping("/editarUsuario/{idUsuario}")
	public String editarUsuario(Model model, HttpSession session, @PathVariable Integer idUsuario) {

		if (!Autenticar.getInstancia().autenticado(session))
			return "redirect:/usuario/login";
		if (!Autenticar.getInstancia().acceso(session, 3))
			return "redirect:/aplicacion/home";

		Usuario usuario = usuarioUseCase.obtenerUsuario(idUsuario);
		if (usuario == null) {
			return "redirect:/usuario/gestionarUsuarios";
		}
		model.addAttribute("usuario", usuario);
		model.addAttribute("listaCategorias", categoriaUseCase.listarCategoria());
		return "editarUsuario";
	}

	@PostMapping("/editarUsuario")
	public String editarUsuario(RedirectAttributes redirectAttribute, Model model, @RequestParam Integer idUsuario,
			@RequestParam String username, @RequestParam String password, @RequestParam Boolean estado,
			@RequestParam Integer idCategoria) {

		Categoria categoria = categoriaUseCase.obtenerCategoria(idCategoria);

		Usuario usuario = usuarioUseCase.obtenerUsuario(idUsuario);
		usuario.setUsername(username);
		usuario.setPassword(password);
		usuario.setEstado(estado);
		usuario.setCategoria(categoria);

		if (usuarioUseCase.actualizarUsuario(usuario) <= 0) {
			redirectAttribute.addFlashAttribute("mensajeError",
					"Ha ocurrido un error al momento de actualizar al usuario, intentalo mas tarde.");
			return "redirect:/usuario/editarUsuario/" + idUsuario;
		}

		redirectAttribute.addFlashAttribute("mensajeExito", "Usuario actualizado con exito.");
		return "redirect:/usuario/editarUsuario/" + idUsuario;
	}

}
