package com.example.demo;

import com.example.demo.Nota;
import com.example.demo.NotaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotaServicioImplementar implements NotaServicio {

    @Autowired
    private NotaRepository repositorio;

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
    public void marcarFavorito(Long id) {
        Nota nota = repositorio.findById(id).orElse(null);
        if (nota != null) {
            nota.marcarFavorito(); // Método que creamos en la clase Nota
            repositorio.save(nota);
        }
    }
}