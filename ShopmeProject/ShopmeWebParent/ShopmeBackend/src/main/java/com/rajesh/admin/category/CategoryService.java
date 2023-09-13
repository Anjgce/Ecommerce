package com.rajesh.admin.category;

import java.util.ArrayList;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.rajesh.common.entity.Category;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class CategoryService {

	@Autowired
	private CategoryRepository catRepo;
	
	public static final int ROOT_CATEGORIES_PER_PAGE = 4;
	
	public List<Category> listByPage(CategoryPageInfo pageInfo, int pageNum, String sortDir, String keyword) {
		Sort sort = Sort.by("name");
		
		if(sortDir == null || sortDir.isEmpty()) {
			sort = sort.ascending();
		}else if(sortDir.equals("asc")) {
			sort = sort.ascending();
		}else if(sortDir.equals("desc")){
			sort = sort.descending();
		}
		
		Pageable  pageable = PageRequest.of(pageNum-1, ROOT_CATEGORIES_PER_PAGE, sort);
		Page<Category> pageCategories = null;
		
		if(keyword != null && !keyword.isEmpty()) {
			pageCategories = catRepo.search(keyword, pageable);
		}else {
			pageCategories = catRepo.findRootCategories(pageable);
		}
		
		List<Category> rootCategories = pageCategories.getContent();
		
		pageInfo.setTotalElements(pageCategories.getTotalElements());
		pageInfo.setTotalPages(pageCategories.getTotalPages());
		
		if(keyword != null && !keyword.isEmpty()) {
			List<Category> searchResult = pageCategories.getContent();
			for(Category category : searchResult) {
				category.setHasChildren(category.getChildren().size()>0);
			}
			return searchResult;
		}else {
			return listHierarchicalCategories(rootCategories, sortDir);
		}
		
		
	}
	
	public List<Category> listHierarchicalCategories(List<Category> rootCategories, String sortDir){
		List<Category> hierarchicalCategories = new ArrayList<>();
		for(Category rootCategory : rootCategories) {
			hierarchicalCategories.add(Category.copyFull(rootCategory));
			
			Set<Category> children = sortSubCategories(rootCategory.getChildren(), sortDir);
			
			for(Category subCategory : children) {
				String name = "--"+subCategory.getName();
				hierarchicalCategories.add(Category.copyFull(subCategory, name));
				listSubHierarchicalCategories(hierarchicalCategories, subCategory, 1, sortDir);
			}
		}
		
		return hierarchicalCategories;
	}
	
	public void listSubHierarchicalCategories(List<Category> hierarchicalCategories,Category parent, int subLevel, String sortDir) {
		Set<Category> children = sortSubCategories(parent.getChildren(), sortDir);
		int newSubLevel = subLevel + 1;
		for(Category subCategory : children) {
			String name = "";
			for(int i=0; i<newSubLevel; i++) {
				name+="--";
			}
			name+=subCategory.getName();
			hierarchicalCategories.add(Category.copyFull(subCategory, name));
			listSubHierarchicalCategories(hierarchicalCategories, subCategory, newSubLevel, sortDir);
		}
	}
	
	public Category save(Category category) {
		Category parent = category.getParent();
		if(parent != null) {
			String allParentIDs = parent.getAllParentIDs() == null ? "-" : parent.getAllParentIDs();
			allParentIDs+=String.valueOf(parent.getId()) + "-";
			category.setAllParentIDs(allParentIDs);
		}
		return catRepo.save(category);
	}
	
	public List<Category> listCategoriesUsedInForm(){
		List<Category> categoriesUsedInForm = new ArrayList<>();
		
		Iterable<Category> categoriesInDB = catRepo.findRootCategories(Sort.by("name").ascending());
		
		for(Category category : categoriesInDB) {
			if(category.getParent() == null) {
				categoriesUsedInForm.add(Category.copyIdAndName(category));
				
				Set<Category> children = sortSubCategories(category.getChildren());
				
				for(Category subCategory : children) {
					String name = "--"+subCategory.getName();
					categoriesUsedInForm.add(Category.copyIdAndName(subCategory.getId(), name));
					listSubCategoriesUsedInForm(categoriesUsedInForm, subCategory, 1);
				}
			}
		}	
		
		return categoriesUsedInForm;
	}
	private void listSubCategoriesUsedInForm(List<Category> categoriesUsedInForm, Category parent, int subLevel) {
		
		int newSubLevel = subLevel + 1;
		Set<Category> children = sortSubCategories(parent.getChildren());
		for(Category subCategory : children) {
			String name = "";
			for(int i=0; i<newSubLevel; i++) {
				name+="--";
			}
			name+=subCategory.getName();
			categoriesUsedInForm.add(Category.copyIdAndName(subCategory.getId(), name));
			listSubCategoriesUsedInForm(categoriesUsedInForm,subCategory, newSubLevel);
		}
	}
	
	public Category get(Integer id) throws CategoryNotFoundException {
		try {
			return catRepo.findById(id).get();
		}catch(NoSuchElementException ex) {
			throw new CategoryNotFoundException("Could not found any category with ID : "+id);
		}
	}
	
	public String checkUnique(Integer id, String name, String alias) {
		boolean isCreatingNew = (id == null || id == 0);
		Category categoryByName = catRepo.findByName(name);
		
		if(isCreatingNew) {
			if(categoryByName != null) {
				return "DuplicateName";
			}else {
				Category categoryByAlias = catRepo.findByAlias(alias);
				if(categoryByAlias != null) {
					return "DuplicateAlias";
				}
			}
		}else {
			if(categoryByName != null && categoryByName.getId() != id) {
				return "DuplicateName";
			}
			
			Category categoryByAlias = catRepo.findByAlias(alias);
			if(categoryByAlias != null && categoryByAlias.getId() != id) {
				return "DuplicateAlias";
			}
		}
		return "OK";
	}
	
	//return sub categories used in form
	private Set<Category> sortSubCategories(Set<Category> children){
		return sortSubCategories(children, "asc");
	}

	private Set<Category> sortSubCategories(Set<Category> children, String sortDir){
		if(sortDir == null || sortDir.isEmpty()) {
			return children.stream().sorted((name1, name2)->name1.getName().compareTo(name2.getName())).collect(Collectors.toSet());
		}else if(sortDir.equals("asc")) {
			return children.stream().sorted((name1, name2)->name1.getName().compareTo(name2.getName())).collect(Collectors.toSet());
		}else {
			return children.stream().sorted((name1, name2)->name2.getName().compareTo(name1.getName())).collect(Collectors.toSet());
		}
		
	}
	
	public void updateCategoryEnabledStatus(Integer id, boolean enabled) {
		catRepo.updateEnabledStatus(id, enabled);
	}
	
	public void delete(Integer id) throws CategoryNotFoundException {
		Long count = catRepo.countById(id);
		if(count == null || count == 0) {
			throw new CategoryNotFoundException("Could not find any category with ID "+id);
		}
		catRepo.deleteById(id);
	}
}
