package org.tribenet.tribenet.service;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.tribenet.tribenet.model.User;
import org.tribenet.tribenet.model.UserPrincipal;
import org.tribenet.tribenet.repository.UserRepo;

@Service
public class UserService implements UserDetailsService {

    private final UserRepo repo;

    public UserService(UserRepo repo){
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = repo.findByUsername(username);

        if(user == null){
            throw new UsernameNotFoundException("User not found: " + username);
        }
        return new UserPrincipal(user);
    }

    public User findByUsername(String username) {
        return repo.findByUsername(username);
    }

    public List<User> getAllUsers(Authentication auth){
        User user = getUserFromAuth(auth);
        return repo.findAllExceptUserId(user.getId());
    }

    private User getUserFromAuth(Authentication auth) {
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        return findByUsername(userDetails.getUsername());
    }
}
