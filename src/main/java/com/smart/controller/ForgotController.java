package com.smart.controller;
import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entity.User;
import com.smart.service.EmailService;
@Controller
public class ForgotController {

	
	Random random=new Random(1000);
	@Autowired
	private EmailService emailService;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@GetMapping("/forgot")
	public String forgetPassword() {
		return "forgot_password_page";
	}
	
	
	@PostMapping("/send-otp")
	public String processOtp(@RequestParam("email")String email,HttpSession session) {
		System.out.println("Email====="+email);
		
		//generating 4 digit OTP		
		int otp=random.nextInt(999999);  //here otp is created and it's digit will be depends on the passed no of digit into nextInt() method
		System.out.println("OTP======"+otp);
		
		//write code to send OTP to email....
		String subject="OTP from LCM";
		String message="<div style='border:1px; solid #e2e2e2; padding:20px'>"
				       +"<h2>"
				       +"Your OTP is "
				       +"<b>"+otp
				       +"</b>"
				       +"</h2>"
				       +"</div>";
		String to=email;
		boolean flag=this.emailService.sendEmail(subject, message, to);
		if(flag) {
			session.setAttribute("myOtp", otp);   //Save the OTP for matching the sended OTP by the user that is sended on user gmail
			session.setAttribute("myEmail", email);
			return "verify_otp";
		}else {
			session.setAttribute("message", "Please! Enter Correct Email!");
			return "forgot_password_page";
		}		
	}
	
	//verify OTP
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp")int otp,HttpSession session) {
		int myOtp=(int) session.getAttribute("myOtp");
		String myEmail=(String) session.getAttribute("myEmail");
		//Check here OTP sended by the user is correct or not
		if(myOtp==otp) {
			//Return Change Password form
			User user=userRepository.getUserByUsername(myEmail);
			if(user==null) {
				//Send error message
				session.setAttribute("message", "User does not exist with this email !!");
				return "forgot_password_page";
			}else {
				//change password form 
				return "password_change_form";
			}			
		}else {
			//otherwise send same page with message
			session.setAttribute("message", "Please Enter Valid OTP !!");
			return "verify_otp";
		}
	}
	
	//Change Password handler
	@PostMapping("/changePassword")
	public String changePassword(@RequestParam("newpassword")String newPassword,HttpSession session) {
		String username=(String) session.getAttribute("myEmail");
		User user=userRepository.getUserByUsername(username);
		user.setPassword(this.bCryptPasswordEncoder.encode(newPassword)); //set new password in encoded form
		this.userRepository.save(user);
		
		return  "redirect:/signin?change=Password changed successfully...";
		
	}
	
}
