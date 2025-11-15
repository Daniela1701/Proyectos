package org.restauranteqr.persitence;

import java.util.List;

import org.restauranteqr.entity.Detalle;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
public class DetalleRepository {

	// TRANSACCIONAL
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@Transactional(readOnly = true)
	public List<Detalle> listarDetalle(){
		return entityManager.createQuery("SELECT d FROM Detalle d", Detalle.class).getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<Detalle> listarDetalle(Integer idPedido){
		return entityManager.createQuery("SELECT d FROM Detalle d WHERE d.pedido.idPedido = :idPedido", Detalle.class)
				.setParameter("idPedido", idPedido).getResultList();
	}
	
	@Transactional
	public Integer agregarDetalle(Detalle detalle) {
		return entityManager.createQuery("INSERT INTO Detalle(cantidad, precioUnitario, subtotal, producto.idProducto, pedido.idPedido) VALUES(:cantidad, :precioUnitario, :subtotal, :idProducto, :idPedido)")
				.setParameter("cantidad", detalle.getCantidad())
				.setParameter("precioUnitario", detalle.getProducto().getPrecio())
				.setParameter("subtotal", detalle.getSubtotal())
				.setParameter("idProducto", detalle.getProducto().getIdProducto())
				.setParameter("idPedido", detalle.getPedido().getIdPedido()).executeUpdate();
	}

}
