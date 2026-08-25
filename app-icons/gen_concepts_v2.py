"""QuickScan Pro - 6 Play-Store-style concepts in the app's own color theme.

App palette: primary #004AC6, dark #141B2B, accent #8596FF, soft #B4C5FF,
             container #DCE2F7, light bg #F9F9FF, secondary slate #475569.

  7. ProScan   - deep primary blue tile, white QR + corner brackets (classic)
  8. BeamDark  - dark navy tile, accent-blue QR, single crisp scan beam
  9. SnowClean - ultra-light tile, bold primary QR (clean store look)
 10. CircleInk - primary->indigo gradient circle, white QR
 11. FrameGlow - dark tile, glowing accent viewfinder brackets + white QR center
 12. Badge     - primary tile, white QR, corner accent badge (one darker corner)
"""
import math
from PIL import Image, ImageDraw, ImageFilter

S = 1024
OUT = "/home/ubuntu/QR-Code-Scanner-by-Dhanuk/app-icons"

# ---- app theme colors ----
PRIMARY = (0, 74, 198)        # #004AC6
PRIMARY_DK = (0, 58, 158)     # #003A9E
ACCENT = (133, 150, 255)      # #8596FF
SOFT = (180, 197, 255)        # #B4C5FF
CONTAINER = (220, 226, 247)   # #DCE2F7
INK = (20, 27, 43)            # #141B2B
SNOW = (249, 249, 255)        # #F9F9FF
SLATE = (71, 85, 105)         # #475569


def lerp(a, b, t):
    return tuple(int(a[c] + (b[c] - a[c]) * t) for c in range(3))


def rounded_mask(size, radius):
    m = Image.new("L", (size, size), 0)
    ImageDraw.Draw(m).rounded_rectangle([0, 0, size - 1, size - 1], radius=radius, fill=255)
    return m


def circle_mask(size):
    m = Image.new("L", (size, size), 0)
    ImageDraw.Draw(m).ellipse([0, 0, size - 1, size - 1], fill=255)
    return m


def diag_gradient(size, c1, c2, mask=None):
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    span = size * 2
    for i in range(span):
        d.line([(i, 0), (0, i)], fill=lerp(c1, c2, i / span), width=2)
    if mask is not None:
        img.putalpha(mask)
    return img


def tile(c1=PRIMARY, gradient_to=None, shape="square"):
    if gradient_to:
        m = rounded_mask(S, int(S * 0.22)) if shape == "square" else circle_mask(S)
        return diag_gradient(S, c1, gradient_to, m)
    img = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    ImageDraw.Draw(img).rounded_rectangle([0, 0, S - 1, S - 1], radius=int(S * 0.22), fill=c1)
    return img


def qr_module(d, x, y, size, color, radius_ratio=0.28):
    s = size / 2
    d.rounded_rectangle([x - s, y - s, x + s, y + s], radius=size * radius_ratio, fill=color)


def finder(d, x, y, size, color):
    """Solid QR finder square with center dot in tile color cut through."""
    qr_module(d, x, y, size, color, 0.22)
    inner = size * 0.55
    s = inner / 2
    d.rounded_rectangle([x - s, y - s, x + s, y + s], radius=inner * 0.22, fill=(0, 0, 0, 0)) \
        if False else None
    # draw hole via same-color trick handled by caller; here keep simple:
    return


def qr_pattern(d, cx, cy, cell, fg, hole):
    """Classic mini QR: 3 finders + data modules."""
    off = 1.6 * cell
    for (dx, dy) in [(-off, -off), (off, -off), (-off, off)]:
        size = cell * 1.5
        d.rounded_rectangle([cx + dx - size / 2, cy + dy - size / 2,
                             cx + dx + size / 2, cy + dy + size / 2],
                            radius=size * 0.2, fill=fg)
        inn = size * 0.62
        d.rounded_rectangle([cx + dx - inn / 2, cy + dy - inn / 2,
                             cx + dx + inn / 2, cy + dy + inn / 2],
                            radius=inn * 0.2, fill=hole)
        dot = inn * 0.5
        qr_module(d, cx + dx, cy + dy, dot, fg, 0.25)
    # data modules bottom-right
    mods = [(0.1, 1.55), (1.35, 1.55), (1.55, 0.1), (0.35, 0.35), (1.9, 1.45), (1.45, 1.9)]
    for (i, j) in mods:
        qr_module(d, cx + i * cell * 0.85, cy + j * cell * 0.85, cell * 0.72, fg)


def brackets(d, cx, cy, o, L, T, color, radius=None):
    r = radius or T // 2
    for (sx, sy) in [(-1, -1), (1, -1), (-1, 1), (1, 1)]:
        bx, by = cx + sx * o, cy + sy * o
        hx0 = bx if sx < 0 else bx - L
        d.rounded_rectangle([hx0, by - T / 2, hx0 + L, by + T / 2], radius=r, fill=color)
        vy0 = by if sy < 0 else by - L
        d.rounded_rectangle([bx - T / 2, vy0, bx + T / 2, vy0 + L], radius=r, fill=color)


def save(img, name):
    img.save(f"{OUT}/{name}.png")
    print("saved", name)


# 7. ProScan: primary tile, white QR, accent brackets
def c7():
    img = tile(PRIMARY, PRIMARY_DK)
    d = ImageDraw.Draw(img)
    cx = cy = S / 2
    qr_pattern(d, cx, cy, 96, (255, 255, 255), PRIMARY)
    brackets(d, cx, cy, 430, 170, 52, (255, 255, 255))
    save(img, "concept-7-proscan")


# 8. BeamDark: ink tile, accent QR, crisp scan beam
def c8():
    img = tile(INK, (32, 41, 66))
    d = ImageDraw.Draw(img)
    cx = cy = S / 2
    qr_pattern(d, cx, cy, 92, SOFT, INK)
    # beam
    beam = Image.new("RGBA", img.size, (0, 0, 0, 0))
    bd = ImageDraw.Draw(beam)
    bd.rounded_rectangle([cx - 380, cy - 22, cx + 380, cy + 22], radius=22, fill=ACCENT + (255,))
    img.alpha_composite(beam.filter(ImageFilter.GaussianBlur(34)))
    img.alpha_composite(beam.filter(ImageFilter.GaussianBlur(12)))
    d = ImageDraw.Draw(img)
    d.rounded_rectangle([cx - 380, cy - 6, cx + 380, cy + 6], radius=6, fill=(255, 255, 255))
    save(img, "concept-8-beamdark")


# 9. SnowClean: light tile, bold primary QR
def c9():
    img = tile(SNOW)
    d = ImageDraw.Draw(img)
    cx = cy = S / 2
    # subtle container disc behind QR
    d.ellipse([cx - 430, cy - 430, cx + 430, cy + 430], fill=CONTAINER)
    qr_pattern(d, cx, cy, 96, PRIMARY, CONTAINER)
    save(img, "concept-9-snowclean")


# 10. CircleInk: primary->indigo gradient circle, white QR
def c10():
    img = diag_gradient(S, PRIMARY, (68, 86, 186), circle_mask(S))  # #4456BA
    d = ImageDraw.Draw(img)
    cx = cy = S / 2
    d.ellipse([cx - 420, cy - 420, cx + 420, cy + 420], outline=(255, 255, 255), width=30)
    qr_pattern(d, cx, cy, 88, (255, 255, 255), PRIMARY)
    save(img, "concept-10-circleink")


# 11. FrameGlow: ink tile, glowing accent brackets + white QR center
def c11():
    img = tile(INK, (32, 41, 66))
    cx = cy = S / 2
    # glow behind brackets
    glow = Image.new("RGBA", img.size, (0, 0, 0, 0))
    brackets(ImageDraw.Draw(glow), cx, cy, 380, 190, 56, ACCENT + (255,))
    img.alpha_composite(glow.filter(ImageFilter.GaussianBlur(28)))
    d = ImageDraw.Draw(img)
    brackets(d, cx, cy, 380, 190, 56, ACCENT)
    qr_pattern(d, cx, cy, 66, (255, 255, 255), INK)
    save(img, "concept-11-frameglow")


# 12. Badge: primary tile, white QR, darker accent corner
def c12():
    img = tile(PRIMARY)
    d = ImageDraw.Draw(img)
    # darker corner badge wedge (bottom-right triangle inside tile)
    corner = Image.new("RGBA", img.size, (0, 0, 0, 0))
    cd = ImageDraw.Draw(corner)
    cd.polygon([(S, int(S * 0.42)), (S, S), (int(S * 0.42), S)], fill=PRIMARY_DK + (255,))
    img.alpha_composite(corner)
    d = ImageDraw.Draw(img)
    cx = cy = S / 2 - 30
    qr_pattern(d, cx, cy, 84, (255, 255, 255), PRIMARY)
    # accent dot in corner
    d.ellipse([S - 240, S - 240, S - 120, S - 120], fill=SOFT)
    save(img, "concept-12-badge")


if __name__ == "__main__":
    c7(); c8(); c9(); c10(); c11(); c12()
    print("done")
