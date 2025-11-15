package org.restauranteqr.model;

import java.util.List;
import org.restauranteqr.entity.ItemCarrito;
import org.restauranteqr.entity.Perfil;
import org.restauranteqr.entity.Sede;
import org.restauranteqr.entity.Usuario;
import org.restauranteqr.persitence.TransactionRepository;
import org.restauranteqr.usecase.TransactionUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransactionModel implements TransactionUseCase{

	@Autowired
	private TransactionRepository transactionRepository;
	
	@Override
	public Integer registrarUsuario(Usuario usuario, Perfil perfil) {
		return transactionRepository.registrarUsuario(usuario, perfil);
	}

	@Override
	public Integer registrarPedido(Usuario usuario, Sede sede, List<ItemCarrito> carrito) {
		return transactionRepository.registrarPedido(usuario, sede, carrito);
	}

	@Override
	public Integer actualizarPerfil(Usuario usuario, Perfil perfil) {
		return transactionRepository.actualizarPerfil(usuario, perfil);
	}

}
