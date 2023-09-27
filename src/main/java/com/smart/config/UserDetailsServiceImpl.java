package com.smart.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.smart.dao.UserRepository;
import com.smart.entity.User;

public class UserDetailsServiceImpl implements UserDetailsService{

	@Autowired
	private UserRepository repository;
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		//fetching user form database.
		User user=repository.getUserByUsername(username);
		if(user==null) {
			throw new UsernameNotFoundException("Username could not foud !!");
		}
		UserDetailsImpl userDetails=new UserDetailsImpl(user);
		return userDetails;
	}

}
