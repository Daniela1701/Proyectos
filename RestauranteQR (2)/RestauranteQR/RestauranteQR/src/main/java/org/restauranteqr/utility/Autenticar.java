package org.restauranteqr.utility;

import org.restauranteqr.entity.Usuario;

import jakarta.servlet.http.HttpSession;

public class Autenticar {

	private static Autenticar instancia;
	public static Autenticar getInstancia() {
		if(instancia == null) {
			instancia = new Autenticar();
		}
		return instancia;
	}
	
	public boolean autenticado(HttpSession session) {
	    return session.getAttribute("usuario") != null;
	}

	public boolean acceso(HttpSession session, int... idCategoriasAcceso) {
	    Usuario usuario = (Usuario) session.getAttribute("usuario");
	    if (usuario == null) return false;

	    int idUsuarioCategoria = usuario.getCategoria().getIdCategoria();
	    for (int id : idCategoriasAcceso) {
	        if (idUsuarioCategoria == id) return true;
	    }

	    return false;
	}
}
