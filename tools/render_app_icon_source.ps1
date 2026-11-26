Add-Type -AssemblyName System.Drawing
Add-Type -ReferencedAssemblies 'System.Drawing.Common', 'System.Drawing.Primitives', 'System.Private.Windows.GdiPlus', 'System.Private.Windows.Core' -TypeDefinition @'
using System;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Imaging;
using System.IO;

public static class TurinautaIconRenderer
{
    // The same closed Bézier outline is used by the SVG and Android vector drawable.
    public const string RoadPath = "M 92,382 C 140,365 248,330 290,294 C 330,260 265,246 230,226 C 182,198 214,170 260,158 C 297,149 325,145 351,143 L 351,153 C 326,154 296,161 269,169 C 236,179 217,195 243,209 C 278,228 343,247 320,288 C 294,335 194,377 115,414 C 100,403 95,391 92,382 Z";

    private static GraphicsPath CreateRoad()
    {
        var path = new GraphicsPath();
        path.StartFigure();
        path.AddBezier(92, 382, 140, 365, 248, 330, 290, 294);
        path.AddBezier(290, 294, 330, 260, 265, 246, 230, 226);
        path.AddBezier(230, 226, 182, 198, 214, 170, 260, 158);
        path.AddBezier(260, 158, 297, 149, 325, 145, 351, 143);
        path.AddLine(351, 143, 351, 153);
        path.AddBezier(351, 153, 326, 154, 296, 161, 269, 169);
        path.AddBezier(269, 169, 236, 179, 217, 195, 243, 209);
        path.AddBezier(243, 209, 278, 228, 343, 247, 320, 288);
        path.AddBezier(320, 288, 294, 335, 194, 377, 115, 414);
        path.AddBezier(115, 414, 100, 403, 95, 391, 92, 382);
        path.CloseFigure();
        return path;
    }

    private static void Draw(string path, bool disc)
    {
        using (var bitmap = new Bitmap(1024, 1024, PixelFormat.Format32bppArgb))
        using (var graphics = Graphics.FromImage(bitmap))
        using (var road = CreateRoad())
        using (var teal = new SolidBrush(Color.FromArgb(19, 80, 84)))
        using (var white = new SolidBrush(Color.White))
        using (var ochre = new SolidBrush(Color.FromArgb(196, 138, 45)))
        {
            graphics.Clear(Color.Transparent);
            graphics.SmoothingMode = SmoothingMode.AntiAlias;
            graphics.CompositingQuality = CompositingQuality.HighQuality;
            graphics.PixelOffsetMode = PixelOffsetMode.HighQuality;
            graphics.ScaleTransform(2f, 2f);
            if (disc) graphics.FillEllipse(teal, 30, 30, 452, 452);
            graphics.FillPath(white, road);
            graphics.FillEllipse(ochre, 348, 121, 78, 78);
            bitmap.Save(path, ImageFormat.Png);
        }
    }

    public static void Render(string outputDirectory)
    {
        Draw(Path.Combine(outputDirectory, "app_icon_source.png"), true);
        Draw(Path.Combine(outputDirectory, "app_icon_foreground.png"), false);
        var svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 512 512\">\n" +
            "  <circle cx=\"256\" cy=\"256\" r=\"226\" fill=\"#135054\"/>\n" +
            "  <path d=\"" + RoadPath + "\" fill=\"#FFFFFF\"/>\n" +
            "  <circle cx=\"387\" cy=\"160\" r=\"39\" fill=\"#C48A2D\"/>\n" +
            "</svg>\n";
        File.WriteAllText(Path.Combine(outputDirectory, "app_icon_source.svg"), svg);
    }
}
'@

[TurinautaIconRenderer]::Render((Join-Path $PSScriptRoot '..\assets'))
