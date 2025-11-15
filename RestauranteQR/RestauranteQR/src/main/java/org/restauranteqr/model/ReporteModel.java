package org.restauranteqr.model;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.restauranteqr.entity.Pedido;
import org.restauranteqr.entity.Perfil;
import org.restauranteqr.entity.Producto;
import org.restauranteqr.usecase.ReporteUseCase;
import org.springframework.stereotype.Service;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;

@Service
public class ReporteModel implements ReporteUseCase{

	@Override
	public byte[] generarReporteProducto(String rutaJasper, Producto producto) {
		try {
			
			InputStream reporteStream = getClass().getClassLoader().getResourceAsStream("reportes/" + rutaJasper + ".jasper");

			Map<String, Object> parametros = new HashMap<>();
			parametros.put("idProducto", producto.getIdProducto());
			parametros.put("Nombre", producto.getNombre());
			parametros.put("Categoria", producto.getCategoria());
			parametros.put("Precio", producto.getPrecio());
			parametros.put("Stock", producto.getStock());
			parametros.put("Estado", producto.getEstado() ? "Habilitado" : "Inhabilitado");
			parametros.put("Url", producto.getUrl());
			JasperPrint jasperPrint = JasperFillManager.fillReport(reporteStream, parametros, new JREmptyDataSource());
			return JasperExportManager.exportReportToPdf(jasperPrint);

		} catch (Exception e) {
		    throw new RuntimeException("Error al generar el reporte", e);
		}

	}

	@Override
	public byte[] generarReportePedido(String rutaJasper, Pedido pedido) {
		try {
	        InputStream reporteStream = getClass().getClassLoader().getResourceAsStream("reportes/" + rutaJasper + ".jasper");

	        Perfil perfil = pedido.getUsuario().getPerfil();
	        Map<String, Object> parametros = new HashMap<>();
	        parametros.put("idPedido", pedido.getIdPedido());
	        LocalDateTime fecha = pedido.getFecha();
	        String fechaFormateada = fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
	        parametros.put("fecha", fechaFormateada);
	        parametros.put("usuario", perfil.getApellido() + " " + perfil.getNombre());
	        parametros.put("sede", pedido.getSede().getNombre());
	        parametros.put("subtotal", pedido.getSubtotal());
	        parametros.put("igv", pedido.getIgv());
	        parametros.put("total", pedido.getTotal());

	        JasperPrint jasperPrint = JasperFillManager.fillReport(reporteStream, parametros, new JREmptyDataSource());
	        return JasperExportManager.exportReportToPdf(jasperPrint);

	    } catch (Exception e) {
	        throw new RuntimeException("Error al generar la boleta", e);
	    }
	}
	




}
