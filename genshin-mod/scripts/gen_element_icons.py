"""生成元素头顶图标（32x32，原创几何图形，非原神素材）。

每个元素一个抽象符号：火=焰形、水=水滴、雷=折线闪电、草=叶、冰=雪花，
加复合元素：燃烧=双焰、冻结=冰晶方块、激化=叶+闪电叠印。
"""
from PIL import Image, ImageDraw
import math, os

OUT = "genshin-mod/src/main/resources/assets/teyvat_elements/textures/element"
os.makedirs(OUT, exist_ok=True)

S = 32  # 画布边长


def canvas():
    return Image.new("RGBA", (S, S), (0, 0, 0, 0))


def flame(color, inner):
    img = canvas()
    d = ImageDraw.Draw(img)
    # 焰体：底部圆 + 顶部尖角的多边形
    d.polygon([(16, 3), (24, 14), (25, 22), (16, 29), (7, 22), (8, 14)], fill=color)
    d.ellipse([9, 14, 23, 28], fill=color)
    # 内焰
    d.polygon([(16, 12), (20, 18), (20, 23), (16, 26), (12, 23), (12, 18)], fill=inner)
    return img


def droplet(color, inner):
    img = canvas()
    d = ImageDraw.Draw(img)
    d.polygon([(16, 3), (24, 16), (16, 16)], fill=color)
    d.polygon([(16, 3), (8, 16), (16, 16)], fill=color)
    d.ellipse([8, 12, 24, 28], fill=color)
    d.ellipse([12, 17, 20, 25], fill=inner)
    return img


def bolt(color):
    img = canvas()
    d = ImageDraw.Draw(img)
    d.polygon([(19, 2), (10, 17), (15, 17), (12, 30), (23, 13), (17, 13)], fill=color)
    return img


def leaf(color, vein):
    img = canvas()
    d = ImageDraw.Draw(img)
    # 叶形：两条弧线围成的梭形
    d.polygon([(16, 3), (26, 13), (16, 29), (6, 13)], fill=color)
    d.line([(16, 6), (16, 26)], fill=vein, width=2)
    return img


def snowflake(color):
    img = canvas()
    d = ImageDraw.Draw(img)
    cx = cy = 16
    for k in range(6):
        a = math.radians(k * 60)
        x, y = cx + 12 * math.cos(a), cy + 12 * math.sin(a)
        d.line([(cx, cy), (x, y)], fill=color, width=3)
    d.ellipse([13, 13, 19, 19], fill=color)
    return img


def diamond(color, inner):
    img = canvas()
    d = ImageDraw.Draw(img)
    d.polygon([(16, 3), (27, 16), (16, 29), (5, 16)], fill=color)
    d.polygon([(16, 9), (22, 16), (16, 23), (10, 16)], fill=inner)
    return img


ICONS = {
    "pyro": flame((255, 90, 60, 255), (255, 190, 80, 255)),
    "hydro": droplet((60, 140, 255, 255), (150, 210, 255, 255)),
    "electro": bolt((177, 92, 255, 255)),
    "dendro": leaf((90, 200, 60, 255), (30, 120, 30, 255)),
    "cryo": snowflake((159, 216, 232, 255)),
    "anemo": canvas(),  # 风不能附着，占位
    "geo": diamond((232, 181, 60, 255), (255, 220, 130, 255)),
    "burning": flame((255, 60, 30, 255), (255, 240, 120, 255)),
    "freeze": diamond((180, 255, 255, 255), (230, 255, 255, 255)),
    "quicken": leaf((1, 232, 88, 255), (180, 255, 200, 255)),
}

for name, img in ICONS.items():
    img.save(f"{OUT}/{name}.png")
    print("saved", name)
