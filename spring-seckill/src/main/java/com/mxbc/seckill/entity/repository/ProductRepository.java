package com.mxbc.seckill.entity.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mxbc.seckill.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
	// 方法名查询：按分类和价格范围查询
	List<Product> findByCategoryAndPriceBetween(String category, BigDecimal min, BigDecimal max);

	// 关键词查询：包含、起始、结束
	List<Product> findByNameContaining(String keyword);

	List<Product> findByNameStartingWith(String prefix);

	List<Product> findByNameEndingWith(String suffix);

	// 忽略大小写查询
	List<Product> findByNameIgnoreCaseContaining(String keyword);

	// 排序查询
	List<Product> findByCategoryOrderByPriceDesc(String category);

	// 分页查询
	Page<Product> findByCategory(String category, Pageable pageable);

	// 查询前N条记录
	List<Product> findTop10ByOrderByPriceAsc();

	List<Product> findFirst5ByStockGreaterThan(Integer stock);

	// 统计查询
	long countByCategory(String category);

	boolean existsByName(String name);

	// @Query注解：JPQL查询
	@Query("SELECT p FROM Product p WHERE p.price BETWEEN :min AND :max")
	List<Product> findByPriceRange(@Param("min") BigDecimal min, @Param("max") BigDecimal max);

	// @Query注解：原生SQL查询
//	@Query(value = "SELECT * FROM products WHERE category = :category ORDER BY RAND() LIMIT :limit", nativeQuery = true)
//	List<Product> findRandomProductsByCategory(@Param("category") String category, @Param("limit") int limit);

	// 投影查询：只返回部分字段
	@Query("SELECT p.name, p.price FROM Product p WHERE p.category = :category")
	List<Object[]> findProductNamesAndPricesByCategory(@Param("category") String category);

	// 聚合查询
	@Query("SELECT AVG(p.price) FROM Product p WHERE p.category = :category")
	Double getAveragePriceByCategory(@Param("category") String category);

	// 更新操作（需要@Modifying和@Transactional）
	@Modifying
	@Query("UPDATE Product p SET p.stock = p.stock - :quantity WHERE p.id = :id")
	int decreaseStock(@Param("id") Long id, @Param("quantity") Integer quantity);

	// 批量删除
	@Modifying
	@Query("DELETE FROM Product p WHERE p.stock = 0")
	int deleteOutOfStockProducts();
}
