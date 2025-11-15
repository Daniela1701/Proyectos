package org.restauranteqr.entity;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "descuento")
public class Descuento {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_descuento")
	private Integer idDescuento;
	
	@Column(name = "valor_descuento")
	private Double valorDescuento;
	
	@Column(name = "fecha_inicio")
	private LocalDateTime fechaInicio;
	
	@Column(name = "fecha_fin")
	private LocalDateTime fechaFin;
	
	@Column(name = "estado")
	private Boolean estado;
	
	@ToString.Exclude
	@OneToOne
	@JoinColumn(name = "id_producto")
	private Producto producto;

	

	public Descuento() {
		super();
	}

	public Integer getIdDescuento() {
		return idDescuento;
	}

	public void setIdDescuento(Integer idDescuento) {
		this.idDescuento = idDescuento;
	}

	public Double getValorDescuento() {
		return valorDescuento;
	}

	public void setValorDescuento(Double valorDescuento) {
		this.valorDescuento = valorDescuento;
	}

	public LocalDateTime getFechaInicio() {
		return fechaInicio;
	}

	public void setFechaInicio(LocalDateTime fechaInicio) {
		this.fechaInicio = fechaInicio;
	}

	public LocalDateTime getFechaFin() {
		return fechaFin;
	}

	public void setFechaFin(LocalDateTime fechaFin) {
		this.fechaFin = fechaFin;
	}

	public Boolean getEstado() {
		return estado;
	}

	public void setEstado(Boolean estado) {
		this.estado = estado;
	}

	public Producto getProducto() {
		return producto;
	}

	public void setProducto(Producto producto) {
		this.producto = producto;
	}
	
	
}
