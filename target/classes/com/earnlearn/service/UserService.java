package com.earnlearn.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.earnlearn.model.Role;
import com.earnlearn.model.User;
import com.earnlearn.repository.RoleRepository;
import com.earnlearn.repository.UserRepository;

@Service
public class UserService implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	public void save(User user, String roleName) {
		// Check if username already exists
		if (userRepository.findByUsername(user.getUsername()).isPresent()) {
			throw new IllegalArgumentException("User with this username already exists.");
		}
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		Role role = roleRepository.findByName(roleName)
				.orElseThrow(() -> new IllegalArgumentException("Role not found"));
		user.setRoles(Collections.singleton(role));
		userRepository.save(user);
	}

	// Load user for authentication
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

		// Map roles from database and add "ROLE_" prefix manually
        List<GrantedAuthority> authorities = user.getRoles().stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName())) 
            .collect(Collectors.toList());
        
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
            );
	}

	// Fetch user by usernames
	public Optional<User> findByUsername(String username) {
		return userRepository.findByUsername(username);
	}

	// Fetch all users
	public List<User> findAllUsers() {
		return userRepository.findAll();
	}

	// Update user details
	public void updateUser(User user) {
		User existingUser = userRepository.findById(user.getId())
				.orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + user.getId()));

		existingUser.setName(user.getName());
		existingUser.setEmail(user.getEmail());
		existingUser.setDateOfBirth(user.getDateOfBirth());
		existingUser.setGender(user.getGender());
		userRepository.save(existingUser);
	}

	public void deleteUserById(Long id) {
		userRepository.deleteById(id);
	}

	public String getName(String username) {
		return userRepository.findByUsername(username).map(User::getName)
				.orElseThrow(() -> new UsernameNotFoundException("User not found:" + username));
	}
	
	public Optional<User> findById(Long id){
		return userRepository.findById(id);
	}
	
	//update userrole
	public void updateUserRoles(Long userId, Set<String> roleNames) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Set<Role> roles = roleNames.stream()
            .map(roleName -> roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName)))
            .collect(Collectors.toSet());

        user.setRoles(roles);
        userRepository.save(user);
    }
}
