package org.restauranteqr.model;

import java.util.List;

import org.restauranteqr.entity.Sede;
import org.restauranteqr.persitence.SedeRepository;
import org.restauranteqr.usecase.SedeUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SedeModel implements SedeUseCase{

	@Autowired
	private SedeRepository sedeRepository;

	@Override
	public List<Sede> listarSede() {
		return sedeRepository.listarSede();
	}

	@Override
	public Sede obtenerSede(Integer idSede) {
		return sedeRepository.obtenerSede(idSede);
	}
	
}
