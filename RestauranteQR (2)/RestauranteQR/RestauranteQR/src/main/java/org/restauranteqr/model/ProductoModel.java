package org.restauranteqr.model;

import java.util.List;

import org.restauranteqr.entity.Producto;
import org.restauranteqr.persitence.ProductoRepository;
import org.restauranteqr.usecase.ProductoUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductoModel implements ProductoUseCase{

	@Autowired
	private ProductoRepository productoRepository;
	
	@Override
	public List<Producto> listarProducto() {
		return productoRepository.listarProducto();
	}

	@Override
	public List<Producto> listarProducto(Boolean estado) {
		return productoRepository.listarProducto(estado);
	}
	
	@Override
	public List<Producto> listarProductoSinDescuento() {
		return productoRepository.listarProductoSinDescuento();
	}

	@Override
	public List<Producto> listarProductoSinDescuento(String categoria) {
		return productoRepository.listarProductoSinDescuento(categoria);
	}
	
	@Override
	public Integer agregarProducto(Producto producto) {
		return productoRepository.save(producto).getIdProducto();
	}

	@Override
	public Integer actualizarProducto(Producto producto) {
		return productoRepository.save(producto).getIdProducto();
	}

	@Override
	public Producto obtenerProducto(Integer idProducto) {
		return productoRepository.obtenerProducto(idProducto);
	}

}
