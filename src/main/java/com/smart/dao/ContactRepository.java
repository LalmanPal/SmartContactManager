package com.smart.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.smart.entity.Contact;
import com.smart.entity.User;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Integer>{

	//current Page- page
	//Current per page- 5
	@Query("select c from Contact c where c.user.id=:userId")
	public Page<Contact> findContactByUser(@Param("userId")int userId,Pageable pageable);
	//search
	public List<Contact> findByNameContainingAndUser(String keyword,User user);
}
