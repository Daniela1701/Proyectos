package org.restauranteqr.usecase;

import java.util.List;
import org.restauranteqr.entity.Categoria;
import org.springframework.stereotype.Component;

@Component
public interface CategoriaUseCase {

	public List<Categoria> listarCategoria();
	public Categoria obtenerCategoria(Integer idCategoria);
}
