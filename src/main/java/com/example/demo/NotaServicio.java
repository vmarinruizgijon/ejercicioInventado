package com.example.demo;

import com.example.demo.Nota;
import java.util.List;

public interface NotaServicio {
    Nota guardarNota(Nota nota);
    void borrarNota(Long id);
    Nota obtenerNota(Long id);
    void marcarFavorito(Long usuarioId, Long notaId); // Método especial
    List<Nota> obtenerHistorialDeNota(Long idNota);
}
