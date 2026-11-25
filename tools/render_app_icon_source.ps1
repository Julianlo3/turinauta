Add-Type -AssemblyName System.Drawing
Add-Type -ReferencedAssemblies 'System.Drawing' -TypeDefinition @'
using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Imaging;
using System.Globalization;
using System.IO;
using System.Text;

public static class TurinautaTaperedIconPreview
{
    private static PointF Cubic(PointF[] p, double t)
    {
        double s = 1.0 - t;
        return new PointF(
            (float)(s*s*s*p[0].X + 3*s*s*t*p[1].X + 3*s*t*t*p[2].X + t*t*t*p[3].X),
            (float)(s*s*s*p[0].Y + 3*s*s*t*p[1].Y + 3*s*t*t*p[2].Y + t*t*t*p[3].Y));
    }

    private static PointF Tangent(PointF[] p, double t)
    {
        double s = 1.0 - t;
        return new PointF(
            (float)(3*s*s*(p[1].X-p[0].X) + 6*s*t*(p[2].X-p[1].X) + 3*t*t*(p[3].X-p[2].X)),
            (float)(3*s*s*(p[1].Y-p[0].Y) + 6*s*t*(p[2].Y-p[1].Y) + 3*t*t*(p[3].Y-p[2].Y)));
    }

    private static string Coordinate(PointF p)
    {
        return p.X.ToString("0.00", CultureInfo.InvariantCulture) + "," +
            p.Y.ToString("0.00", CultureInfo.InvariantCulture);
    }

    public static void Render(string outputDirectory)
    {
        var curves = new[] {
            new[] { new PointF(72, 405), new PointF(170, 373), new PointF(308, 328), new PointF(296, 291) },
            new[] { new PointF(296, 291), new PointF(284, 254), new PointF(186, 254), new PointF(196, 219) },
            new[] { new PointF(196, 219), new PointF(206, 184), new PointF(277, 169), new PointF(337, 164) }
        };

        var centers = new List<PointF>();
        var tangents = new List<PointF>();
        foreach (var curve in curves) {
            for (int step = 0; step <= 96; step++) {
                if (centers.Count > 0 && step == 0) continue;
                double t = step / 96.0;
                centers.Add(Cubic(curve, t));
                tangents.Add(Tangent(curve, t));
            }
        }

        var distances = new List<double> { 0.0 };
        for (int i = 1; i < centers.Count; i++) {
            double dx = centers[i].X - centers[i-1].X;
            double dy = centers[i].Y - centers[i-1].Y;
            distances.Add(distances[i-1] + Math.Sqrt(dx*dx + dy*dy));
        }

        var leftEdge = new List<PointF>();
        var rightEdge = new List<PointF>();
        for (int i = 0; i < centers.Count; i++) {
            double length = Math.Sqrt(tangents[i].X*tangents[i].X + tangents[i].Y*tangents[i].Y);
            double normalX = -tangents[i].Y / length;
            double normalY = tangents[i].X / length;
            double progress = distances[i] / distances[distances.Count - 1];
            double halfWidth = (46.0 - 40.0 * progress) / 2.0;
            leftEdge.Add(new PointF((float)(centers[i].X + normalX*halfWidth), (float)(centers[i].Y + normalY*halfWidth)));
            rightEdge.Add(new PointF((float)(centers[i].X - normalX*halfWidth), (float)(centers[i].Y - normalY*halfWidth)));
        }

        var outline = new List<PointF>(leftEdge);
        var end = centers[centers.Count - 1];
        var direction = tangents[tangents.Count - 1];
        double directionLength = Math.Sqrt(direction.X*direction.X + direction.Y*direction.Y);
        double ux = direction.X / directionLength;
        double uy = direction.Y / directionLength;
        double nx = -uy;
        double ny = ux;
        for (int step = 1; step <= 20; step++) {
            double angle = Math.PI * step / 20.0;
            outline.Add(new PointF(
                (float)(end.X + 3.0*(nx*Math.Cos(angle) + ux*Math.Sin(angle))),
                (float)(end.Y + 3.0*(ny*Math.Cos(angle) + uy*Math.Sin(angle)))));
        }
        for (int i = rightEdge.Count - 2; i >= 0; i--) outline.Add(rightEdge[i]);

        using (var bitmap = new Bitmap(1024, 1024, PixelFormat.Format32bppArgb))
        using (var graphics = Graphics.FromImage(bitmap))
        using (var disc = new GraphicsPath())
        using (var road = new GraphicsPath())
        using (var teal = new SolidBrush(Color.FromArgb(19, 80, 84)))
        using (var ivory = new SolidBrush(Color.FromArgb(250, 247, 242)))
        using (var gold = new SolidBrush(Color.FromArgb(196, 138, 45)))
        {
            graphics.Clear(Color.Transparent);
            graphics.SmoothingMode = SmoothingMode.AntiAlias;
            graphics.CompositingQuality = CompositingQuality.HighQuality;
            graphics.PixelOffsetMode = PixelOffsetMode.HighQuality;
            graphics.ScaleTransform(2f, 2f);
            disc.AddEllipse(30, 30, 452, 452);
            road.AddPolygon(outline.ToArray());
            graphics.FillPath(teal, disc);
            graphics.SetClip(disc);
            graphics.FillPath(ivory, road);
            graphics.ResetClip();
            graphics.FillEllipse(gold, 348, 121, 78, 78);
            bitmap.Save(Path.Combine(outputDirectory, "app_icon_source.png"), ImageFormat.Png);

            using (var foregroundBitmap = new Bitmap(1024, 1024, PixelFormat.Format32bppArgb))
            using (var foregroundGraphics = Graphics.FromImage(foregroundBitmap))
            {
                foregroundGraphics.Clear(Color.Transparent);
                foregroundGraphics.SmoothingMode = SmoothingMode.AntiAlias;
                foregroundGraphics.CompositingQuality = CompositingQuality.HighQuality;
                foregroundGraphics.PixelOffsetMode = PixelOffsetMode.HighQuality;
                foregroundGraphics.ScaleTransform(2f, 2f);
                foregroundGraphics.SetClip(disc);
                foregroundGraphics.FillPath(ivory, road);
                foregroundGraphics.ResetClip();
                foregroundGraphics.FillEllipse(gold, 348, 121, 78, 78);
                foregroundBitmap.Save(Path.Combine(outputDirectory, "app_icon_foreground.png"), ImageFormat.Png);
            }
        }

        var svg = new StringBuilder();
        svg.AppendLine("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 512 512\" width=\"1024\" height=\"1024\">");
        svg.AppendLine("  <defs><clipPath id=\"disc\"><circle cx=\"256\" cy=\"256\" r=\"226\"/></clipPath></defs>");
        svg.AppendLine("  <circle cx=\"256\" cy=\"256\" r=\"226\" fill=\"#135054\"/>");
        svg.Append("  <path clip-path=\"url(#disc)\" fill=\"#FAF7F2\" d=\"M ");
        svg.Append(Coordinate(outline[0]));
        for (int i = 1; i < outline.Count; i++) svg.Append(" L ").Append(Coordinate(outline[i]));
        svg.AppendLine(" Z\"/>");
        svg.AppendLine("  <circle cx=\"387\" cy=\"160\" r=\"39\" fill=\"#C48A2D\"/>");
        svg.AppendLine("</svg>");
        File.WriteAllText(Path.Combine(outputDirectory, "app_icon_source.svg"), svg.ToString(), Encoding.UTF8);
    }
}
'@

[TurinautaTaperedIconPreview]::Render((Join-Path $PSScriptRoot '..\assets'))
