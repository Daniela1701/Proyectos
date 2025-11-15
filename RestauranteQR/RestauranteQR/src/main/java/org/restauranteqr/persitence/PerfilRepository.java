package org.restauranteqr.persitence;

import java.util.List;

import org.restauranteqr.entity.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PerfilRepository extends JpaRepository<Perfil, Integer>{

	@Query("SELECT p FROM Perfil p")
	public List<Perfil> listarPerfil();
	
	@Query("SELECT p FROM Perfil p WHERE p.idPerfil = :idPerfil")
	public Perfil obtenerPerfil(@Param("idPerfil") Integer idPerfil);
}
