package org.restauranteqr.persitence;

import java.util.List;
import org.restauranteqr.entity.Pedido;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
public class PedidoRepository{

	
	// TRANSACCIONAL
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@Transactional(readOnly = true)
	public List<Pedido> listarPedido(){
		return entityManager.createQuery("SELECT p FROM Pedido p", Pedido.class).getResultList();
	}
	
	
	@Transactional(readOnly = true)
	public List<Pedido> listarPedido(Integer idUsuario){
		 return entityManager.createQuery("SELECT p FROM Pedido p WHERE p.usuario.idUsuario = :idUsuario", Pedido.class)
		            .setParameter("idUsuario", idUsuario)
		            .getResultList();	
	}
	
	@Transactional
	public Integer agregarPedido(Pedido pedido) {
		entityManager.createQuery("INSERT INTO Pedido(fecha, subtotal, igv, total, usuario.idUsuario, sede.idSede) VALUES(:fecha, :subtotal, :igv, :total, :idUsuario, :idSede)")
				.setParameter("fecha", pedido.getFecha())
				.setParameter("subtotal", pedido.getSubtotal())
				.setParameter("igv", pedido.getIgv())
				.setParameter("total", pedido.getTotal())
				.setParameter("idUsuario", pedido.getUsuario().getIdUsuario())
				.setParameter("idSede", pedido.getSede().getIdSede()).executeUpdate();
		return listarPedido().size();
				
	}
	
	@Transactional(readOnly = true)
	public Pedido obtenerPedido(Integer idPedido) {
		return entityManager.createQuery("SELECT p FROM Pedido p WHERE p.idPedido = :idPedido", Pedido.class)
				.setParameter("idPedido", idPedido).getSingleResult();
	}
	
}
