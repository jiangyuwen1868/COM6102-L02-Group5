const API_BASE_URL = window.location.protocol + '//' + window.location.host + '/api/auth';
const TOKEN_KEY = 'auth_token';
const USER_KEY = 'user_info';

let currentCaptchaId = null;

document.addEventListener('DOMContentLoaded', function() {
    const tabs = document.querySelectorAll('.tab');
    const forms = document.querySelectorAll('.form');
    const loginForm = document.getElementById('loginForm');
    const registerForm = document.getElementById('registerForm');
    const messageBox = document.getElementById('messageBox');
    
    refreshCaptcha();
    
    tabs.forEach(tab => {
        tab.addEventListener('click', function() {
            const tabName = this.getAttribute('data-tab');
            
            tabs.forEach(t => t.classList.remove('active'));
            this.classList.add('active');
            
            forms.forEach(form => {
                form.classList.remove('active');
                if (form.id === tabName + 'Form') {
                    form.classList.add('active');
                }
            });
            
            hideMessage();
        });
    });
    
    loginForm.addEventListener('submit', async function(e) {
        e.preventDefault();
        
        const username = document.getElementById('loginUsername').value.trim();
        const password = document.getElementById('loginPassword').value;
        const captchaCode = document.getElementById('captchaCode').value.trim();
        const rememberMe = document.getElementById('rememberMe').checked;
        
        if (!username || !password) {
            showMessage('请输入用户名和密码', 'error');
            shakeForm(loginForm);
            return;
        }
        
        if (!captchaCode) {
            showMessage('请输入验证码', 'error');
            shakeForm(loginForm);
            return;
        }
        
        const submitBtn = loginForm.querySelector('button[type="submit"]');
        submitBtn.disabled = true;
        submitBtn.textContent = '登录中...';
        
        try {
            const response = await fetch(`${API_BASE_URL}/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ 
                    username, 
                    password,
                    captchaId: currentCaptchaId,
                    captchaCode 
                })
            });
            
            const data = await response.json();
            
            if (data.code === 0) {
                localStorage.setItem(TOKEN_KEY, data.token);
                localStorage.setItem(USER_KEY, JSON.stringify(data.user));
                
                if (rememberMe) {
                    localStorage.setItem('remember_me', 'true');
                } else {
                    localStorage.removeItem('remember_me');
                }
                
                showMessage('登录成功，正在跳转...', 'success');
                
                setTimeout(() => {
                    // 尝试获取之前的页面URL，如果没有则跳转到index.html
                    const previousUrl = localStorage.getItem('previous_url');
                    if (previousUrl) {
                        localStorage.removeItem('previous_url');
                        window.location.href = previousUrl;
                    } else {
                        window.location.href = '/index.html';
                    }
                }, 1000);
            } else {
                showMessage(data.message || '登录失败', 'error');
                shakeForm(loginForm);
            }
        } catch (error) {
            console.error('Login error:', error);
            showMessage('网络错误，请稍后重试', 'error');
            shakeForm(loginForm);
        } finally {
            submitBtn.disabled = false;
            submitBtn.textContent = '登录';
        }
    });
    
    registerForm.addEventListener('submit', async function(e) {
        e.preventDefault();
        
        const username = document.getElementById('regUsername').value.trim();
        const password = document.getElementById('regPassword').value;
        const confirmPassword = document.getElementById('regConfirmPassword').value;
        const email = document.getElementById('regEmail').value.trim();
        const phone = document.getElementById('regPhone').value.trim();
        const nickname = document.getElementById('regNickname').value.trim();
        
        if (!username) {
            showMessage('请输入用户名', 'error');
            shakeForm(registerForm);
            return;
        }
        
        if (!password || password.length < 12) {
            showMessage('密码至少需要12位', 'error');
            shakeForm(registerForm);
            return;
        }
        
        if (!isPasswordStrong(password)) {
            showMessage('密码必须包含大小写字母、数字和特殊符号', 'error');
            shakeForm(registerForm);
            return;
        }
        
        if (password !== confirmPassword) {
            showMessage('两次输入的密码不一致', 'error');
            shakeForm(registerForm);
            return;
        }
        
        const submitBtn = registerForm.querySelector('button[type="submit"]');
        submitBtn.disabled = true;
        submitBtn.textContent = '注册中...';
        
        try {
            const response = await fetch(`${API_BASE_URL}/register`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ 
                    username, 
                    password, 
                    email: email || null, 
                    phone: phone || null, 
                    nickname: nickname || username 
                })
            });
            
            const data = await response.json();
            
            if (data.code === 0) {
                showMessage('注册成功，请登录', 'success');
                
                setTimeout(() => {
                    tabs[0].click();
                    document.getElementById('loginUsername').value = username;
                    document.getElementById('regUsername').value = '';
                    document.getElementById('regPassword').value = '';
                    document.getElementById('regConfirmPassword').value = '';
                    document.getElementById('regEmail').value = '';
                    document.getElementById('regPhone').value = '';
                    document.getElementById('regNickname').value = '';
                }, 1500);
            } else {
                showMessage(data.message || '注册失败', 'error');
                shakeForm(registerForm);
            }
        } catch (error) {
            console.error('Register error:', error);
            showMessage('网络错误，请稍后重试', 'error');
            shakeForm(registerForm);
        } finally {
            submitBtn.disabled = false;
            submitBtn.textContent = '注册';
        }
    });
    
    function showMessage(message, type) {
        messageBox.textContent = message;
        messageBox.className = 'message-box ' + type + ' show';
    }
    
    function hideMessage() {
        messageBox.className = 'message-box';
        messageBox.textContent = '';
    }
    
    function shakeForm(form) {
        form.classList.add('shake');
        setTimeout(() => {
            form.classList.remove('shake');
        }, 500);
    }
    
    checkAuth();
});

function checkAuth() {
    const token = localStorage.getItem('auth_token');
    
    if (token) {
                fetch(API_BASE_URL + '/check', {
                    method: 'GET',
                    headers: {
                        'Authorization': 'Bearer ' + token
                    }
                })
        .then(response => response.json())
        .then(data => {
            if (data.code === 0 && data.authenticated) {
                // 尝试获取之前的页面URL，如果没有则跳转到index.html
                const previousUrl = localStorage.getItem('previous_url');
                if (previousUrl) {
                    localStorage.removeItem('previous_url');
                    window.location.href = previousUrl;
                } else {
                    window.location.href = '/index.html';
                }
            }
        })
        .catch(error => {
            console.error('Auth check error:', error);
        });
    }
}

async function refreshCaptcha() {
    try {
        const response = await fetch(`${API_BASE_URL}/captcha`);
        const data = await response.json();
        
        console.log('验证码响应:', data);
        
        if (data.code === 0) {
            currentCaptchaId = data.captchaId;
            const captchaImage = document.getElementById('captchaImage');
            captchaImage.src = data.image;
            console.log('验证码图片src设置成功:', data.image.substring(0, 50) + '...');
        } else {
            showMessage('获取验证码失败', 'error');
        }
    } catch (error) {
        console.error('Get captcha error:', error);
        showMessage('获取验证码失败，请刷新页面', 'error');
    }
}

function isPasswordStrong(password) {
    if (password.length < 12) {
        return false;
    }
    
    const hasUpperCase = /[A-Z]/.test(password);
    const hasLowerCase = /[a-z]/.test(password);
    const hasDigit = /[0-9]/.test(password);
    const hasSpecial = /[!@#$%^&*()_+\-=\[\]{};':",./<>?\\|`~]/.test(password);
    
    return hasUpperCase && hasLowerCase && hasDigit && hasSpecial;
}

function checkPasswordStrength() {
    const password = document.getElementById('regPassword').value;
    const strengthDiv = document.getElementById('passwordStrength');
    
    if (password.length === 0) {
        strengthDiv.textContent = '';
        strengthDiv.className = 'password-strength';
        return;
    }
    
    if (password.length < 12) {
        strengthDiv.textContent = '密码长度不足12位';
        strengthDiv.className = 'password-strength weak';
        return;
    }
    
    const hasUpperCase = /[A-Z]/.test(password);
    const hasLowerCase = /[a-z]/.test(password);
    const hasDigit = /[0-9]/.test(password);
    const hasSpecial = /[!@#$%^&*()_+\-=\[\]{};':",./<>?\\|`~]/.test(password);
    
    const criteria = [hasUpperCase, hasLowerCase, hasDigit, hasSpecial];
    const metCriteria = criteria.filter(c => c).length;
    
    if (metCriteria === 4) {
        strengthDiv.textContent = '密码强度：强';
        strengthDiv.className = 'password-strength strong';
    } else if (metCriteria >= 2) {
        strengthDiv.textContent = `密码强度：中（还需要：${getMissingCriteria(criteria)}）`;
        strengthDiv.className = 'password-strength medium';
    } else {
        strengthDiv.textContent = `密码强度：弱（还需要：${getMissingCriteria(criteria)}）`;
        strengthDiv.className = 'password-strength weak';
    }
}

function getMissingCriteria(criteria) {
    const missing = [];
    if (!criteria[0]) missing.push('大写字母');
    if (!criteria[1]) missing.push('小写字母');
    if (!criteria[2]) missing.push('数字');
    if (!criteria[3]) missing.push('特殊符号');
    return missing.join('、');
}

function togglePassword(inputId, toggleElement) {
    const input = document.getElementById(inputId);
    if (input.type === 'password') {
        input.type = 'text';
        toggleElement.textContent = '🙈';
    } else {
        input.type = 'password';
        toggleElement.textContent = '👁️';
    }
}