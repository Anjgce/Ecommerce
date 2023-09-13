package com.rajesh.admin.product;
/*
* @Author : Anuj Kumar Rajesh
*/

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.Rollback;

import com.rajesh.common.entity.Brand;
import com.rajesh.common.entity.Category;
import com.rajesh.common.entity.Product;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
public class ProductRepositoryTests {

	@Autowired
	private ProductRepository repo;
	
	@Autowired
	private TestEntityManager entityManager;
	
	@Test
	public void testCreateProduct() {
		Brand brand = entityManager.find(Brand.class, 1);
		Category category = entityManager.find(Category.class, 9);
		
		Product product = new Product();
		product.setName("Acer Bags");
		product.setAlias("acer_bags");
		product.setShortDescription("A good bag by acer");
		product.setFullDescription("This is a very good bag from acer full description");
		
		product.setBrand(brand);
		product.setCategory(category);
		product.setPrice(500);
		product.setCost(600);
		product.setEnabled(true);
		product.setInStock(true);
		
		product.setCreatedTime(new Date());
		product.setUpdatedTime(new Date());
		
		Product savedProduct = repo.save(product);
		
		assertThat(savedProduct).isNotNull();
		assertThat(savedProduct.getId()).isGreaterThan(0);
	}
	
	@Test
	public void testListAllProducts() {
		Iterable<Product> iterableProducts = repo.findAll();
		
		iterableProducts.forEach(product->System.out.println(product));
	}
	
	@Test
	public void testGetProduct() {
		Integer id = 2;
		Product product = repo.findById(id).get();
		System.out.println(product);
		assertThat(product).isNotNull();
	}
	
	@Test
	public void testUpdateProduct() {
		Integer id = 1;
		Product product = repo.findById(id).get();
		product.setPrice(499);
		
		repo.save(product);
		
		Product updatedProduct = entityManager.find(Product.class, id);
		
		assertThat(updatedProduct.getPrice()).isEqualTo(499);
	}
	
	@Test
	public void testDeleteProduct() {
		Integer id = 3;
		repo.deleteById(id);
		
		Optional<Product> result = repo.findById(id);
		assertThat(!result.isPresent());
	}
	
	@Test
	public void testSaveProductWithImages() {
		Integer id = 1;
		Product product = repo.findById(id).get();
		product.setMainImage("main image.jpg");
		product.addExtraImage("extra image1.png");
		product.addExtraImage("extra_image2.png");
		product.addExtraImage("extra-image1.png");
		
		Product savedProduct = repo.save(product);
		
		assertThat(savedProduct.getImages().size()).isEqualTo(3);
	}
	
	@Test
	public void testSaveProductDetails() {
		Integer id = 1;
		Product product = repo.findById(id).get();
		product.addDetail("Device Memory", "128 GB");
		product.addDetail("CPU Model", "MediaTek");
		product.addDetail("OS", "Android 14");
		
		Product savedProduct = repo.save(product);
		
		assertThat(savedProduct.getDetails()).isNotEmpty();
	}
}
