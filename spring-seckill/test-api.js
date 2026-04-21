// 测试API响应格式的脚本
const API_BASE = window.location.protocol + '//' + window.location.host + '/api';

// 测试登录API
async function testLogin() {
    try {
        const response = await fetch(`${API_BASE}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ 
                username: 'test', 
                password: '123456',
                captchaId: 'test',
                captchaCode: '1234' 
            })
        });
        
        const data = await response.json();
        console.log('登录API响应:', data);
        
        if (data.code === 0) {
            console.log('登录成功，token:', data.token);
            // 测试用户信息API
            testUserInfo(data.token);
        } else {
            console.log('登录失败:', data.message);
        }
    } catch (error) {
        console.error('测试登录API失败:', error);
    }
}

// 测试用户信息API
async function testUserInfo(token) {
    try {
        const response = await fetch(`${API_BASE}/user/info`, {
            headers: {
                'Authorization': 'Bearer ' + token
            }
        });
        
        const data = await response.json();
        console.log('用户信息API响应:', data);
    } catch (error) {
        console.error('测试用户信息API失败:', error);
    }
}

// 运行测试
testLogin();