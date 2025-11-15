package org.restauranteqr.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "pedido")
public class Pedido implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_pedido")
	private Integer idPedido;
	
	@Column(name = "fecha")
	private LocalDateTime fecha;
	
	@Column(name = "subtotal")
	private Double subtotal;
	
	@Column(name = "igv")
	private Double igv;
	
	@Column(name = "total")
	private Double total;
	
	@ToString.Exclude
	@ManyToOne
	@JoinColumn(name = "id_usuario", nullable = false)
	private Usuario usuario;
	
	@ToString.Exclude
	@ManyToOne
	@JoinColumn(name = "id_sede", nullable = false)
	private Sede sede;
	
	@ToString.Exclude
	@OneToMany(mappedBy = "pedido")
	private List<Detalle> detalles;

	public Pedido(Integer idPedido, LocalDateTime fecha, Double subtotal, Double igv, Double total, Usuario usuario,
			Sede sede, List<Detalle> detalles) {
		super();
		this.idPedido = idPedido;
		this.fecha = fecha;
		this.subtotal = subtotal;
		this.igv = igv;
		this.total = total;
		this.usuario = usuario;
		this.sede = sede;
		this.detalles = detalles;
	}

	public Integer getIdPedido() {
		return idPedido;
	}

	public void setIdPedido(Integer idPedido) {
		this.idPedido = idPedido;
	}

	public LocalDateTime getFecha() {
		return fecha;
	}

	public void setFecha(LocalDateTime fecha) {
		this.fecha = fecha;
	}

	public Double getSubtotal() {
		return subtotal;
	}

	public void setSubtotal(Double subtotal) {
		this.subtotal = subtotal;
	}

	public Double getIgv() {
		return igv;
	}

	public void setIgv(Double igv) {
		this.igv = igv;
	}

	public Double getTotal() {
		return total;
	}

	public void setTotal(Double total) {
		this.total = total;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public Sede getSede() {
		return sede;
	}

	public void setSede(Sede sede) {
		this.sede = sede;
	}

	public List<Detalle> getDetalles() {
		return detalles;
	}

	public void setDetalles(List<Detalle> detalles) {
		this.detalles = detalles;
	}
	
	
}
