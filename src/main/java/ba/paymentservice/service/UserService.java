package ba.paymentservice.service;

import ba.paymentservice.dto.UserCreationRequest;
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
        User user = new User();
        user.setUsername(request.username());

        return userRepository.save(user);
    }
}
