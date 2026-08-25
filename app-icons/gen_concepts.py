"""QuickScan Pro - 6 NEW concept logos, each a very different design language.

Concepts:
  1. ScanLine  - dark navy + glowing cyan scan beam over QR  (tech/pro)
  2. Bolt      - lightning bolt cutting through QR            (speed)
  3. OrbitQ    - circular ring + pixel QR dot core            (modern/abstract)
  4. Frame     - camera viewfinder brackets + tiny QR         (minimal camera)
  5. Prism     - vibrant multi-gradient tiles QR              (playful/vibrant)
  6. Mono      - flat single-color bold Q built from pixels   (wordmark-ish)
"""
import math
from PIL import Image, ImageDraw, ImageFilter

S = 1024           # canvas
OUT = "/home/ubuntu/QR-Code-Scanner-by-Dhanuk/app-icons"


# ---------- helpers ----------
def lerp(a, b, t):
    return tuple(int(a[c] + (b[c] - a[c]) * t) for c in range(len(a)))


def rounded_mask(size, radius):
    m = Image.new("L", (size, size), 0)
    ImageDraw.Draw(m).rounded_rectangle([0, 0, size - 1, size - 1], radius=radius, fill=255)
    return m


def circle_mask(size):
    m = Image.new("L", (size, size), 0)
    ImageDraw.Draw(m).ellipse([0, 0, size - 1, size - 1], fill=255)
    return m


def diag_gradient(size, c1, c2, mask=None):
    """Diagonal gradient image (top-left c1 -> bottom-right c2)."""
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    span = size * 2
    for i in range(span):
        t = i / span
        col = lerp(c1, c2, t)
        d.line([(i, 0), (0, i)], fill=col, width=2)
    if mask is not None:
        img.putalpha(mask)
    return img


def glow_dot(base_img, xy, radius, color, blur, alpha=220):
    """Add a soft glow with a bright core."""
    layer = Image.new("RGBA", base_img.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(layer)
    x, y = xy
    d.ellipse([x - radius, y - radius, x + radius, y + radius], fill=color + (alpha,))
    layer = layer.filter(ImageFilter.GaussianBlur(blur))
    base_img.alpha_composite(layer)
    d = ImageDraw.Draw(base_img)
    d.ellipse([x - radius * 0.55, y - radius * 0.55, x + radius * 0.55, y + radius * 0.55], fill=color)
    return base_img


def draw_finder(d, x, y, size, ring_color, inner_bg, dot_color, radius_scale=0.2):
    """QR finder: rounded ring square + inner square + dot."""
    r = size * radius_scale
    d.rounded_rectangle([x - size / 2, y - size / 2, x + size / 2, y + size / 2],
                        radius=r, outline=ring_color, width=max(8, int(size * 0.16)))
    inner = size * 0.42
    d.rounded_rectangle([x - inner / 2, y - inner / 2, x + inner / 2, y + inner / 2],
                        radius=r * 0.6, fill=inner_bg)
    dot = size * 0.18
    d.ellipse([x - dot / 2, y - dot / 2, x + dot / 2, y + dot / 2], fill=dot_color)


def save(img, name):
    img.save(f"{OUT}/{name}.png")
    print("saved", name, img.size)


# ---------- 1. ScanLine: dark navy, glowing cyan beam over QR ----------
def concept1():
    navy1, navy2 = (7, 12, 30), (16, 28, 58)
    cyan = (0, 230, 255)
    img = diag_gradient(S, navy1, navy2, rounded_mask(S, int(S * 0.22)))
    d = ImageDraw.Draw(img)

    # QR pattern (white-ish, part dimmed)
    cell, gap = 120, 16
    grid = [(i, j) for i in range(5) for j in range(5)
            if (i * j) % 3 != 1 or (i + j) % 2 == 0]
    cx, cy = S / 2, S / 2 - 40
    for (i, j) in grid:
        x = cx + (i - 2) * (cell + gap)
        y = cy + (j - 2) * (cell + gap)
        col = (210, 230, 255) if (i + j) % 4 == 0 else (150, 170, 210)
        d.rounded_rectangle([x - cell / 2, y - cell / 2, x + cell / 2, y + cell / 2],
                            radius=26, fill=col)

    # glowing horizontal scan beam
    beam = Image.new("RGBA", img.size, (0, 0, 0, 0))
    bd = ImageDraw.Draw(beam)
    by = cy
    bd.rounded_rectangle([cx - 400, by - 26, cx + 400, by + 26], radius=26, fill=cyan + (255,))
    beam = beam.filter(ImageFilter.GaussianBlur(30))
    beam2 = beam.filter(ImageFilter.GaussianBlur(60))
    img.alpha_composite(beam2)
    img.alpha_composite(beam)
    d = ImageDraw.Draw(img)
    d.rounded_rectangle([cx - 400, by - 10, cx + 400, by + 10], radius=10, fill=(240, 255, 255))
    # corner glow dots
    glow_dot(img, (cx - 400, by), 40, cyan, 40)
    glow_dot(img, (cx + 400, by), 40, cyan, 40)
    save(img, "concept-1-scanline")


# ---------- 2. Bolt: lightning cutting through QR ----------
def concept2():
    img = diag_gradient(S, (16, 24, 40), (30, 42, 70), rounded_mask(S, int(S * 0.22)))
    d = ImageDraw.Draw(img)
    amber = (255, 191, 0)

    # QR modules around the middle (leave diagonal lane empty)
    cell = 108
    cx, cy = S / 2, S / 2
    mods = [(-2, -2), (-1, -2), (2, -2), (2, -1), (-2, -1), (2, 1),
            (-2, 2), (1, 2), (2, 2), (-2, 1), (-1, 2)]
    for (i, j) in mods:
        x, y = cx + i * cell, cy + j * cell
        d.rounded_rectangle([x - cell / 2, y - cell / 2, x + cell / 2, y + cell / 2],
                            radius=22, fill=(88, 108, 168))

    # lightning bolt polygon (diagonal)
    bolt = [(470, 190), (700, 190), (560, 470), (670, 470), (350, 850), (430, 540), (330, 540)]
    shadow = [(x + 10, y + 14) for x, y in bolt]
    d.polygon(shadow, fill=(0, 0, 0, 120))
    d.polygon(bolt, fill=amber)
    # bolt sheen
    d.polygon([(470, 190), (700, 190), (620, 330), (500, 330)], fill=(255, 214, 80))
    # small spark dots
    for (sx, sy, r) in [(300, 300, 14), (740, 360, 12), (660, 780, 16), (380, 690, 10)]:
        d.ellipse([sx - r, sy - r, sx + r, sy + r], fill=(255, 214, 80))
    save(img, "concept-2-bolt")


# ---------- 3. OrbitQ: ring + pixel core ----------
def concept3():
    teal1, teal2 = (0, 150, 136), (0, 96, 100)
    img = diag_gradient(S, teal1, teal2, circle_mask(S))
    d = ImageDraw.Draw(img)

    cx = cy = S / 2
    # outer broken ring (orbital stroke)
    ring_r = 400
    w = 46
    for a0, a1 in [(200, 340), (20, 160)]:
        d.arc([cx - ring_r, cy - ring_r, cx + ring_r, cy + ring_r],
              a0, a1, fill=(255, 255, 255), width=w)
    # orbiting dot on ring
    ang = math.radians(-35)
    ox, oy = cx + ring_r * math.cos(ang), cy + ring_r * math.sin(ang)
    d.ellipse([ox - 34, oy - 34, ox + 34, oy + 34], fill=(255, 255, 255))
    d.ellipse([ox - 16, oy - 16, ox + 16, oy + 16], fill=teal2)

    # pixel-Q core: Q built from rounded modules
    cell = 88
    q = [(0, 0), (1, 0), (2, 0), (0, 1), (2, 1), (0, 2), (1, 2), (2, 2), (2, 3)]
    ox0, oy0 = cx - cell, cy - cell * 1.4
    for (i, j) in q:
        x, y = ox0 + i * cell, oy0 + j * cell
        d.rounded_rectangle([x - 34, y - 34, x + 34, y + 34], radius=16, fill=(255, 255, 255))
    save(img, "concept-3-orbitq")


# ---------- 4. Frame: viewfinder brackets + QR ----------
def concept4():
    img = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    indigo = (79, 70, 229)
    d.rounded_rectangle([0, 0, S - 1, S - 1], radius=int(S * 0.22), fill=(248, 248, 255))
    save_with_mask = rounded_mask(S, int(S * 0.22))

    cx = cy = S / 2
    # viewfinder corner brackets
    L, T = 190, 60   # arm length, thickness
    r = 30
    o = 330          # offset from center to corner block
    for (sx, sy) in [(-1, -1), (1, -1), (-1, 1), (1, 1)]:
        bx, by = cx + sx * o, cy + sy * o
        # horizontal arm
        d.rounded_rectangle([bx - (0 if sx < 0 else L), by - sy * (0),
                             bx + (L if sx < 0 else 0), by], radius=r, fill=indigo) \
            if False else None
        # draw arms explicitly
        hx0 = bx if sx < 0 else bx - L
        hy0 = by - T / 2
        d.rounded_rectangle([hx0, hy0, hx0 + L, hy0 + T], radius=r, fill=indigo)
        vy0 = by if sy < 0 else by - L
        vx0 = bx - T / 2
        d.rounded_rectangle([vx0, vy0, vx0 + T, vy0 + L], radius=r, fill=indigo)

    # center small QR
    cell = 70
    for (i, j, s) in [(-1, -1, 1.4), (1, -1, 1.4), (-1, 1, 1.4), (1, 1, 0.9), (0, 0, 0.7)]:
        x, y = cx + i * cell, cy + j * cell
        hh = cell * s / 2
        d.rounded_rectangle([x - hh, y - hh, x + hh, y + hh], radius=18, fill=indigo)
    img.putalpha(save_with_mask)
    save(img, "concept-4-frame")


# ---------- 5. Prism: gradient tiles QR ----------
def concept5():
    img = diag_gradient(S, (18, 18, 28), (28, 20, 44), rounded_mask(S, int(S * 0.22)))
    d = ImageDraw.Draw(img)

    grad_cols = [((255, 94, 158), (255, 153, 102)), ((124, 92, 255), (0, 210, 255)),
                 ((0, 200, 150), (180, 255, 120)), ((255, 196, 0), (255, 94, 120))]
    cell, gap = 150, 26
    cx = cy = S / 2
    tiles = [(-1, -1, 0), (0, -1, 1), (1, -1, 2),
             (-1, 0, 3), (1, 0, 0),
             (-1, 1, 1), (0, 1, 2), (1, 1, 3)]
    for (i, j, ci) in tiles:
        (c1, c2) = grad_cols[ci]
        x0 = cx + i * (cell + gap) - cell / 2
        y0 = cy + j * (cell + gap) - cell / 2
        tile = diag_gradient(cell, c1, c2, rounded_mask(cell, 40))
        img.alpha_composite(tile, (int(x0), int(y0)))

    # white module dots on top (glassy)
    for (x, y, r) in [(cx, cy, 46), (cx - cell - gap, cy, 34), (cx, cy - cell - gap, 34)]:
        d.ellipse([x - r, y - r, x + r, y + r], fill=(255, 255, 255))
    # sheen bar
    sheen = Image.new("RGBA", img.size, (0, 0, 0, 0))
    sd = ImageDraw.Draw(sheen)
    sd.polygon([(0, 0), (S * 0.55, 0), (0, S * 0.9)], fill=(255, 255, 255, 26))
    img.alpha_composite(Image.composite(sheen, Image.new("RGBA", img.size, (0, 0, 0, 0)),
                                        rounded_mask(S, int(S * 0.22))))
    save(img, "concept-5-prism")


# ---------- 6. Mono: single-color bold pixel Q ----------
def concept6():
    red = (232, 68, 52)
    img = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rounded_rectangle([0, 0, S - 1, S - 1], radius=int(S * 0.22), fill=red)

    cx, cy = S / 2, S / 2 - 30
    cell = 130
    # Q from blocky pixels (hole in middle) + tail
    ring_px = [(-1.5, -1.5), (-0.5, -1.5), (0.5, -1.5), (1.5, -1.5),
               (-1.5, -0.5), (1.5, -0.5),
               (-1.5, 0.5), (1.5, 0.5),
               (-1.5, 1.5), (-0.5, 1.5), (0.5, 1.5), (1.5, 1.5)]
    for (i, j) in ring_px:
        x, y = cx + i * cell, cy + j * cell
        d.rounded_rectangle([x - cell / 2, y - cell / 2, x + cell / 2, y + cell / 2],
                            radius=26, fill=(255, 255, 255))
    # tail (two diagonal blocks, Q leg)
    for k in (0, 1):
        x = cx + (1.6 + k * 0.85) * cell
        y = cy + (0.6 + k * 0.85) * cell
        d.rounded_rectangle([x - cell / 2, y - cell / 2, x + cell / 2, y + cell / 2],
                            radius=26, fill=(255, 255, 255))
    # inner accent: yellow pixel center
    d.rounded_rectangle([cx - cell / 2, cy - cell / 2, cx + cell / 2, cy + cell / 2],
                        radius=26, fill=(255, 200, 60))
    img.putalpha(rounded_mask(S, int(S * 0.22)))
    save(img, "concept-6-mono-q")


if __name__ == "__main__":
    concept1(); concept2(); concept3(); concept4(); concept5(); concept6()
    print("done")
