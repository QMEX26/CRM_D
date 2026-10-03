$h1 = @{ "X-Sync-Secret" = "AKfycbyOg6Lq8pJKMPkQoVgE__cUwIMvXa0YmHTihK4iHDBsgWY6kMRzKEBXPRmpbQX53CN9" }
try { 
  $r1 = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/google-sheets/pull" -Method Post -Headers $h1 -ContentType "application/json"
  Write-Host "Old secret works: $($r1.success)"
} catch {
  Write-Host "Old secret failed: $($_.Exception.Message)"
}

$h2 = @{ "X-Sync-Secret" = "AKfycbyQFNJjPe8hL6Zfmeatdtizd77E0mKSOikhOW6L9Ye1gdrU2ZFvrJpD0jfhRROUL_JL" }
try { 
  $r2 = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/google-sheets/pull" -Method Post -Headers $h2 -ContentType "application/json"
  Write-Host "New secret works: $($r2.success)"
} catch {
  Write-Host "New secret failed: $($_.Exception.Message)"
}
