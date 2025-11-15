package org.restauranteqr.model;

import java.util.List;

import org.restauranteqr.entity.Detalle;
import org.restauranteqr.persitence.DetalleRepository;
import org.restauranteqr.usecase.DetalleUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DetalleModel implements DetalleUseCase{

	@Autowired
	private DetalleRepository detalleRepository;
	
	@Override
	public List<Detalle> listarDetalle() {
		return detalleRepository.listarDetalle();
	}

	@Override
	public List<Detalle> listarDetalle(Integer idPedido) {
		return detalleRepository.listarDetalle(idPedido);
	}

	@Override
	public Integer agregarDetalle(Detalle detalle) {
		return detalleRepository.agregarDetalle(detalle);
	}

}
