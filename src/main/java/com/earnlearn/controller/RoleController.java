package com.earnlearn.controller;

import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.earnlearn.model.Role;
import com.earnlearn.model.User;
import com.earnlearn.service.RoleService;
import com.earnlearn.service.UserService;

@Controller
@RequestMapping("/roles")
public class RoleController {

	@Autowired
	private UserService userService;

	@Autowired
	private RoleService roleService;

	@GetMapping("/editRole/{id}")
	public String showUpdateRoleForm(@PathVariable Long id, Model model) {
		User user = userService.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
		Set<String> roleNames = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
		model.addAttribute("user", user);
		model.addAttribute("roles", roleService.findAll());
		model.addAttribute("roleNames", roleNames);
		return "user-role-edit";
	}

	@PostMapping("/updateRoles/{id}")
	public String updateUserRoles(@PathVariable Long id, @RequestParam Set<String> roleNames) {
		userService.updateUserRoles(id, roleNames);
		return "redirect:/users/list";
	}

}
