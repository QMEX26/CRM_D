$headers = @{ 
    "X-Sync-Secret" = "AKfycbyQFNJjPe8hL6Zfmeatdtizd77E0mKSOikhOW6L9Ye1gdrU2ZFvrJpD0jfhRROUL_JL"
    "bypass-tunnel-reminder" = "true"
}
$res = Invoke-RestMethod -Uri "https://crm-local-sync-99.loca.lt/api/v1/google-sheets/pull" -Method Post -Headers $headers -ContentType "application/json"
Write-Host "Success:" $res.success
Write-Host "Total Records:" $res.data.totalRecords
Write-Host "Message:" $res.message
