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
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        com.appfor.ne3ma.model.User user =userRepository.findByEmail(email)
                .orElseThrow(()-> new UsernameNotFoundException("user not found"));
        return new com.appfor.ne3ma.security.UserPrincipal(user);


    }
}
