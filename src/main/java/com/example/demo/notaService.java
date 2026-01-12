package com.example.demo;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
//Esta clase sirve para meter metodos que vamos a utilizar en el controller pero creamos esto para que el controller no este tan cargado de metodos
@Service
public class notaService {
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
