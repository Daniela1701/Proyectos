package org.restauranteqr.model;

import java.util.List;

import org.restauranteqr.entity.Pedido;
import org.restauranteqr.persitence.PedidoRepository;
import org.restauranteqr.usecase.PedidoUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PedidoModel implements PedidoUseCase{

	@Autowired
	private PedidoRepository pedidoRepository;
	
	@Override
	public List<Pedido> listarPedido() {
		return pedidoRepository.listarPedido();
	}

	@Override
	public List<Pedido> listarPedido(Integer idUsuario) {
		return pedidoRepository.listarPedido(idUsuario);
	}

	@Override
	public Integer agregarPedido(Pedido pedido) {
		return pedidoRepository.agregarPedido(pedido);
	}

	@Override
	public Pedido obtenerPedido(Integer idPedido) {
		return pedidoRepository.obtenerPedido(idPedido);
	}

}
