-- 初始化商品数据
INSERT INTO products (id, name, description, price, original_price, stock, category, image_url, start_time, end_time, created_at, updated_at) VALUES
(1, 'iPhone 14 Pro', '全新A16芯片，超视网膜XDR显示屏，灵动岛设计', 8999.00, 9999.00, 100, '手机', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=iPhone%2014%20Pro%20smartphone%20with%20sleek%20design&image_size=landscape_16_9', NOW(), NOW() + INTERVAL 7 DAY, NOW(), NOW()),
(2, 'MacBook Pro 14英寸', 'M2 Pro芯片，Liquid Retina XDR显示屏，长效电池', 14999.00, 16999.00, 50, '电脑', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=MacBook%20Pro%2014%20inch%20laptop%20with%20sleek%20design&image_size=landscape_16_9', NOW(), NOW() + INTERVAL 7 DAY, NOW(), NOW()),
(3, 'AirPods Pro 2', '主动降噪，空间音频，自适应通透模式', 1899.00, 1999.00, 200, '耳机', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=AirPods%20Pro%202%20wireless%20earbuds&image_size=landscape_16_9', NOW(), NOW() + INTERVAL 7 DAY, NOW(), NOW()),
(4, 'Apple Watch Series 8', '全天候视网膜显示屏，先进的健康监测功能', 2999.00, 3199.00, 150, '智能手表', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=Apple%20Watch%20Series%208%20smartwatch&image_size=landscape_16_9', NOW(), NOW() + INTERVAL 7 DAY, NOW(), NOW());

-- 初始化活动数据
INSERT INTO seckill_activities (id, name, description, product_id, seckill_price, stock, start_time, end_time, status, created_at, updated_at) VALUES
(1, 'iPhone 14 Pro 限时秒杀', '限时秒杀，数量有限，先到先得！', 1, 7199.20, 50, NOW() - INTERVAL 1 HOUR, NOW() + INTERVAL 2 HOUR, 'ACTIVE', NOW(), NOW()),
(2, 'iPhone 14 Pro 明日秒杀', '明日限时秒杀，敬请期待！', 1, 6299.30, 33, NOW() + INTERVAL 3 HOUR, NOW() + INTERVAL 5 HOUR, 'UPCOMING', NOW(), NOW()),
(3, 'MacBook Pro 14英寸 限时秒杀', '限时秒杀，数量有限，先到先得！', 2, 11999.20, 25, NOW() - INTERVAL 1 HOUR, NOW() + INTERVAL 2 HOUR, 'ACTIVE', NOW(), NOW()),
(4, 'MacBook Pro 14英寸 明日秒杀', '明日限时秒杀，敬请期待！', 2, 10499.30, 16, NOW() + INTERVAL 3 HOUR, NOW() + INTERVAL 5 HOUR, 'UPCOMING', NOW(), NOW()),
(5, 'AirPods Pro 2 限时秒杀', '限时秒杀，数量有限，先到先得！', 3, 1519.20, 100, NOW() - INTERVAL 1 HOUR, NOW() + INTERVAL 2 HOUR, 'ACTIVE', NOW(), NOW()),
(6, 'AirPods Pro 2 明日秒杀', '明日限时秒杀，敬请期待！', 3, 1329.30, 66, NOW() + INTERVAL 3 HOUR, NOW() + INTERVAL 5 HOUR, 'UPCOMING', NOW(), NOW()),
(7, 'Apple Watch Series 8 限时秒杀', '限时秒杀，数量有限，先到先得！', 4, 2399.20, 75, NOW() - INTERVAL 1 HOUR, NOW() + INTERVAL 2 HOUR, 'ACTIVE', NOW(), NOW()),
(8, 'Apple Watch Series 8 明日秒杀', '明日限时秒杀，敬请期待！', 4, 2099.30, 50, NOW() + INTERVAL 3 HOUR, NOW() + INTERVAL 5 HOUR, 'UPCOMING', NOW(), NOW());
