package com.bodega.pedidos.controller;

import com.bodega.pedidos.dto.AuthResponseDTO;
import com.bodega.pedidos.entity.Usuario;
import com.bodega.pedidos.security.JwtService;
import com.bodega.pedidos.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<?> registrar(@Valid @RequestBody Usuario usuario, Authentication authentication) {
        try {
            boolean esAdmin = authentication != null && authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR") || a.getAuthority().equals("ADMINISTRADOR")
                                || a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ADMIN"));

            String rolSolicitado = usuario.getRol() != null ? usuario.getRol().toUpperCase() : "";
            
            // Si intenta crear roles de ENCARGADO, CAJERO o ADMINISTRADOR, solo el ADMINISTRADOR puede hacerlo
            if (!esAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Acceso denegado: Solo el usuario ADMINISTRADOR tiene permisos para crear usuarios (Encargados y Cajeros).");
            }

            // Validación de coherencia de fechas si fueron enviadas
            if (usuario.getFechaInicio() != null && usuario.getFechaFin() != null) {
                if (usuario.getFechaFin().isBefore(usuario.getFechaInicio())) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("La fecha de fin de labores no puede ser anterior a la fecha de inicio.");
                }
            }

            Usuario nuevo = usuarioService.registrar(usuario);
            String token = jwtService.generateToken(nuevo.getUsername(), nuevo.getRol());
            AuthResponseDTO response = AuthResponseDTO.builder()
                    .token(token)
                    .type("Bearer")
                    .id(nuevo.getId())
                    .username(nuevo.getUsername())
                    .rol(nuevo.getRol())
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Usuario credenciales) {
        try {
            Usuario usuario = usuarioService.login(credenciales.getUsername(), credenciales.getPassword());
            String token = jwtService.generateToken(usuario.getUsername(), usuario.getRol());
            AuthResponseDTO response = AuthResponseDTO.builder()
                    .token(token)
                    .type("Bearer")
                    .id(usuario.getId())
                    .username(usuario.getUsername())
                    .rol(usuario.getRol())
                    .build();
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> obtenerUsuarioActual(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No hay una sesión autenticada");
        }
        return ResponseEntity.ok(Map.of(
                "username", authentication.getName(),
                "roles", authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList())
        ));
    }
}
