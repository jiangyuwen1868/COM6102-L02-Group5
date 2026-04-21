package com.mxbc.seckill.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mxbc.seckill.entity.Product;
import com.mxbc.seckill.entity.repository.ProductRepository;

@RestController
@RequestMapping("/api/admin/product")
public class ProductManagementController {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // 新增商品
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createProduct(@RequestBody Map<String, Object> productData) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Product product = new Product();
            product.setName((String) productData.get("name"));
            product.setDescription((String) productData.get("description"));
            
            // 处理价格字段
            if (productData.get("price") != null) {
                if (productData.get("price") instanceof String) {
                    product.setPrice(new BigDecimal((String) productData.get("price")));
                } else if (productData.get("price") instanceof Number) {
                    product.setPrice(new BigDecimal(productData.get("price").toString()));
                }
            }
            
            if (productData.get("originalPrice") != null) {
                if (productData.get("originalPrice") instanceof String) {
                    product.setOriginalPrice(new BigDecimal((String) productData.get("originalPrice")));
                } else if (productData.get("originalPrice") instanceof Number) {
                    product.setOriginalPrice(new BigDecimal(productData.get("originalPrice").toString()));
                }
            }
            
            product.setStock((Integer) productData.get("stock"));
            product.setCategory((String) productData.get("category"));
            product.setImageUrl((String) productData.get("imageUrl"));
            
            // 处理时间字段
            if (productData.get("startTime") != null && !productData.get("startTime").equals("")) {
                try {
                    String startTimeStr = productData.get("startTime").toString();
                    // 尝试解析不同格式的时间字符串
                    if (startTimeStr.contains("T")) {
                        startTimeStr = startTimeStr.replace("T", " ");
                    }
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    product.setStartTime(LocalDateTime.parse(startTimeStr, formatter));
                } catch (Exception e) {
                    // 时间格式解析失败，设置为null
                    product.setStartTime(null);
                }
            }
            
            if (productData.get("endTime") != null && !productData.get("endTime").equals("")) {
                try {
                    String endTimeStr = productData.get("endTime").toString();
                    // 尝试解析不同格式的时间字符串
                    if (endTimeStr.contains("T")) {
                        endTimeStr = endTimeStr.replace("T", " ");
                    }
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    product.setEndTime(LocalDateTime.parse(endTimeStr, formatter));
                } catch (Exception e) {
                    // 时间格式解析失败，设置为null
                    product.setEndTime(null);
                }
            }
            
            // 设置创建和更新时间
            LocalDateTime now = LocalDateTime.now();
            product.setCreatedAt(now);
            product.setUpdatedAt(now);
            
            // 保存商品
            Product savedProduct = productRepository.save(product);
            
            // 更新Redis缓存
            updateProductCache(savedProduct);
            
            result.put("code", 0);
            result.put("message", "商品创建成功");
            result.put("data", savedProduct);
        } catch (Exception e) {
            result.put("code", -1);
            result.put("message", "商品创建失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 修改商品
    @PutMapping("/update/{productId}")
    public ResponseEntity<Map<String, Object>> updateProduct(@PathVariable Long productId, @RequestBody Map<String, Object> productData) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 查找商品
            Product existingProduct = productRepository.findById(productId).orElse(null);
            if (existingProduct == null) {
                result.put("code", -1);
                result.put("message", "商品不存在");
                return ResponseEntity.ok(result);
            }
            
            // 更新商品信息
            existingProduct.setName((String) productData.get("name"));
            existingProduct.setDescription((String) productData.get("description"));
            
            // 处理价格字段
            if (productData.get("price") != null) {
                if (productData.get("price") instanceof String) {
                    existingProduct.setPrice(new BigDecimal((String) productData.get("price")));
                } else if (productData.get("price") instanceof Number) {
                    existingProduct.setPrice(new BigDecimal(productData.get("price").toString()));
                }
            }
            
            if (productData.get("originalPrice") != null) {
                if (productData.get("originalPrice") instanceof String) {
                    existingProduct.setOriginalPrice(new BigDecimal((String) productData.get("originalPrice")));
                } else if (productData.get("originalPrice") instanceof Number) {
                    existingProduct.setOriginalPrice(new BigDecimal(productData.get("originalPrice").toString()));
                }
            }
            
            existingProduct.setStock((Integer) productData.get("stock"));
            existingProduct.setCategory((String) productData.get("category"));
            existingProduct.setImageUrl((String) productData.get("imageUrl"));
            
            // 处理时间字段
            if (productData.get("startTime") != null && !productData.get("startTime").equals("")) {
                try {
                    String startTimeStr = productData.get("startTime").toString();
                    // 尝试解析不同格式的时间字符串
                    if (startTimeStr.contains("T")) {
                        startTimeStr = startTimeStr.replace("T", " ");
                    }
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    existingProduct.setStartTime(LocalDateTime.parse(startTimeStr, formatter));
                } catch (Exception e) {
                    // 时间格式解析失败，设置为null
                    existingProduct.setStartTime(null);
                }
            } else {
                existingProduct.setStartTime(null);
            }
            
            if (productData.get("endTime") != null && !productData.get("endTime").equals("")) {
                try {
                    String endTimeStr = productData.get("endTime").toString();
                    // 尝试解析不同格式的时间字符串
                    if (endTimeStr.contains("T")) {
                        endTimeStr = endTimeStr.replace("T", " ");
                    }
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    existingProduct.setEndTime(LocalDateTime.parse(endTimeStr, formatter));
                } catch (Exception e) {
                    // 时间格式解析失败，设置为null
                    existingProduct.setEndTime(null);
                }
            } else {
                existingProduct.setEndTime(null);
            }
            
            existingProduct.setUpdatedAt(LocalDateTime.now());
            
            // 保存商品
            Product updatedProduct = productRepository.save(existingProduct);
            
            // 更新Redis缓存
            updateProductCache(updatedProduct);
            
            result.put("code", 0);
            result.put("message", "商品更新成功");
            result.put("data", updatedProduct);
        } catch (Exception e) {
            result.put("code", -1);
            result.put("message", "商品更新失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 删除商品
    @DeleteMapping("/delete/{productId}")
    public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable Long productId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 查找商品
            Product existingProduct = productRepository.findById(productId).orElse(null);
            if (existingProduct == null) {
                result.put("code", -1);
                result.put("message", "商品不存在");
                return ResponseEntity.ok(result);
            }
            
            // 删除商品
            productRepository.delete(existingProduct);
            
            // 删除Redis缓存
            deleteProductCache(productId);
            
            result.put("code", 0);
            result.put("message", "商品删除成功");
        } catch (Exception e) {
            result.put("code", -1);
            result.put("message", "商品删除失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 批量删除商品
    @DeleteMapping("/batch-delete")
    public ResponseEntity<Map<String, Object>> batchDeleteProducts(@RequestBody List<Long> productIds) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 批量删除商品
            productRepository.deleteAllById(productIds);
            
            // 批量删除Redis缓存
            for (Long productId : productIds) {
                deleteProductCache(productId);
            }
            
            result.put("code", 0);
            result.put("message", "商品批量删除成功");
        } catch (Exception e) {
            result.put("code", -1);
            result.put("message", "商品批量删除失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 更新商品库存
    @PutMapping("/update-stock/{productId}")
    public ResponseEntity<Map<String, Object>> updateStock(@PathVariable Long productId, @RequestBody Map<String, Integer> request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Integer stock = request.get("stock");
            if (stock == null) {
                result.put("code", -1);
                result.put("message", "库存数量不能为空");
                return ResponseEntity.ok(result);
            }
            
            // 查找商品
            Product existingProduct = productRepository.findById(productId).orElse(null);
            if (existingProduct == null) {
                result.put("code", -1);
                result.put("message", "商品不存在");
                return ResponseEntity.ok(result);
            }
            
            // 更新库存
            existingProduct.setStock(stock);
            existingProduct.setUpdatedAt(LocalDateTime.now());
            Product updatedProduct = productRepository.save(existingProduct);
            
            // 更新Redis缓存
            updateProductCache(updatedProduct);
            
            result.put("code", 0);
            result.put("message", "库存更新成功");
            result.put("data", updatedProduct);
        } catch (Exception e) {
            result.put("code", -1);
            result.put("message", "库存更新失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 获取所有商品（管理端）
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getProductList() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Iterable<Product> products = productRepository.findAll();
            result.put("code", 0);
            result.put("data", products);
        } catch (Exception e) {
            result.put("code", -1);
            result.put("message", "获取商品列表失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 获取单个商品详情（管理端）
    @GetMapping("/detail/{productId}")
    public ResponseEntity<Map<String, Object>> getProductDetail(@PathVariable Long productId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Product product = productRepository.findById(productId).orElse(null);
            if (product == null) {
                result.put("code", -1);
                result.put("message", "商品不存在");
                return ResponseEntity.ok(result);
            }
            
            result.put("code", 0);
            result.put("data", product);
        } catch (Exception e) {
            result.put("code", -1);
            result.put("message", "获取商品详情失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 更新商品缓存
    private void updateProductCache(Product product) {
        String stockKey = "stock:" + product.getId();
        redisTemplate.opsForValue().set(stockKey, product.getStock());
    }
    
    // 删除商品缓存
    private void deleteProductCache(Long productId) {
        String stockKey = "stock:" + productId;
        String statusKey = "seckill:status:" + productId;
        redisTemplate.delete(stockKey);
        redisTemplate.delete(statusKey);
    }
}