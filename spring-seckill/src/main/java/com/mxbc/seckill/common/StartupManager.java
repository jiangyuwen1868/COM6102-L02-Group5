package com.mxbc.seckill.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.mxbc.seckill.entity.Product;
import com.mxbc.seckill.entity.SeckillActivity;
import com.mxbc.seckill.entity.SeckillActivity.ActivityStatus;
import com.mxbc.seckill.entity.User;
import com.mxbc.seckill.entity.repository.ProductRepository;
import com.mxbc.seckill.entity.repository.SeckillActivityRepository;
import com.mxbc.seckill.entity.repository.UserRepository;
import com.mxbc.seckill.util.SM3Util;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Component
@Order(100)
public class StartupManager implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(StartupManager.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SeckillActivityRepository activityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    private volatile boolean initialized = false;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (initialized) {
            return;
        }

        logger.info("========================================");
        logger.info("开始执行系统启动初始化...");
        logger.info("========================================");

        try {
            // 阶段1: 初始化基础数据
            initBaseData();

            // 阶段2: 预热缓存（带降级）
            warmUpCache();

            initialized = true;
            logger.info("========================================");
            logger.info("系统启动初始化完成");
            logger.info("========================================");
        } catch (Exception e) {
            logger.error("系统启动初始化失败", e);
            // 启动失败不阻断，记录错误继续运行
        }
    }

    private void initBaseData() {
        logger.info("[阶段1/2] 初始化基础数据...");

        try {
            // 初始化用户
            initUsersIfNeeded();

            // 初始化商品
            initProductsIfNeeded();

            // 初始化活动
            initActivitiesIfNeeded();

            logger.info("[阶段1/2] 基础数据初始化完成");
        } catch (Exception e) {
            logger.error("[阶段1/2] 基础数据初始化失败", e);
            throw e;
        }
    }

    private void initUsersIfNeeded() {
        try {
            long count = userRepository.count();
            if (count > 0) {
                logger.info("用户数据已存在({}条)，跳过初始化", count);
                return;
            }

            logger.info("开始初始化用户数据...");

            // 创建默认管理员用户
            createUser("admin", "Admin123!@#", "admin@example.com", "13800138000", "管理员");

            // 创建测试用户
            createUser("test", "Test123!@#", "test@example.com", "13900139000", "测试用户");

            logger.info("用户数据初始化完成");
        } catch (Exception e) {
            logger.error("初始化用户数据失败", e);
            throw e;
        }
    }

    private void createUser(String username, String password, String email, String phone, String nickname) {
        String salt = SM3Util.generateSalt();
        String hashedPassword = SM3Util.hashWithSalt(password, salt);

        User user = new User();
        user.setUsername(username);
        user.setPassword(hashedPassword);
        user.setSalt(salt);
        user.setEmail(email);
        user.setPhone(phone);
        user.setNickname(nickname);
        user.setAge(18);
        user.setCreateTime(LocalDateTime.now());
        user.setStatus(1);

        userRepository.save(user);
        logger.info("创建用户: {} (ID: {})", username, user.getId());
    }

    private void initProductsIfNeeded() {
        try {
            long count = productRepository.count();
            if (count > 0) {
                logger.info("商品数据已存在({}条)，跳过初始化", count);
                return;
            }

            logger.info("开始初始化商品数据...");

            createProduct("iPhone 14 Pro",
                    "全新A16芯片，超视网膜XDR显示屏，灵动岛设计",
                    new BigDecimal("8999"),
                    new BigDecimal("9999"),
                    100,
                    "手机");

            createProduct("MacBook Pro 14英寸",
                    "M2 Pro芯片，Liquid Retina XDR显示屏，长效电池",
                    new BigDecimal("14999"),
                    new BigDecimal("16999"),
                    50,
                    "电脑");

            createProduct("AirPods Pro 2",
                    "主动降噪，空间音频，自适应通透模式",
                    new BigDecimal("1899"),
                    new BigDecimal("1999"),
                    200,
                    "耳机");

            createProduct("Apple Watch Series 8",
                    "全天候视网膜显示屏，先进的健康监测功能",
                    new BigDecimal("2999"),
                    new BigDecimal("3199"),
                    150,
                    "智能手表");

            logger.info("商品数据初始化完成");
        } catch (Exception e) {
            logger.error("初始化商品数据失败", e);
            throw e;
        }
    }

    private void createProduct(String name, String description, BigDecimal price,
                               BigDecimal originalPrice, int stock, String category) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setOriginalPrice(originalPrice);
        product.setStock(stock);
        product.setCategory(category);
        product.setStartTime(LocalDateTime.now());
        product.setEndTime(LocalDateTime.now().plusDays(7));
        productRepository.save(product);
        logger.info("创建商品: {} (ID: {})", name, product.getId());
    }

    private void initActivitiesIfNeeded() {
        try {
            long count = activityRepository.count();
            if (count > 0) {
                logger.info("活动数据已存在({}条)，跳过初始化", count);
                return;
            }

            logger.info("开始初始化活动数据...");

            Iterable<Product> products = productRepository.findAll();
            int productCount = 0;
            for (Product product : products) {
                productCount++;
                // 创建进行中的活动
                createActivity(product.getName() + " 限时秒杀",
                        "限时秒杀，数量有限，先到先得！",
                        product,
                        product.getPrice().multiply(new BigDecimal("0.8")),
                        product.getStock() / 2,
                        LocalDateTime.now().minusHours(1),
                        LocalDateTime.now().plusHours(2),
                        ActivityStatus.ACTIVE);

                // 创建即将开始的活动
                createActivity(product.getName() + " 明日秒杀",
                        "明日限时秒杀，敬请期待！",
                        product,
                        product.getPrice().multiply(new BigDecimal("0.7")),
                        product.getStock() / 3,
                        LocalDateTime.now().plusHours(3),
                        LocalDateTime.now().plusHours(5),
                        ActivityStatus.UPCOMING);
            }

            logger.info("活动数据初始化完成，为 {} 个商品创建了活动", productCount);
        } catch (Exception e) {
            logger.error("初始化活动数据失败", e);
            throw e;
        }
    }

    private void createActivity(String name, String description, Product product,
                                BigDecimal seckillPrice, int stock,
                                LocalDateTime startTime, LocalDateTime endTime,
                                ActivityStatus status) {
        SeckillActivity activity = new SeckillActivity();
        activity.setName(name);
        activity.setDescription(description);
        activity.setProduct(product);
        activity.setSeckillPrice(seckillPrice);
        activity.setStock(stock);
        activity.setStartTime(startTime);
        activity.setEndTime(endTime);
        activity.setStatus(status);
        activityRepository.save(activity);
        logger.info("创建活动: {} (ID: {}, 状态: {})", name, activity.getId(), status);
    }

    private void warmUpCache() {
        logger.info("[阶段2/2] 预热缓存...");

        if (stringRedisTemplate == null) {
            logger.warn("Redis未配置，跳过缓存预热");
            return;
        }

        try {
            // 测试Redis连接
            stringRedisTemplate.opsForValue().set("system:startup:test", "ok", 10, TimeUnit.SECONDS);
            String testValue = stringRedisTemplate.opsForValue().get("system:startup:test");

            if (!"ok".equals(testValue)) {
                logger.warn("Redis连接测试失败，跳过缓存预热");
                return;
            }

            // 预热库存
            Iterable<Product> products = productRepository.findAll();
            int count = 0;
            for (Product product : products) {
                if (product.getStock() != null && product.getStock() > 0) {
                    String stockKey = "stock:" + product.getId();
                    stringRedisTemplate.opsForValue().set(stockKey, String.valueOf(product.getStock()));
                    count++;
                }
            }

            logger.info("[阶段2/2] 缓存预热完成，预热了 {} 个商品库存", count);
        } catch (Exception e) {
            logger.error("[阶段2/2] 缓存预热失败，系统将继续运行", e);
            // 缓存预热失败不阻断启动
        }
    }
}
