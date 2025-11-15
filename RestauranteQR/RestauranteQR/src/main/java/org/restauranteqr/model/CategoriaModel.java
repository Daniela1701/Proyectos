package org.restauranteqr.model;

import java.util.List;
import org.restauranteqr.entity.Categoria;
import org.restauranteqr.persitence.CategoriaRepository;
import org.restauranteqr.usecase.CategoriaUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoriaModel implements CategoriaUseCase{

	@Autowired
	private CategoriaRepository categoriaRepository;
	
	@Override
	public List<Categoria> listarCategoria() {
		return categoriaRepository.listarCategoria();
	}

	@Override
	public Categoria obtenerCategoria(Integer idCategoria) {
		return categoriaRepository.obtenerCategoria(idCategoria);
	}

}
