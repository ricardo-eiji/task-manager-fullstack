package com.taskmanager;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController
{
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank @Size(min = 8, max = 72) String password) {}

    public record RegisterResponse(String username) {}
    public record LoginRequest(String username, String password) {}
    public record LoginResponse(String token) {}

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request)
    {
        if (userRepository.existsByUsername(request.username()))
        {
            throw new UsernameAlreadyExistsException();
        }

        User user = new User(request.username(), passwordEncoder.encode(request.password()));
        userRepository.save(user);
        return new RegisterResponse(user.getUsername());
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request)
    {
        User user = userRepository.findByUsername(request.username())
            .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash()))
        {
            throw new InvalidCredentialsException();
        }

        return new LoginResponse(jwtUtil.generateToken(user.getUsername()));
    }
}