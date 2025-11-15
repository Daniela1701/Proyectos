package org.restauranteqr.usecase;

import java.util.List;
import org.restauranteqr.entity.Descuento;
import org.springframework.stereotype.Component;

@Component
public interface DescuentoUseCase {

	public List<Descuento> listarDescuento();
	public List<Descuento> listarDescuento(Boolean estado);
	public List<Descuento> listarDescuentoByCategoria(String categoria);
	public Integer agregarDescuento(Descuento descuento);
	public Integer actualizarDescuento(Descuento descuento);
	public Descuento obtenerDescuento(Integer idDescuento);
}
