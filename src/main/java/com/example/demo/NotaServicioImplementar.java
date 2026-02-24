package com.example.demo;

import com.example.demo.Nota;
import com.example.demo.NotaRepository;

import java.util.List;
import java.util.Optional;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.stereotype.Service;

@Service
public class NotaServicioImplementar implements NotaServicio {

    @Autowired
    private NotaRepository repositorio;
    
    @Autowired
    private FavoritaRepository favoritaRepository;

    @Autowired
    private UsuarioServicio usuarioServicio; // O UsuarioRepository si lo prefieres
    
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Nota guardarNota(Nota nota) {
        return repositorio.save(nota);
    }

    @Override
    @Transactional
    public void borrarNota(Long id) {
        repositorio.deleteById(id);
    }

    @Override
    public Nota obtenerNota(Long id) {
        return repositorio.findById(id).orElse(null);
    }
    
    // Ejemplo de lógica de negocio específica para notas
    @Override
    public void marcarFavorito(Long usuarioId, Long notaId) {
    	// 1. Buscamos al usuario y a la nota
        Usuario usuario = usuarioServicio.obtenerUsuario(usuarioId);
        Nota nota = repositorio.findById(notaId).orElse(null);

        if (usuario != null && nota != null) {
            // 2. Buscamos si ya existe ese favorito en la tabla intermedia
            Optional<Favoritas> favOpt = favoritaRepository.findByUsuarioAndNota(usuario, nota);

            if (favOpt.isPresent()) {
                // Si ya existe, significa que quiere DESMARCARLO (lo borramos de la tabla favoritos)
                favoritaRepository.delete(favOpt.get());
            } else {
                // Si NO existe, creamos el vínculo y lo guardamos en la tabla favoritos
                Favoritas nuevoFavorito = new Favoritas(usuario, nota);
                favoritaRepository.save(nuevoFavorito);
            }
        }
    }
    
    
//  Método para tener un historial de la nota
    public List<Nota> obtenerHistorialDeNota(Long idNota) {
        // Creamos el "lector" de historial
        AuditReader auditReader = AuditReaderFactory.get(entityManager);

        // Buscamos todas las versiones de la Nota con ese ID
        List<Nota> historial = auditReader.createQuery()
                .forRevisionsOfEntity(Nota.class, true, true)
                .add(AuditEntity.id().eq(idNota))
                .getResultList();

        return historial; // Devuelve una lista con cómo era la nota en el pasado
    }
    
    	
    
}