package com.mediaserver.security;

import com.mediaserver.model.RootUser;
import com.mediaserver.repository.RootUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RootUserDetailsService implements UserDetailsService {

    @Autowired
    private RootUserRepository rootUserRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        RootUser rootUser = rootUserRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Root user not found with username: " + username)
                );

        return RootUserPrincipal.create(rootUser);
    }

    @Transactional
    public UserDetails loadUserById(String id) {
        RootUser rootUser = rootUserRepository.findById(id)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Root user not found with id: " + id)
                );

        return RootUserPrincipal.create(rootUser);
    }
}
