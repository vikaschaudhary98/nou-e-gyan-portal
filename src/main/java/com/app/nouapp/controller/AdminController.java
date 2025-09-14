package com.app.nouapp.controller;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import java.util.jar.Attributes;

import org.hibernate.sql.ast.tree.update.Assignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.nouapp.dto.StudyMaterialDto;
import com.app.nouapp.model.AdminInfo;
import com.app.nouapp.model.Enquiry;
import com.app.nouapp.model.Response;
import com.app.nouapp.model.StudentInfo;
import com.app.nouapp.model.StudyMaterial;
import com.app.nouapp.service.AdminInfoRepository;
import com.app.nouapp.service.EnquiryRepository;
import com.app.nouapp.service.ResponseRepository;
import com.app.nouapp.service.StudentInfoRepository;
import com.app.nouapp.service.StudyMaterialRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	AdminInfoRepository adrepo;

	@Autowired
	StudentInfoRepository stdrepo;

   

    @Autowired
     EnquiryRepository eqrepo;	
	
	@Autowired
	ResponseRepository resrepo;

	@Autowired
	StudyMaterialRepository smrepo;

	@GetMapping("/adhome")
	public String ShowAdminDaseboard(HttpSession session, RedirectAttributes attributes, Model model) {
		if (session.getAttribute("admin") != null) {
			AdminInfo ad = adrepo.getById(session.getAttribute("admin").toString());
			model.addAttribute("name", ad.getName());

			int scount=(int) stdrepo.count();
		    model.addAttribute("scount", scount);
		    List<StudyMaterial> smlist=smrepo.findByMaterialType("studymaterial");
		    int smcount=smlist.size();
		    model.addAttribute("smcount", smcount);
		    List<StudyMaterial> aslist=smrepo.findByMaterialType("assignment");
		    int acount=aslist.size();
		    model.addAttribute("acount", acount);
		    List<Response> flist=resrepo.findByResponseType("feedback");
		    int fcount=flist.size();
		    model.addAttribute("fcount", fcount);
		    List<Response> clist=resrepo.findByResponseType("complaint");
		    int ccount=clist.size();
		    model.addAttribute("ccount", ccount);
		    int ecount=(int) eqrepo.count();
		    model.addAttribute("ecount", ecount);
			return "admin/admindashboard";
		} else {
			attributes.addFlashAttribute("msg", "session Expired");
			return "redirect:/adminlogin";
		}
	}

	@GetMapping("/manstudent")
	public String ShowManageStudent(HttpSession session, RedirectAttributes attributes, Model model) {
		if (session.getAttribute("admin") != null) {
			AdminInfo ad = adrepo.getById(session.getAttribute("admin").toString());
			model.addAttribute("name", ad.getName());

			List<StudentInfo> slist = stdrepo.findAll();
			model.addAttribute("slist", slist);

			return "admin/manstudent";
		} else {
			attributes.addFlashAttribute("msg", "session Expired!");
			return "redirect:/adminlogin";
		}
	}

	@GetMapping("/deletestudent")
	public String DeleteStudent(@RequestParam long enroll, HttpSession session, RedirectAttributes attributes,
			Model model) {
		if (session.getAttribute("admin")!= null) {
			StudentInfo stdinfo = stdrepo.findById(enroll).get();
			stdrepo.delete(stdinfo);
			attributes.addFlashAttribute("msg", stdinfo.getName() + " is deleted Successfully");
			return "redirect:/admin/manstudent";
		} else {
			attributes.addFlashAttribute("msg", "Session Expired");
			return "redirect:/adminlogin";
		}
	}

	@GetMapping("/addstudymaterial")
	public String ShowAddStudyMaterial(HttpSession session, RedirectAttributes attributes, Model model) {
		if (session.getAttribute("admin") != null) {
			AdminInfo ad = adrepo.getById(session.getAttribute("admin").toString());
			model.addAttribute("name", ad.getName());

			StudyMaterialDto dto = new StudyMaterialDto();
			model.addAttribute("dto", dto);

			return "admin/addstudymaterial";
		} 
		else
		{
			attributes.addFlashAttribute("msg", "Session Expired");
			return "redirect:/adminlogin";
		}

	}

	@PostMapping("/addstudymaterial")
	public String AddStudyMaterial(@ModelAttribute StudyMaterialDto dto, RedirectAttributes attributes) {
		try {
			MultipartFile filedata = dto.getFilename();
			String strogeFileName = filedata.getOriginalFilename();
			long size = filedata.getSize();
			int s = (int) size / (1024 * 1024); // file size in MB
			if (size >5242880) {
				attributes.addFlashAttribute("msg", "File Should be Less than 5 MB");
				return "redirect:/admin/addstudymaterial";
			}
			String uploadDir = "public/mat/";
			Path UploadPath = Paths.get(uploadDir);

			if (!Files.exists(UploadPath)) {
				Files.createDirectories(UploadPath);
			}

			try (InputStream inputStream = filedata.getInputStream()) {
				Files.copy(inputStream, Paths.get(uploadDir + strogeFileName), StandardCopyOption.REPLACE_EXISTING);
			}

			StudyMaterial material = new StudyMaterial();
			material.setCourse(dto.getCourse());
			material.setBranch(dto.getBranch());
			material.setYear(dto.getYear());
			material.setSubject(dto.getSubject());
			material.setTopic(dto.getTopic());
			material.setMaterialtype(dto.getMaterialtype());
			material.setFilename(strogeFileName);

			Date dt = new Date();
			SimpleDateFormat df = new SimpleDateFormat();
			String posteddate = df.format(dt);
			material.setPosteddate(posteddate);
			smrepo.save(material);
			attributes.addFlashAttribute("msg", "Material Uploaded Successfully");
			return "redirect:/admin/addstudymaterial";

		} catch (Exception e) {
			attributes.addFlashAttribute("msg", "Something went wrong " + e.getMessage());
			return "redirect:/admin/addstudymaterial";
		}
	}

	@GetMapping("/managestudymaterial")
	public String ShowManageStudyMaterial(HttpSession session, RedirectAttributes attributes,Model model)
	{
		
		if(session.getAttribute("admin")!=null)
		{
			AdminInfo ad=adrepo.getById(session.getAttribute("admin").toString());
			  List<StudyMaterial> mslist= smrepo.findAll();
			  model.addAttribute("mslist", mslist);
			  
		    model.addAttribute("name", ad.getName());
		 
			return "admin/managestudymaterial";
		}
		else
		{
			attributes.addFlashAttribute("msg", "Session Expired");
			return "redirect:/adminlogin";
		}
	}
	@GetMapping("/deletestudymaterial")
	public String DeleteStudyMaterial(@RequestParam int id, HttpSession session, RedirectAttributes attributes,Model model)
	{
		
		if(session.getAttribute("admin")!=null)
		{
			StudyMaterial sm=smrepo.findById(id).get();
			smrepo.delete(sm);
			attributes.addFlashAttribute("msg", sm.getFilename() + "is deleted succesfully");
			return "redirect:/admin/addstudymaterial";
		}
		else
		{
			attributes.addFlashAttribute("msg", "Session Expired");
			return "redirect:/adminlogin";
		}
	}
	
	@GetMapping("/manageassignment")
	public String ShowManageAssignment(HttpSession session, RedirectAttributes attributes,Model model)
	{
		
		if(session.getAttribute("admin")!=null)
		{
			AdminInfo ad=adrepo.getById(session.getAttribute("admin").toString());
			  List<StudyMaterial> aslist= smrepo.findAll();
			  model.addAttribute("aslist", aslist);
			  
		    model.addAttribute("name", ad.getName());
		 
			return "admin/managestudymaterial";
		}
		else
		{
			attributes.addFlashAttribute("msg", "Session Expired");
			return "redirect:/adminlogin";
		}
	}
	
	//@GetMapping("/deleteassignment")
	//public String DeleteAssignment(@RequestParam int id, HttpSession session, RedirectAttributes attributes,Model model)
	//{
		
	//	if(session.getAttribute("admin")!=null)
	//	{
	//		Assignment as=smrepo.findById(id).get();
	//		smrepo.delete(as);
	//		attributes.addFlashAttribute("msg", as.getFilename() + "is deleted succesfully");
	//		return "redirect:/admin/addstudymaterial";
	//	}
	//	else
	//	{
	//		attributes.addFlashAttribute("msg", "Session Expired");
	//		return "redirect:/adminlogin";
	//	}
	//}
	
	
	
	
	@GetMapping("/viewfeedback")
	public String ShowViewFeedback(HttpSession session, RedirectAttributes attributes, Model model) {
		if (session.getAttribute("admin") != null) {
			AdminInfo ad = adrepo.getById(session.getAttribute("admin").toString());
			model.addAttribute("name", ad.getName());

			List<Response> flist = resrepo.findByResponseType("feedback");
			model.addAttribute("flist",flist);
			return "admin/viewfeedback";
		} else {
			attributes.addFlashAttribute("msg", "Session Expired");
			return "redirect:/adminlogin";
		}
	}
	@GetMapping("/deletefeedback")
	public String DeleteFeedback(@RequestParam int id, HttpSession session, RedirectAttributes attributes,Model model)
	{
		
		if(session.getAttribute("admin")!=null)
		{
			Response res=resrepo.findById(id).get();
			resrepo.delete(res);
			attributes.addFlashAttribute("msg", res.getName() + "is deleted succesfully");
			return "redirect:/admin/viewfeedback";
		}
		else
		{
			attributes.addFlashAttribute("msg", "Session Expired");
			return "redirect:/adminlogin";
		}
	}

	@GetMapping("/viewcomplaint")
	public String ShowComplaint(HttpSession session, RedirectAttributes attributes, Model model) {
		if (session.getAttribute("admin") != null) {
			AdminInfo ad = adrepo.getById(session.getAttribute("admin").toString());
			model.addAttribute("name", ad.getName());
			List<Response> clist = resrepo.findByResponseType("complaint");
			model.addAttribute("clist", clist);
			return "admin/viewcomplaint";
		} else {
			attributes.addFlashAttribute("msg", "Session Expired");
			return "redirect:/adminlogin";
		}

	}
	@GetMapping("/deletecomplaint")
	public String DeleteComplaint(@RequestParam int id, HttpSession session, RedirectAttributes attributes,Model model)
	{
		
		if(session.getAttribute("admin")!=null)
		{
			Response res=resrepo.findById(id).get();
			resrepo.delete(res);
			attributes.addFlashAttribute("msg", res.getName() + "is deleted succesfully");
			return "redirect:/admin/viewcomplaint";
		}
		else
		{
			attributes.addFlashAttribute("msg", "Session Expired");
			return "redirect:/adminlogin";
		}
	}

	@GetMapping("/viewenquiry")
	public String ShowViewEnquiry(HttpSession session, RedirectAttributes attributes, Model model) {
		if (session.getAttribute("admin") != null) {
			AdminInfo ad = adrepo.getById(session.getAttribute("admin").toString());
			model.addAttribute("name", ad.getName());

			List<Enquiry> elist = eqrepo.findAll();
			model.addAttribute("elist", elist);
			return "admin/viewenquiry";
		} else {
			attributes.addFlashAttribute("msg", "session Expired!");
			return "redirect:/adminlogin";
		}
	}
	
	@GetMapping("/deleteenquiry")
	public String DeleteEnquiry(@RequestParam int id,HttpSession session, RedirectAttributes attributes, Model model)
	{
		if(session.getAttribute("admin")!=null)
		{
			Enquiry eq = eqrepo.findById(id).get();
			eqrepo.delete(eq);
			attributes.addFlashAttribute("msg", eq.getName() +"is delete successfully");
			return "redirect:/admin/manstudent";
		}
		else {
			attributes.addFlashAttribute("msg","Session Expired");
			return "redirect:/adminlogin";
		}
	}
     
	@GetMapping("/changepassword")
	public String ShowChangePassword(HttpSession session,Model model,RedirectAttributes attributes )
{
		
		if(session.getAttribute("admin")!=null)
		{
			AdminInfo ad=adrepo.getById(session.getAttribute("admin").toString());
		   
		    model.addAttribute("name", ad.getName());
			return "admin/changepassword";
		}
		else
		{
			attributes.addFlashAttribute("msg", "Session Expired");
			return "redirect:/adminlogin";
		}
	}
	
	@PostMapping("/changepassword")
	public String ChangePassword(HttpSession session, RedirectAttributes attributes, HttpServletRequest request) {
		try {

			AdminInfo adinfo = adrepo.findById(session.getAttribute("admin").toString()).get();
			String oldpass = request.getParameter("oldpass");
			String newpass = request.getParameter("newpass");
			String confirmpass = request.getParameter("confirmpass");

			if (newpass.equals(confirmpass)) {
				if (oldpass.equals(adinfo.getPassword())) {
					adinfo.setPassword(confirmpass);
					adrepo.save(adinfo);
					session.invalidate();
					attributes.addFlashAttribute("msg", "Password Changed Succesfully 😀");
					return "redirect:/adminlogin";
				} else {
					attributes.addFlashAttribute("message", "Invalid Old Password");
				}
			} else {
				attributes.addFlashAttribute("message", "New Password & Confirm Password Not Matched!");
			}
			return "redirect:/admin/changepassword";

		} catch (Exception e) {
			attributes.addFlashAttribute("message", "Something went wrong " + e.getMessage());
			return "redirect:/admin/changepassword";
		}
	}

	@GetMapping("/logout")
	public String Logout(HttpSession session, RedirectAttributes attributes) {
		if (session.getAttribute("admin") != null) {
			session.invalidate();
			attributes.addFlashAttribute("msg", "Successfully Logout");
			return "redirect:/adminlogin";
		} else {
			attributes.addFlashAttribute("msg", "session expire");
			return "redirect:/adminlogin";
		}
	}
}
