$targetPorts = @(8761, 8080, 8081, 8082, 8083)
$lines = netstat -ano | Select-String "LISTENING"
foreach ($line in $lines) {
    $parts = ($line.ToString().Trim() -split '\s+')
    if ($parts.Count -ge 5) {
        $localAddr = $parts[1]
        $procId = $parts[4]
        foreach ($p in $targetPorts) {
            if ($localAddr -match ":$p$") {
                Write-Host "Freeing port $p (PID: $procId)..."
                Stop-Process -Id $procId -Force -ErrorAction SilentlyContinue
            }
        }
    }
}
