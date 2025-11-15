package org.restauranteqr.usecase;

import java.util.List;
import org.restauranteqr.entity.Pedido;
import org.springframework.stereotype.Component;

@Component
public interface PedidoUseCase {

	public List<Pedido> listarPedido();
	public List<Pedido> listarPedido(Integer idUsuario);
	public Integer agregarPedido(Pedido pedido);
	public Pedido obtenerPedido(Integer idPedido);
}
