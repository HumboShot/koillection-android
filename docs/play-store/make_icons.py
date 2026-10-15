"""Renders the Koillection launcher PNGs and Play Store graphics with no image library.

Run from the repository root:
    python docs/play-store/make_icons.py app/src/main/res docs/play-store

The adaptive icon for Android 8 and higher is the vector drawable ic_launcher_foreground.xml.
Change the glyph and colors there and here together.
"""
import math
import os
import re
import struct
import sys
import zlib

GLYPH = ("M20.54,5.23l-1.39,-1.68C18.88,3.21 18.47,3 18,3H6c-0.47,0 -0.88,0.21 -1.16,0.55L3.46,5.23"
         "C3.17,5.57 3,6.02 3,6.5V19c0,1.1 0.89,2 2,2h14c1.1,0 2,-0.9 2,-2V6.5c0,-0.48 -0.17,-0.93 -0.46,-1.27z"
         "M12,17.5L6.5,12H10v-2h4v2h3.5L12,17.5zM5.12,5l0.81,-1h12l0.94,1H5.12z")

TEAL = (0x00, 0x96, 0x88)
TEAL_LIGHT = (0x1A, 0xB0, 0xA2)
WHITE = (0xFF, 0xFF, 0xFF)


def parse_path(d):
    """Returns a list of closed polygons (lists of (x, y)) from an SVG path."""
    tokens = re.findall(r"[MmLlHhVvCcSsZz]|-?\d*\.?\d+(?:e-?\d+)?", d)
    polys, cur, i = [], [], 0
    x = y = sx = sy = 0.0
    cmd = None
    last_ctrl = None

    def num():
        nonlocal i
        v = float(tokens[i])
        i += 1
        return v

    def cubic(p0, p1, p2, p3):
        for k in range(1, 17):
            t = k / 16
            mt = 1 - t
            cur.append((mt ** 3 * p0[0] + 3 * mt * mt * t * p1[0] + 3 * mt * t * t * p2[0] + t ** 3 * p3[0],
                        mt ** 3 * p0[1] + 3 * mt * mt * t * p1[1] + 3 * mt * t * t * p2[1] + t ** 3 * p3[1]))

    while i < len(tokens):
        if re.match(r"[A-Za-z]", tokens[i]):
            cmd = tokens[i]
            i += 1
            if cmd in "Zz":
                if cur:
                    polys.append(cur)
                cur = []
                x, y = sx, sy
                last_ctrl = None
                continue
        rel = cmd.islower()
        c = cmd.upper()
        if c == "M":
            nx, ny = num(), num()
            x, y = (x + nx, y + ny) if rel else (nx, ny)
            if cur:
                polys.append(cur)
            cur = [(x, y)]
            sx, sy = x, y
            cmd = "l" if rel else "L"
            last_ctrl = None
        elif c == "L":
            nx, ny = num(), num()
            x, y = (x + nx, y + ny) if rel else (nx, ny)
            cur.append((x, y))
            last_ctrl = None
        elif c == "H":
            nx = num()
            x = x + nx if rel else nx
            cur.append((x, y))
            last_ctrl = None
        elif c == "V":
            ny = num()
            y = y + ny if rel else ny
            cur.append((x, y))
            last_ctrl = None
        elif c in "CS":
            if c == "C":
                x1, y1 = num(), num()
                if rel:
                    x1, y1 = x + x1, y + y1
            else:
                x1, y1 = (2 * x - last_ctrl[0], 2 * y - last_ctrl[1]) if last_ctrl else (x, y)
            x2, y2, x3, y3 = num(), num(), num(), num()
            if rel:
                x2, y2, x3, y3 = x + x2, y + y2, x + x3, y + y3
            cubic((x, y), (x1, y1), (x2, y2), (x3, y3))
            last_ctrl = (x2, y2)
            x, y = x3, y3
    if cur:
        polys.append(cur)
    return polys


def transform(polys, scale, dx, dy):
    return [[(px * scale + dx, py * scale + dy) for px, py in p] for p in polys]


def circle(cx, cy, r, n=180):
    return [[(cx + r * math.cos(2 * math.pi * k / n), cy + r * math.sin(2 * math.pi * k / n)) for k in range(n)]]


def rounded_rect(x0, y0, x1, y1, r, n=24):
    pts = []
    for cx, cy, a0 in ((x1 - r, y0 + r, -90), (x1 - r, y1 - r, 0), (x0 + r, y1 - r, 90), (x0 + r, y0 + r, 180)):
        for k in range(n + 1):
            a = math.radians(a0 + 90 * k / n)
            pts.append((cx + r * math.cos(a), cy + r * math.sin(a)))
    return [pts]


def coverage(polys, w, h, ss=4):
    """Anti-aliased coverage (0..1 per pixel) with the nonzero winding rule."""
    edges = []
    for p in polys:
        for k in range(len(p)):
            (xa, ya), (xb, yb) = p[k], p[(k + 1) % len(p)]
            if ya != yb:
                edges.append((xa, ya, xb, yb, 1 if yb > ya else -1))
    cov = [[0.0] * w for _ in range(h)]
    step = 1.0 / ss
    for row in range(h * ss):
        sy = (row + 0.5) * step
        hits = []
        for xa, ya, xb, yb, d in edges:
            lo, hi = (ya, yb) if ya < yb else (yb, ya)
            if lo <= sy < hi:
                hits.append((xa + (sy - ya) * (xb - xa) / (yb - ya), d))
        hits.sort()
        wind = 0
        line = cov[int(sy)]
        for k in range(len(hits) - 1):
            wind += hits[k][1]
            if wind == 0:
                continue
            a, b = max(hits[k][0], 0.0), min(hits[k + 1][0], float(w))
            if b <= a:
                continue
            pa, pb = int(a), min(int(b), w - 1)
            if pa == pb:
                line[pa] += (b - a) * step
                continue
            line[pa] += (pa + 1 - a) * step
            for px in range(pa + 1, pb):
                line[px] += step
            line[pb] += (b - pb) * step
    return cov


def compose(w, h, layers, background=None):
    px = [[list(background) + [255] if background else [0, 0, 0, 0] for _ in range(w)] for _ in range(h)]
    for polys, color in layers:
        cov = coverage(polys, w, h)
        for y in range(h):
            for x in range(w):
                a = min(cov[y][x], 1.0)
                if a <= 0:
                    continue
                r, g, b, da = px[y][x]
                da_f = da / 255
                out_a = a + da_f * (1 - a)
                for ch in range(3):
                    px[y][x][ch] = round((color[ch] * a + px[y][x][ch] * da_f * (1 - a)) / out_a)
                px[y][x][3] = round(out_a * 255)
    return px


def write_png(path, px, alpha=True):
    h, w = len(px), len(px[0])
    raw = bytearray()
    for row in px:
        raw.append(0)
        for p in row:
            raw.extend(p if alpha else p[:3])
    color_type = 6 if alpha else 2

    def chunk(kind, data):
        c = kind + data
        return struct.pack(">I", len(data)) + c + struct.pack(">I", zlib.crc32(c) & 0xFFFFFFFF)

    with open(path, "wb") as f:
        f.write(b"\x89PNG\r\n\x1a\n")
        f.write(chunk(b"IHDR", struct.pack(">IIBBBBB", w, h, 8, color_type, 0, 0, 0)))
        f.write(chunk(b"IDAT", zlib.compress(bytes(raw), 9)))
        f.write(chunk(b"IEND", b""))


def glyph_in(size, fraction):
    """The glyph centered in a square of [size], filling [fraction] of it (the glyph spans 18 of 24 units)."""
    scale = size * fraction / 18
    offset = size / 2 - 12 * scale
    return transform(parse_path(GLYPH), scale, offset, offset)


def main():
    res, store = sys.argv[1], sys.argv[2]
    os.makedirs(store, exist_ok=True)
    for name, size in (("mdpi", 48), ("hdpi", 72), ("xhdpi", 96), ("xxhdpi", 144), ("xxxhdpi", 192)):
        folder = os.path.join(res, f"mipmap-{name}")
        m = size / 48
        square = rounded_rect(2 * m, 2 * m, 46 * m, 46 * m, 8 * m)
        write_png(os.path.join(folder, "ic_launcher.png"), compose(size, size, [(square, TEAL), (glyph_in(size, 0.5), WHITE)]))
        write_png(os.path.join(folder, "ic_launcher_round.png"),
                  compose(size, size, [(circle(size / 2, size / 2, 22 * m), TEAL), (glyph_in(size, 0.5), WHITE)]))

    # Google Play masks the icon itself, so it is a full square without alpha.
    write_png(os.path.join(store, "play-icon-512.png"),
              compose(512, 512, [(glyph_in(512, 0.5), WHITE)], background=TEAL), alpha=False)

    glyph = transform(parse_path(GLYPH), 160 / 18, 512 - 12 * 160 / 18, 250 - 12 * 160 / 18)
    write_png(os.path.join(store, "feature-graphic-1024x500.png"),
              compose(1024, 500, [(circle(512, 250, 150), TEAL_LIGHT), (glyph, WHITE)], background=TEAL), alpha=False)


if __name__ == "__main__":
    main()
