const API_BASE = window.location.protocol + '//' + window.location.host;
console.log('API_BASE:', API_BASE);
let products = [];
let countdownIntervals = {};
let isInitialized = false;
const seckillRequests = new Map();
const TOKEN_KEY = 'auth_token';
const USER_KEY = 'user_info';
let currentUser = null;
let isFetching = false;

async function fetchProducts() {
    // 避免重复请求
    if (isFetching) {
        console.log('正在获取商品列表，跳过重复请求');
        return;
    }
    
    try {
        isFetching = true;
        console.log('开始获取商品列表...');
        
        // 获取所有活动
        const allResponse = await fetch(`${API_BASE}/api/activities`);
        const allActivities = await allResponse.json();
        
        console.log('获取到活动数量:', allActivities.length);
        console.log('活动数据:', allActivities);
        
        // 将活动转换为商品格式
        const newProducts = allActivities.map(activity => {
            // 调试日志：打印活动的时间格式
            console.log('活动:', activity.name, '时间格式:', {
                startTime: activity.startTime,
                endTime: activity.endTime,
                status: activity.status
            });
            
            // 处理时间格式（后端返回的是数组）
            const parseTimeArray = (timeArray) => {
                if (Array.isArray(timeArray) && timeArray.length >= 6) {
                    // 数组格式: [year, month, day, hour, minute, second]
                    const [year, month, day, hour, minute, second] = timeArray;
                    // 创建本地日期时间，避免时区转换问题
                    // 注意：这里使用 new Date(year, month - 1, day, hour, minute, second) 会自动转换为本地时间
                    return new Date(year, month - 1, day, hour, minute, second);
                }
                return timeArray;
            };
            
            const startTime = parseTimeArray(activity.startTime);
            const endTime = parseTimeArray(activity.endTime);
            // 使用活动 ID 作为唯一标识，确保每个活动都有唯一的 ID
            const productId = activity.id;
            // 保存商品 ID，用于秒杀请求
            const actualProductId = activity.product ? activity.product.id : null;
            
            // 直接使用后端返回的状态，不重新计算
            let status = activity.status ? activity.status.toLowerCase() : 'inactive';
            
            console.log('活动:', activity.name, '后端返回的状态:', status);
            
            return {
                id: productId,
                activityId: activity.id,
                productId: actualProductId, // 保存商品 ID，用于秒杀请求
                name: activity.product ? activity.product.name : activity.name,
                description: activity.product ? activity.product.description : activity.description,
                price: activity.seckillPrice ? Number(activity.seckillPrice) : 0,
                originalPrice: activity.product ? activity.product.originalPrice : 0,
                stock: activity.stock,
                imageUrl: activity.product ? activity.product.imageUrl : '',
                status: status,
                startTime: startTime,
                endTime: endTime
            };
        });
        
        // 过滤掉没有有效数据的活动
        const validProducts = newProducts.filter(product => product.name && product.price > 0);
        console.log('过滤后的有效商品数量:', validProducts.length);
        
        console.log('转换后的商品数据:', newProducts);
        
        if (!isInitialized) {
            products = validProducts;
            console.log('首次初始化商品列表，数量:', products.length);
            renderProducts();
            isInitialized = true;
        } else {
            console.log('更新商品列表，数量:', validProducts.length);
            updateProducts(validProducts);
        }
    } catch (error) {
        console.error('获取商品列表失败:', error);
    } finally {
        isFetching = false;
    }
}

function renderProducts() {
    const productList = document.getElementById('productList');
    productList.innerHTML = '';
    
    console.log('渲染商品列表，数量:', products.length);
    
    if (products.length === 0) {
        productList.innerHTML = `
            <div class="loading">
                <p>暂无秒杀活动</p>
            </div>
        `;
        return;
    }
    
    products.forEach(product => {
        console.log('渲染商品:', product);
        const card = createProductCard(product, product.id);
        productList.appendChild(card);
        
        if (product.status === 'active') {
            startCountdown(product.endTime, product.id, 'active');
        } else if (product.status === 'upcoming') {
            startCountdown(product.startTime, product.id, 'upcoming');
        }
    });
}

function updateProducts(newProducts) {
    console.log('开始更新商品列表，新商品数量:', newProducts.length);
    
    // 首先更新或添加商品
    newProducts.forEach(newProduct => {
        const oldProduct = products.find(p => p.id === newProduct.id);
        
        if (oldProduct) {
            const stockChanged = oldProduct.stock !== newProduct.stock;
            const statusChanged = oldProduct.status !== newProduct.status;
            
            // 检查时间值是否为日期对象
            const isOldEndTimeDate = oldProduct.endTime instanceof Date;
            const isNewEndTimeDate = newProduct.endTime instanceof Date;
            const isOldStartTimeDate = oldProduct.startTime instanceof Date;
            const isNewStartTimeDate = newProduct.startTime instanceof Date;
            
            // 只有当两边都是日期对象时才比较时间
            const endTimeChanged = isOldEndTimeDate && isNewEndTimeDate ? oldProduct.endTime.getTime() !== newProduct.endTime.getTime() : true;
            const startTimeChanged = isOldStartTimeDate && isNewStartTimeDate ? oldProduct.startTime.getTime() !== newProduct.startTime.getTime() : true;
            
            console.log(`商品 ${newProduct.name} (ID: ${newProduct.id}):`, {
                oldStatus: oldProduct.status,
                newStatus: newProduct.status,
                statusChanged: statusChanged,
                stockChanged: stockChanged
            });
            
            if (stockChanged) {
                updateStockDisplay(newProduct.id, newProduct.stock);
            }
            
            if (statusChanged) {
                updateStatusDisplay(newProduct.id, newProduct.status);
            }
            
            if (endTimeChanged || statusChanged) {
                console.log('状态或结束时间变化，重新开始倒计时:', {
                    newStatus: newProduct.status,
                    endTime: newProduct.endTime,
                    startTime: newProduct.startTime,
                    productId: newProduct.id
                });
                
                if (newProduct.status === 'active') {
                    startCountdown(newProduct.endTime, newProduct.id, 'active');
                } else if (newProduct.status === 'upcoming') {
                    startCountdown(newProduct.startTime, newProduct.id, 'upcoming');
                } else if (countdownIntervals[newProduct.id]) {
                    clearInterval(countdownIntervals[newProduct.id]);
                }
            }
            
            oldProduct.stock = newProduct.stock;
            oldProduct.status = newProduct.status;
            oldProduct.endTime = newProduct.endTime;
            oldProduct.startTime = newProduct.startTime;
        } else {
            console.log('新商品:', newProduct.name, '添加到列表');
            products.push(newProduct);
            const card = createProductCard(newProduct, newProduct.id);
            document.getElementById('productList').appendChild(card);
            
            if (newProduct.status === 'active') {
                startCountdown(newProduct.endTime, newProduct.id, 'active');
            } else if (newProduct.status === 'upcoming') {
                startCountdown(newProduct.startTime, newProduct.id, 'upcoming');
            }
        }
    });
    
    // 移除不再存在的商品
    const newProductIds = newProducts.map(p => p.id);
    const productsToRemove = products.filter(p => !newProductIds.includes(p.id));
    
    productsToRemove.forEach(product => {
        console.log('移除商品:', product.name);
        const card = document.getElementById(`product-card-${product.id}`);
        if (card) {
            card.remove();
        }
        if (countdownIntervals[product.id]) {
            clearInterval(countdownIntervals[product.id]);
        }
    });
    
    products = products.filter(p => newProductIds.includes(p.id));
}

function createProductCard(product, productId) {
    const card = document.createElement('div');
    card.className = 'product-card';
    card.id = `product-card-${productId}`;
    
    const stock = product.stock || 0;
    const originalPrice = product.originalPrice || 0;
    const price = product.price || 0;
    
    let statusClass = 'inactive';
    let statusText = '未开始';
    let countdownLabel = '距离秒杀结束还有';
    let showCountdown = false;
    
    switch (product.status) {
        case 'active':
            statusClass = 'active';
            statusText = '进行中';
            countdownLabel = '距离秒杀结束还剩';
            showCountdown = true;
            break;
        case 'upcoming':
            statusClass = 'upcoming';
            statusText = '即将开始';
            countdownLabel = '距离秒杀开始还有';
            showCountdown = true;
            break;
        case 'ended':
            statusClass = 'ended';
            statusText = '已结束';
            countdownLabel = '活动已结束';
            showCountdown = false;
            break;
        default:
            statusClass = 'inactive';
            statusText = '未开始';
            countdownLabel = '活动未开始';
            showCountdown = false;
    }
    
    card.innerHTML = `
        <div class="product-image">
            <div class="badge">限时秒杀</div>
            <div class="status-badge ${statusClass}" id="statusBadge-${productId}">
                ${statusText}
            </div>
            ${product.imageUrl ? `<img src="${product.imageUrl}" alt="${product.name}" onerror="this.style.display='none'">` : ''}
        </div>
        
        <div class="product-info">
            <h2 class="product-title">${product.name}</h2>
            <p class="product-desc">${product.description || '精选好物，限时抢购'}</p>
            
            <div class="price-section">
                <div class="price-row">
                    <span class="price-label">秒杀价</span>
                    <span class="price-value">¥${formatPrice(price)}</span>
                </div>
                ${originalPrice > 0 ? `
                <div class="price-row">
                    <span class="price-label">原价</span>
                    <span class="original-price">¥${formatPrice(originalPrice)}</span>
                </div>
                ` : ''}
            </div>
            
            <div class="stock-section">
                <div class="stock-bar">
                    <div class="stock-fill" id="stockFill-${productId}" style="width: ${stock / 100 * 100}%"></div>
                </div>
                <div class="stock-info">
                    <span>剩余库存：<strong id="stockCount-${productId}">${stock}</strong>件</span>
                    <span class="stock-percent" id="stockPercent-${productId}">${stock}%</span>
                </div>
            </div>
            
            ${showCountdown ? `
            <div class="countdown-section">
                <div class="countdown-label">${countdownLabel}</div>
                <div class="countdown-timer">
                    <div class="time-block">
                        <span class="time-value" id="hours-${productId}">00</span>
                        <span class="time-label">时</span>
                    </div>
                    <div class="time-separator">:</div>
                    <div class="time-block">
                        <span class="time-value" id="minutes-${productId}">00</span>
                        <span class="time-label">分</span>
                    </div>
                    <div class="time-separator">:</div>
                    <div class="time-block">
                        <span class="time-value" id="seconds-${productId}">00</span>
                        <span class="time-label">秒</span>
                    </div>
                </div>
            </div>
            ` : ''}
            
            <div class="action-section">
                <button class="seckill-btn" id="seckillBtn-${productId}" 
                          ${product.status !== 'active' ? 'disabled' : ''} 
                          onclick="seckill(${product.id}, ${productId})">
                    <span class="btn-icon">⚡</span>
                    <span class="btn-text">${product.status === 'active' ? '立即秒杀' : '活动未开始'}</span>
                </button>
                <div class="btn-tips">每人限购1件 · 数量有限先到先得</div>
            </div>
        </div>
    `;
    
    return card;
}

function formatPrice(price) {
    return Number(price).toLocaleString('zh-CN', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}

function startCountdown(endTime, productId, status) {
    console.log('startCountdown called:', { endTime, productId, status, type: typeof endTime });
    
    if (countdownIntervals[productId]) {
        clearInterval(countdownIntervals[productId]);
    }
    
    if (!endTime) {
        console.warn('商品 ' + productId + ' 没有设置结束时间');
        return;
    }
    
    // 尝试解析时间
    let endTimeDate;
    if (typeof endTime === 'string') {
        endTimeDate = new Date(endTime.replace(' ', 'T'));
    } else {
        endTimeDate = new Date(endTime);
    }
    
    console.log('endTimeDate:', endTimeDate, 'isNaN:', isNaN(endTimeDate.getTime()));
    
    if (isNaN(endTimeDate.getTime())) {
        console.error('无效的结束时间:', endTime);
        return;
    }
    
    // 更新倒计时显示
    const updateCountdown = () => {
        const now = new Date();
        // 转换为本地时间进行比较
        const nowLocal = new Date(now.getFullYear(), now.getMonth(), now.getDate(), now.getHours(), now.getMinutes(), now.getSeconds());
        const endTimeLocal = new Date(endTimeDate.getFullYear(), endTimeDate.getMonth(), endTimeDate.getDate(), endTimeDate.getHours(), endTimeDate.getMinutes(), endTimeDate.getSeconds());
        const diff = endTimeLocal - nowLocal;
        
        console.log('倒计时更新:', { productId, diff, nowLocal, endTimeLocal });
        
        if (diff <= 0) {
            const hoursEl = document.getElementById(`hours-${productId}`);
            const minutesEl = document.getElementById(`minutes-${productId}`);
            const secondsEl = document.getElementById(`seconds-${productId}`);
            
            if (hoursEl) hoursEl.textContent = '00';
            if (minutesEl) minutesEl.textContent = '00';
            if (secondsEl) secondsEl.textContent = '00';
            
            clearInterval(countdownIntervals[productId]);
            
            // 倒计时结束后，重新获取活动状态
            fetchProducts();
            return;
        }
        
        const hours = Math.floor(diff / (1000 * 60 * 60));
        const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
        const seconds = Math.floor((diff % (1000 * 60)) / 1000);
        
        const hoursEl = document.getElementById(`hours-${productId}`);
        const minutesEl = document.getElementById(`minutes-${productId}`);
        const secondsEl = document.getElementById(`seconds-${productId}`);
        
        if (hoursEl) hoursEl.textContent = String(hours).padStart(2, '0');
        if (minutesEl) minutesEl.textContent = String(minutes).padStart(2, '0');
        if (secondsEl) secondsEl.textContent = String(seconds).padStart(2, '0');
    };
    
    // 立即更新一次
    updateCountdown();
    
    // 然后每秒更新一次
    countdownIntervals[productId] = setInterval(updateCountdown, 1000);
}

function updateStockDisplay(productId, stock) {
    console.log('updateStockDisplay called:', { productId: productId, stock: stock });
    
    // 检查stock参数是否为undefined或null，如果是，则设置默认值
    const stockValue = stock || 0;
    
    const stockCount = document.getElementById(`stockCount-${productId}`);
    const stockFill = document.getElementById(`stockFill-${productId}`);
    const stockPercent = document.getElementById(`stockPercent-${productId}`);
    
    console.log('DOM elements found:', {
        stockCount: !!stockCount,
        stockFill: !!stockFill,
        stockPercent: !!stockPercent
    });
    
    if (stockCount) {
        stockCount.textContent = stockValue;
        console.log('Updated stockCount:', stockCount.textContent);
    }
    if (stockFill) {
        stockFill.style.width = stockValue + '%';
        console.log('Updated stockFill width:', stockFill.style.width);
    }
    if (stockPercent) {
        stockPercent.textContent = stockValue + '%';
        console.log('Updated stockPercent:', stockPercent.textContent);
    }
    
    // 只有当stockFill和stockPercent存在时才更新样式
    if (stockFill && stockPercent) {
        if (stockValue <= 10) {
            stockFill.style.background = 'linear-gradient(90deg, #ef4444 0%, #dc2626 100%)';
            stockPercent.style.color = '#ef4444';
        } else if (stockValue <= 30) {
            stockFill.style.background = 'linear-gradient(90deg, #f59e0b 0%, #d97706 100%)';
            stockPercent.style.color = '#f59e0b';
        } else {
            stockFill.style.background = 'linear-gradient(90deg, #10b981 0%, #059669 100%)';
            stockPercent.style.color = '#10b981';
        }
    }
}

function updateStatusDisplay(productId, status) {
    console.log('updateStatusDisplay called:', { productId, status });
    
    const statusBadge = document.getElementById(`statusBadge-${productId}`);
    const seckillBtn = document.getElementById(`seckillBtn-${productId}`);
    const productCard = document.getElementById(`product-card-${productId}`);
    
    let badgeClass = 'inactive';
    let badgeText = '未开始';
    let btnDisabled = true;
    let btnText = '活动未开始';
    let countdownLabel = '活动未开始';
    let showCountdown = false;
    
    switch (status) {
        case 'active':
            badgeClass = 'active';
            badgeText = '进行中';
            btnDisabled = false;
            btnText = '立即秒杀';
            countdownLabel = '距离秒杀结束还剩';
            showCountdown = true;
            break;
        case 'upcoming':
            badgeClass = 'upcoming';
            badgeText = '即将开始';
            btnDisabled = true;
            btnText = '活动未开始';
            countdownLabel = '距离秒杀开始还有';
            showCountdown = true;
            break;
        case 'ended':
            badgeClass = 'ended';
            badgeText = '已结束';
            btnDisabled = true;
            btnText = '活动已结束';
            countdownLabel = '活动已结束';
            showCountdown = false;
            break;
        default:
            badgeClass = 'inactive';
            badgeText = '未开始';
            btnDisabled = true;
            btnText = '活动未开始';
            countdownLabel = '活动未开始';
            showCountdown = false;
    }
    
    if (statusBadge) {
        statusBadge.className = `status-badge ${badgeClass}`;
        statusBadge.textContent = badgeText;
    }
    
    if (seckillBtn) {
        seckillBtn.disabled = btnDisabled;
        seckillBtn.innerHTML = `<span class="btn-icon">⚡</span><span class="btn-text">${btnText}</span>`;
    }
    
    // 启动或停止倒计时
    if (showCountdown) {
        const product = products.find(p => p.id === productId);
        if (product) {
            if (status === 'active' && product.endTime) {
                startCountdown(product.endTime, productId, 'active');
            } else if (status === 'upcoming' && product.startTime) {
                startCountdown(product.startTime, productId, 'upcoming');
            }
        }
    } else if (countdownIntervals[productId]) {
        clearInterval(countdownIntervals[productId]);
    }
    
    if (productCard) {
        const countdownSection = productCard.querySelector('.countdown-section');
        const countdownLabelEl = productCard.querySelector('.countdown-label');
        
        if (showCountdown) {
            if (!countdownSection) {
                const actionSection = productCard.querySelector('.action-section');
                const newCountdownSection = document.createElement('div');
                newCountdownSection.className = 'countdown-section';
                newCountdownSection.innerHTML = `
                    <div class="countdown-label">${countdownLabel}</div>
                    <div class="countdown-timer">
                        <div class="time-block">
                            <span class="time-value" id="hours-${productId}">00</span>
                            <span class="time-label">时</span>
                        </div>
                        <div class="time-separator">:</div>
                        <div class="time-block">
                            <span class="time-value" id="minutes-${productId}">00</span>
                            <span class="time-label">分</span>
                        </div>
                        <div class="time-separator">:</div>
                        <div class="time-block">
                            <span class="time-value" id="seconds-${productId}">00</span>
                            <span class="time-label">秒</span>
                        </div>
                    </div>
                `;
                actionSection.parentNode.insertBefore(newCountdownSection, actionSection);
            } else if (countdownLabelEl) {
                countdownLabelEl.textContent = countdownLabel;
            }
        } else {
            if (countdownSection) {
                countdownSection.remove();
            }
        }
    }
}

async function seckill(productId, index) {
    const btn = document.getElementById(`seckillBtn-${index}`);
    const product = products.find(p => p.id === productId);
    
    if (!product || product.status !== 'active') {
        return;
    }
    
    const token = localStorage.getItem(TOKEN_KEY);
    const userInfo = localStorage.getItem(USER_KEY);
    
    console.log('秒杀请求 - 检查登录状态:', { hasToken: !!token, hasUserInfo: !!userInfo });
    
    if (!token || !userInfo) {
        currentUser = null;
        updateAuthUI(false);
        showModal('error', '请先登录', '请登录后再参与秒杀活动');
        setTimeout(() => {
            window.location.href = '/login.html';
        }, 1500);
        return;
    }
    
    try {
        currentUser = JSON.parse(userInfo);
        updateAuthUI(true);
    } catch (error) {
        console.error('Parse user info error:', error);
        currentUser = null;
        updateAuthUI(false);
        showModal('error', '请先登录', '请登录后再参与秒杀活动');
        setTimeout(() => {
            window.location.href = '/login.html';
        }, 1500);
        return;
    }
    
    if (product.stock <= 0) {
        showModal('error', '库存不足', '很遗憾，商品已售罄！');
        return;
    }
    
    const requestKey = `${productId}-${index}`;
    if (seckillRequests.has(requestKey)) {
        showModal('error', '请勿重复点击', '您的请求正在处理中，请稍候...');
        return;
    }
    
    seckillRequests.set(requestKey, true);
    
    const requestId = Date.now() + Math.random().toString(36).substr(2, 9);
    
    btn.disabled = true;
    btn.innerHTML = '<span class="btn-icon">⏳</span><span class="btn-text">抢购中...</span>';
    
    try {
        const headers = {
            'Content-Type': 'application/json',
            'X-Real-IP': '127.0.0.1'
        };
        
        if (token) {
            headers['Authorization'] = token;
        }
        
        // 使用商品 ID 而不是活动 ID 来发送秒杀请求
        const actualProductId = product.productId;
        if (!actualProductId) {
            showModal('error', '商品不存在', '该秒杀活动关联的商品不存在');
            return;
        }
        
        console.log('发送秒杀请求:', { productId: actualProductId, requestId: requestId });
        
        // 设置请求超时时间为5秒
        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), 5000);
        
        const response = await fetch(`${API_BASE}/api/seckill/${actualProductId}?requestId=${requestId}`, {
            method: 'POST',
            headers: headers,
            signal: controller.signal
        });
        
        clearTimeout(timeoutId);
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const result = await response.json();
        
        console.log('秒杀响应:', result);
        
        if (result.success || result.code === 0) {
            // 更新商品对象的库存
            product.stock = (product.stock || 0) - 1;
            console.log('秒杀成功，更新库存:', { productId: productId, newStock: product.stock });
            
            // 立即更新页面显示的库存
            updateStockDisplay(productId, product.stock);
            
            // 显示成功模态框
            showModal('success', '秒杀成功', `恭喜您成功抢购到${product.name}！`, result.orderId);
        } else {
            const message = result.message || '秒杀失败，请重试';
            console.log('秒杀失败:', { code: result.code, message: message });
            
            let title = '秒杀失败';
            if (result.code === 2) {
                title = '库存不足';
                product.stock = 0;
                console.log('库存不足，更新库存为0:', { productId: productId });
                updateStockDisplay(productId, 0);
            } else if (result.code === 3) {
                title = '已参与过';
            } else if (result.code === -1) {
                title = '操作失败';
            }
            
            showModal('error', title, message);
        }
    } catch (error) {
        console.error('秒杀请求失败:', error);
        // 网络错误后，尝试检查是否已经秒杀成功
        checkSeckillStatus(product.productId, productId);
    } finally {
        seckillRequests.delete(requestKey);
        btn.disabled = false;
        btn.innerHTML = '<span class="btn-icon">⚡</span><span class="btn-text">立即秒杀</span>';
    }
}

// 检查秒杀状态
async function checkSeckillStatus(actualProductId, productId) {
    const token = localStorage.getItem(TOKEN_KEY);
    if (!token) return;
    
    try {
        // 发送状态检查请求
        const response = await fetch(`${API_BASE}/api/orders/check-seckill?productId=${actualProductId}`, {
            headers: {
                'Authorization': token
            }
        });
        
        const result = await response.json();
        console.log('秒杀状态检查结果:', result);
        
        if (result.success) {
            // 秒杀成功，更新库存并显示成功提示
            const product = products.find(p => p.id === productId);
            if (product) {
                product.stock = (product.stock || 0) - 1;
                updateStockDisplay(productId, product.stock);
                showModal('success', '秒杀成功', `恭喜您成功抢购到${product.name}！`, result.orderId);
            }
        } else {
            // 确实秒杀失败，显示网络错误提示
            showModal('error', '网络错误', '请求失败，请检查网络连接后重试');
        }
    } catch (error) {
        console.error('检查秒杀状态失败:', error);
        showModal('error', '网络错误', '请求失败，请检查网络连接后重试');
    }
}

function showModal(type, title, message, orderId = null) {
    const modal = document.getElementById('resultModal');
    const icon = document.getElementById('modalIcon');
    const titleEl = document.getElementById('modalTitle');
    const messageEl = document.getElementById('modalMessage');
    const orderInfo = document.getElementById('orderInfo');
    const orderIdEl = document.getElementById('orderId');
    
    if (type === 'success') {
        icon.textContent = '✅';
        icon.style.animation = 'pulse 0.5s ease-in-out';
    } else {
        icon.textContent = '❌';
        icon.style.animation = 'shake 0.5s ease-in-out';
    }
    
    titleEl.textContent = title;
    messageEl.textContent = message;
    
    if (orderId) {
        orderInfo.style.display = 'block';
        orderIdEl.textContent = orderId;
    } else {
        orderInfo.style.display = 'none';
    }
    
    modal.style.display = 'flex';
}

function closeModal() {
    const modal = document.getElementById('resultModal');
    modal.style.display = 'none';
}

document.addEventListener('DOMContentLoaded', () => {
    checkAuth();
    fetchProducts();
    setInterval(fetchProducts, 5000);
});

function checkAuth() {
    const token = localStorage.getItem(TOKEN_KEY);
    const userInfo = localStorage.getItem(USER_KEY);
    
    if (token && userInfo) {
        try{
            currentUser = JSON.parse(userInfo);
            updateAuthUI(true);
            
            fetch(`${API_BASE}/api/auth/check`, {
                method: 'GET',
                headers: {
                    'Authorization': 'Bearer ' + token
                }
            })
            .then(response => response.json())
            .then(data => {
                if (data.code === 0 && data.authenticated && data.user) {
                    // 更新localStorage中的用户信息
                    localStorage.setItem(USER_KEY, JSON.stringify(data.user));
                    currentUser = data.user;
                    updateAuthUI(true);
                } else {
                    logout();
                }
            })
            .catch(error => {
                console.error('Auth check error:', error);
                logout();
            });
        } catch (error) {
            console.error('Parse user info error:', error);
            logout();
        }
    } else {
        currentUser = null;
        updateAuthUI(false);
    }
}

function updateAuthUI(isLoggedIn) {
    const userInfo = document.getElementById('userInfo');
    const loginNotice = document.getElementById('loginNotice');
    const userName = document.getElementById('userName');
    
    if (isLoggedIn && currentUser) {
        userInfo.style.display = 'flex';
        loginNotice.style.display = 'none';
        userName.textContent = currentUser.nickname || currentUser.username;
    } else {
        userInfo.style.display = 'none';
        loginNotice.style.display = 'block';
    }
}

function logout() {
    const token = localStorage.getItem(TOKEN_KEY);
    
    if (token) {
        fetch(`${API_BASE}/api/auth/logout`, {
            method: 'POST',
            headers: {
                'Authorization': 'Bearer ' + token
            }
        }).catch(error => {
            console.error('Logout error:', error);
        });
    }
    
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    currentUser = null;
    
    updateAuthUI(false);
    window.location.href = '/login.html';
}

document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
        closeModal();
    }
});

document.getElementById('resultModal').addEventListener('click', (e) => {
    if (e.target.id === 'resultModal') {
        closeModal();
    }
});