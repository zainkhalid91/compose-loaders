# Compares frame times of Compose Loaders against an equivalent Lottie spinner on a connected phone.
# The phone must be unlocked with the screen on. Usage: tools\benchmark.ps1 [-Serial <adb serial>] [-Count 24] [-Seconds 20]
param(
    [string]$Serial = "",
    [int]$Count = 24,
    [int]$Seconds = 20,
    [int]$Runs = 3
)
$ErrorActionPreference = "Stop"
$root = Split-Path $PSScriptRoot -Parent
$pkg = "io.github.zainkhalid91.loaders.benchmark"
$adb = if ($Serial) { @("adb", "-s", $Serial) } else { @("adb") }
function Adb { & $adb[0] $adb[1..($adb.Length - 1)] @args }

Push-Location $root
& .\gradlew.bat :benchmark:assembleRelease -q
Pop-Location
Adb install -r "$root\benchmark\build\outputs\apk\release\benchmark-release.apk" | Out-Null
$model = (Adb shell getprop ro.product.model).Trim()
$sdk = (Adb shell getprop ro.build.version.sdk).Trim()
"Device: $model (API $sdk), $Count spinners, ${Seconds}s per run, $Runs runs"

$results = @()
foreach ($run in 1..$Runs) {
    foreach ($mode in "compose", "lottie") {
        Adb shell am force-stop $pkg
        Adb shell am start -W -n "$pkg/.BenchActivity" --es mode $mode --ei count $Count | Out-Null
        Start-Sleep -Seconds 3
        Adb shell dumpsys gfxinfo $pkg reset | Out-Null
        Start-Sleep -Seconds $Seconds
        $stats = Adb shell dumpsys gfxinfo $pkg
        $row = [ordered]@{ Run = $run; Mode = $mode }
        foreach ($line in $stats) {
            if ($line -match "Total frames rendered: (\d+)") { $row.Frames = [int]$Matches[1] }
            if ($line -match "^Janky frames: (\d+) \(([\d.]+)%\)") { $row.JankyPct = [double]$Matches[2] }
            if ($line -match "^50th percentile: (\d+)ms") { $row.P50 = [int]$Matches[1] }
            if ($line -match "^90th percentile: (\d+)ms") { $row.P90 = [int]$Matches[1] }
            if ($line -match "^99th percentile: (\d+)ms") { $row.P99 = [int]$Matches[1] }
            if ($line -match "^90th gpu percentile: (\d+)ms") { $row.GpuP90 = [int]$Matches[1] }
        }
        $mem = Adb shell dumpsys meminfo $pkg | Select-String "TOTAL PSS:\s+(\d+)"
        if ($mem) { $row.PssMB = [math]::Round([int]$mem.Matches[0].Groups[1].Value / 1024, 1) }
        $results += [pscustomobject]$row
    }
}
Adb shell am force-stop $pkg
$results | Format-Table -AutoSize
"Median per mode:"
foreach ($mode in "compose", "lottie") {
    $rows = $results | Where-Object Mode -eq $mode
    $median = { param($v) ($v | Sort-Object)[[int][math]::Floor($v.Count / 2)] }
    "{0,-8} p50 {1} ms  p90 {2} ms  p99 {3} ms  janky {4}%  gpu p90 {5} ms  PSS {6} MB" -f $mode,
        (& $median $rows.P50), (& $median $rows.P90), (& $median $rows.P99), (& $median $rows.JankyPct),
        (& $median $rows.GpuP90), (& $median $rows.PssMB)
}
