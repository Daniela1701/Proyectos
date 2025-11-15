package org.restauranteqr.controllers;

import java.util.List;
import org.restauranteqr.entity.ItemCarrito;
import org.restauranteqr.entity.Producto;
import org.restauranteqr.entity.Sede;
import org.restauranteqr.entity.Usuario;
import org.restauranteqr.usecase.ProductoUseCase;
import org.restauranteqr.usecase.SedeUseCase;
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
@RequestMapping("/carrito")
public class CarritoController {

	@Autowired
	private SedeUseCase sedeUseCase;

	@Autowired
	private ProductoUseCase productoUseCase;

	@Autowired
	private TransactionUseCase transactionUseCase;

	@SuppressWarnings("unchecked")
	@GetMapping("verCarrito")
	public String verCarrito(Model model, HttpSession session) {

		if (!Autenticar.getInstancia().autenticado(session))
			return "redirect:/usuario/login";
		if (!Autenticar.getInstancia().acceso(session, 1, 2, 3))
			return "redirect:/aplicacion/home";

		List<ItemCarrito> carrito = (List<ItemCarrito>) session.getAttribute("carrito");
		if (carrito == null || carrito.isEmpty()) {
			model.addAttribute("mensajeInfo", "Tu carrito está tan vacío como tu estómago. ¡Agrega algo rico!");
		} else {
			for (ItemCarrito item : carrito) {
				Producto producto = productoUseCase.obtenerProducto(item.getIdProducto());
				if (item.getCantidad() > producto.getStock()) {
					item.setCantidad(producto.getStock());
				}
			}
			model.addAttribute("carrito", carrito);
			model.addAttribute("sede", sedeUseCase.listarSede());
		}

		return "verCarrito";
	}

	@PostMapping("/aplicarVenta")
	@SuppressWarnings("unchecked")
	public String aplicarVenta(@RequestParam List<Integer> idProductos, @RequestParam List<Integer> cantidades,
			@RequestParam Integer idSede, HttpSession session, RedirectAttributes redirectAttributes) {

		Usuario usuarioLogin = (Usuario) session.getAttribute("usuario");
		if (usuarioLogin == null) {
			return "redirect:/usuario/login";
		}

		List<ItemCarrito> carrito = (List<ItemCarrito>) session.getAttribute("carrito");

		if (carrito == null || carrito.isEmpty()) {
			redirectAttributes.addFlashAttribute("mensajeInfo", "No hay productos en el carrito.");
			return "redirect:/carrito/verCarrito";
		}

		for (int i = 0; i < idProductos.size(); i++) {
			Integer idProducto = idProductos.get(i);
			Integer nuevaCantidad = cantidades.get(i);
			System.out.println("ANTES: " + idProducto + " --- " + nuevaCantidad);
			for (ItemCarrito item : carrito) {
				System.out.println("DURANTE: " + item.getCantidad());
				if (item.getIdProducto().equals(idProducto)) {
					item.setCantidad(nuevaCantidad);
					System.out.println("DESPUES: " + item.getCantidad());
					break;
				}
			}
		}

		Sede sede = sedeUseCase.obtenerSede(idSede);
		if (sede == null) {
			redirectAttributes.addFlashAttribute("mensajeError", "Sede no encontrada.");
			return "redirect:/carrito/verCarrito";
		}

		transactionUseCase.registrarPedido(usuarioLogin, sede, carrito);
		session.removeAttribute("carrito");
		redirectAttributes.addFlashAttribute("mensajeExito", "¡Venta aplicada con éxito!");
		return "redirect:/carrito/verCarrito";
	}

	@SuppressWarnings("unchecked")
	@PostMapping("/eliminarItem")
	public String eliminarItemCarrito(@RequestParam Integer idProducto, HttpSession session,
			RedirectAttributes redirectAttributes) {

		List<ItemCarrito> carrito = (List<ItemCarrito>) session.getAttribute("carrito");

		if (carrito != null) {
			carrito.removeIf(item -> item.getIdProducto().equals(idProducto));
			session.setAttribute("carrito", carrito);

			if (carrito.isEmpty()) {
				session.removeAttribute("carrito");
				redirectAttributes.addFlashAttribute("mensajeInfo", "Se eliminó el último producto del carrito.");
			} else {
				redirectAttributes.addFlashAttribute("mensajeExito", "Producto eliminado del carrito.");
			}
		}

		return "redirect:/carrito/verCarrito";
	}

}
