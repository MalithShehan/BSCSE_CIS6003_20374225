package service;

import dao.UserDAO;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import util.PasswordUtil;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit & Mock Tests")
public class AuthServiceTest {

    @Mock
    private UserDAO userDAO;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;
    private final String rawPassword = "Password@123";

    @BeforeEach
    void setUp() {
        String hashedPassword = PasswordUtil.hashPassword(rawPassword);
        sampleUser = new User(1, "receptionist", hashedPassword, "Kasun Fernando", "RECEPTIONIST", true);
    }

    @Test
    @DisplayName("Should successfully authenticate user with valid credentials")
    void testAuthenticate_Success() throws SQLException {
        when(userDAO.findByUsername("receptionist")).thenReturn(sampleUser);

        User authenticated = authService.authenticate("receptionist", rawPassword);

        assertNotNull(authenticated);
        assertEquals("receptionist", authenticated.getUsername());
        assertEquals("RECEPTIONIST", authenticated.getRole());
        verify(userDAO, times(1)).findByUsername("receptionist");
    }

    @Test
    @DisplayName("Should throw SecurityException when username does not exist")
    void testAuthenticate_UserNotFound() throws SQLException {
        when(userDAO.findByUsername(anyString())).thenReturn(null);

        assertThrows(SecurityException.class, () -> 
            authService.authenticate("nonexistent_user", "AnyPassword@123")
        );
    }

    @Test
    @DisplayName("Should throw SecurityException on wrong password")
    void testAuthenticate_WrongPassword() throws SQLException {
        when(userDAO.findByUsername("receptionist")).thenReturn(sampleUser);

        assertThrows(SecurityException.class, () -> 
            authService.authenticate("receptionist", "WrongPassword!123")
        );
    }

    @Test
    @DisplayName("Should throw SecurityException when user account is deactivated")
    void testAuthenticate_InactiveUser() throws SQLException {
        sampleUser.setActive(false);
        when(userDAO.findByUsername("receptionist")).thenReturn(sampleUser);

        SecurityException ex = assertThrows(SecurityException.class, () -> 
            authService.authenticate("receptionist", rawPassword)
        );
        assertTrue(ex.getMessage().contains("inactive"));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when inputs are blank")
    void testAuthenticate_EmptyInputs() {
        assertThrows(IllegalArgumentException.class, () -> authService.authenticate("", "Password"));
        assertThrows(IllegalArgumentException.class, () -> authService.authenticate("user", ""));
    }
}
