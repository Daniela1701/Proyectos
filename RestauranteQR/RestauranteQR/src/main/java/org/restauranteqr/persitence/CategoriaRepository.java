package org.restauranteqr.persitence;

import java.util.List;

import org.restauranteqr.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Integer>{

	@Query("SELECT c FROM Categoria c")
	public List<Categoria> listarCategoria();
	
	@Query("SELECT c FROM Categoria c WHERE c.idCategoria = :idCategoria")
	public Categoria obtenerCategoria(@Param("idCategoria") Integer idCategoria);
}
