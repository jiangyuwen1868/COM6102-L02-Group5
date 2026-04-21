/**
 * 通用模态框组件
 * 提供成功、失败、警告、信息等类型的模态框提示
 */

(function() {
    'use strict';

    // 创建模态框容器
    function createModalContainer() {
        if (document.getElementById('customModalContainer')) {
            return;
        }

        const container = document.createElement('div');
        container.id = 'customModalContainer';
        container.innerHTML = `
            <div id="customModal" class="custom-modal">
                <div class="custom-modal-content">
                    <div class="custom-modal-header">
                        <span id="modalTitle">提示</span>
                        <span class="custom-modal-close">&times;</span>
                    </div>
                    <div class="custom-modal-body">
                        <div class="custom-modal-icon" id="modalIcon"></div>
                        <p id="modalMessage"></p>
                    </div>
                    <div class="custom-modal-footer">
                        <button id="modalConfirmBtn" class="modal-btn modal-btn-primary">确定</button>
                        <button id="modalCancelBtn" class="modal-btn modal-btn-secondary" style="display:none;">取消</button>
                    </div>
                </div>
            </div>
        `;

        // 添加样式
        const style = document.createElement('style');
        style.textContent = `
            .custom-modal {
                display: none;
                position: fixed;
                z-index: 10000;
                left: 0;
                top: 0;
                width: 100%;
                height: 100%;
                background-color: rgba(0, 0, 0, 0.5);
                animation: fadeIn 0.3s;
            }

            .custom-modal.show {
                display: flex;
                align-items: center;
                justify-content: center;
            }

            @keyframes fadeIn {
                from { opacity: 0; }
                to { opacity: 1; }
            }

            @keyframes slideIn {
                from { transform: translateY(-50px); opacity: 0; }
                to { transform: translateY(0); opacity: 1; }
            }

            .custom-modal-content {
                background-color: #fff;
                border-radius: 12px;
                box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
                width: 90%;
                max-width: 420px;
                animation: slideIn 0.3s;
                overflow: hidden;
            }

            .custom-modal-header {
                padding: 16px 20px;
                border-bottom: 1px solid #eee;
                display: flex;
                justify-content: space-between;
                align-items: center;
                background: #f8f9fa;
            }

            #modalTitle {
                font-size: 18px;
                font-weight: 600;
                color: #333;
            }

            .custom-modal-close {
                font-size: 28px;
                color: #999;
                cursor: pointer;
                line-height: 1;
                transition: color 0.2s;
            }

            .custom-modal-close:hover {
                color: #333;
            }

            .custom-modal-body {
                padding: 30px 20px;
                text-align: center;
            }

            .custom-modal-icon {
                font-size: 48px;
                margin-bottom: 15px;
            }

            .custom-modal-body p {
                font-size: 15px;
                color: #666;
                line-height: 1.6;
                margin: 0;
                word-break: break-word;
            }

            .custom-modal-footer {
                padding: 15px 20px;
                border-top: 1px solid #eee;
                display: flex;
                justify-content: center;
                gap: 12px;
                background: #f8f9fa;
            }

            .modal-btn {
                padding: 10px 30px;
                font-size: 14px;
                border-radius: 6px;
                cursor: pointer;
                transition: all 0.2s;
                border: none;
                outline: none;
            }

            .modal-btn-primary {
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                color: white;
            }

            .modal-btn-primary:hover {
                transform: translateY(-1px);
                box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
            }

            .modal-btn-secondary {
                background: #e9ecef;
                color: #666;
            }

            .modal-btn-secondary:hover {
                background: #dee2e6;
            }

            /* 图标样式 */
            .modal-icon-success {
                color: #28a745;
            }

            .modal-icon-error {
                color: #dc3545;
            }

            .modal-icon-warning {
                color: #ffc107;
            }

            .modal-icon-info {
                color: #17a2b8;
            }
        `;

        document.head.appendChild(style);
        document.body.appendChild(container);

        // 绑定关闭事件
        document.querySelector('.custom-modal-close').addEventListener('click', closeModal);
        document.getElementById('modalConfirmBtn').addEventListener('click', closeModal);

        // 点击背景关闭
        container.addEventListener('click', function(e) {
            if (e.target === container) {
                closeModal();
            }
        });

        // ESC键关闭
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape' && container.classList.contains('show')) {
                closeModal();
            }
        });
    }

    // 关闭模态框
    function closeModal() {
        const modal = document.getElementById('customModal');
        if (modal) {
            modal.classList.remove('show');
        }
    }

    // 显示模态框
    function showModal(options) {
        createModalContainer();

        const modal = document.getElementById('customModal');
        const titleEl = document.getElementById('modalTitle');
        const messageEl = document.getElementById('modalMessage');
        const iconEl = document.getElementById('modalIcon');
        const confirmBtn = document.getElementById('modalConfirmBtn');
        const cancelBtn = document.getElementById('modalCancelBtn');

        // 设置标题
        titleEl.textContent = options.title || '提示';

        // 设置消息
        messageEl.textContent = options.message || '';

        // 设置图标和颜色
        iconEl.className = 'custom-modal-icon';
        switch (options.type) {
            case 'success':
                iconEl.innerHTML = '&#10004;';
                iconEl.classList.add('modal-icon-success');
                break;
            case 'error':
                iconEl.innerHTML = '&#10006;';
                iconEl.classList.add('modal-icon-error');
                break;
            case 'warning':
                iconEl.innerHTML = '&#9888;';
                iconEl.classList.add('modal-icon-warning');
                break;
            case 'info':
            default:
                iconEl.innerHTML = '&#8505;';
                iconEl.classList.add('modal-icon-info');
                break;
        }

        // 设置按钮
        if (options.showCancel) {
            cancelBtn.style.display = 'inline-block';
            cancelBtn.onclick = function() {
                closeModal();
                if (options.onCancel) {
                    options.onCancel();
                }
            };
        } else {
            cancelBtn.style.display = 'none';
        }

        // 设置确认按钮回调
        confirmBtn.onclick = function() {
            closeModal();
            if (options.onConfirm) {
                options.onConfirm();
            }
        };

        // 显示模态框
        modal.classList.add('show');
    }

    // 简化调用方法
    window.showMessage = function(message, type) {
        showModal({
            message: message,
            type: type || 'info'
        });
    };

    window.showSuccess = function(message) {
        showModal({
            message: message,
            type: 'success'
        });
    };

    window.showError = function(message) {
        showModal({
            message: message,
            type: 'error'
        });
    };

    window.showWarning = function(message) {
        showModal({
            message: message,
            type: 'warning'
        });
    };

    window.showInfo = function(message) {
        showModal({
            message: message,
            type: 'info'
        });
    };

    // 确认对话框
    window.showConfirm = function(message, onConfirm, onCancel) {
        showModal({
            message: message,
            type: 'info',
            showCancel: true,
            onConfirm: onConfirm,
            onCancel: onCancel
        });
    };

    // 初始化
    createModalContainer();
})();
