package com.example.demo;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.envers.Audited; // IMPORTANTE AÑADIR ESTO

import org.hibernate.envers.NotAudited;
import jakarta.persistence.*;

@Entity
@Table(name = "notas")
@Audited
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
    
    @NotAudited
    @OneToMany(mappedBy = "nota", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Favoritas> marcadaPorUsuarios;

    // RELACIÓN: Aquí guardamos quién es el dueño de la nota
    @NotAudited
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id") // Crea la columna en la BBDD
    @JsonIgnore
    private Usuario usuario;
	
 // Constructor personalizado
    public Nota(String titulo, String contenido) {
        this.titulo = titulo;
        this.contenido = contenido;
        this.fecha = LocalDate.now(); // Pone la fecha de hoy automáticamente
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

    // Usuario
    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
    
    //Favoritos
    public List<Favoritas> getMarcadaPorUsuarios() {
        return marcadaPorUsuarios;
    }

    public void setMarcadaPorUsuarios(List<Favoritas> marcadaPorUsuarios) {
        this.marcadaPorUsuarios = marcadaPorUsuarios;
    }
	
	
}//Fin class
