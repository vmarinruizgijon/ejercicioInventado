package com.example.demo;

import jakarta.persistence.*;

@Entity
@Table(name = "favoritos") // Esto creará la tabla 'favoritos' en MySQL
public class Favoritas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación con el usuario que da a "Me gusta"
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // Relación con la nota que ha sido marcada
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nota_id", nullable = false)
    private Nota nota;

    // Constructor vacío obligatorio para Spring Boot
    public Favoritas() {}

    // Constructor para usarlo fácilmente
    public Favoritas(Usuario usuario, Nota nota) {
        this.usuario = usuario;
        this.nota = nota;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    
    public Nota getNota() { return nota; }
    public void setNota(Nota nota) { this.nota = nota; }
}