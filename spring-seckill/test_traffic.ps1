$baseUrl = "http://localhost:8080/test/testSeckill/999/1"
$success = 0
$rateLimited = 0
$errors = 0
$startTime = Get-Date

Write-Host "开始测试流量削峰功能..."
Write-Host "将在1秒内发送150个请求"

$jobs = @()
1..150 | ForEach-Object {
    $jobs += Start-Job -ScriptBlock {
        param($url)
        try {
            $response = Invoke-RestMethod -Method Post -Uri $url -TimeoutSec 1
            if ($response.message -match "参与人数过多") {
                return "rate_limited"
            } else {
                return "success"
            }
        } catch {
            return "error"
        }
    } -ArgumentList $baseUrl
}

$results = @()
$jobs | ForEach-Object {
    $result = Receive-Job -Job $_ -Wait
    $results += $result
    Remove-Job -Job $_
}

$success = ($results | Where-Object { $_ -eq "success" }).Count
$rateLimited = ($results | Where-Object { $_ -eq "rate_limited" }).Count
$errors = ($results | Where-Object { $_ -eq "error" }).Count

$endTime = Get-Date
$duration = ($endTime - $startTime).TotalSeconds

Write-Host "`n测试结果:"
Write-Host "总请求数: 150"
Write-Host "成功: $success"
Write-Host "被限流: $rateLimited"
Write-Host "错误: $errors"
Write-Host "耗时: $duration 秒"
Write-Host "平均速率: $([math]::Round(150 / $duration, 2)) 请求/秒"

if ($rateLimited -gt 0) {
    Write-Host "`n 流量削峰功能正常工作！"
} else {
    Write-Host "`n 流量削峰功能未触发，可能需要调整限流参数"
}
