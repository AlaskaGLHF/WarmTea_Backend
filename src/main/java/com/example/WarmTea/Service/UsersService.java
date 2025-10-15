package com.example.WarmTea.Service;

import com.example.WarmTea.Dtos.UsersDto.LoginRequestDTO;
import com.example.WarmTea.Dtos.UsersDto.LoginResponseDTO;
import com.example.WarmTea.Dtos.UsersDto.UserRequestDTO;
import com.example.WarmTea.Dtos.UsersDto.UserResponseDTO;
import com.example.WarmTea.Models.Users;
import com.example.WarmTea.Repository.UsersRepository;
import com.example.WarmTea.Utils.JwtUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UsersService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public UsersService(UsersRepository usersRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    public List<Users> getAllUsers() {
        return usersRepository.findAll();
    }

    public Users getUserById(Long id) {
        return usersRepository.findById(id).orElse(null);
    }

    public Users getUserByEmail(String email) {
        return usersRepository.findByEmail(email);
    }


    public UserResponseDTO createUser(UserRequestDTO userRequestDTO) {
        String hashedPassword = passwordEncoder.encode(userRequestDTO.getPassword());
        Users entity = new Users();
        entity.setUsername(userRequestDTO.getUsername());
        entity.setEmail(userRequestDTO.getEmail());
        entity.setPassword_hash(hashedPassword);

        Users savedUser = usersRepository.save(entity);
        return toDTO(savedUser);
    }

    public UserResponseDTO updateUser(Long id, UserRequestDTO request) {
        Optional<Users> existingOpt = usersRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return null;
        }

        Users existing = existingOpt.get();
        existing.setUsername(request.getUsername());
        existing.setEmail(request.getEmail());
        existing.setPassword_hash(passwordEncoder.encode(request.getPassword()));

        Users updated = usersRepository.save(existing);
        return toDTO(updated);
    }

    public boolean deleteUser(Long id) {
        if (usersRepository.existsById(id)) {
            usersRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        Optional<Users> userOpt = usersRepository.findByUsername(request.getUsername());
        if (userOpt.isEmpty()) {
            return null;
        }

        Users user = userOpt.get();
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword_hash())) {
            return null;
        }

        String token = jwtUtils.generateToken(user.getUsername());
        return new LoginResponseDTO(token);
    }


    // === Вспомогательный метод для маппинга на DTO ===
    private UserResponseDTO toDTO(Users user) {
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getCreated_at()
        );
    }
}
