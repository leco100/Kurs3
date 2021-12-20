package com.app.config;

import com.app.entity.Users;
import com.app.entity.Role;
import com.app.repos.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


/**
 * служба, которая отвечает за авторизацию пользователя
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsersRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users user = userRepository.findById(username.toLowerCase()).get();
        if (user == null) {
            throw new UsernameNotFoundException("User " + username + " was not found in the database");
        }
        Role role = user.getRole();

        List<GrantedAuthority> grantList = new ArrayList<>();

        GrantedAuthority authority = new SimpleGrantedAuthority(role.getName());
        grantList.add(authority);
        UserDetails  details = (UserDetails) new User(user.getLogin(),user.getPassword(),grantList);
        return details;
    }
}
