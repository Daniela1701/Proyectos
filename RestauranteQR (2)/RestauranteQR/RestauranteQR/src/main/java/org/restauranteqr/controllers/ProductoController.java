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
import org.restauranteqr.usecase.ReporteUseCase;
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

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/producto")
public class ProductoController {

	@Autowired
	private ReporteUseCase reporteUseCase;

	@Autowired
	private CarritoUseCase carritoUseCase;

	@Autowired
	private ProductoUseCase productoUseCase;

	@Autowired
	private DescuentoUseCase descuentoUseCase;

	@GetMapping("/gestionarProductos")
	public String gestionarProductos(Model model, HttpSession session) {

		if (!Autenticar.getInstancia().autenticado(session))
			return "redirect:/usuario/login";
		if (!Autenticar.getInstancia().acceso(session, 2, 3))
			return "redirect:/aplicacion/home";

		model.addAttribute("listaProductos", productoUseCase.listarProducto());
		return "gestionarProductos";
	}

	@GetMapping("/reporte/{id}")
	public void verReporteProducto(@PathVariable int id, HttpServletResponse response, HttpSession session) {

		if (!Autenticar.getInstancia().autenticado(session))
			return;
		if (!Autenticar.getInstancia().acceso(session, 2, 3))
			return;

		try {
			Producto producto = productoUseCase.obtenerProducto(id);
			byte[] pdfBytes = reporteUseCase.generarReporteProducto("Producto", producto);

			response.setContentType("application/pdf");

			response.setHeader("Content-Disposition", "attachment; filename=producto-" + id + ".pdf");

			response.getOutputStream().write(pdfBytes);
			response.getOutputStream().flush();
		} catch (Exception e) {
			throw new RuntimeException("Error al generar el reporte del producto", e);
		}
	}

	@GetMapping("/registroProducto")
	public String registroProducto(Model model, HttpSession session) {

		if (!Autenticar.getInstancia().autenticado(session))
			return "redirect:/usuario/login";
		if (!Autenticar.getInstancia().acceso(session, 2, 3))
			return "redirect:/aplicacion/home";

		return "registroProducto";
	}

	@PostMapping("/registroProducto")
	public String registroProducto(RedirectAttributes redirectAttributes, Model model, @RequestParam String nombre,
			@RequestParam String categoria, @RequestParam Double precio, @RequestParam Integer stock,
			@RequestParam Boolean estado, @RequestParam String url) {
		Producto producto = new Producto();
		producto.setNombre(nombre);
		producto.setCategoria(categoria);
		producto.setPrecio(precio);
		producto.setStock(stock);
		producto.setEstado(estado);
		producto.setUrl(url);

		producto.setIdProducto(productoUseCase.agregarProducto(producto));
		if (producto.getIdProducto() <= 0) {
			redirectAttributes.addFlashAttribute("mensajeError",
					"El producto no se ha podido registrar, intentelo mas tarde.");
			return "redirect:/producto/registroProducto";
		}
		Descuento descuento = new Descuento();
		descuento.setEstado(false);
		descuento.setValorDescuento(0.0);
		descuento.setFechaFin(null);
		descuento.setFechaInicio(null);
		descuento.setProducto(producto);

		if (descuentoUseCase.agregarDescuento(descuento) <= 0) {
			redirectAttributes.addFlashAttribute("mensajeError",
					"El producto no se ha podido registrar, intentelo mas tarde.");
			return "redirect:/producto/registroProducto";
		}

		redirectAttributes.addFlashAttribute("mensajeExito", "El producto se ha registrado con exito.");
		return "redirect:/producto/registroProducto";
	}

	@GetMapping("/editarProducto/{idProducto}")
	public String editarProducto(Model model, HttpSession session, @PathVariable Integer idProducto) {

		if (!Autenticar.getInstancia().autenticado(session))
			return "redirect:/usuario/login";
		if (!Autenticar.getInstancia().acceso(session, 2, 3))
			return "redirect:/aplicacion/home";

		Producto producto = productoUseCase.obtenerProducto(idProducto);
		if (producto == null) {
			return "redirect:/producto/gestionarProductos";
		}

		model.addAttribute("producto", producto);

		return "editarProducto";
	}

	@PostMapping("/editarProducto")
	public String editarProducto(RedirectAttributes redirectAttributes, Model model, @RequestParam Integer idProducto,
			@RequestParam String nombre, @RequestParam String categoria, @RequestParam Double precio,
			@RequestParam Integer stock, @RequestParam String url, @RequestParam Boolean estado) {

		Producto producto = productoUseCase.obtenerProducto(idProducto);
		producto.setNombre(nombre);
		producto.setCategoria(categoria);
		producto.setPrecio(precio);
		producto.setStock(stock);
		producto.setUrl(url);
		producto.setEstado(estado);

		if (productoUseCase.actualizarProducto(producto) <= 0) {
			redirectAttributes.addFlashAttribute("mensajeError",
					"Ha ocurrido un error al momento de actualizar el producto, intentelo mas tarde.");
			return "editarProducto";
		}
		redirectAttributes.addFlashAttribute("mensajeExito", "Producto actualizado con éxito.");
		return "redirect:/producto/editarProducto/" + idProducto;
	}

	// No requiere autenticacion
	@GetMapping("/listarProductos/{categoria}")
	public String listarProductos(Model model, @PathVariable String categoria) {
		model.addAttribute("listaProductos", productoUseCase.listarProductoSinDescuento(categoria));
		model.addAttribute("categoria", categoria);
		return "listarProductos";
	}

	@SuppressWarnings("unchecked")
	@PostMapping("/agregarCarrito")
	public String agregarCarrito(RedirectAttributes redirectAttributes, Model model, HttpSession session,
			@RequestParam Integer idProducto, @RequestParam String categoria) {
		
		if (!Autenticar.getInstancia().autenticado(session))
			return "redirect:/usuario/login";
		List<ItemCarrito> carrito = (List<ItemCarrito>) session.getAttribute("carrito");

		if (carrito == null)
			carrito = new ArrayList<>();
		Producto producto = productoUseCase.obtenerProducto(idProducto);
		ItemCarrito itemCarrito = new ItemCarrito();
		itemCarrito.setIdProducto(producto.getIdProducto());
		itemCarrito.setNombreProducto(producto.getNombre());
		itemCarrito.setCantidad(1);
		itemCarrito.setStock(producto.getStock());
		itemCarrito.setPrecioOriginal(producto.getPrecio());
		itemCarrito.setPrecio(producto.getPrecio());

		carritoUseCase.agregarItemCarrito(carrito, itemCarrito);

		redirectAttributes.addFlashAttribute("mensajeExito",
				"El producto se ha agregado al carrito correctamente: <br>" + LocalDateTime.now());
		session.setAttribute("carrito", carrito);

		return "redirect:/producto/listarProductos/" + categoria;
	}

}
