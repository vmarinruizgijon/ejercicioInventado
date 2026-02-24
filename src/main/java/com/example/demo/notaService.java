package com.example.demo;

import java.util.HashMap;
import java.util.Map;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.stereotype.Service;

import java.util.List;

import org.springframework.stereotype.Service;
//Esta clase sirve para meter metodos que vamos a utilizar en el controller pero creamos esto para que el controller no este tan cargado de metodos
@Service
public class notaService {
	
	@PersistenceContext
    private EntityManager entityManager;
	
	// Estructura para almacenar usuarios y sus notas
	 private Map<String, Usuario> usuarios = new HashMap<>();
	 
	 // Mñétodo para registrar un nuevo usuario
	    public Usuario getUsuario(String nombre) {
	        return usuarios.get(nombre);
	    }
	    
	 //  Método para registrar un nuevo usuario en la aplicación
	    public void registrarUsuario(String nombre, String contraseña) {
	        usuarios.put(nombre, new Usuario(nombre, contraseña));
	    }


	
}
