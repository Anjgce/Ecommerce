package com.rajesh.admin.category;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.rajesh.admin.user.UserService;
import com.rajesh.admin.user.export.UserCsvExporter;
import com.rajesh.common.entity.Category;
import com.rajesh.common.entity.User;

import jakarta.servlet.http.HttpServletResponse;

@Controller
public class CategoryController {

	@Autowired
	private CategoryService catService;
	
	@GetMapping("/categories")
	public String listFirstPage(@Param("sortDir") String sortDir, Model model) {
		return listByPage(1, "asc", model, null);
	}
	
	@GetMapping("/categories/page/{pageNum}")
	public String listByPage(@PathVariable("pageNum") int pageNum, @Param("sortDir") String sortDir, Model model, @Param("keyword") String keyword) {
		//System.out.println("pageNum : "+pageNum);
		CategoryPageInfo pageInfo = new CategoryPageInfo();
		List<Category> listCategories = catService.listByPage(pageInfo, pageNum, sortDir, keyword);
		if(sortDir == null || sortDir.isEmpty()) {
			sortDir = "asc";
		}
		long startCount = (pageNum-1) * CategoryService.ROOT_CATEGORIES_PER_PAGE+1;
		long endCount = startCount + CategoryService.ROOT_CATEGORIES_PER_PAGE-1;
		if(endCount > pageInfo.getTotalElements()) {
			endCount = pageInfo.getTotalElements();
		}
		
		String reverseSortDir = sortDir.equals("asc") ? "desc" : "asc"; 
		
		model.addAttribute("totalPages", pageInfo.getTotalPages());
		model.addAttribute("totalItems", pageInfo.getTotalElements());
		model.addAttribute("currentPage", pageNum);
		model.addAttribute("sortField", "name");
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("keyword", keyword);
		model.addAttribute("startCount", startCount);
		model.addAttribute("endCount", endCount);
		model.addAttribute("listCategories", listCategories);
		model.addAttribute("reverseSortDir", reverseSortDir);
		return "categories/categories";
	}
	
	@GetMapping("/categories/new")
	public String newCategory(Model model) {
		List<Category> listCategories = catService.listCategoriesUsedInForm();
		model.addAttribute("category", new Category());
		model.addAttribute("listCategories", listCategories);
		model.addAttribute("pageTitle", "Create New Category");
		return "categories/category_form";
	}
	
	@PostMapping("/categories/save")
	public String saveCategory(Category category, @RequestParam ("fileImage") MultipartFile multipartFile, RedirectAttributes redirectAttributes) throws IOException {
		if(!multipartFile.isEmpty()) {
			String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
			category.setImage(fileName);
			
			Category savedCategory = catService.save(category);
			String uploadDir = "../category-images/" + savedCategory.getId();
			
			FileUploadUtil.cleanDir(uploadDir);
			FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);
		}else {
			catService.save(category);
		}
		
		redirectAttributes.addFlashAttribute("message", "The Category has been saved successfully.");
		
		return "redirect:/categories";
	}
	
	@GetMapping("/categories/edit/{id}")
	public String editCategory(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
		try {
			Category category =catService.get(id);
			List<Category> listCategories = catService.listCategoriesUsedInForm();
			model.addAttribute("category", category);
			model.addAttribute("listCategories", listCategories);
			model.addAttribute("pageTitle", "Edit Category ID : "+id);
			
			return "categories/category_form";
		}catch(CategoryNotFoundException ex) {
			redirectAttributes.addFlashAttribute("message", ex.getMessage());
			return "redirect:/categories";
		}
	}
	
	@GetMapping("/categories/{id}/enabled/{status}")
	public String updateCategoryEnabledStatus(@PathVariable("id") Integer id, @PathVariable("status") boolean enabled, RedirectAttributes redirectAttributes) {
		catService.updateCategoryEnabledStatus(id, enabled);
		String status = enabled ? "enabled" : "disabled";
		String message = "The category id "+id+" has been "+status;
		redirectAttributes.addFlashAttribute("message", message);
		return "redirect:/categories";
	}
	
	@GetMapping("/categories/delete/{id}")
	public String deleteCategory(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
		try {
			catService.delete(id);
			String categoryDir = "../category-images/" + id;
			FileUploadUtil.removeDir(categoryDir);
			
			redirectAttributes.addFlashAttribute("message", "The category id " + id + " has been deleted successfully");
		}catch(CategoryNotFoundException ex) {
			redirectAttributes.addFlashAttribute("message", ex.getMessage());
		}
		
		return "redirect:/categories";
	}
	
	@GetMapping("/categories/export/csv")
	public void exportToCSV(HttpServletResponse response) throws IOException {
		List<Category> listCategories = catService.listCategoriesUsedInForm();
		CategoryCsvExporter exporter = new CategoryCsvExporter();
		exporter.export(listCategories, response);
	}
}
