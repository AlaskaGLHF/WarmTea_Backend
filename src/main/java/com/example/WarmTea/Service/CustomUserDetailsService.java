package com.example.WarmTea.Service;

import com.example.WarmTea.Models.User;
import com.example.WarmTea.Repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsersRepository usersRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPasswordHash(),
                getAuthorities(user)
        );
    }

    private List<GrantedAuthority> getAuthorities(User user) {
        // В твоей модели роли хранятся как один объект Roles
        // Если будет несколько ролей, можно адаптировать под List<Roles>
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName()));
    }

    public User getUserEntityByUsername(String username) {
        return usersRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + username));
    }


}
