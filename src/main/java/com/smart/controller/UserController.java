package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entity.Contact;
import com.smart.entity.User;
import com.smart.helper.Messages;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;   //By this, we can verify our old password and set new Password in encoded form.
	
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;
	
	//method to adding common data to response
	@ModelAttribute
	public void addCommonData(Model m,Principal principal) {
		String userName=principal.getName();
		//get the user by username
		User user=userRepository.getUserByUsername(userName);
		m.addAttribute("user",user);
	}
	
	//dashboard home
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal) {
		model.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}
	
	//open add contact handler form
	@GetMapping("/add-contact")
	public String openAddFormHandler(Model model) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	} 
	
	//process contact
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,@RequestParam("profileImage")MultipartFile file,   Principal principal,HttpSession session) {
		try {
			String name=principal.getName();
			User user=userRepository.getUserByUsername(name);
			
			//processing and uploading file....
			if(file.isEmpty()) {
				//if file is empty then fire this message...
				
				contact.setImages("Google_Contacts_logo.png");
				
			}else {
				//fill the file to folder and update the name to the contact...
				contact.setImages(file.getOriginalFilename());
				File saveFile=new ClassPathResource("static/Image").getFile();
				Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("File has been uploaded....");
			}
			
			//to add user id into contact
			contact.setUser(user);
			//to add contact into user
			user.getContact().add(contact);
			this.userRepository.save(user);
			
			
			//message success.....
			session.setAttribute("message", new Messages("Successfully added to the contact!! Add more", "success"));
			
		}catch(Exception e) {
			System.out.println("Error "+e.getMessage());
			e.printStackTrace();
			//error message.....
			session.setAttribute("message", new Messages("Failed to add contact !! Try again", "danger"));
			
		}
		
		return "normal/add_contact_form";
	}
	
	//show contacts handler
	//Per page content=5 
	//Current page =0[page]
	@GetMapping("/showContacts/{page}")
	public String showContacts(@PathVariable("page")Integer page, Model model,Principal principal) {
		String username=principal.getName();
		User user=userRepository.getUserByUsername(username);
		//current Page- page
		//Current per page- 5
		Pageable pageable=PageRequest.of(page, 8);
		Page<Contact> contacts=contactRepository.findContactByUser(user.getId(),pageable);
		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("title", "Contacts");
		//To get number of pages
		model.addAttribute("totalPages", contacts.getTotalPages());
		return "normal/show_contacts";
	}
	
	
	//Showing Specific details...
	@GetMapping("/{cid}/contact")
	public String showContactDetails(@PathVariable("cid")Integer cid,Model model,Principal principal) {
		Optional<Contact> contactOptional=contactRepository.findById(cid);
		Contact contact=contactOptional.get();
		String name=principal.getName();
		User user=this.userRepository.getUserByUsername(name);
		if(user.getId()==contact.getUser().getId()) {
			model.addAttribute("contact",contact);
			model.addAttribute("title",contact.getName());
		}
		
		
		System.out.println("Cid===  "+cid);
		return "normal/contact_details";
	}
	
	//Delete contact handler
	@GetMapping("/delete/{cid}")
	@Transactional
	public String deleteContact(@PathVariable("cid")Integer cid,Model model,Principal principal,HttpSession session) {
		
		Optional<Contact> contactOptional=this.contactRepository.findById(cid);
		Contact contact=contactOptional.get();
		String name=principal.getName();
		User user=this.userRepository.getUserByUsername(name);
        user.getContact().remove(contact);
		this.userRepository.save(user);
		session.setAttribute("message",new Messages("Contact deleted successfully...","success"));
				
		return "redirect:/user/showContacts/0";
	}
	
	//Open update form handler
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid")Integer cid, Model model) {
		model.addAttribute("title","Update Contact");
		Contact contact=this.contactRepository.findById(cid).get();
		model.addAttribute("contact",contact);
		
		return "normal/update_form";
	}
	
	//updated form handler
	@PostMapping("/process-update")
	@Transactional
	public String updatedForm(@ModelAttribute Contact contact,@RequestParam("profileImage")MultipartFile file,Model model,HttpSession session,Principal principal) {
		try {
			//old contact details
				Contact oldContact=this.contactRepository.findById(contact.getCid()).get();
			//image
			if(!file.isEmpty()) {
				//file-work
				//re-write
				
				//delete old photo
				File deleteFile=new ClassPathResource("static/Image").getFile();
				File file1=new File(deleteFile,oldContact.getImages());
				file1.delete();
				
				//update new photo
				File saveFile=new ClassPathResource("static/Image").getFile();
				Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImages(file.getOriginalFilename());
	
			}else {
				//If new image is not selected then set old again
				contact.setImages(oldContact.getImages());
			}
			
			String name=principal.getName();
			User user=this.userRepository.getUserByUsername(name);
			//Here is a problem that when we are set this user with contact then contact is not updating. 
			contact.setUser(user);
			
			this.contactRepository.save(contact);
			session.setAttribute("message",new Messages("Your contact has been updated!!", "success"));
			
		}catch (Exception e) {
			e.printStackTrace();
		}		
		return "redirect:/user/"+contact.getCid()+"/contact";
	}
	
	
	//Your Profile
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		model.addAttribute("title","Your Profile");
		return "normal/profile";
	}
	
	 //Return view page to enter  your old and new password
	@GetMapping("/settings")
	public String settingProfile(Model model) {
		model.addAttribute("title", "Change Password");
		return "normal/settings";
	}
	//change password handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword")String oldPassword,@RequestParam("newPassword")String newPassword,Principal principal,HttpSession session) {
		System.out.println("Old Password==="+oldPassword);
		System.out.println("New Password==="+newPassword);
		String username=principal.getName();
		User currentUser=this.userRepository.getUserByUsername(username);
		System.out.println("currentPassword===="+currentUser.getPassword());
		
		//To verify our old password
		if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword)); //set new password in encoded form
			this.userRepository.save(currentUser);
			session.setAttribute("message",new Messages("Your Password Successfully Changed!!", "success"));
			return "redirect:/user/index";
		}else {
			//error...
			session.setAttribute("message",new Messages("Please Enter Correct Old Password!!", "danger"));
			return "normal/settings";
		}
		
	}
}
