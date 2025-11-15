package org.restauranteqr.controllers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.restauranteqr.entity.Descuento;
import org.restauranteqr.entity.ItemCarrito;
import org.restauranteqr.entity.Producto;
import org.restauranteqr.usecase.CarritoUseCase;
import org.restauranteqr.usecase.DescuentoUseCase;
import org.restauranteqr.usecase.ProductoUseCase;
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
@RequestMapping("/descuento")
public class DescuentoController {

	@Autowired
	private ProductoUseCase productoUseCase;

	@Autowired
	private CarritoUseCase carritoUseCase;

	@Autowired
	private DescuentoUseCase descuentoUseCase;

	// No requiere autenticacion
	@GetMapping("/listarDescuentos")
	public String descuento(Model model, HttpSession session) {
		model.addAttribute("descuento", descuentoUseCase.listarDescuento(true));
		return "listarDescuentos";
	}

	@SuppressWarnings("unchecked")
	@PostMapping("/agregarCarrito")
	public String agregarCarrito(RedirectAttributes redirectAttributes, Model model, HttpSession session,
			@RequestParam Integer idDescuento) {
		if (!Autenticar.getInstancia().autenticado(session))
			return "redirect:/usuario/login";
		List<ItemCarrito> carrito = (List<ItemCarrito>) session.getAttribute("carrito");

		if (carrito == null)
			carrito = new ArrayList<>();

		Descuento descuento = descuentoUseCase.obtenerDescuento(idDescuento);
		ItemCarrito itemCarrito = new ItemCarrito();
		itemCarrito.setIdProducto(descuento.getProducto().getIdProducto());
		itemCarrito.setNombreProducto(descuento.getProducto().getNombre());
		itemCarrito.setCantidad(1);
		itemCarrito.setStock(descuento.getProducto().getStock());
		itemCarrito.setPrecioOriginal(descuento.getProducto().getPrecio());
		itemCarrito.setPrecio(descuento.getProducto().getPrecio() - descuento.getValorDescuento());

		carritoUseCase.agregarItemCarrito(carrito, itemCarrito);
		redirectAttributes.addFlashAttribute("mensajeExito",
				"El producto se ha agregado al carrito correctamente: <br>" + LocalDateTime.now());
		session.setAttribute("carrito", carrito);

		return "redirect:/descuento/listarDescuentos";
	}

	@GetMapping("/gestionarDescuentos")
	public String gestionarDescuentos(Model model, HttpSession session) {

		if (!Autenticar.getInstancia().autenticado(session))
			return "redirect:/usuario/login";
		if (!Autenticar.getInstancia().acceso(session, 3))
			return "redirect:/aplicacion/home";

		model.addAttribute("listarDescuentos", descuentoUseCase.listarDescuento());
		return "gestionarDescuentos";
	}

	@GetMapping("/editarDescuento/{idDescuento}")
	public String editarDescuento(Model model, HttpSession session, @PathVariable Integer idDescuento) {

		if (!Autenticar.getInstancia().autenticado(session))
			return "redirect:/usuario/login";
		if (!Autenticar.getInstancia().acceso(session, 3))
			return "redirect:/aplicacion/home";

		model.addAttribute("descuento", descuentoUseCase.obtenerDescuento(idDescuento));
		return "editarDescuento";
	}

	@PostMapping("/editarDescuento")
	public String editarDescuento(RedirectAttributes redirectAttributes, HttpSession session,
			@RequestParam Integer idDescuento, @RequestParam Integer idProducto, @RequestParam Double valorDescuento,
			@RequestParam LocalDateTime fechaInicio, @RequestParam LocalDateTime fechaFinal,
			@RequestParam Boolean estado) {
		Producto producto = productoUseCase.obtenerProducto(idProducto);
		if (producto == null) {
			redirectAttributes.addFlashAttribute("mensajeError", "El producto seleccionado no existe.");
			return "redirect:/descuento/editarDescuento/" + idProducto;
		}

		if (producto.getPrecio() <= valorDescuento) {
			redirectAttributes.addFlashAttribute("mensajeError",
					"El precio del descuento no puede ser mayor al precio del producto. <br>Precio del producto: "
							+ producto.getPrecio());
			return "redirect:/descuento/editarDescuento/" + idProducto;
		}

		Descuento descuento = new Descuento();
		descuento.setIdDescuento(idDescuento);
		descuento.setValorDescuento(valorDescuento);
		descuento.setFechaInicio(fechaInicio);
		descuento.setFechaFin(fechaFinal);
		descuento.setEstado(estado);
		descuento.setProducto(producto);

		if (descuentoUseCase.actualizarDescuento(descuento) <= 0) {
			redirectAttributes.addFlashAttribute("mensajeError",
					"Ha ocurrido un error al momento de registrar el descuento, intentelo mas tarde.");
			return "redirect:/descuento/editarDescuento/" + idProducto;
		}

		redirectAttributes.addFlashAttribute("mensajeExito", "El descuento se ha registrado de manera correcta.");
		return "redirect:/descuento/editarDescuento/" + idProducto;
	}
}
