package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // Indica que esta clase es una API REST y devuelve JSON
@RequestMapping("/api/usuarios") // Todas las URL de este controlador empezarán por /api/usuarios
public class ApiRestController {

    @Autowired
    private UsuarioServicio usuarioServicio;

    @Autowired
    private PasswordEncoder passwordEncoder; //para encriptar contraseñas desde la API

    //  GET 
    
    // GET /api/usuarios Obtener todos los usuarios
    @GetMapping
    public List<Usuario> listarUsuariosAPI() {
        return usuarioServicio.listarUsuarios();
    }

    // GET /api/usuarios/{id} Obtener un usuario por su ID
    @GetMapping("/{id}")
    public Usuario obtenerUsuarioAPI(@PathVariable Long id) {
        return usuarioServicio.obtenerUsuario(id);
    }
    
    //  POST 

    // POST /api/usuarios Crear un nuevo usuario
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // Devuelve un estado 201 (Creado) si todo va bien
    public Usuario crearUsuarioAPI(@RequestBody Usuario nuevoUsuario) {
        
        // 1. Encriptamos la contraseña que nos llega en el JSON
        String passEncriptada = passwordEncoder.encode(nuevoUsuario.getContraseña());
        nuevoUsuario.setContraseña(passEncriptada);
        
        // 2. Si no envían un rol, forzamos el de usuario normal por seguridad
        if (nuevoUsuario.getRol() == null || nuevoUsuario.getRol().isEmpty()) {
            nuevoUsuario.setRol("ROLE_USER");
        }

        // 3. Lo guardamos en la base de datos y devolvemos el objeto creado
        return usuarioServicio.guardarUsuario(nuevoUsuario);
    }

    // ==================== PUT ====================

    // PUT /api/usuarios/{id} Actualizar un usuario existente
    @PutMapping("/{id}")
    public Usuario actualizarUsuarioAPI(@PathVariable Long id, @RequestBody Usuario usuarioActualizado) {
        
        // 1. Buscamos el usuario en la base de datos
        Usuario usuarioExistente = usuarioServicio.obtenerUsuario(id);
        
        if (usuarioExistente != null) {
            // 2. Actualizamos los datos básicos
            usuarioExistente.setNombre(usuarioActualizado.getNombre());
            usuarioExistente.setRol(usuarioActualizado.getRol());
            
            // 3. Solo actualizamos la contraseña si nos han enviado una nueva en el JSON
            if (usuarioActualizado.getContraseña() != null && !usuarioActualizado.getContraseña().isEmpty()) {
                usuarioExistente.setContraseña(passwordEncoder.encode(usuarioActualizado.getContraseña()));
            }

            // 4. Guardamos los cambios
            return usuarioServicio.actualizarUsuario(usuarioExistente);
        } else {
            // Si el usuario no existe, devolvemos null (lo ideal sería un ResponseEntity con error 404)
            return null;
        }
    }

    // ==================== DELETE ====================

    // DELETE /api/usuarios/{id} Borrar un usuario
    @DeleteMapping("/{id}")
    public String eliminarUsuarioAPI(@PathVariable Long id) {
        usuarioServicio.borrarUsuario(id);
        return "Usuario con ID " + id + " eliminado correctamente de la BD.";
    }
}