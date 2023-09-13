package com.rajesh.admin.brand;
/*
* @Author : Anuj Kumar Rajesh
*/

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.rajesh.admin.FileUploadUtil;
import com.rajesh.admin.category.CategoryNotFoundException;
import com.rajesh.admin.category.CategoryService;
import com.rajesh.admin.user.UserService;
import com.rajesh.common.entity.Brand;
import com.rajesh.common.entity.Category;
import com.rajesh.common.entity.User;

@Controller
public class BrandController {

	private static final Logger LOGGER = LoggerFactory.getLogger(BrandController.class);
	@Autowired
	private BrandService brandService;
	
	@Autowired
	private CategoryService catService;
	
	@GetMapping("/brands")
	public String listByFirstPage(Model model) {
		LOGGER.info("Entering into listByFirstPage() in BrandContoller");
		return listByPage(1, model, "name", "asc", null);
		
	}
	
	@GetMapping("/brands/page/{pageNum}")
	public String listByPage(@PathVariable("pageNum") int pageNum, Model model, @Param("sortField") String sortField, 
																				@Param("sortDir") String sortDir,
																				@Param("keyword") String keyword) {
		Page<Brand> page = brandService.listByPage(pageNum, sortField, sortDir, keyword);
		List<Brand> listBrands = page.getContent();
		long startCount = (pageNum-1) * BrandService.BRANDS_PER_PAGE+1;
		long endCount = startCount + BrandService.BRANDS_PER_PAGE-1;
		if(endCount > page.getTotalElements()) {
			endCount = page.getTotalElements();
		}
		String reverseSortDir = sortDir.equals("asc") ? "desc" : "asc";
		model.addAttribute("currentPage", pageNum);
		model.addAttribute("totalPages", page.getTotalPages());
		model.addAttribute("startCount", startCount);
		model.addAttribute("endCount", endCount);
		model.addAttribute("totalItems", page.getTotalElements());
		model.addAttribute("listBrands", listBrands);
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("reverseSortDir", reverseSortDir);
		model.addAttribute("keyword", keyword);
		return "brands/brands";
	}
	
	@GetMapping("/brands/new")
	public String newBrand(Model model) {
		List<Category> listCategories = catService.listCategoriesUsedInForm();
		model.addAttribute("brand", new Brand());
		model.addAttribute("listCategories", listCategories);
		model.addAttribute("pageTitle", "Create New Brand");
		
		
		return "brands/brand_form";
	}
	
	@PostMapping("/brands/save")
	public String saveBrand(Brand brand, @RequestParam ("fileImage") MultipartFile multipartFile, RedirectAttributes redirectAttributes) throws IOException {
		if(!multipartFile.isEmpty()) {
			String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
			brand.setLogo(fileName);
			Brand savedBrand = brandService.save(brand);
			String uploadDir = "../brand-logos/" + savedBrand.getId();
			FileUploadUtil.cleanDir(uploadDir);
			FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);
			
		}else {
			brandService.save(brand);
		}
		
		redirectAttributes.addFlashAttribute("message", "The Brand has been saved successfully.");
		
		return "redirect:/brands";
	}
	
	@GetMapping("/brands/edit/{id}")
	public String editBrand(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
		try {
			Brand brand =brandService.get(id);
			List<Category> listCategories = catService.listCategoriesUsedInForm();
			model.addAttribute("brand", brand);
			model.addAttribute("listCategories", listCategories);
			model.addAttribute("pageTitle", "Edit Brand ID : "+id);
			
			return "brands/brand_form";
		}catch(BrandNotFoundException ex) {
			redirectAttributes.addFlashAttribute("message", ex.getMessage());
			return "redirect:/brands";
		}
	}
	
	@GetMapping("/brands/delete/{id}")
	public String deleteBrand(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
		try {
			brandService.delete(id);
			String brandDir = "../brand-logos/" + id;
			FileUploadUtil.removeDir(brandDir);
			
			redirectAttributes.addFlashAttribute("message", "The brand id " + id + " has been deleted successfully");
		}catch(BrandNotFoundException ex) {
			redirectAttributes.addFlashAttribute("message", ex.getMessage());
		}
		
		return "redirect:/brands";
	}
}
