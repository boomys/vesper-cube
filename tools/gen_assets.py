#!/usr/bin/env python3
"""Generates the mod's pixel art assets (entity texture, icon, spawn egg) with no dependencies."""
import os
import struct
import zlib

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

RED = (176, 21, 30, 255)
RED_DARK = (120, 12, 20, 255)
RED_LIGHT = (214, 48, 54, 255)
WHITE = (245, 245, 245, 255)
BLACK = (18, 12, 12, 255)
SKY = (126, 186, 235, 255)
SKY_DARK = (92, 152, 205, 255)


def png(path, width, height, pixels):
    """pixels: list of rows, each row a list of (r, g, b, a) tuples."""
    raw = b""
    for row in pixels:
        raw += b"\x00" + b"".join(struct.pack("BBBB", *px) for px in row)

    def chunk(tag, data):
        return (struct.pack(">I", len(data)) + tag + data
                + struct.pack(">I", zlib.crc32(tag + data) & 0xFFFFFFFF))

    header = struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0)
    blob = (b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", header)
            + chunk(b"IDAT", zlib.compress(raw, 9)) + chunk(b"IEND", b""))
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "wb") as handle:
        handle.write(blob)
    print("wrote", os.path.relpath(path, ROOT), f"{width}x{height}")


class Canvas:
    def __init__(self, width, height, fill):
        self.width = width
        self.height = height
        self.rows = [[fill for _ in range(width)] for _ in range(height)]

    def rect(self, x0, y0, x1, y1, color):
        for y in range(max(0, y0), min(self.height, y1)):
            for x in range(max(0, x0), min(self.width, x1)):
                self.rows[y][x] = color

    def pixels(self):
        return self.rows


def entity_texture():
    """128x64 cube texture net, cube of 32 units at u=0, v=0.

    Region layout for w=h=d=32:
      top    (32, 0)  bottom (64, 0)
      -X     ( 0,32)  front(-Z) (32,32)   +Z (64,32)   +X (96,32)
    """
    canvas = Canvas(128, 64, RED)

    # Soft shading so the flat red cube still reads as a volume.
    canvas.rect(0, 32, 32, 64, RED_DARK)        # -X side
    canvas.rect(96, 32, 128, 64, RED_DARK)      # +X side
    canvas.rect(64, 32, 96, 64, RED_DARK)       # +Z (back)
    canvas.rect(32, 0, 64, 32, RED_LIGHT)       # top
    canvas.rect(64, 0, 96, 32, RED_DARK)        # bottom

    # Face on the front (-Z) region: (32,32) - (64,64)
    eyes = ((37, 36, 45, 48), (51, 36, 59, 48))
    pupils = ((39, 39, 44, 47), (52, 39, 57, 47))
    for x0, y0, x1, y1 in eyes:
        canvas.rect(x0, y0, x1, y1, WHITE)
    for x0, y0, x1, y1 in pupils:
        canvas.rect(x0, y0, x1, y1, BLACK)
    canvas.rect(38, 53, 58, 61, BLACK)          # mouth
    canvas.rect(41, 55, 55, 58, (60, 30, 30, 255))
    return canvas


def icon():
    canvas = Canvas(128, 128, SKY)
    canvas.rect(0, 96, 128, 128, SKY_DARK)
    canvas.rect(16, 24, 112, 120, RED)
    canvas.rect(16, 24, 112, 36, RED_LIGHT)
    canvas.rect(32, 48, 56, 70, WHITE)
    canvas.rect(72, 48, 96, 70, WHITE)
    canvas.rect(38, 54, 50, 68, BLACK)
    canvas.rect(78, 54, 90, 68, BLACK)
    canvas.rect(36, 84, 92, 102, BLACK)
    return canvas


def spawn_egg():
    """16x16 spawn egg icon (1.21.5 spawn eggs are plain item models, not tinted)."""
    canvas = Canvas(16, 16, (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            nx = (x - 7.5) / 5.5
            ny = (y - 8.0) / 6.5
            if nx * nx + ny * ny <= 1.0:
                canvas.rows[y][x] = RED_DARK if 7 <= y <= 9 else RED
    for x, y in ((5, 5), (6, 5), (5, 6)):
        canvas.rows[y][x] = RED_LIGHT
    return canvas


def main():
    target = os.path.join(ROOT, "src/main/resources/assets/vespercube")
    texture = entity_texture()
    png(os.path.join(target, "textures/entity/red_cube.png"), texture.width, texture.height, texture.pixels())
    logo = icon()
    png(os.path.join(target, "icon.png"), logo.width, logo.height, logo.pixels())
    egg = spawn_egg()
    png(os.path.join(target, "textures/item/red_cube_spawn_egg.png"), egg.width, egg.height, egg.pixels())


if __name__ == "__main__":
    main()
