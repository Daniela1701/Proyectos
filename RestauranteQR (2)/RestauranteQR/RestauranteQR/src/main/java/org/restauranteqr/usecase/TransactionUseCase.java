package org.restauranteqr.usecase;

import java.util.List;
import org.restauranteqr.entity.ItemCarrito;
import org.restauranteqr.entity.Perfil;
import org.restauranteqr.entity.Sede;
import org.restauranteqr.entity.Usuario;
import org.springframework.stereotype.Component;

@Component
public interface TransactionUseCase {

	public Integer registrarUsuario(Usuario usuario, Perfil perfil);
	public Integer actualizarPerfil(Usuario usuario, Perfil perfil);
	public Integer registrarPedido(Usuario usuario, Sede sede, List<ItemCarrito> carrito);
}
