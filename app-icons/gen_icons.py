"""QuickScan Pro - 6 professional app icon variants (512x512 PNG)."""
from PIL import Image, ImageDraw

S = 512

def rounded_mask(size, radius):
    m = Image.new("L", (size, size), 0)
    d = ImageDraw.Draw(m)
    d.rounded_rectangle([0, 0, size - 1, size - 1], radius=radius, fill=255)
    return m

def qr_glyph(draw, x, y, cell, light, edge=None, finder_ring=True):
    """Draw stylized QR glyph: 3 finder squares + scattered modules."""
    def finder(cx, cy, size):
        r = size * 0.18
        if finder_ring and edge:
            draw.rounded_rectangle([x + cx - size/2 - cell*0.35, y + cy - size/2 - cell*0.35,
                                    x + cx + size/2 + cell*0.35, y + cy + size/2 + cell*0.35],
                                   radius=r * 1.4, outline=light, width=max(3, int(cell*0.28)))
        draw.rounded_rectangle([x + cx - size/2, y + cy - size/2, x + cx + size/2, y + cy + size/2],
                               radius=r, fill=light)
        inner = size * 0.45
        draw.rounded_rectangle([x + cx - inner/2, y + cy - inner/2, x + cx + inner/2, y + cy + inner/2],
                               radius=r * 0.7, fill=edge or (24, 32, 48))
        dot = inner * 0.5
        draw.ellipse([x + cx - dot/2, y + cy - dot/2, x + cx + dot/2, y + cy + dot/2], fill=light)

    finder(-3 * cell, -3 * cell, 2.4 * cell)   # top-left
    finder( 3 * cell, -3 * cell, 2.4 * cell)   # top-right
    finder(-3 * cell,  3 * cell, 2.4 * cell)   # bottom-left

    # data modules (bottom-right quadrant scatter)
    mods = [(1, 1, 1.1), (3, 3, 1.0), (1, 3, 0.9), (3, 1, 0.9), (0, 2, 0.8), (2, 0, 0.8)]
    for gx, gy, sc in mods:
        s = cell * sc
        draw.rounded_rectangle([x + gx * cell - s/2, y + gy * cell - s/2,
                                x + gx * cell + s/2, y + gy * cell + s/2],
                               radius=s * 0.25, fill=light)

def base(bg1, bg2=None, shape="square", light=(255, 255, 255)):
    img = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    if bg2:  # diagonal gradient
        for i in range(S * 2):
            t = i / (S * 2)
            col = tuple(int(bg1[c] + (bg2[c] - bg1[c]) * t) for c in range(3))
            d.line([(i, 0), (0, i)], fill=col, width=2)
    else:
        col = bg1
        if shape == "square":
            d.rounded_rectangle([0, 0, S - 1, S - 1], radius=int(S * 0.22), fill=col)
        elif shape == "circle":
            d.ellipse([0, 0, S - 1, S - 1], fill=col)
    if bg2:  # clip gradient
        mask = rounded_mask(S, int(S * 0.22)) if shape == "square" else None
        if shape == "circle":
            mask = Image.new("L", (S, S), 0)
            ImageDraw.Draw(mask).ellipse([0, 0, S - 1, S - 1], fill=255)
        img.putalpha(mask)
    return img, d

C = S // 2

def v1():  # Brand blue square, white QR
    img, d = base((0, 74, 198))
    qr_glyph(d, C, C, 52, (255, 255, 255), edge=(0, 74, 198), finder_ring=False)
    return img

def v2():  # Blue->violet gradient, white QR with rings
    img, d = base((43, 108, 255), (90, 62, 224), shape="square")
    qr_glyph(d, C, C, 50, (255, 255, 255), edge=(60, 85, 235), finder_ring=True)
    return img

def v3():  # Circle, dark navy bg, accent blue QR
    img = Image.new("RGBA", (S, S), (15, 23, 42, 255))
    mask = Image.new("L", (S, S), 0)
    ImageDraw.Draw(mask).ellipse([0, 0, S - 1, S - 1], fill=255)
    img.putalpha(mask)
    d = ImageDraw.Draw(img)
    qr_glyph(d, C, C, 50, (96, 165, 250), edge=(15, 23, 42), finder_ring=False)
    return img

def v4():  # White square, brand blue QR (clean/minimal)
    img, d = base((255, 255, 255))
    qr_glyph(d, C, C, 52, (0, 74, 198), edge=(255, 255, 255), finder_ring=False)
    return img

def v5():  # Indigo circle, white QR with rings
    img, d = base((67, 56, 202), (99, 102, 241), shape="circle")
    qr_glyph(d, C, C, 50, (255, 255, 255), edge=(79, 70, 229), finder_ring=True)
    return img

def v6():  # Emerald square, white QR (fresh alt)
    img, d = base((4, 120, 87), (5, 150, 105), shape="square")
    qr_glyph(d, C, C, 52, (236, 253, 245), edge=(6, 95, 70), finder_ring=False)
    return img

names = {
    1: "icon-v1-brand-blue", 2: "icon-v2-blue-violet-gradient", 3: "icon-v3-dark-navy-circle",
    4: "icon-v4-minimal-white", 5: "icon-v5-indigo-circle", 6: "icon-v6-emerald",
}
for i, fn in enumerate([v1, v2, v3, v4, v5, v6], 1):
    img = fn()
    img.save(f"/tmp/opencode/icons/{names[i]}.png")
    print(names[i], img.size)
