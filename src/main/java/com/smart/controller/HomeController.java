package com.smart.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.smart.dao.UserRepository;
import com.smart.entity.User;
import com.smart.helper.Messages;


@Controller
public class HomeController {
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private UserRepository repository;
	
	@GetMapping("/")
	public String home(Model model) {
        model.addAttribute("title", "Home-Smart Contact Manager");
		return "home";
	}
	@GetMapping("/about")
	public String about(Model model) {
        model.addAttribute("title", "About-Smart Contact Manager");
		return "about";
	}
	
	@GetMapping("/signup")
	public String signup(Model model) {
        model.addAttribute("title", "Register-Smart Contact Manager");
        model.addAttribute("user", new User());
		return "signup";
	}
	
	//This handler for registering user.
	@PostMapping("/do_register")
	public String registerUser(@Valid @ModelAttribute("user") User user,BindingResult br,@RequestParam(value = "agreement",defaultValue = "false")boolean agreement,Model model,HttpSession session) {
		try {
			if(!agreement) {
				System.out.println("You have not agreed Terms and conditions.");
				throw new Exception("You have not agreed Terms and conditions.");
				
			}
			if(br.hasErrors()) {
				model.addAttribute("user",user);
				return "signup";
			}
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImagesUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			
			System.out.println("User Agreement= "+agreement);
			System.out.println("User === "+user);
			User result=this.repository.save(user);
			model.addAttribute("user", new User());
			session.setAttribute("message",new Messages("Successfully registered !! ","alert-success"));
			return  "signup";
		} catch (Exception e) {
			
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message",new Messages("Something went wrong !! "+e.getMessage(),"alert-danger"));
			return  "signup";
		}
		
	}
	
	//This is for custom login 
	@GetMapping("/signin")
	public String customLogin(Model model) {
		model.addAttribute("title", "Login Page");
		return "login";
	}
}
