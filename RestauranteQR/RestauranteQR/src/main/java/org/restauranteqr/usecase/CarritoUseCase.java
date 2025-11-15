package org.restauranteqr.usecase;

import java.util.List;

import org.restauranteqr.entity.ItemCarrito;
import org.springframework.stereotype.Component;

@Component
public interface CarritoUseCase {

	public List<ItemCarrito> listarCarrito(List<ItemCarrito> items);
	public ItemCarrito buscarProductoCarritoByID(List<ItemCarrito> items, Integer idProducto);
	public Boolean agregarItemCarrito(List<ItemCarrito> items, ItemCarrito itemCarrito);
	public Boolean eliminarItemCarrito(List<ItemCarrito> items, Integer idProducto);
	public Double obtenerSubtotalCarrito(List<ItemCarrito> items);
	public Double obtenerIgvCarrito(List<ItemCarrito> items);
	public Double obtenerTotalCarrito(List<ItemCarrito> items);
	public Integer tamanioCarrito(List<ItemCarrito> items);
	public Boolean existeItemCarrito(List<ItemCarrito> items, Integer idProducto);
	
}
