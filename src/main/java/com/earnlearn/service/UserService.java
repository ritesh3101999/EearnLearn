package com.earnlearn.service;

import java.util.Collections;
import java.util.HashSet;
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
import jakarta.transaction.Transactional; 

@Service
public class UserService implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	public void save(User user, String roleName) {
		if (userRepository.findByUsername(user.getUsername()).isPresent()) {
			throw new IllegalArgumentException("User with this username already exists.");
		}
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		Role role = roleRepository.findByName(roleName)
				.orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));
		user.setRoles(Collections.singleton(role));
		userRepository.save(user);
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
		List<GrantedAuthority> authorities = user.getRoles().stream()
				.map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName())).collect(Collectors.toList());
		return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(),
				authorities);
	}

	public Optional<User> findByUsername(String username) {
		return userRepository.findByUsername(username);
	}

	public List<User> findAllUsers() {
		return userRepository.findAll();
	}

	// This method is more for admin updates or when ID is known and trusted
	@Transactional
	public void updateUser(User user) {
		User existingUser = userRepository.findById(user.getId())
				.orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + user.getId()));
		existingUser.setName(user.getName());
		existingUser.setEmail(user.getEmail());
		existingUser.setDateOfBirth(user.getDateOfBirth());
		existingUser.setGender(user.getGender());
		userRepository.save(existingUser);
	}

	@Transactional
	public User updateAuthenticatedUserProfile(String currentUsername, User userUpdates) {
		User userToUpdate = userRepository.findByUsername(currentUsername)
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + currentUsername));

		// Update only allowed fields
		userToUpdate.setName(userUpdates.getName());
		userToUpdate.setEmail(userUpdates.getEmail()); // Consider adding email validation/verification logic if it
														// changes
		userToUpdate.setDateOfBirth(userUpdates.getDateOfBirth());
		userToUpdate.setGender(userUpdates.getGender());
		return userRepository.save(userToUpdate);
	}

	public void deleteUserById(Long id) {
		if (!userRepository.existsById(id)) {
			throw new IllegalArgumentException("User not found with ID: " + id + " for deletion.");
		}
		userRepository.deleteById(id);
	}

	public String getName(String username) {
		return userRepository.findByUsername(username).map(User::getName)
				.orElseThrow(() -> new UsernameNotFoundException("User not found:" + username));
	}

	public Optional<User> findById(Long id) {
		return userRepository.findById(id);
	}

	@Transactional
	public void updateUserRoles(Long userId, Set<String> roleNames) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
		Set<Role> roles = new HashSet<>();
		if (roleNames != null && !roleNames.isEmpty()) {
			roles = roleNames.stream()
					.map(roleName -> roleRepository.findByName(roleName)
							.orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName)))
					.collect(Collectors.toSet());
		}
		user.setRoles(roles);
		userRepository.save(user);
	}

	@Transactional
	public void adminUpdateUser(User userFromForm, Set<String> roleNames) {
		User existingUser = userRepository.findById(userFromForm.getId())
				.orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userFromForm.getId()));
		existingUser.setName(userFromForm.getName());
		existingUser.setEmail(userFromForm.getEmail());
		existingUser.setDateOfBirth(userFromForm.getDateOfBirth());
		existingUser.setGender(userFromForm.getGender());
		Set<Role> newRoles = new HashSet<>();
		if (roleNames != null && !roleNames.isEmpty()) {
			for (String roleName : roleNames) {
				Role role = roleRepository.findByName(roleName)
						.orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));
				newRoles.add(role);
			}
		}
		existingUser.setRoles(newRoles);
		userRepository.save(existingUser);
	}

	public long countTotalUsers() {
		return userRepository.count();
	}
}
