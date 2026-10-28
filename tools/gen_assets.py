#!/usr/bin/env python3
"""Generates the mod's pixel art assets (entity textures, eggs, icon) with no dependencies.

Vesper and the boss each get a closed-mouth and an open-mouth texture; the
renderer swaps them while the entity is talking, which is what makes the mouth
"open" during speech.
"""
import os
import struct
import zlib

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

RED = (176, 21, 30, 255)
RED_DARK = (120, 12, 20, 255)
RED_LIGHT = (214, 48, 54, 255)
TYRANT = (44, 10, 52, 255)
TYRANT_DARK = (26, 5, 32, 255)
TYRANT_LIGHT = (68, 18, 78, 255)
WHITE = (245, 245, 245, 255)
BLACK = (18, 12, 12, 255)
SKY = (126, 186, 235, 255)
SKY_DARK = (92, 152, 205, 255)
MOUTH_INNER = (86, 16, 24, 255)


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


def email_skin(base, dark, light):
    """Base 128x64 texture net for a 32-unit cube, with flat side shading."""
    canvas = Canvas(128, 64, base)
    canvas.rect(0, 32, 32, 64, dark)        # -X side
    canvas.rect(96, 32, 128, 64, dark)      # +X side
    canvas.rect(64, 32, 96, 64, dark)       # +Z (back)
    canvas.rect(32, 0, 64, 32, light)       # top
    canvas.rect(64, 0, 96, 32, dark)        # bottom
    return canvas


def draw_face(canvas, open_mouth, eye_color=WHITE, pupil_color=BLACK):
    """Paints the face onto the front (-Z) region: (32,32) - (64,64)."""
    eyes = ((37, 36, 45, 48), (51, 36, 59, 48))
    pupils = ((39, 39, 44, 47), (52, 39, 57, 47))
    for x0, y0, x1, y1 in eyes:
        canvas.rect(x0, y0, x1, y1, eye_color)
    for x0, y0, x1, y1 in pupils:
        canvas.rect(x0, y0, x1, y1, pupil_color)

    if open_mouth:
        # Wide open maw with teeth: this is the "talking" frame.
        canvas.rect(36, 49, 60, 63, BLACK)
        canvas.rect(39, 52, 57, 61, MOUTH_INNER)
        canvas.rect(39, 50, 57, 53, WHITE)
        canvas.rect(39, 60, 57, 62, WHITE)
    else:
        canvas.rect(38, 53, 58, 61, BLACK)
        canvas.rect(41, 55, 55, 58, MOUTH_INNER)


def entity_texture(base, dark, light, open_mouth):
    canvas = email_skin(base, dark, light)
    draw_face(canvas, open_mouth)
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


def spawn_egg(base, dark, highlight):
    """16x16 spawn egg icon (1.21.5 spawn eggs are plain item models, not tinted)."""
    canvas = Canvas(16, 16, (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            nx = (x - 7.5) / 5.5
            ny = (y - 8.0) / 6.5
            if nx * nx + ny * ny <= 1.0:
                canvas.rows[y][x] = dark if 7 <= y <= 9 else base
    for x, y in ((5, 5), (6, 5), (5, 6)):
        canvas.rows[y][x] = highlight
    return canvas


def main():
    target = os.path.join(ROOT, "src/main/resources/assets/vespercube")

    for name, open_mouth in (("red_cube", False), ("red_cube_talking", True)):
        texture = entity_texture(RED, RED_DARK, RED_LIGHT, open_mouth)
        png(os.path.join(target, f"textures/entity/{name}.png"),
            texture.width, texture.height, texture.pixels())

    for name, open_mouth in (("cube_tyrant", False), ("cube_tyrant_talking", True)):
        texture = entity_texture(TYRANT, TYRANT_DARK, TYRANT_LIGHT, open_mouth)
        png(os.path.join(target, f"textures/entity/{name}.png"),
            texture.width, texture.height, texture.pixels())

    logo = icon()
    png(os.path.join(target, "icon.png"), logo.width, logo.height, logo.pixels())

    egg = spawn_egg(RED, RED_DARK, RED_LIGHT)
    png(os.path.join(target, "textures/item/red_cube_spawn_egg.png"),
        egg.width, egg.height, egg.pixels())

    boss_egg = spawn_egg(TYRANT, TYRANT_DARK, TYRANT_LIGHT)
    png(os.path.join(target, "textures/item/cube_tyrant_spawn_egg.png"),
        boss_egg.width, boss_egg.height, boss_egg.pixels())


if __name__ == "__main__":
    main()
