package com.appfor.ne3ma.service;

import com.appfor.ne3ma.repository.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class MyuserdetailsServer implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userIdentity) throws UsernameNotFoundException {
        return loadUserByIdentity(userIdentity, null);
    }

    public UserDetails loadUserByIdentity(String userIdentity, String type) throws UsernameNotFoundException {
        com.appfor.ne3ma.model.User user = switch (normalizeType(type)) {
            case "phone" -> userRepository.findByPhone(userIdentity)
                    .or(() -> userRepository.findByEmail(userIdentity))
                    .orElseThrow(() -> new UsernameNotFoundException("user not found"));
            case "email" -> userRepository.findByEmail(userIdentity)
                    .or(() -> userRepository.findByPhone(userIdentity))
                    .orElseThrow(() -> new UsernameNotFoundException("user not found"));
            default -> userRepository.findByEmail(userIdentity)
                    .or(() -> userRepository.findByPhone(userIdentity))
                    .orElseThrow(() -> new UsernameNotFoundException("user not found"));
        };
        return new com.appfor.ne3ma.security.UserPrincipal(user);
    }

    private String normalizeType(String type) {
        if (type == null) {
            return "";
        }
        return type.trim().toLowerCase();
    }
}
