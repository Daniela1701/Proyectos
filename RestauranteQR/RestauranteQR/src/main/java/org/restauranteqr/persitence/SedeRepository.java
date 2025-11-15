package org.restauranteqr.persitence;

import java.util.List;

import org.restauranteqr.entity.Sede;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
public class SedeRepository {

	// TRANSACCIONAL
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@Transactional(readOnly = true)
	public List<Sede> listarSede(){
		return entityManager.createQuery("SELECT s FROM Sede s", Sede.class).getResultList();
	}
	
	@Transactional(readOnly = true)
	public Sede obtenerSede(Integer idSede) {
		return entityManager.createQuery("SELECT s FROM Sede s WHERE s.idSede = :idSede", Sede.class)
				.setParameter("idSede", idSede).getSingleResult();
	}
}
