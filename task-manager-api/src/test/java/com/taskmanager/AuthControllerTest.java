package com.taskmanager;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "jwt.secret=test-secret-test-secret-test-secret-test-secret-test-secret-1234"
})
@AutoConfigureMockMvc
public class AuthControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private UserRepository userRepository;

    private String json(String username, String password)
    {
        return "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
    }

    @Test
    public void shouldRegisterNewUser() throws Exception
    {
        Mockito.when(userRepository.existsByUsername("ricardo")).thenReturn(false);

        mockMvc.perform(post("/auth/register")
                .contentType("application/json")
                .content(json("ricardo", "dummy-pass-123")))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.username").value("ricardo"));

        Mockito.verify(userRepository).save(Mockito.any(User.class));
    }

    @Test
    public void shouldRejectDuplicateUsername() throws Exception
    {
        Mockito.when(userRepository.existsByUsername("ricardo")).thenReturn(true);

        mockMvc.perform(post("/auth/register")
                .contentType("application/json")
                .content(json("ricardo", "dummy-pass-123")))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value("Username already taken"));
    }

    @Test
    public void shouldRejectShortPassword() throws Exception
    {
        mockMvc.perform(post("/auth/register")
                .contentType("application/json")
                .content(json("bob", "123")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.password").exists());
    }

    @Test
    public void shouldLoginWithCorrectPassword() throws Exception
    {
        User user = new User("ricardo", passwordEncoder.encode("dummy-pass-123"));
        Mockito.when(userRepository.findByUsername("ricardo")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/auth/login")
                .contentType("application/json")
                .content(json("ricardo", "dummy-pass-123")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    public void shouldRejectWrongPassword() throws Exception
    {
        User user = new User("ricardo", passwordEncoder.encode("dummy-pass-123"));
        Mockito.when(userRepository.findByUsername("ricardo")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/auth/login")
                .contentType("application/json")
                .content(json("ricardo", "wrong-password")))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Invalid credentials"));
    }

    @Test
    public void shouldRejectUnknownUser() throws Exception
    {
        Mockito.when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        mockMvc.perform(post("/auth/login")
                .contentType("application/json")
                .content(json("ghost", "whatever-123")))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Invalid credentials"));
    }
}