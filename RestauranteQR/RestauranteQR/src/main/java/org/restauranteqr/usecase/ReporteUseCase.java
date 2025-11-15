package org.restauranteqr.usecase;

import org.restauranteqr.entity.Pedido;
import org.restauranteqr.entity.Producto;
import org.springframework.stereotype.Component;

@Component
public interface ReporteUseCase {

	public byte[] generarReporteProducto(String rutaJasper, Producto producto);
	public byte[] generarReportePedido(String rutaJasper, Pedido pedido);
}
