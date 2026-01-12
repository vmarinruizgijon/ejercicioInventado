package com.example.demo;

import com.example.demo.Usuario;
import java.util.List;

public interface UsuarioServicio {
    
    List<Usuario> listarUsuarios();
    
    Usuario guardarUsuario(Usuario usuario);
    
    Usuario obtenerUsuario(Long id); // Ojo: Long, no Integer
    
    Usuario actualizarUsuario(Usuario usuario);
    
    void borrarUsuario(Long id);
}
