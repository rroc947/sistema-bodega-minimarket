package com.bodega.pedidos.service;

import com.bodega.pedidos.entity.Usuario;
import com.bodega.pedidos.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Usuario registrar(Usuario usuario) {
        if (usuarioRepository.findByUsername(usuario.getUsername()).isPresent()) {
            throw new RuntimeException("El nombre de usuario ya está en uso");
        }
        String rolFormateado = usuario.getRol().toUpperCase();
        if (!rolFormateado.startsWith("ROLE_")) {
            rolFormateado = "ROLE_" + rolFormateado;
        }
        usuario.setRol(rolFormateado);
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        return usuarioRepository.save(usuario);
    }

    public Usuario login(String username, String password) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        // Validación de vigencia de labores por fechas
        java.time.LocalDate hoy = java.time.LocalDate.now();
        if (usuario.getFechaInicio() != null && hoy.isBefore(usuario.getFechaInicio())) {
            throw new RuntimeException("Su periodo de labores aún no inicia. Acceso denegado hasta el " + usuario.getFechaInicio());
        }
        if (usuario.getFechaFin() != null && hoy.isAfter(usuario.getFechaFin())) {
            throw new RuntimeException("Su periodo de labores ha vencido el " + usuario.getFechaFin() + ". Su cuenta ya no tiene acceso al sistema.");
        }

        return usuario;
    }
}
