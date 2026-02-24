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
	
	@Column(nullable = false)
    private String rol = "ROLE_USER";
	
	@Column(nullable = true)
	private String correo;
	
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Nota> notas;
    
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Favoritas> favoritas;
	
	//Constructor
	public Usuario(String nombre, String contraseña) {

		this.nombre = nombre;
		this.contraseña = contraseña;
		this.rol = "ROLE_USER";
	}
	public Usuario() {

	}
	
	
	//getter y setter
	
	public String getRol() { 
		return rol; 
	}
	
    public void setRol(String rol) { 
    	this.rol = rol; 
    }
	
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
    
 // Getters y setters para la nueva lista
    public List<Favoritas> getFavoritas() {
        return favoritas;
    }

    public void setFavoritas(List<Favoritas> favoritas) {
        this.favoritas = favoritas;
    }
    
    public String getCorreo() { 
        return correo; 
    }
    public void setCorreo(String correo) { 
        this.correo = correo; 
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
    
    public void agregarAFavoritos(Nota nota) {
        Favoritas nuevaFavorita = new Favoritas(this, nota);
        favoritas.add(nuevaFavorita);
    }

    // Método helper para quitar de favoritos
    public void quitarDeFavoritos(Favoritas favorita) {
        favoritas.remove(favorita);
        favorita.setUsuario(null);
        favorita.setNota(null);
    }
	
	
}
