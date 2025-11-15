package org.restauranteqr.usecase;

import java.util.List;
import org.restauranteqr.entity.Detalle;
import org.springframework.stereotype.Component;

@Component
public interface DetalleUseCase {

	public List<Detalle> listarDetalle();
	public List<Detalle> listarDetalle(Integer idPedido);
	public Integer agregarDetalle(Detalle detalle);
}
