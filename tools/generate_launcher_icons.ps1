param(
    [string]$Source = (Join-Path $PSScriptRoot '..\assets\app_icon_source.png')
)

Add-Type -AssemblyName System.Drawing

$resourceRoot = Join-Path $PSScriptRoot '..\app\src\main\res'
$sourceBitmap = [System.Drawing.Bitmap]::new((Resolve-Path -LiteralPath $Source).Path)
$minX = $sourceBitmap.Width
$minY = $sourceBitmap.Height
$maxX = 0
$maxY = 0

function Save-ScaledImage {
    param(
        [System.Drawing.Image]$Image,
        [int]$CanvasSize,
        [int]$ImageSize,
        [string]$Path
    )
    $bitmap = [System.Drawing.Bitmap]::new($CanvasSize, $CanvasSize)
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    try {
        $graphics.Clear([System.Drawing.Color]::Transparent)
        $graphics.CompositingMode = [System.Drawing.Drawing2D.CompositingMode]::SourceCopy
        $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
        $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
        $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
        $offset = [int](($CanvasSize - $ImageSize) / 2)
        $graphics.DrawImage($Image, [System.Drawing.Rectangle]::new($offset, $offset, $ImageSize, $ImageSize))
        $bitmap.Save($Path, [System.Drawing.Imaging.ImageFormat]::Png)
    } finally {
        $graphics.Dispose()
        $bitmap.Dispose()
    }
}

# The themed icon contains only the route and dot, so Android can tint those shapes.
$monochrome = [System.Drawing.Bitmap]::new($sourceBitmap.Width, $sourceBitmap.Height)
for ($y = 0; $y -lt $sourceBitmap.Height; $y++) {
    for ($x = 0; $x -lt $sourceBitmap.Width; $x++) {
        $pixel = $sourceBitmap.GetPixel($x, $y)
        if ($pixel.A -gt 128) {
            $minX = [Math]::Min($minX, $x)
            $minY = [Math]::Min($minY, $y)
            $maxX = [Math]::Max($maxX, $x)
            $maxY = [Math]::Max($maxY, $y)
        }
        $difference = [Math]::Abs([int]$pixel.R - 19) +
            [Math]::Abs([int]$pixel.G - 80) + [Math]::Abs([int]$pixel.B - 84)
        if ($pixel.A -gt 0 -and $difference -gt 40) {
            $monochrome.SetPixel($x, $y, [System.Drawing.Color]::FromArgb(255, 255, 255, 255))
        }
    }
}
$artworkDiameter = [Math]::Max($maxX - $minX + 1, $maxY - $minY + 1)
$artworkScale = $sourceBitmap.Width / $artworkDiameter

try {
    foreach ($density in @(
        @{ Name = 'mdpi'; Scale = 1 },
        @{ Name = 'hdpi'; Scale = 1.5 },
        @{ Name = 'xhdpi'; Scale = 2 },
        @{ Name = 'xxhdpi'; Scale = 3 },
        @{ Name = 'xxxhdpi'; Scale = 4 }
    )) {
        $folder = Join-Path $resourceRoot "mipmap-$($density.Name)"
        $legacySize = [int](48 * $density.Scale)
        $adaptiveSize = [int](108 * $density.Scale)
        $legacyArtworkSize = [int][Math]::Round($legacySize * $artworkScale)
        $foregroundSize = [int][Math]::Round(72 * $density.Scale * $artworkScale)
        Save-ScaledImage $sourceBitmap $legacySize $legacyArtworkSize (Join-Path $folder 'ic_launcher.png')
        Save-ScaledImage $sourceBitmap $legacySize $legacyArtworkSize (Join-Path $folder 'ic_launcher_round.png')
        Save-ScaledImage $sourceBitmap $adaptiveSize $foregroundSize (Join-Path $folder 'ic_launcher_foreground.png')
        Save-ScaledImage $monochrome $adaptiveSize $foregroundSize (Join-Path $folder 'ic_launcher_monochrome.png')
    }
    $logoFolder = Join-Path $resourceRoot 'drawable-nodpi'
    New-Item -ItemType Directory -Force -Path $logoFolder | Out-Null
    Save-ScaledImage $sourceBitmap 512 ([int][Math]::Round(512 * $artworkScale)) (Join-Path $logoFolder 'turinauta_logo.png')
} finally {
    $monochrome.Dispose()
    $sourceBitmap.Dispose()
}
