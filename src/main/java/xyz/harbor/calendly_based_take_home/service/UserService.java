package xyz.harbor.calendly_based_take_home.service;

import xyz.harbor.calendly_based_take_home.request.UpdateUserPasswordRequest;
import xyz.harbor.calendly_based_take_home.response.UserResponse;
import xyz.harbor.calendly_based_take_home.mapper.UserMapper;
import xyz.harbor.calendly_based_take_home.model.User;
import xyz.harbor.calendly_based_take_home.repository.UserRepository;
import xyz.harbor.calendly_based_take_home.request.CreateUserRequest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final PasswordManagerService passwordManagerService;

    @Autowired
    public UserService(UserRepository userRepository, PasswordManagerService passwordManagerService){
        this.userRepository = userRepository;
        this.passwordManagerService = passwordManagerService;
    }

    public UserResponse createNewUser(CreateUserRequest createUserRequest){
        Optional<User> usernameExists = userRepository.findByUsername(createUserRequest.getUsername());
        if(usernameExists.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "User already exist with same username");
        }

        String salt = passwordManagerService.getNextSalt();
        String hashedPassword = passwordManagerService.hash(createUserRequest.getPassword(), salt);

        User user = User.builder()
                .userId(UUID.randomUUID().toString())
                .email(createUserRequest.getEmail())
                .firstName(createUserRequest.getFirstName())
                .lastName(createUserRequest.getLastName())
                .username(createUserRequest.getUsername())
                .hashedPassword(hashedPassword)
                .salt(salt)
                .build();

        userRepository.save(user);
        return UserMapper.INSTANCE.modelToResponse(user);
    }

    public UserResponse updatePassword(UpdateUserPasswordRequest updateUserPasswordRequest){
        Optional<User> usernameExists = userRepository.findByUsername(updateUserPasswordRequest.getUsername());
        if(usernameExists.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Username or password mismatch");
        if(!passwordManagerService.isExpectedPassword(updateUserPasswordRequest.getOldPassword(),
                usernameExists.get().getSalt(),
                usernameExists.get().getHashedPassword()))
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Username or password mismatch");
        User user = usernameExists.get();
        String salt = passwordManagerService.getNextSalt();
        user.setHashedPassword(passwordManagerService.hash(updateUserPasswordRequest.getNewPassword(), salt));
        user.setSalt(salt);
        userRepository.save(user);
        return UserMapper.INSTANCE.modelToResponse(user);
    }
}
