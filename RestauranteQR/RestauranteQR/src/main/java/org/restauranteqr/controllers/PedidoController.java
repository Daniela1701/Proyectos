package org.restauranteqr.controllers;

import java.util.List;

import org.restauranteqr.entity.Detalle;
import org.restauranteqr.entity.Pedido;
import org.restauranteqr.entity.Usuario;
import org.restauranteqr.usecase.DetalleUseCase;
import org.restauranteqr.usecase.PedidoUseCase;
import org.restauranteqr.usecase.ReporteUseCase;
import org.restauranteqr.usecase.UsuarioUseCase;
import org.restauranteqr.utility.Autenticar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/pedido")
public class PedidoController {

	@Autowired
	private ReporteUseCase reporteUseCase;

	@Autowired
	private DetalleUseCase detalleUseCase;

	@Autowired
	private PedidoUseCase pedidoUseCase;

	@Autowired
	private UsuarioUseCase usuarioUseCase;

	@GetMapping("/listarPedidos")
	public String listarPedidos(Model model, HttpSession session) {

		if (!Autenticar.getInstancia().autenticado(session))
			return "redirect:/usuario/login";
		if (!Autenticar.getInstancia().acceso(session, 3))
			return "redirect:/aplicacion/home";

		List<Pedido> pedidos = pedidoUseCase.listarPedido();
		model.addAttribute("pedidos", pedidos);
		return "listarPedidos";
	}

	@GetMapping("/listarPedidos/{idUsuario}")
	public String listarPedidos(Model model, HttpSession session, @PathVariable Integer idUsuario) {

		if (!Autenticar.getInstancia().autenticado(session))
			return "redirect:/usuario/login";
		if (!Autenticar.getInstancia().acceso(session, 3))
			return "redirect:/aplicacion/home";

		List<Pedido> pedidos = pedidoUseCase.listarPedido(idUsuario);

		Usuario usuario = usuarioUseCase.obtenerUsuario(idUsuario);

		model.addAttribute("pedidos", pedidos);
		model.addAttribute("usuario", usuario);

		return "listarPedidos";
	}

	@GetMapping("/listarMisPedidos")
	public String listarMisPedidos(Model model, HttpSession session) {

		if (!Autenticar.getInstancia().autenticado(session))
			return "redirect:/usuario/login";

		Usuario usuario = (Usuario) session.getAttribute("usuario");

		List<Pedido> pedidos = pedidoUseCase.listarPedido(usuario.getIdUsuario());

		model.addAttribute("pedidos", pedidos);
		model.addAttribute("usuario", usuario);
		return "listarMisPedidos";
	}

	@GetMapping("/detalles/{idPedido}")
	public String detalles(Model model, HttpSession session, @PathVariable Integer idPedido) {

		if (!Autenticar.getInstancia().autenticado(session))
			return "redirect:/usuario/login";
		if (!Autenticar.getInstancia().acceso(session, 3))
			return "redirect:/aplicacion/home";

		Pedido pedido = pedidoUseCase.obtenerPedido(idPedido);
		List<Detalle> detalles = detalleUseCase.listarDetalle(idPedido);

		model.addAttribute("pedido", pedido);
		model.addAttribute("detalles", detalles);

		return "detalles";
	}

	@GetMapping("/misDetalles/{idPedido}")
	public String misDetalles(Model model, HttpSession session, @PathVariable Integer idPedido) {

		if (!Autenticar.getInstancia().autenticado(session))
			return "redirect:/usuario/login";

		Pedido pedido = pedidoUseCase.obtenerPedido(idPedido);
		List<Detalle> detalles = detalleUseCase.listarDetalle(idPedido);

		model.addAttribute("pedido", pedido);
		model.addAttribute("detalles", detalles);

		return "misDetalles";
	}

	@GetMapping("/boleta/{id}")
	public void generarBoleta(@PathVariable Integer id, HttpServletResponse response, HttpSession session) {
		try {
			Pedido pedido = pedidoUseCase.obtenerPedido(id);
			byte[] pdfBytes = reporteUseCase.generarReportePedido("Pedido", pedido);

			response.setContentType("application/pdf");
			response.setHeader("Content-Disposition", "attachment; filename=boleta-" + id + ".pdf");
			response.getOutputStream().write(pdfBytes);
			response.getOutputStream().flush();
		} catch (Exception e) {
			throw new RuntimeException("Error al generar la boleta", e);
		}
	}

}
