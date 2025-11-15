package org.restauranteqr.usecase;

import java.util.List;
import org.restauranteqr.entity.Sede;
import org.springframework.stereotype.Component;

@Component
public interface SedeUseCase {

	public List<Sede> listarSede();
	public Sede obtenerSede(Integer idSede);
}
