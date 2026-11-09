"""生成三把元素法杖的 16x16 占位贴图（原创占位图，不使用原神素材）。"""
from PIL import Image, ImageDraw
import os

OUT = "genshin-mod/src/main/resources/assets/teyvat_elements/textures/item"
os.makedirs(OUT, exist_ok=True)

WANDS = {
    "pyro_wand": (255, 90, 60),
    "hydro_wand": (60, 140, 255),
    "electro_wand": (177, 92, 255),
    "cryo_wand": (159, 216, 232),
    "anemo_wand": (90, 224, 200),
    "dendro_wand": (90, 200, 60),
    "geo_wand": (232, 181, 60),
}

for name, (r, g, b) in WANDS.items():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # 杖柄：从左下到中上的棕色斜线
    d.line([(3, 14), (10, 8)], fill=(101, 67, 33, 255), width=2)
    # 宝珠：深色描边外圈
    d.ellipse([8, 1, 15, 8], fill=(max(r - 80, 20), max(g - 80, 20), max(b - 80, 20), 255))
    # 宝珠主体
    d.ellipse([9, 2, 14, 7], fill=(r, g, b, 255))
    # 高光点
    d.point([(10, 3), (11, 2)], fill=(255, 255, 255, 230))
    img.save(f"{OUT}/{name}.png")
    print("saved", name)
