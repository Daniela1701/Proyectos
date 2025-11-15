package org.restauranteqr.controllers;

import org.restauranteqr.usecase.ProductoUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/aplicacion")
public class HomeController {

	@Autowired
	private ProductoUseCase productoUseCase;

	@GetMapping("/home")
	public String home(Model model, HttpSession session) {
		model.addAttribute("listaProductos", productoUseCase.listarProducto(true));
		return "home";
	}
	
	@GetMapping("/contacto")
	public String contacto() {
	    return "contacto";
	}
	
	@GetMapping("/integrantes")
	public String integrantes() {
	    return "integrantes";
	}
}
