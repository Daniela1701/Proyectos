package org.restauranteqr.model;

import java.util.List;

import org.restauranteqr.entity.Usuario;
import org.restauranteqr.persitence.UsuarioRepository;
import org.restauranteqr.usecase.UsuarioUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UsuarioModel implements UsuarioUseCase{

	@Autowired
	private UsuarioRepository usuarioRepository;
	
	@Override
	public List<Usuario> listarUsuario() {
		return usuarioRepository.listarUsuario();
	}

	@Override
	public Integer agregarUsuario(Usuario usuario) {
		return usuarioRepository.save(usuario).getIdUsuario();
	}

	@Override
	public Integer actualizarUsuario(Usuario usuario) {
		return usuarioRepository.save(usuario).getIdUsuario();
	}

	@Override
	public Usuario obtenerUsuario(Integer idUsuario) {
		return usuarioRepository.obtenerUsuario(idUsuario);
	}

	@Override
	public Usuario verificarUsuario(String username, String password) {
		return usuarioRepository.verificarUsuario(username, password);
	}

	@Override
	public Usuario buscarPorUsernameOCorreo(String username, String correo) {
		return usuarioRepository.buscarPorUsernameOCorreo(username, correo);
	}

}
