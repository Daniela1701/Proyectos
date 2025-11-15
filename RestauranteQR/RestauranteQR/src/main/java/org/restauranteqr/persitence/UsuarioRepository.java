package org.restauranteqr.persitence;

import java.util.List;

import org.restauranteqr.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer>{

	@Query("SELECT u FROM Usuario u")
	public List<Usuario> listarUsuario();
	
	@Query("SELECT u FROM Usuario u WHERE u.idUsuario = :idUsuario")
	public Usuario obtenerUsuario(@Param("idUsuario") Integer idUsuario);
	
	@Query("SELECT u FROM Usuario u WHERE LOWER(u.username) = LOWER(:username) AND u.password = :password")
	public Usuario verificarUsuario(@Param("username") String username,@Param("password") String password);
	
	@Query("SELECT u FROM Usuario u WHERE LOWER(u.username) = LOWER(:username) OR LOWER(u.perfil.correo) = LOWER(:correo)")
	Usuario buscarPorUsernameOCorreo(@Param("username") String username, @Param("correo") String correo);

}
