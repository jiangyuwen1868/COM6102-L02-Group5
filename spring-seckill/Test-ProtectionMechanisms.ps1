<#
.SYNOPSIS
    测试秒杀系统的限流、防刷和熔断机制

.DESCRIPTION
    此脚本通过发送多个并发请求来测试秒杀系统的保护机制，包括：
    - 限流机制（Rate Limiter）
    - 防刷机制（Anti-brush）
    - 熔断机制（Circuit Breaker）

.PARAMETER ApiBase
    API基础URL，默认为 http://localhost:8080/api

.PARAMETER ProductId
    商品ID，默认为 1

.PARAMETER UserId
    用户ID，默认为 1

.PARAMETER Token
    认证token

.PARAMETER TestCount
    测试总次数，默认为 100

.PARAMETER ConcurrentCount
    并发请求数，默认为 20

.EXAMPLE
    .\Test-ProtectionMechanisms.ps1 -Token "your-auth-token-here"

.EXAMPLE
    .\Test-ProtectionMechanisms.ps1 -ApiBase "http://localhost:8080/api" -ProductId 2 -TestCount 50 -ConcurrentCount 10 -Token "your-token"
#>

param (
    [string]$ApiBase = "http://localhost:8080/api",
    [int]$ProductId = 1,
    [int]$UserId = 349,
    [string]$Token = "009d87a0054c43e8b5ee93a4db09f748",
    [int]$TestCount = 100,
    [int]$ConcurrentCount = 20
)

# 检查token是否提供
if ([string]::IsNullOrEmpty($Token)) {
    Write-Host "错误: 请提供有效的认证token" -ForegroundColor Red
    Write-Host "使用示例: .\Test-ProtectionMechanisms.ps1 -Token 'your-auth-token-here'" -ForegroundColor Yellow
    exit 1
}

# 测试配置
$seckillUrl = "$ApiBase/seckill/$ProductId"
$headers = @{
    "Authorization" = "Bearer $Token"
    "Content-Type" = "application/x-www-form-urlencoded"
}

# 结果统计
$results = @{
    "success" = 0
    "rate_limit" = 0
    "anti_brush" = 0
    "circuit_breaker" = 0
    "other_failure" = 0
    "total" = 0
}

# 开始测试
Write-Host "========================================" -ForegroundColor Green
Write-Host "秒杀系统保护机制测试"
Write-Host "测试目标: $seckillUrl" -ForegroundColor Yellow
Write-Host "测试用户: $UserId" -ForegroundColor Yellow
Write-Host "测试次数: $TestCount" -ForegroundColor Yellow
Write-Host "并发数: $ConcurrentCount" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Green
Write-Host

# 测试函数
function Test-SeckillRequest {
    param (
        [int]$RequestId,
        [int]$IpSuffix
    )
    
    try {
        # 构建请求参数
        $body = @{
            "requestId" = "req_$RequestId`_$(Get-Date -Format 'HHmmss')"
        }
        
        # 构建IP地址
        $ip = "192.168.1.$IpSuffix"
        $requestHeaders = $headers.Clone()
        $requestHeaders.Add("X-Real-IP", $ip)
        
        # 发送请求
        $response = Invoke-RestMethod -Uri $seckillUrl -Method POST -Headers $requestHeaders -Body $body
        
        # 分析响应
        if ($response.code -eq 0 -or $response.success) {
            return "success"
        } elseif ($response.message -like "*参与人数过多*" -or $response.message -like "*稍后再试*") {
            return "rate_limit"
        } elseif ($response.message -like "*请求过于频繁*" -or $response.message -like "*请勿重复提交*" ) {
            return "anti_brush"
        } elseif ($response.message -like "*系统繁忙*" -or $response.message -like "*服务熔断*" ) {
            return "circuit_breaker"
        } else {
            return "other_failure"
        }
    } catch {
        # 处理异常
        $errorMessage = $_.Exception.Message
        if ($errorMessage -like "*500*" -or $errorMessage -like "*服务熔断*" ) {
            return "circuit_breaker"
        } else {
            return "other_failure"
        }
    }
}

# 批量测试
$requestTasks = @()
for ($i = 1; $i -le $TestCount; $i++) {
    $ipSuffix = ($i % 10) + 1  # 使用10个不同的IP
    $task = Start-Job -ScriptBlock {
        param($url, $headers, $requestId, $ipSuffix, $productId, $userId, $token)
        
        # 重新构建URL和参数
        $seckillUrl = "$url/seckill/$productId"
        $requestHeaders = @{
            "Authorization" = "Bearer $token"
            "Content-Type" = "application/x-www-form-urlencoded"
            "X-Real-IP" = "192.168.1.$ipSuffix"
        }
        
        $body = @{
            "requestId" = "req_$requestId`_$(Get-Date -Format 'HHmmss')"
        }
        
        try {
            $response = Invoke-RestMethod -Uri $seckillUrl -Method POST -Headers $requestHeaders -Body $body
            
            if ($response.code -eq 0 -or $response.success) {
                return "success"
            } elseif ($response.message -like "*参与人数过多*" -or $response.message -like "*稍后再试*") {
                return "rate_limit"
            } elseif ($response.message -like "*请求过于频繁*" -or $response.message -like "*请勿重复提交*" ) {
                return "anti_brush"
            } elseif ($response.message -like "*系统繁忙*" -or $response.message -like "*服务熔断*" ) {
                return "circuit_breaker"
            } else {
                return "other_failure"
            }
        } catch {
            $errorMessage = $_.Exception.Message
            if ($errorMessage -like "*500*" -or $errorMessage -like "*服务熔断*" ) {
                return "circuit_breaker"
            } else {
                return "other_failure"
            }
        }
    } -ArgumentList $ApiBase, $headers, $i, $ipSuffix, $ProductId, $UserId, $Token
    
    $requestTasks += $task
    
    # 控制并发数
    if ($requestTasks.Count -ge $ConcurrentCount) {
        # 等待部分任务完成
        $completedTasks = Wait-Job -Job $requestTasks -Any -Timeout 10
        foreach ($task in $completedTasks) {
            $result = Receive-Job -Job $task
            $results[$result]++
            $results.total++
            $requestTasks = $requestTasks | Where-Object { $_.Id -ne $task.Id }
        }
    }
    
    # 显示进度
    if ($i % 10 -eq 0) {
        Write-Host -NoNewline "."
    }
}

# 等待所有任务完成
Write-Host
Write-Host "等待所有请求完成..." -ForegroundColor Yellow
foreach ($task in $requestTasks) {
    $result = Receive-Job -Job $task
    $results[$result]++
    $results.total++
}

# 清理作业
Remove-Job -Job $requestTasks -Force

# 显示结果
Write-Host
Write-Host "========================================" -ForegroundColor Green
Write-Host "测试结果分析:" -ForegroundColor Yellow
Write-Host "成功次数: " -NoNewline
Write-Host $results.success -ForegroundColor Green
Write-Host "限流次数: " -NoNewline
Write-Host $results.rate_limit -ForegroundColor Yellow
Write-Host "防刷次数: " -NoNewline
Write-Host $results.anti_brush -ForegroundColor Red
Write-Host "熔断次数: " -NoNewline
Write-Host $results.circuit_breaker -ForegroundColor Red
Write-Host "其他失败: " -NoNewline
Write-Host $results.other_failure -ForegroundColor Red
Write-Host "总请求数: " -NoNewline
Write-Host $results.total -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Green

# 计算百分比
if ($results.total -gt 0) {
    $successRate = [math]::Round(($results.success / $results.total) * 100, 2)
    $rateLimitRate = [math]::Round(($results.rate_limit / $results.total) * 100, 2)
    $antiBrushRate = [math]::Round(($results.anti_brush / $results.total) * 100, 2)
    $circuitBreakerRate = [math]::Round(($results.circuit_breaker / $results.total) * 100, 2)
    
    Write-Host
    Write-Host "百分比分析:" -ForegroundColor Yellow
    Write-Host "成功率: $successRate%" -ForegroundColor Green
    Write-Host "限流率: $rateLimitRate%" -ForegroundColor Yellow
    Write-Host "防刷触发率: $antiBrushRate%" -ForegroundColor Red
    Write-Host "熔断触发率: $circuitBreakerRate%" -ForegroundColor Red
}

Write-Host
Write-Host "测试完成！" -ForegroundColor Green
Write-Host "请检查系统日志以获取更详细的信息。" -ForegroundColor Cyan
Write-Host
