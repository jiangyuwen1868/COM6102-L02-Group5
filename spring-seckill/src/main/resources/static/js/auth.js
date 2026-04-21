const API_BASE = window.location.protocol + '//' + window.location.host + '/api';
const TOKEN_KEY = 'auth_token';
let currentUser = null;

function getToken() {
    return localStorage.getItem(TOKEN_KEY);
}

function getUserInfo() {
    const userInfo = localStorage.getItem('user_info');
    if (userInfo) {
        try {
            return JSON.parse(userInfo);
        } catch (e) {
            return null;
        }
    }
    return null;
}

function checkAuth() {
    const token = getToken();
    const userInfo = getUserInfo();
    
    if (!token || !userInfo) {
        return false;
    }
    
    try {
        currentUser = JSON.parse(userInfo);
        return true;
    } catch (error) {
        return false;
    }
}

function requireAuth() {
    if (!checkAuth()) {
        window.location.href = 'login.html';
        return false;
    }
    return true;
}

function updateAuthUI() {
    const isLoggedIn = checkAuth();
    const userInfo = document.getElementById('userInfo');
    const loginNotice = document.getElementById('loginNotice');
    const userName = document.getElementById('userName');
    
    if (userInfo && loginNotice) {
        if (isLoggedIn && currentUser) {
            userInfo.style.display = 'flex';
            loginNotice.style.display = 'none';
            if (userName) {
                userName.textContent = currentUser.nickname || currentUser.username;
            }
        } else {
            userInfo.style.display = 'none';
            loginNotice.style.display = 'block';
        }
    }
}

function logout() {
    const token = getToken();
    
    if (token) {
        fetch(`${API_BASE}/auth/logout`, {
            method: 'POST',
            headers: {
                'Authorization': token
            }
        }).catch(error => console.error('Logout error:', error));
    }
    
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem('user_info');
    currentUser = null;
    window.location.href = 'login.html';
}

document.addEventListener('DOMContentLoaded', function() {
    updateAuthUI();
});
