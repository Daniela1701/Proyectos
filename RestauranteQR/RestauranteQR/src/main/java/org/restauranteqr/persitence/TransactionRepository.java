package org.restauranteqr.persitence;

import java.time.LocalDateTime;
import java.util.List;
import org.restauranteqr.entity.Detalle;
import org.restauranteqr.entity.ItemCarrito;
import org.restauranteqr.entity.Pedido;
import org.restauranteqr.entity.Perfil;
import org.restauranteqr.entity.Producto;
import org.restauranteqr.entity.Sede;
import org.restauranteqr.entity.Usuario;
import org.restauranteqr.usecase.CarritoUseCase;
import org.restauranteqr.usecase.DetalleUseCase;
import org.restauranteqr.usecase.PedidoUseCase;
import org.restauranteqr.usecase.ProductoUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Component
public class TransactionRepository {

	@Autowired
	private CarritoUseCase carritoUseCase;

	@Autowired
	private ProductoUseCase productoUseCase;

	@Autowired
	private PedidoUseCase pedidoUseCase;

	@Autowired
	private DetalleUseCase detalleUseCase;

	@PersistenceContext
	private EntityManager entityManager;

	@Transactional
	public Integer registrarUsuario(Usuario usuario, Perfil perfil) {
		entityManager.createQuery(
				"INSERT INTO Usuario(username, password, estado, categoria.idCategoria) VALUES(:username, :password, :estado, :idCategoria)")
				.setParameter("username", usuario.getUsername()).setParameter("password", usuario.getPassword())
				.setParameter("estado", true).setParameter("idCategoria", 1).executeUpdate();

		Integer idUsuario = ((Number) entityManager.createNativeQuery("SELECT LAST_INSERT_ID()").getSingleResult())
				.intValue();

		entityManager.createQuery(
				"INSERT INTO Perfil(nombre, apellido, correo, usuario.idUsuario) VALUES(:nombre, :apellido, :correo, :idUsuario)")
				.setParameter("nombre", perfil.getNombre()).setParameter("apellido", perfil.getApellido())
				.setParameter("correo", perfil.getCorreo()).setParameter("idUsuario", idUsuario).executeUpdate();

		return idUsuario;
	}

	@Transactional
	public Integer registrarPedido(Usuario usuario, Sede sede, List<ItemCarrito> carrito) {
		try {

			entityManager.createNativeQuery(
					"INSERT INTO pedido (fecha, subtotal, igv, total, id_usuario, id_sede) VALUES (?, ?, ?, ?, ?, ?)")
					.setParameter(1, LocalDateTime.now())
					.setParameter(2, carritoUseCase.obtenerSubtotalCarrito(carrito))
					.setParameter(3, carritoUseCase.obtenerIgvCarrito(carrito))
					.setParameter(4, carritoUseCase.obtenerTotalCarrito(carrito))
					.setParameter(5, usuario.getIdUsuario()).setParameter(6, sede.getIdSede()).executeUpdate();

			Integer idPedido = ((Number) entityManager.createNativeQuery("SELECT LAST_INSERT_ID()").getSingleResult())
					.intValue();
			Pedido pedido = pedidoUseCase.obtenerPedido(idPedido);

			for (ItemCarrito itemCarrito : carrito) {
				Producto producto = productoUseCase.obtenerProducto(itemCarrito.getIdProducto());
				Detalle detalle = new Detalle();
				detalle.setCantidad(itemCarrito.getCantidad());
				detalle.setPrecioUnitario(itemCarrito.getPrecio());
				detalle.setSubtotal(itemCarrito.getCantidad() * itemCarrito.getPrecio());
				detalle.setPedido(pedido);
				detalle.setProducto(producto);
				detalleUseCase.agregarDetalle(detalle);

				producto.setStock(producto.getStock() - itemCarrito.getCantidad());
				if (producto.getStock() == 0) {
					producto.setEstado(false);
				}
				productoUseCase.actualizarProducto(producto);
			}
			return idPedido;
		} catch (Exception e) {
			throw new RuntimeException("Error al registrar pedido y sus detalles", e);
		}
	}

	@Transactional
	public Integer actualizarPerfil(Usuario usuario, Perfil perfil) {
		try {
			// Actualiza Perfil (por id del usuario)
			entityManager.createQuery(
					"UPDATE Perfil p SET p.nombre = :nombre, p.apellido = :apellido, p.correo = :correo WHERE p.usuario.idUsuario = :idUsuario")
					.setParameter("nombre", perfil.getNombre()).setParameter("apellido", perfil.getApellido())
					.setParameter("correo", perfil.getCorreo()).setParameter("idUsuario", usuario.getIdUsuario())
					.executeUpdate();

			return usuario.getIdUsuario();
		} catch (Exception e) {
			throw new RuntimeException("Error al actualizar el perfil y usuario", e);
		}
	}

}
