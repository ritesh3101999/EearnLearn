package com.earnlearn.service;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.earnlearn.model.Role;
import com.earnlearn.repository.RoleRepository;

@Service
public class RoleService {
	@Autowired
	private RoleRepository roleRepository;

	public Role save(Role role) {
		return roleRepository.save(role);
	}

	public Optional<Role> findByName(String name) {
		return roleRepository.findByName(name);
	}

	public List<Role> findAll() {
		return roleRepository.findAll();
	}

}
