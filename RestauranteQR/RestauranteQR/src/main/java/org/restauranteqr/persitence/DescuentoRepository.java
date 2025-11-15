package org.restauranteqr.persitence;

import java.util.List;

import org.restauranteqr.entity.Descuento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DescuentoRepository extends JpaRepository<Descuento, Integer>{

	@Query("SELECT d FROM Descuento d JOIN d.producto p WHERE d.producto.idProducto = p.idProducto AND p.estado = true")
	public List<Descuento> listarDescuento();
	
	@Query("SELECT d FROM Descuento d JOIN d.producto p WHERE d.producto.idProducto = p.idProducto AND d.estado = :estado AND p.estado = true")
	public List<Descuento> listarDescuento(@Param("estado") Boolean estado);
	
	@Query("SELECT d FROM Descuento d WHERE d.producto.categoria = :categoria")
	public List<Descuento> listarDescuentoByCategoria(@Param("categoria") String categoria);
	
	@Query("SELECT d FROM Descuento d WHERE d.idDescuento = :idDescuento")
	public Descuento obtenerDescuento(@Param("idDescuento") Integer idDescuento);
}
