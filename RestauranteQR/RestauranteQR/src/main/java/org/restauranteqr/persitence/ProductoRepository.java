package org.restauranteqr.persitence;

import java.util.List;

import org.restauranteqr.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer>{

	@Query("SELECT p FROM Producto p")
	public List<Producto> listarProducto();
	
	@Query("SELECT p FROM Producto p WHERE p.estado = :estado")
	public List<Producto> listarProducto(@Param("estado") Boolean estado);
	
	@Query("SELECT p FROM Producto p JOIN p.descuento d WHERE d.estado = false AND p.estado = true")
	public List<Producto> listarProductoSinDescuento();

	@Query("SELECT p FROM Producto p JOIN p.descuento d WHERE d.estado = false AND p.estado = true AND LOWER(p.categoria) = LOWER(:categoria)")
	public List<Producto> listarProductoSinDescuento(@Param("categoria") String categoria);
	
	@Query("SELECT p FROM Producto p WHERE p.idProducto = :idProducto")
	public Producto obtenerProducto(@Param("idProducto") Integer idProducto);
}
