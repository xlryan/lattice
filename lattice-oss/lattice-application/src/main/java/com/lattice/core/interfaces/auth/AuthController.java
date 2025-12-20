package com.lattice.core.interfaces.auth;

import com.lattice.core.domain.auth.User;
import com.lattice.core.infrastructure.logging.TraceContextHolder;
import com.lattice.core.infrastructure.security.JwtTokenService;
import com.lattice.core.interfaces.shared.ApiResponse;
import com.lattice.core.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@Tag(name = "Authentication")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService tokenService;
    private final UserRepository userRepository;

    public AuthController(PasswordEncoder passwordEncoder,
                          JwtTokenService tokenService,
                          UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

    @Operation(summary = "Login and receive JWT token (Initializes Admin if no users exist)")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
        long userCount = userRepository.count();

        User user;
        if (userCount == 0) {
            // First time setup: Create the admin user
            String encodedPassword = passwordEncoder.encode(request.password());
            user = new User(request.username(), encodedPassword, "OWNER");
            userRepository.save(user);
        } else {
            // Normal login
            Optional<User> userOpt = userRepository.findByUsername(request.username());
            if (userOpt.isEmpty() || !passwordEncoder.matches(request.password(), userOpt.get().getPasswordHash())) {
                 return ResponseEntity.status(401)
                    .body(ApiResponse.failure("UNAUTHORIZED", "Invalid credentials", TraceContextHolder.currentTraceId()));
            }
            user = userOpt.get();
        }

        String token = tokenService.generateToken(user.getUsername(), Map.of("role", user.getRole()));
        return ResponseEntity.ok(ApiResponse.success(new TokenResponse(token)));
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {
    }

    public record TokenResponse(String token) {
    }
}
