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
    public UserDetails loadUserByUsername(String identity) throws UsernameNotFoundException {
        String normalizedIdentity = identity == null ? "" : identity.trim();
        com.appfor.ne3ma.model.User user = userRepository.findByEmail(normalizedIdentity)
                .or(() -> userRepository.findByPhone(normalizedIdentity))
                .orElseThrow(() -> new UsernameNotFoundException("user not found"));
        return new com.appfor.ne3ma.security.UserPrincipal(user);
    }
}
