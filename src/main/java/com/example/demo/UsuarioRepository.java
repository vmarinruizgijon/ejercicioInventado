package com.example.demo;

import com.example.demo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Spring crea automáticamente métodos como save(), findAll(), delete(), etc.
}