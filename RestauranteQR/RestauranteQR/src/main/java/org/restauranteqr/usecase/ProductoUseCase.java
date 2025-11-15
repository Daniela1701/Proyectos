package org.restauranteqr.usecase;

import java.util.List;
import org.restauranteqr.entity.Producto;
import org.springframework.stereotype.Component;

@Component
public interface ProductoUseCase {

	public List<Producto> listarProducto();
	public List<Producto> listarProducto(Boolean estado);
	public Integer agregarProducto(Producto producto);
	public Integer actualizarProducto(Producto producto);
	public Producto obtenerProducto(Integer idProducto);
	// CONSULTAS A IMPLEMENTAR
	public List<Producto> listarProductoSinDescuento();
	public List<Producto> listarProductoSinDescuento(String categoria);
}
