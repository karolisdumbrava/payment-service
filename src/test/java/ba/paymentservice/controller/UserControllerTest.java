package ba.paymentservice.controller;

import ba.paymentservice.dto.UserCreationRequest;
import ba.paymentservice.exception.BadRequestException;
import ba.paymentservice.model.User;
import ba.paymentservice.service.PaymentService;
import ba.paymentservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(UserControllerTest.TestConfig.class)
class UserControllerTest {

    @TestConfiguration
    static class TestConfig {

        @Bean
        public UserService userService() {
            return Mockito.mock(UserService.class);
        }

        @Bean
        public PaymentService paymentService() {
            return Mockito.mock(PaymentService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private PaymentService paymentService;

    @Test
    void createUser_ReturnsCreatedUser() throws Exception {
        // Arrange
        UserCreationRequest request = new UserCreationRequest("john_doe");
        User createdUser = new User();
        createdUser.setId(1L);
        createdUser.setUsername("john_doe");

        when(userService.createUser(any(UserCreationRequest.class))).thenReturn(createdUser);

        // Act & Assert
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("john_doe"));
    }

    @Test
    void createUser_DuplicateUsername_ReturnsBadRequest() throws Exception {
        // Arrange
        UserCreationRequest request = new UserCreationRequest("john_doe");

        when(userService.createUser(any(UserCreationRequest.class)))
                .thenThrow(new BadRequestException("Username already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username already exists"));
    }

    @Test
    void getPaymentIdsByUser_ReturnsListOfPaymentIds() throws Exception {
        // Arrange
        Long userId = 1L;
        List<Long> paymentIds = List.of(100L, 101L);
        when(paymentService.getPaymentIdsByUser(eq(userId))).thenReturn(paymentIds);

        // Act & Assert
        mockMvc.perform(get("/api/users/{userId}/payments", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(100))
                .andExpect(jsonPath("$[1]").value(101));
    }

    @Test
    void deleteUser_ReturnsNoContent() throws Exception {
        // Arrange
        Long userId = 1L;
        doNothing().when(userService).deleteUser(eq(userId));

        // Act & Assert
        mockMvc.perform(delete("/api/users/{userId}", userId))
                .andExpect(status().isNoContent());
    }
}
