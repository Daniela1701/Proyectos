package org.restauranteqr.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemCarrito {

	private Integer idItemCarrito;
	
	private Integer idProducto;
	
	private String nombreProducto;
	
	private Double precio;
	
	private Double precioOriginal;
	
	private Integer cantidad;
	
	private Integer stock;


	public ItemCarrito() {
		super();
	}

	public Integer getIdItemCarrito() {
		return idItemCarrito;
	}

	public void setIdItemCarrito(Integer idItemCarrito) {
		this.idItemCarrito = idItemCarrito;
	}

	public Integer getIdProducto() {
		return idProducto;
	}

	public void setIdProducto(Integer idProducto) {
		this.idProducto = idProducto;
	}

	public String getNombreProducto() {
		return nombreProducto;
	}

	public void setNombreProducto(String nombreProducto) {
		this.nombreProducto = nombreProducto;
	}

	public Double getPrecio() {
		return precio;
	}

	public void setPrecio(Double precio) {
		this.precio = precio;
	}

	public Double getPrecioOriginal() {
		return precioOriginal;
	}

	public void setPrecioOriginal(Double precioOriginal) {
		this.precioOriginal = precioOriginal;
	}

	public Integer getCantidad() {
		return cantidad;
	}

	public void setCantidad(Integer cantidad) {
		this.cantidad = cantidad;
	}

	public Integer getStock() {
		return stock;
	}

	public void setStock(Integer stock) {
		this.stock = stock;
	}
	
	
}
