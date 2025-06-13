package com.earnlearn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.earnlearn.model.Role;
import com.earnlearn.repository.RoleRepository;

@Component
public class RoleSetting implements CommandLineRunner {

	@Autowired
	private RoleRepository roleRepository;

	@Override
	public void run(String... args) throws Exception {
		if (roleRepository.findByName("USER").isEmpty()) {
			roleRepository.save(new Role("USER"));
		}
		if (roleRepository.findByName("ADMIN").isEmpty()) {
			roleRepository.save(new Role("ADMIN"));
		}
		if (roleRepository.findByName("INSTRUCTOR").isEmpty()) {
			roleRepository.save(new Role("INSTRUCTOR"));
		}
		if (roleRepository.findByName("STUDENT").isEmpty()) {
			roleRepository.save(new Role("STUDENT"));
		}
		if (roleRepository.findByName("SELLER").isEmpty()) {
			roleRepository.save(new Role("SELLER"));
		}

	}

}
