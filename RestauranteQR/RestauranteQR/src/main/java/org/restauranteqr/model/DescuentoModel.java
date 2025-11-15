package org.restauranteqr.model;

import java.util.List;

import org.restauranteqr.entity.Descuento;
import org.restauranteqr.persitence.DescuentoRepository;
import org.restauranteqr.usecase.DescuentoUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DescuentoModel implements DescuentoUseCase{

	@Autowired
	private DescuentoRepository descuentoRepository;

	@Override
	public List<Descuento> listarDescuento() {
		return descuentoRepository.listarDescuento();
	}
	
	@Override
	public List<Descuento> listarDescuento(Boolean estado) {
		return descuentoRepository.listarDescuento(estado);
	}
	
	@Override
	public List<Descuento> listarDescuentoByCategoria(String categoria) {
		return descuentoRepository.listarDescuentoByCategoria(categoria);
	}
	
	@Override
	public Integer agregarDescuento(Descuento descuento) {
		return descuentoRepository.save(descuento).getIdDescuento();
	}

	@Override
	public Integer actualizarDescuento(Descuento descuento) {
		return descuentoRepository.save(descuento).getIdDescuento();
	}

	@Override
	public Descuento obtenerDescuento(Integer idDescuento) {
		return descuentoRepository.obtenerDescuento(idDescuento);
	}

}
