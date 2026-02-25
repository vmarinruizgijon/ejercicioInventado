package com.example.demo;

import com.example.demo.Nota;
import com.example.demo.Usuario;
import com.example.demo.NotaServicio;
import com.example.demo.UsuarioServicio;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication; 
import org.springframework.security.core.context.SecurityContextHolder; 
import org.springframework.security.crypto.password.PasswordEncoder; 

import java.util.List;

@Controller
public class notaController {

    @Autowired
    private UsuarioServicio usuarioServicio;

    @Autowired
    private NotaServicio notaServicio; 
    
    @Autowired
    private FavoritaRepository favoritaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; //Para encriptar en el registro

    @Autowired
    private EmailService emailService; // Para poder poner las funciones de email
    
    @Autowired
    private QrService qrService;
    
    // --- MÉTODO HELPER PARA SPRING SECURITY ---
    // Este método sustituye a tu antiguo session.getAttribute("usuario")
    private Usuario obtenerUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        String username = auth.getName();
        
        List<Usuario> usuarios = usuarioServicio.listarUsuarios();
        for (Usuario u : usuarios) {
            if (u.getNombre().equals(username)) {
                return u;
            }
        }
        return null;
    }

    // --- LOGIN Y SESIÓN ---

    @GetMapping("/")
    public String mostrarLogin(@CookieValue(value = "ultimoUsuarioId", defaultValue = "") String ultimoUsuarioId,
                               @RequestParam(value = "error", required = false) String error,
                               @RequestParam(value = "logout", required = false) String logout,
                               Model model) {
        
        // Mantenemos tu lectura de cookie, aunque Spring Security tiene su propia forma de auto-login
        if (!ultimoUsuarioId.isEmpty()) {
            // Nota: Con Spring Security activo, si no hay sesión real, redirigir a /dashboard
            // hará que Spring Security lo devuelva al login automáticamente.
            return "redirect:/dashboard"; 
        }

        if (error != null) {
            model.addAttribute("error", "Usuario o contraseña incorrectos");
        }
        if (logout != null) {
            model.addAttribute("mensaje", "Has cerrado sesión correctamente");
        }
        
        model.addAttribute("usuario", new Usuario());
        return "login"; 
    }

    // EL POST /login LO ELIMINAMOS: 
    // Ahora Spring Security lo intercepta automáticamente para validar la contraseña segura.


    // --- ZONA DE REGISTRO ---

    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(@RequestParam("nombre") String nombre, 
                                   @RequestParam("contrasenia") String contrasenia,
                                   @RequestParam("correo") String correo, // NUEVO PARÁMETRO
                                   RedirectAttributes redirectAttributes) { 
        try {
            String passEncriptada = passwordEncoder.encode(contrasenia);
            Usuario nuevoUsuario = new Usuario(nombre, passEncriptada);
            nuevoUsuario.setCorreo(correo); // GUARDAMOS EL CORREO
            
            usuarioServicio.guardarUsuario(nuevoUsuario);

            redirectAttributes.addFlashAttribute("mensaje", "¡Usuario creado con éxito! Ahora inicia sesión.");
            return "redirect:/"; 

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: El nombre de usuario ya existe.");
            return "redirect:/registro";
        }
    }
    
    // El GET /logout lo eliminamos también porque Spring Security lo gestiona por nosotros
    // y borra las cookies/sesiones que le digamos en el SecurityConfig.


    // --- DASHBOARD ---

    @GetMapping("/dashboard")
    public String dashboard(HttpServletResponse response, Model model) {
        Usuario usuarioSession = obtenerUsuarioAutenticado();
        if (usuarioSession == null) return "redirect:/";

        // MANTENEMOS TU COOKIE: La creamos aquí justo después de que Spring Security haga el login exitoso
        Cookie cookie = new Cookie("ultimoUsuarioId", String.valueOf(usuarioSession.getId()));
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(cookie);

        Usuario usuarioFresco = usuarioServicio.obtenerUsuario(usuarioSession.getId());
        model.addAttribute("usuario", usuarioFresco);
        return "dashboard"; 
    }

    // --- CREAR NOTA ---

    @GetMapping("/nuevaNota")
    public String mostrarFormulario(Model model) {
        Usuario usuarioSession = obtenerUsuarioAutenticado();
        if (usuarioSession == null) return "redirect:/";

        Usuario usuarioFresco = usuarioServicio.obtenerUsuario(usuarioSession.getId());
        model.addAttribute("usuario", usuarioFresco);
        return "nuevaNota";
    }

    @PostMapping("/nuevaNota")
    public String crearNota(@RequestParam String titulo, @RequestParam String contenido) {
        Usuario usuarioSession = obtenerUsuarioAutenticado();
        if (usuarioSession == null) return "redirect:/";

        Usuario usuario = usuarioServicio.obtenerUsuario(usuarioSession.getId());
        Nota nuevaNota = new Nota(titulo, contenido);
        usuario.agregarNota(nuevaNota);
        usuarioServicio.actualizarUsuario(usuario);

        return "redirect:/dashboard"; 
    }

    // --- EDITAR NOTA ---

    @GetMapping("/modificarNota")
    public String mostrarSeleccionModificar(Model model) {
        Usuario usuarioSession = obtenerUsuarioAutenticado();
        if (usuarioSession == null) return "redirect:/";
        
        Usuario usuarioFresco = usuarioServicio.obtenerUsuario(usuarioSession.getId());
        
        model.addAttribute("usuario", usuarioFresco);
        model.addAttribute("notas", usuarioFresco.getNotas());
        
        return "modificarNota"; 
    }

    @PostMapping("/modificarNota")
    public String procesarSeleccionModificar(@RequestParam("id") Long id) {
        return "redirect:/editarNota?id=" + id;
    }

    @GetMapping("/editarNota")
    public String mostrarFormularioEdicion(@RequestParam("id") Long id, Model model) {
        Usuario usuarioSession = obtenerUsuarioAutenticado();
        if (usuarioSession == null) return "redirect:/";

        Nota nota = notaServicio.obtenerNota(id);
        
        if (nota != null && nota.getUsuario().getId().equals(usuarioSession.getId())) {
            model.addAttribute("nota", nota); 
            return "editarNota"; 
        }
        
        return "redirect:/dashboard"; 
    }

    @PostMapping("/editarNota")
    public String guardarCambiosNota(@RequestParam("id") Long id, 
                                     @RequestParam("titulo") String titulo, 
                                     @RequestParam("contenido") String contenido) {
        
        Usuario usuarioSession = obtenerUsuarioAutenticado();
        if (usuarioSession == null) return "redirect:/";

        Nota nota = notaServicio.obtenerNota(id);

        if (nota != null && nota.getUsuario().getId().equals(usuarioSession.getId())) {
            nota.setTitulo(titulo);
            nota.setContenido(contenido);
            notaServicio.guardarNota(nota);
        }

        return "redirect:/dashboard"; 
    }

    // --- VER Y FAVORITOS ---

    @GetMapping("/verNotasUsu")
    public String verNotas(Model model) {
        Usuario usuarioSession = obtenerUsuarioAutenticado();
        if (usuarioSession == null) return "redirect:/";

        Usuario usuarioFresco = usuarioServicio.obtenerUsuario(usuarioSession.getId());
        
        List<Favoritas> listaFavoritos = favoritaRepository.findByUsuario(usuarioFresco);
        List<Long> notasFavoritasIds = listaFavoritos.stream()
                                            .map(fav -> fav.getNota().getId())
                                            .toList();

        model.addAttribute("usuario", usuarioFresco);
        model.addAttribute("notas", usuarioFresco.getNotas());
        model.addAttribute("notasFavoritasIds", notasFavoritasIds); 
        
        return "VerNotasUsu";
    }
    
    @GetMapping("/verFavoritas")
    public String verFavoritas(Model model) {
        Usuario usuarioSession = obtenerUsuarioAutenticado();
        if (usuarioSession == null) return "redirect:/";

        Usuario usuarioFresco = usuarioServicio.obtenerUsuario(usuarioSession.getId());
        
        List<Favoritas> listaFavoritos = favoritaRepository.findByUsuario(usuarioFresco);
        List<Nota> notasFavoritas = listaFavoritos.stream()
                                            .map(fav -> fav.getNota())
                                            .toList();

        model.addAttribute("usuario", usuarioFresco);
        model.addAttribute("notasFavoritas", notasFavoritas);
        
        return "verFavoritas";
    }

    @PostMapping("/quitarFavoritoDesdeLista")
    public String quitarFavoritoDesdeLista(@RequestParam Long id) {
        Usuario usuarioSession = obtenerUsuarioAutenticado();
        if (usuarioSession == null) return "redirect:/";

        notaServicio.marcarFavorito(usuarioSession.getId(), id);
        
        return "redirect:/verFavoritas"; 
    }

    @PostMapping("/marcarFavorito")
    public String marcarFavorito(@RequestParam Long id) {
        Usuario usuarioSession = obtenerUsuarioAutenticado();
        if (usuarioSession == null) return "redirect:/";

        notaServicio.marcarFavorito(usuarioSession.getId(), id);
        
        return "redirect:/verNotasUsu";
    }

    // --- ELIMINAR NOTAS ---

    @GetMapping("/eliminarNotas")
    public String mostrarEliminarNotas(Model model) {
        Usuario usuarioSession = obtenerUsuarioAutenticado();
        if (usuarioSession == null) return "redirect:/";

        Usuario usuarioFresco = usuarioServicio.obtenerUsuario(usuarioSession.getId());
        
        List<Favoritas> listaFavoritos = favoritaRepository.findByUsuario(usuarioFresco);
        List<Long> notasFavoritasIds = listaFavoritos.stream()
                                            .map(fav -> fav.getNota().getId())
                                            .toList();

        model.addAttribute("usuario", usuarioFresco);
        model.addAttribute("notas", usuarioFresco.getNotas());
        model.addAttribute("notasFavoritasIds", notasFavoritasIds); 
        
        return "eliminarNotas";
    }

    @PostMapping("/eliminarNotas")
    public String eliminarNota(@RequestParam("id") Long id) {
        
        Usuario usuarioSession = obtenerUsuarioAutenticado();
        if (usuarioSession == null) return "redirect:/";

        notaServicio.borrarNota(id);
        
        return "redirect:/eliminarNotas";
    }
    

    //           -- ZONA ADMINISTRADOR --
 

    // 1. Mostrar el Panel de Control
    @GetMapping("/admin/panel")
    public String panelAdmin(Model model) {
        Usuario admin = obtenerUsuarioAutenticado();
        
        // Verificación de seguridad: ¿Es realmente un admin?
        if (admin == null || !admin.getRol().equals("ROLE_ADMIN")) {
            return "redirect:/dashboard"; 
        }
        
        // Si es admin, le pasamos la lista de TODOS los usuarios
        List<Usuario> todosLosUsuarios = usuarioServicio.listarUsuarios();
        model.addAttribute("admin", admin);
        model.addAttribute("usuarios", todosLosUsuarios);
        
        return "adminPanel";
    }

    // 2. Eliminar un usuario (y sus notas en cascada)
    @PostMapping("/admin/eliminarUsuario")
    public String adminEliminarUsuario(@RequestParam("id") Long id, RedirectAttributes redirectAttributes) {
        Usuario admin = obtenerUsuarioAutenticado();
        
        if (admin != null && admin.getRol().equals("ROLE_ADMIN")) {
            // Medida de seguridad: Evitar que el admin se borre a sí mismo por error
            if (admin.getId().equals(id)) {
                redirectAttributes.addFlashAttribute("error", "No puedes borrarte a ti mismo.");
            } else {
                usuarioServicio.borrarUsuario(id);
                redirectAttributes.addFlashAttribute("mensaje", "Usuario eliminado correctamente.");
            }
        }
        return "redirect:/admin/panel";
    }

    // 3. Mostrar formulario para editar a un usuario
    @GetMapping("/admin/editarUsuario")
    public String adminMostrarEditarUsuario(@RequestParam("id") Long id, Model model) {
        Usuario admin = obtenerUsuarioAutenticado();
        
        if (admin == null || !admin.getRol().equals("ROLE_ADMIN")) {
            return "redirect:/dashboard";
        }
        
        Usuario usuarioAEditar = usuarioServicio.obtenerUsuario(id);
        if (usuarioAEditar != null) {
            model.addAttribute("usuarioAEditar", usuarioAEditar);
            return "adminEditarUsuario";
        }
        
        return "redirect:/admin/panel";
    }

    // 4. Guardar los cambios del usuario (Nombre y Rol)
    @PostMapping("/admin/editarUsuario")
    public String adminGuardarUsuarioEditado(@RequestParam("id") Long id,
                                             @RequestParam("nombre") String nombre,
                                             @RequestParam("rol") String rol,
                                             RedirectAttributes redirectAttributes) {
        Usuario admin = obtenerUsuarioAutenticado();
        
        if (admin != null && admin.getRol().equals("ROLE_ADMIN")) {
            Usuario usuarioAEditar = usuarioServicio.obtenerUsuario(id);
            
            if (usuarioAEditar != null) {
                usuarioAEditar.setNombre(nombre);
                usuarioAEditar.setRol(rol); // Podemos ascender o degradar usuarios
                usuarioServicio.actualizarUsuario(usuarioAEditar);
                redirectAttributes.addFlashAttribute("mensaje", "Usuario actualizado con éxito.");
            }
        }
        return "redirect:/admin/panel";
    }
    
 
    //               -- MI PERFIL --
   

    @GetMapping("/miPerfil")
    public String mostrarPerfil(Model model) {
        Usuario usuarioSession = obtenerUsuarioAutenticado();
        if (usuarioSession == null) return "redirect:/";

        Usuario usuarioFresco = usuarioServicio.obtenerUsuario(usuarioSession.getId());
        model.addAttribute("usuario", usuarioFresco);
        
        return "miPerfil"; // Nos llevará a la nueva pantalla
    }

    @PostMapping("/miPerfil")
    public String actualizarPerfil(@RequestParam("correo") String correo, 
                                   @RequestParam(value = "nuevaContrasenia", required = false) String nuevaContrasenia,
                                   RedirectAttributes redirectAttributes) {
        
        Usuario usuarioSession = obtenerUsuarioAutenticado();
        if (usuarioSession == null) return "redirect:/";

        Usuario usuario = usuarioServicio.obtenerUsuario(usuarioSession.getId());
        
        // 1. Actualizamos el correo
        usuario.setCorreo(correo);

        // 2. Si ha escrito algo en la nueva contraseña, la encriptamos y la cambiamos
        if (nuevaContrasenia != null && !nuevaContrasenia.isEmpty()) {
            usuario.setContraseña(passwordEncoder.encode(nuevaContrasenia));
        }

        usuarioServicio.actualizarUsuario(usuario);
        
        redirectAttributes.addFlashAttribute("mensaje", "¡Tu perfil ha sido actualizado con éxito!");
        return "redirect:/dashboard"; 
    }
    
    // ======= MEJORAS ========
    
    //			-- ENVIAR PDF --
    
    @PostMapping("/enviarNotaPdf")
    public String enviarNotaPdf(@RequestParam("id") Long id, RedirectAttributes redirectAttributes) {
        
        Usuario usuarioSession = obtenerUsuarioAutenticado();
        if (usuarioSession == null) return "redirect:/";

        Usuario usuario = usuarioServicio.obtenerUsuario(usuarioSession.getId());

        // Validamos si el usuario ha guardado su correo
        if (usuario.getCorreo() == null || usuario.getCorreo().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "No tienes un correo configurado. Edita tu perfil para añadirlo.");
            return "redirect:/verNotasUsu";
        }

        Nota nota = notaServicio.obtenerNota(id);

        if (nota != null && nota.getUsuario().getId().equals(usuario.getId())) {
            try {
                // Llamamos al servicio para enviar el correo
                emailService.enviarNotaEnPdf(usuario.getCorreo(), nota);
                redirectAttributes.addFlashAttribute("mensaje", "¡Nota enviada a tu correo en formato PDF!");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Error al enviar el correo: " + e.getMessage());
            }
        }
        
        return "redirect:/verNotasUsu";
    }
    
    //			-- HACER QR--
    
    @GetMapping("/verQr")
    public String verQrNota(@RequestParam("id") Long id, Model model) {
        Usuario usuarioSession = obtenerUsuarioAutenticado();
        if (usuarioSession == null) return "redirect:/";

        Nota nota = notaServicio.obtenerNota(id);
        
        if (nota != null && nota.getUsuario().getId().equals(usuarioSession.getId())) {
            // Creamos el texto que irá dentro del QR Título + Contenido
            String textoParaQr = "NOTA: " + nota.getTitulo() + "\n" + nota.getContenido();
            String qrBase64 = qrService.generarQrBase64(textoParaQr);
            
            model.addAttribute("qrImagen", qrBase64);
            model.addAttribute("nota", nota);
            return "verQr"; // Crearemos este HTML ahora
        }
        
        return "redirect:/dashboard";
    }
    
    
    // -- HISTORIAL DE LA NOTA --
    
    @GetMapping("/historialNota")
    public String verHistorial(@RequestParam("id") Long id, Model model) {
        Usuario usuarioSession = obtenerUsuarioAutenticado();
        if (usuarioSession == null) return "redirect:/";

        // Obtenemos la nota actual para saber de qué nota estamos hablando
        Nota notaActual = notaServicio.obtenerNota(id);
        
        // Verificamos por seguridad que la nota es suya
        if (notaActual != null && notaActual.getUsuario().getId().equals(usuarioSession.getId())) {
            
            // Usamos nuestro nuevo método mágico
            List<Nota> historial = notaServicio.obtenerHistorialDeNota(id);
            
            model.addAttribute("notaActual", notaActual);
            model.addAttribute("historial", historial);
            return "historialNota"; // Vamos a crear este HTML
        }
        
        return "redirect:/dashboard";
    }
    
}