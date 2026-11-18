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
            boolean esEncargado = authentication != null && authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ENCARGADO") || a.getAuthority().equals("ENCARGADO"));

            String rolSolicitado = usuario.getRol() != null ? usuario.getRol().toUpperCase() : "";
            if ((rolSolicitado.contains("ENCARGADO") || rolSolicitado.contains("ADMIN")) && !esEncargado) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("No tienes permisos para registrar usuarios con rol ENCARGADO. Se requiere estar autenticado como ENCARGADO.");
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
