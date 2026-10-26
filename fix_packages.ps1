# fix_packages.ps1
# Run this script ONCE in PowerShell to fix ALL wrong package declarations
# Usage: Right-click -> Run with PowerShell  OR  powershell -File fix_packages.ps1

$srcRoot = "C:\Users\Asus\Desktop\school-management-restructured\src\main\java"
$fixedCount = 0
$alreadyOkCount = 0

Write-Host ""
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "  Package Declaration Fixer" -ForegroundColor Cyan
Write-Host "  School Management Project" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""

Get-ChildItem $srcRoot -Recurse -Filter "*.java" | ForEach-Object {
    $filePath = $_.FullName
    $content  = Get-Content $filePath -Raw -Encoding UTF8

    # Get declared package from line 1
    $firstLine = ($content -split "`r?`n")[0].Trim()
    if ($firstLine -notmatch '^package\s+([\w\.]+);') { return }
    $declaredPkg = $Matches[1]

    # Calculate expected package from folder structure
    $relative    = $filePath.Replace($srcRoot + "\", "")
    $relative    = $relative.Replace("\", ".")
    $className   = $_.BaseName
    # Remove ClassName.java from end to get package path
    $expectedPkg = $relative.Replace(".$className.java", "").Replace(".java","")

    if ($declaredPkg -eq $expectedPkg) {
        $alreadyOkCount++
        return
    }

    # Fix it
    $newContent = $content -replace "^package\s+[\w\.]+;", "package $expectedPkg;"
    Set-Content $filePath $newContent -Encoding UTF8 -NoNewline
    $fixedCount++
    Write-Host "FIXED: $($_.Name)" -ForegroundColor Green
    Write-Host "  Was : package $declaredPkg;" -ForegroundColor Red
    Write-Host "  Now : package $expectedPkg;" -ForegroundColor Green
    Write-Host ""
}

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "  DONE!" -ForegroundColor Green
Write-Host "  Fixed  : $fixedCount file(s)" -ForegroundColor Green
Write-Host "  OK     : $alreadyOkCount file(s)" -ForegroundColor Gray
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Now do: STS -> Project -> Clean -> Run As Spring Boot App" -ForegroundColor Yellow
Write-Host ""
Read-Host "Press Enter to exit"
