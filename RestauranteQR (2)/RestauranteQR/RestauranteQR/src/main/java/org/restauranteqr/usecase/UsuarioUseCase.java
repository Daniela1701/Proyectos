package org.restauranteqr.usecase;

import java.util.List;
import org.restauranteqr.entity.Usuario;
import org.springframework.stereotype.Component;

@Component
public interface UsuarioUseCase {

	public List<Usuario> listarUsuario();
	public Integer agregarUsuario(Usuario usuario);
	public Integer actualizarUsuario(Usuario usuario);
	public Usuario obtenerUsuario(Integer idUsuario);
	public Usuario verificarUsuario(String username, String password);
	public Usuario buscarPorUsernameOCorreo(String username,String correo);
}
