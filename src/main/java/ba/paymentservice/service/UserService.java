package ba.paymentservice.service;

import ba.paymentservice.dto.UserCreationRequest;
import ba.paymentservice.exception.BadRequestException;
import ba.paymentservice.model.User;
import ba.paymentservice.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(UserCreationRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BadRequestException("Username already exists");
        }

        var user = new User();
        user.setUsername(request.username());

        return userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
}
