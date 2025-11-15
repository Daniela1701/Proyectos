package org.restauranteqr.model;

import java.util.List;
import org.restauranteqr.entity.ItemCarrito;
import org.restauranteqr.usecase.CarritoUseCase;
import org.springframework.stereotype.Service;

@Service
public class CarritoModel implements CarritoUseCase {

	@Override
	public List<ItemCarrito> listarCarrito(List<ItemCarrito> items) {
		return items;
	}

	@Override
	public Integer tamanioCarrito(List<ItemCarrito> items) {
		return items.size();
	}

	@Override
	public Boolean agregarItemCarrito(List<ItemCarrito> items, ItemCarrito itemCarrito) {
		// Buscar si ya existe el producto
		for (ItemCarrito item : items) {
			if (item.getIdProducto().equals(itemCarrito.getIdProducto())) {
				item.setCantidad(item.getCantidad() + 1);
				return true;
			}
		}
		
		// Solo si NO existe, generamos un nuevo ID y lo agregamos
		itemCarrito.setIdItemCarrito(generarSiguienteId(items));
		return items.add(itemCarrito);
	}

	private int generarSiguienteId(List<ItemCarrito> items) {
		int maxId = 0;
		for (ItemCarrito item : items) {
			if (item.getIdItemCarrito() != null && item.getIdItemCarrito() > maxId) {
				maxId = item.getIdItemCarrito();
			}
		}
		return maxId + 1;
	}

	@Override
	public Boolean eliminarItemCarrito(List<ItemCarrito> items, Integer idProducto) {
		return items.removeIf(item -> item.getIdProducto().equals(idProducto));
	}

	@Override
	public ItemCarrito buscarProductoCarritoByID(List<ItemCarrito> items, Integer idProducto) {
		for (ItemCarrito item : items) {
			if (item.getIdProducto().equals(idProducto)) {
				return item;
			}
		}
		return null;
	}

	@Override
	public Double obtenerSubtotalCarrito(List<ItemCarrito> items) {
		Double subtotal = 0.0;
		for(ItemCarrito item: items) {
			subtotal += item.getPrecio() * item.getCantidad();
		}
		return subtotal;
	}

	@Override
	public Double obtenerIgvCarrito(List<ItemCarrito> items) {
		Double igv = 0.18;
		return obtenerSubtotalCarrito(items) * igv;
	}
	
	@Override
	public Double obtenerTotalCarrito(List<ItemCarrito> items) {
		return obtenerSubtotalCarrito(items) + obtenerIgvCarrito(items);
	}

	@Override
	public Boolean existeItemCarrito(List<ItemCarrito> items, Integer idProducto) {
		for(ItemCarrito item: items) {
			if(item.getIdProducto() == idProducto) {
				return true;
			}
		}
		return false;
	}
}
