package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioServicio usuarioServicio;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        // Buscamos al usuario en la base de datos
        List<Usuario> usuarios = usuarioServicio.listarUsuarios();
        Usuario usuarioEncontrado = null;
        for (Usuario u : usuarios) {
            if (u.getNombre().equals(username)) {
                usuarioEncontrado = u;
                break;
            }
        }

        if (usuarioEncontrado == null) {
            throw new UsernameNotFoundException("Usuario no encontrado");
        }

        // Devolvemos el usuario a Spring Security
        return User.builder()
                .username(usuarioEncontrado.getNombre())
                // OJO: Usamos getContraseña() porque así lo tienes en tu clase Usuario
                .password(usuarioEncontrado.getContraseña()) 
                .authorities(usuarioEncontrado.getRol()) 
                .build();
    }
}