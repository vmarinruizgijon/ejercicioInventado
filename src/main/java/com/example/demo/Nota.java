package com.example.demo;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Entity
@Table(name = "notas")
public class Nota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Validaciones
    @NotBlank(message = "El título no puede estar vacío")
    private String titulo;

    @NotBlank(message = "El contenido no puede estar vacío")
    @Size(max = 500, message = "El contenido no puede superar los 500 caracteres")
    @Column(length = 500)
    private String contenido;

    private LocalDate fecha;

    private boolean favorito;

    // RELACIÓN: Aquí guardamos quién es el dueño de la nota
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id") // Crea la columna en la BBDD
    private Usuario usuario;
	
 // Constructor personalizado
    public Nota(String titulo, String contenido) {
        this.titulo = titulo;
        this.contenido = contenido;
        this.fecha = LocalDate.now(); // Pone la fecha de hoy automáticamente
        this.favorito = false;
    }
    
    public Nota() {
    }
	
	//Getter y setters

 // ID
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // Título
    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    // Contenido
    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    // Fecha
    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    // Favorito (Atención: los booleanos suelen ser 'is' en vez de 'get')
    public boolean isFavorito() {
        return favorito;
    }

    public void setFavorito(boolean favorito) {
        this.favorito = favorito;
    }

    // Usuario
    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
	
	
	
	//Métodos
	//Para marcar fav al atruburo favorito
	public void marcarFavorito() {
		
		this.favorito = true;
		
	}
	//Para marcar fav al atruburo favorito
	public void desmarcarFavorito() {
		
		this.favorito = false;
		
	}
	
	
}//Fin class
