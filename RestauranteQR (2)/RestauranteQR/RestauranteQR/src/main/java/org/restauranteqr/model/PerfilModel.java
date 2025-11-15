package org.restauranteqr.model;

import java.util.List;

import org.restauranteqr.entity.Perfil;
import org.restauranteqr.persitence.PerfilRepository;
import org.restauranteqr.usecase.PerfilUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PerfilModel implements PerfilUseCase{

	@Autowired
	private PerfilRepository perfilRepository;
	
	@Override
	public List<Perfil> listarPerfil() {
		return perfilRepository.listarPerfil();
	}

	@Override
	public Integer agregarPerfil(Perfil perfil) {
		return perfilRepository.save(perfil).getIdPerfil();
	}

	@Override
	public Integer actualizarPerfil(Perfil perfil) {
		return perfilRepository.save(perfil).getIdPerfil();
	}

	@Override
	public Perfil obtenerPerfil(Integer idPerfil) {
		return perfilRepository.obtenerPerfil(idPerfil);
	}

}
