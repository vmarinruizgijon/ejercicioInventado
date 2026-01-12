package com.example.demo;

import java.util.List;

import jakarta.persistence.*;


@Entity
@Table(name = "usuarios")
public class Usuario {
	
	//Atributos
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	@Column(nullable = false, unique = true)
	private String nombre;
	
	@Column(nullable = false)
	private String contraseña;
	
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Nota> notas;
	
	//Constructor
	public Usuario(String nombre, String contraseña) {

		this.nombre = nombre;
		this.contraseña = contraseña;
	}
	public Usuario() {

	}
	
	
	//getter y setter
	public String getNombre() {
		return nombre;
	}
	
	public Long getId() {
		
		return id;
		
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getContraseña() {
		return contraseña;
	}

	public void setContraseña(String contraseña) {
		this.contraseña = contraseña;
	}

	
    public List<Nota> getNotas() {
        return notas;
    }
    
    // MÉTODO IMPORTANTE: Úsalo siempre para añadir notas
    // Vincula la nota con el usuario antes de guardarla
    
    public void agregarNota(Nota nota) {
        notas.add(nota);
        nota.setUsuario(this); 
    }
    
    public void eliminarNota(Nota nota) {
        notas.remove(nota);
        nota.setUsuario(null);
    }
	
	
}
