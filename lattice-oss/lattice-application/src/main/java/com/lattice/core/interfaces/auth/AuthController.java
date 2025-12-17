package com.lattice.core.interfaces.auth;

import com.lattice.core.infrastructure.logging.TraceContextHolder;
import com.lattice.core.infrastructure.security.JwtTokenService;
import com.lattice.core.interfaces.shared.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Authentication")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService tokenService;
    private final String username;
    private final String passwordHash;

    public AuthController(PasswordEncoder passwordEncoder,
                          JwtTokenService tokenService,
                          @Value("${lattice.security.user.username:lattice}") String username,
                          @Value("${lattice.security.user.password:$pbkdf2$3072$uO8oqFzRKbm7JyQKDyBMGQ$OLoE6WuCJMRvp5ZjjG6bwNA9WJJ59HgfxoV8CgduTAk}") String passwordHash) {
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    @Operation(summary = "Login and receive JWT token")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
        if (!username.equals(request.username()) || !passwordEncoder.matches(request.password(), passwordHash)) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.failure("UNAUTHORIZED", "Invalid credentials", TraceContextHolder.currentTraceId()));
        }
        String token = tokenService.generateToken(username, Map.of("role", "OWNER"));
        return ResponseEntity.ok(ApiResponse.success(new TokenResponse(token)));
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {
    }

    public record TokenResponse(String token) {
    }
}
