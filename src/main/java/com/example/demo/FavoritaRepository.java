package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface FavoritaRepository extends JpaRepository<Favoritas, Long> {
    
    // Busca si un usuario ya le ha dado favorito a una nota concreta
    Optional<Favoritas> findByUsuarioAndNota(Usuario usuario, Nota nota);
    
    // Para cuando hagas la página de "Mis Favoritos", este método te dará todas sus notas
    List<Favoritas> findByUsuario(Usuario usuario);
}
