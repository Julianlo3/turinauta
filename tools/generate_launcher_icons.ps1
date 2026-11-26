param(
    [string]$Source = (Join-Path $PSScriptRoot '..\assets\app_icon_source.png'),
    [string]$ForegroundSource = (Join-Path $PSScriptRoot '..\assets\app_icon_foreground.png')
)

Add-Type -AssemblyName System.Drawing

$resourceRoot = Join-Path $PSScriptRoot '..\app\src\main\res'
$sourceBitmap = [System.Drawing.Bitmap]::new((Resolve-Path -LiteralPath $Source).Path)
$foregroundBitmap = [System.Drawing.Bitmap]::new((Resolve-Path -LiteralPath $ForegroundSource).Path)
if ($sourceBitmap.Width -ne $foregroundBitmap.Width -or $sourceBitmap.Height -ne $foregroundBitmap.Height) {
    throw 'El icono y su capa frontal deben tener las mismas dimensiones.'
}
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

function Save-AdaptiveImage {
    param(
        [System.Drawing.Image]$Image,
        [int]$CanvasSize,
        [double]$Scale,
        [string]$Path
    )
    $bitmap = [System.Drawing.Bitmap]::new($CanvasSize, $CanvasSize)
    $scaled = [System.Drawing.Bitmap]::new($CanvasSize, $CanvasSize)
    $graphics = [System.Drawing.Graphics]::FromImage($scaled)
    try {
        $teal = [System.Drawing.Color]::FromArgb(19, 80, 84)
        $bitmapGraphics = [System.Drawing.Graphics]::FromImage($bitmap)
        try { $bitmapGraphics.Clear($teal) } finally { $bitmapGraphics.Dispose() }
        $graphics.Clear([System.Drawing.Color]::Transparent)
        $graphics.CompositingMode = [System.Drawing.Drawing2D.CompositingMode]::SourceCopy
        $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
        $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
        $imageSize = [int][Math]::Round(82 * $Scale)
        $offset = [int](($CanvasSize - $imageSize) / 2)
        $graphics.DrawImage($Image, [System.Drawing.Rectangle]::new($offset, $offset, $imageSize, $imageSize))

        # Blend the circle edge into the extended teal area without touching the road or dot.
        $center = $CanvasSize / 2.0
        $fadeStart = 38 * $Scale
        $fadeEnd = 41 * $Scale
        for ($y = 0; $y -lt $CanvasSize; $y++) {
            for ($x = 0; $x -lt $CanvasSize; $x++) {
                $distance = [Math]::Sqrt(($x + 0.5 - $center) * ($x + 0.5 - $center) + ($y + 0.5 - $center) * ($y + 0.5 - $center))
                if ($distance -ge $fadeEnd) { continue }
                $pixel = $scaled.GetPixel($x, $y)
                if ($pixel.A -eq 0) { continue }
                $fade = [Math]::Min(1.0, ($fadeEnd - $distance) / ($fadeEnd - $fadeStart))
                $alpha = ($pixel.A / 255.0) * $fade
                $red = [int][Math]::Round($pixel.R * $alpha + $teal.R * (1 - $alpha))
                $green = [int][Math]::Round($pixel.G * $alpha + $teal.G * (1 - $alpha))
                $blue = [int][Math]::Round($pixel.B * $alpha + $teal.B * (1 - $alpha))
                $bitmap.SetPixel($x, $y, [System.Drawing.Color]::FromArgb($red, $green, $blue))
            }
        }
        $bitmap.Save($Path, [System.Drawing.Imaging.ImageFormat]::Png)
    } finally {
        $graphics.Dispose()
        $scaled.Dispose()
        $bitmap.Dispose()
    }
}

# The themed icon preserves the route and dot alpha while Android supplies the tint.
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
        $foregroundPixel = $foregroundBitmap.GetPixel($x, $y)
        if ($foregroundPixel.A -gt 0) {
            $monochrome.SetPixel($x, $y, [System.Drawing.Color]::FromArgb($foregroundPixel.A, 255, 255, 255))
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
        Save-AdaptiveImage $sourceBitmap $adaptiveSize $density.Scale (Join-Path $folder 'ic_launcher_adaptive.png')
        Save-ScaledImage $foregroundBitmap $adaptiveSize $foregroundSize (Join-Path $folder 'ic_launcher_foreground.png')
        Save-ScaledImage $monochrome $adaptiveSize $foregroundSize (Join-Path $folder 'ic_launcher_monochrome.png')
    }
    $logoFolder = Join-Path $resourceRoot 'drawable-nodpi'
    New-Item -ItemType Directory -Force -Path $logoFolder | Out-Null
    Save-ScaledImage $sourceBitmap 512 ([int][Math]::Round(512 * $artworkScale)) (Join-Path $logoFolder 'turinauta_logo.png')
} finally {
    $monochrome.Dispose()
    $foregroundBitmap.Dispose()
    $sourceBitmap.Dispose()
}
