package com.rajesh.admin.product;
/*
* @Author : Anuj Kumar Rajesh
*/

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.rajesh.admin.brand.BrandNotFoundException;
import com.rajesh.common.entity.Brand;
import com.rajesh.common.entity.Product;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class ProductService {

	public static final int PRODUCTS_PER_PAGE = 5;

	@Autowired
	private ProductRepository prodRepo;

	List<Product> listAll() {
		return (List<Product>) prodRepo.findAll();
	}

	public Page<Product> listByPage(int pageNum, String sortField, String sortDir, String keyword, Integer categoryId) {
		Sort sort = Sort.by(sortField);
		sort = sortDir.equals("asc") ? sort.ascending() : sort.descending();
		Pageable pageable = PageRequest.of(pageNum - 1, PRODUCTS_PER_PAGE, sort);
		if (keyword != null && !keyword.isEmpty()) {
			if (categoryId != null && categoryId > 0) {
				String categoryIdMatch = "-" + String.valueOf(categoryId) + "-";
				return prodRepo.searchInCategory(categoryId, categoryIdMatch, keyword, pageable);
			}
			return prodRepo.findAll(keyword, pageable);
		}
		if (categoryId != null && categoryId > 0) {
			String categoryIdMatch = "-" + String.valueOf(categoryId) + "-";
			return prodRepo.findAllInCategory(categoryId, categoryIdMatch, pageable);
		}

		return prodRepo.findAll(pageable);
	}

	public Product save(Product product) {
		if (product.getId() == null) {
			product.setCreatedTime(new Date());
		}
		if (product.getAlias() == null || product.getAlias().isEmpty()) {
			String defaultAlias = product.getName().replaceAll(" ", "-");
			product.setAlias(defaultAlias);
		} else {
			product.setAlias(product.getAlias().replace(" ", "-"));
		}
		product.setUpdatedTime(new Date());

		return prodRepo.save(product);
	}
	
	public void saveProductPrice(Product productInForm) {
		Product productInDB = prodRepo.findById(productInForm.getId()).get();
		productInDB.setCost(productInForm.getCost());
		productInDB.setPrice(productInForm.getPrice());
		productInDB.setDiscountPercent(productInForm.getDiscountPercent());
		
		prodRepo.save(productInDB);
	}
	

	public String checkUnique(Integer id, String name) {
		boolean isCreatingNew = (id == null || id == 0);

		Product productByName = prodRepo.findByName(name);

		if (isCreatingNew) {
			if (productByName != null) {
				return "Duplicate";
			}
		} else {
			if (productByName != null && productByName.getId() != id) {
				return "Duplicate";
			}
		}
		return "OK";
	}

	public void updateProductEnabledStatus(Integer id, boolean enabled) {
		prodRepo.updateEnabledStatus(id, enabled);
	}

	public void delete(Integer id) throws ProductNotFoundException {
		Long count = prodRepo.countById(id);
		if (count == null || count == 0) {
			throw new ProductNotFoundException("Could not find any product with ID " + id);
		}
		prodRepo.deleteById(id);
	}

	public Product get(Integer id) throws ProductNotFoundException {
		try {
			return prodRepo.findById(id).get();
		} catch (NoSuchElementException ex) {
			throw new ProductNotFoundException("Could not find any product with ID : " + id);
		}
	}
}
