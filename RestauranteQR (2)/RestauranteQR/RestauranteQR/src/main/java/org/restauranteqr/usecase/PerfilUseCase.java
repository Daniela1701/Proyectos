package org.restauranteqr.usecase;

import java.util.List;
import org.restauranteqr.entity.Perfil;
import org.springframework.stereotype.Component;

@Component
public interface PerfilUseCase {

	public List<Perfil> listarPerfil();
	public Integer agregarPerfil(Perfil perfil);
	public Integer actualizarPerfil(Perfil perfil);
	public Perfil obtenerPerfil(Integer idPerfil);
}
