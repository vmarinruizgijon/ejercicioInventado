package com.example.demo;

import com.example.demo.Nota;
import com.example.demo.Usuario;
import com.example.demo.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioServicioImplementar implements UsuarioServicio {

    @Autowired
    private UsuarioRepository repositorio;

    @Override
    @Transactional(readOnly = true) // Optimización para lectura
    public List<Usuario> listarUsuarios() {
        return repositorio.findAll();
    }

    @Override
    @Transactional // Importante para que guarde usuario + notas como una unidad
    public Usuario guardarUsuario(Usuario usuario) {
        
        // LOGICA EXTRA IMPORTANTE:
        // Vinculamos las notas con el usuario antes de guardar
        // para que la columna 'usuario_id' en la tabla 'notas' no se quede null.
        if (usuario.getNotas() != null) {
            for (Nota nota : usuario.getNotas()) {
                nota.setUsuario(usuario);
            }
        }
        
        return repositorio.save(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario obtenerUsuario(Long id) {
        // Usamos .orElse(null) para evitar errores si no existe el ID
        return repositorio.findById(id).orElse(null); 
    }

    @Override
    @Transactional
    public Usuario actualizarUsuario(Usuario usuario) {
        // En JPA, .save() sirve tanto para crear como para actualizar 
        // si el objeto ya tiene un ID puesto.
        
        // También aseguramos la relación por si se añaden notas nuevas al actualizar
        if (usuario.getNotas() != null) {
            for (Nota nota : usuario.getNotas()) {
                nota.setUsuario(usuario);
            }
        }
        return repositorio.save(usuario);
    }

    @Override
    @Transactional
    public void borrarUsuario(Long id) {
        repositorio.deleteById(id);
    }
}
