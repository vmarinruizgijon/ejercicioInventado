package com.example.demo;

import com.example.demo.Nota;
import com.example.demo.Usuario;
import com.example.demo.NotaServicio;
import com.example.demo.UsuarioServicio;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid; // Para validar las notas
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class notaController { // He corregido la mayúscula de la clase por convención

    @Autowired
    private UsuarioServicio usuarioServicio;

    @Autowired
    private NotaServicio notaServicio; // Necesitamos esto para borrar/editar notas sueltas

    // --- LOGIN Y SESIÓN ---

    @GetMapping("/")
    public String mostrarLogin(@CookieValue(value = "ultimoUsuarioId", defaultValue = "") String ultimoUsuarioId, 
                               HttpSession session, 
                               Model model) {
        
        // Intentar login con cookie
        if (!ultimoUsuarioId.isEmpty()) {
            try {
                Long id = Long.parseLong(ultimoUsuarioId);
                Usuario usuario = usuarioServicio.obtenerUsuario(id);
                if (usuario != null) {
                    session.setAttribute("usuario", usuario);
                    return "redirect:/dashboard";
                }
            } catch (NumberFormatException e) {
                // Cookie ignorada
            }
        }

        model.addAttribute("usuario", new Usuario());
        return "login"; 
    }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam("nombre") String nombre, 
                                @RequestParam("contrasenia") String contrasenia,
                                HttpServletResponse response, 
                                HttpSession session,
                                Model model) {

        List<Usuario> usuarios = usuarioServicio.listarUsuarios();
        Usuario usuarioLogueado = null;
        
        for (Usuario u : usuarios) {
            if (u.getNombre().equals(nombre) && u.getContraseña().equals(contrasenia)) {
                usuarioLogueado = u;
                break;
            }
        }

        if (usuarioLogueado != null) {
            session.setAttribute("usuario", usuarioLogueado);
            Cookie cookie = new Cookie("ultimoUsuarioId", String.valueOf(usuarioLogueado.getId()));
            cookie.setPath("/");
            cookie.setMaxAge(7 * 24 * 60 * 60);
            response.addCookie(cookie);
            return "redirect:/dashboard";
        } else {
            model.addAttribute("error", "Usuario o contraseña incorrectos");
            model.addAttribute("usuario", new Usuario()); 
            return "login";
        }
    }

   
    //ZONA DE REGISTRO
   

    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registro";
    }


    @PostMapping("/registro")
    public String procesarRegistro(@RequestParam("nombre") String nombre, 
                                   @RequestParam("contrasenia") String contrasenia,
                                   RedirectAttributes redirectAttributes) { // Usamos esto para mandar mensajes al redirigir
        
        try {
            // 1. Crear usuario
            Usuario nuevoUsuario = new Usuario(nombre, contrasenia);
            
            // 2. Guardar en Base de Datos
            usuarioServicio.guardarUsuario(nuevoUsuario);

            // 3. Mensaje de éxito (se verá en el login)
            redirectAttributes.addFlashAttribute("mensaje", "¡Usuario creado con éxito! Ahora inicia sesión.");

            // 4. REDIRIGIR AL LOGIN
            return "redirect:/"; 

        } catch (Exception e) {
            // Si falla (por ejemplo nombre repetido), volvemos al registro con error
            redirectAttributes.addFlashAttribute("error", "Error: El nombre de usuario ya existe.");
            return "redirect:/registro";
        }
    }
    
    // ... Resto de métodos (logout, dashboard, etc.) ...
    @GetMapping("/logout")
    public String logout(HttpServletResponse response, HttpSession session) {
        Cookie cookie = new Cookie("ultimoUsuarioId", null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        session.invalidate();
        return "redirect:/";
    }


    // --- DASHBOARD ---

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Usuario usuarioSession = (Usuario) session.getAttribute("usuario");
        if (usuarioSession == null) return "redirect:/"; // Seguridad básica

        // SIEMPRE refrescamos los datos desde la BBDD para ver las notas actualizadas
        Usuario usuarioFresco = usuarioServicio.obtenerUsuario(usuarioSession.getId());
        
        model.addAttribute("usuario", usuarioFresco);
        return "dashboard"; // Asegúrate de que tu HTML usa ${usuario.nombre}
    }

    // --- CREAR NOTA ---

    @GetMapping("/nuevaNota")
    public String mostrarFormulario(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) return "redirect:/";

        // Pasamos el usuario para mostrar sus datos si hace falta
        Usuario usuarioFresco = usuarioServicio.obtenerUsuario(usuario.getId());
        model.addAttribute("usuario", usuarioFresco);
        return "nuevaNota";
    }

    @PostMapping("/nuevaNota")
    public String crearNota(HttpSession session, 
                            @RequestParam String titulo, 
                            @RequestParam String contenido) {
        
        Usuario usuarioSession = (Usuario) session.getAttribute("usuario");
        if (usuarioSession == null) return "redirect:/";

        // 1. Recuperamos al usuario real de la base de datos
        Usuario usuario = usuarioServicio.obtenerUsuario(usuarioSession.getId());

        // 2. Creamos la nota
        Nota nuevaNota = new Nota(titulo, contenido);

        // 3. La añadimos usando el método helper que vincula la relación
        usuario.agregarNota(nuevaNota);

        // 4. Guardamos al usuario (la cascada guardará la nota automáticamente)
        usuarioServicio.actualizarUsuario(usuario);

        return "redirect:/dashboard"; // Mejor ir al dashboard para verla creada
    }

    // --- EDITAR NOTA ---
    // NOTA: He simplificado esto. No hace falta un método intermedio "mandarId".
    // Desde el HTML puedes hacer un enlace directo: <a href="/editarNota?id=5">Editar</a>

    @GetMapping("/modificarNota")
    public String mostrarSeleccionModificar(HttpSession session, Model model) {
        // Recuperamos usuario de sesión
        Usuario usuarioSession = (Usuario) session.getAttribute("usuario");
        if (usuarioSession == null) return "redirect:/";
        
        // Importante: Recargamos el usuario de la BBDD para tener las notas actualizadas
        Usuario usuarioFresco = usuarioServicio.obtenerUsuario(usuarioSession.getId());
        
        // Pasamos la lista de notas al HTML para rellenar el <select>
        model.addAttribute("usuario", usuarioFresco);
        model.addAttribute("notas", usuarioFresco.getNotas());
        
        return "modificarNota"; // Tu archivo modificarNota.html
    }

    // 2. POST: Recibe el ID seleccionado y redirige al editor
    @PostMapping("/modificarNota")
    public String procesarSeleccionModificar(@RequestParam("id") Long id) {
        // Recibimos el ID del formulario y redirigimos a la siguiente pantalla
        // Esto hará que el navegador vaya a: /editarNota?id=5 (por ejemplo)
        return "redirect:/editarNota?id=" + id;
    }

    // ==========================================
    //           PASO 2: EDITAR LA NOTA
    // ==========================================

    // 3. GET: Muestra el formulario con los datos cargados (editarNota.html)
    @GetMapping("/editarNota")
    public String mostrarFormularioEdicion(@RequestParam("id") Long id, HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) return "redirect:/";

        // Buscamos la nota específica en la base de datos
        Nota nota = notaServicio.obtenerNota(id);
        
        // Seguridad: Comprobamos que la nota exista y sea del usuario logueado
        if (nota != null && nota.getUsuario().getId().equals(usuario.getId())) {
            model.addAttribute("nota", nota); // Pasamos la nota al HTML para que rellene los campos
            return "editarNota"; // Tu archivo editarNota.html
        }
        
        // Si intenta editar una nota que no es suya, lo mandamos al dashboard
        return "redirect:/dashboard"; 
    }

    // 4. POST: Guarda los cambios reales en la Base de Datos
    @PostMapping("/editarNota")
    public String guardarCambiosNota(HttpSession session, 
                                     @RequestParam("id") Long id, 
                                     @RequestParam("titulo") String titulo, 
                                     @RequestParam("contenido") String contenido) {
        
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) return "redirect:/";

        // 1. Buscamos la nota original
        Nota nota = notaServicio.obtenerNota(id);

        // 2. Verificamos que sea suya
        if (nota != null && nota.getUsuario().getId().equals(usuario.getId())) {
            // 3. Actualizamos los datos
            nota.setTitulo(titulo);
            nota.setContenido(contenido);
            
            // 4. Guardamos usando el servicio
            notaServicio.guardarNota(nota);
        }

        return "redirect:/dashboard"; // Al terminar, volvemos al menú principal
    }
    // --- VER Y FAVORITOS ---

    @GetMapping("/verNotasUsu")
    public String verNotas(HttpSession session, Model model) {
        Usuario usuarioSession = (Usuario) session.getAttribute("usuario");
        if (usuarioSession == null) return "redirect:/";

        Usuario usuarioFresco = usuarioServicio.obtenerUsuario(usuarioSession.getId());
        
        model.addAttribute("usuario", usuarioFresco);
        model.addAttribute("notas", usuarioFresco.getNotas());
        return "vernotasusu";
    }

    @PostMapping("/marcarFavorito")
    public String marcarFavorito(HttpSession session, @RequestParam Long id) {
        // Usamos el servicio de Nota que creamos específicamente para esto
        notaServicio.marcarFavorito(id);
        return "redirect:/vernotasusu";
    }

    // --- ELIMINAR NOTAS ---

    @GetMapping("/eliminarNotas")
    public String mostrarEliminarNotas(HttpSession session, Model model) {
        Usuario usuarioSession = (Usuario) session.getAttribute("usuario");
        if (usuarioSession == null) return "redirect:/";

        Usuario usuarioFresco = usuarioServicio.obtenerUsuario(usuarioSession.getId());
        
        model.addAttribute("usuario", usuarioFresco);
        model.addAttribute("notas", usuarioFresco.getNotas());
        return "eliminarNotas";
    }

    @PostMapping("/eliminarNotas")
    public String eliminarNota(HttpSession session, @RequestParam("id") Long id) {
        
        // 1. Comprobar seguridad
        Usuario usuarioSession = (Usuario) session.getAttribute("usuario");
        if (usuarioSession == null) return "redirect:/";

        // 2. BORRADO DIRECTO
        // No tocamos la lista del usuario, le decimos a la base de datos:
        // "Elimina la fila con ID X de la tabla notas, ahora mismo".
        notaServicio.borrarNota(id);
        
        // 3. IMPORTANTE: Limpiamos el usuario de la sesión para obligar
        // a que se recargue fresco (sin la nota borrada) en la siguiente pantalla.
        // Si no hacemos esto, a veces la sesión "recuerda" la nota borrada.
        // (Simplemente volvemos a cargar el usuario actualizado)
        Usuario usuarioActualizado = usuarioServicio.obtenerUsuario(usuarioSession.getId());
        session.setAttribute("usuario", usuarioActualizado);

        return "redirect:/eliminarNotas";
    }
}