"""生成护摩之杖的 3D 物品模型（genshin-mod/src/main/resources/.../homa_staff.json）。

参考 Genshin Nature 的比例关系（全长 48 单位、扁平刃垂直于杆、刃沿 Z 展开），
几何体与贴图全部自绘——参考文件是 All Rights Reserved，不能直接复制。

调色板（16x16，每像素一色，模型 UV 按 [i,0,i+1,1] 引用）：
  0 杆深红  1 金饰  2 刃红  3 刃缘暗红  4 宝石深色  5 宝石粉  6 尾箍暗红
"""
import json
import os

RES = "genshin-mod/src/main/resources/assets/teyvat_elements"
TEX_PATH = "teyvat_elements:item/homa_staff"

SHAFT, GOLD, BLADE, EDGE, GEM_DARK, GEM_PINK, DARK = range(7)


def cuboid(frm, to, color, comment=""):
    uv = [color, 0, color + 1, 1]
    faces = {face: {"uv": uv, "texture": "#0"}
             for face in ("north", "south", "east", "west", "up", "down")}
    return {"comment": comment, "from": frm, "to": to, "faces": faces}


elements = [
    # ---- 枪杆 ----
    cuboid([7.5, -12, 7.5], [8.5, 20, 8.5], SHAFT, "枪杆主段"),
    cuboid([7.0, -16, 7.0], [9.0, -14, 9.0], GOLD, "枪尾金箍"),
    cuboid([7.3, -14, 7.3], [8.7, -12, 8.7], DARK, "尾箍暗纹"),
    # 杆上金环（护摩杆身的分段装饰）
    cuboid([7.3, -4, 7.3], [8.7, -3.2, 8.7], GOLD, "金环1"),
    cuboid([7.3, 4, 7.3], [8.7, 4.8, 8.7], GOLD, "金环2"),
    cuboid([7.3, 12, 7.3], [8.7, 12.8, 8.7], GOLD, "金环3"),
    # ---- 枪头 ----
    cuboid([6.9, 19.8, 6.9], [9.1, 21.8, 9.1], GOLD, "枪头金环座"),
    # 扁平刃沿 Z 轴展开（X 向薄）
    cuboid([7.7, 21.8, 5.0], [8.3, 28.0, 10.0], BLADE, "刃身主板"),
    cuboid([7.7, 28.0, 6.0], [8.3, 31.5, 9.0], BLADE, "刃身收窄"),
    cuboid([7.7, 31.5, 7.0], [8.3, 33.5, 8.0], BLADE, "刃尖"),
    cuboid([7.7, 22.5, 4.2], [8.3, 29.5, 5.0], EDGE, "刃前缘暗红倒刺"),
    cuboid([7.7, 20.8, 10.0], [8.3, 25.5, 13.5], BLADE, "刃后翼（护摩标志性的后展翼刃）"),
    cuboid([7.7, 25.5, 11.5], [8.3, 27.5, 12.5], EDGE, "后翼暗红尾梢"),
    # 刃腹宝石（凸出刃面，深色底 + 粉色芯）
    cuboid([7.5, 23.5, 6.8], [8.5, 25.5, 8.2], GEM_DARK, "宝石底座"),
    cuboid([7.4, 24.0, 7.2], [8.6, 25.0, 7.8], GEM_PINK, "宝石粉芯"),
]

model = {
    "credit": "Teyvat Elements 原创（比例参考 Genshin Nature，几何与贴图自绘）",
    "texture_size": [16, 16],
    "textures": {"0": TEX_PATH, "particle": TEX_PATH},
    "elements": elements,
    "display": {
        "thirdperson_righthand": {"translation": [-0.5, 4.75, 0], "scale": [0.8, 0.8, 0.8]},
        "thirdperson_lefthand": {"translation": [0.5, 4.75, 0], "scale": [0.8, 0.8, 0.8]},
        "firstperson_righthand": {"rotation": [0, 5, 0], "translation": [2.25, -3, -3], "scale": [0.8, 0.8, 0.8]},
        "firstperson_lefthand": {"rotation": [-180, -1, -180], "translation": [3, -3, -1], "scale": [0.8, 0.8, 0.8]},
        "ground": {"rotation": [0, 0, 90], "scale": [0.8, 0.8, 0.8]},
        "gui": {"rotation": [106, 60, -114], "translation": [-0.75, 0.25, 0], "scale": [0.36, 0.36, 0.36]},
        "head": {"rotation": [90, 0, -90], "translation": [-0.75, 3.5, 0]},
        "fixed": {"rotation": [0, 90, 0], "translation": [0, 0, -0.5], "scale": [0.5, 0.5, 0.5]},
    },
}

out = os.path.join(RES, "models/item/homa_staff.json")
with open(out, "w", encoding="utf-8") as f:
    json.dump(model, f, ensure_ascii=False, indent=2)
print("model elements:", len(elements), "->", out)

# ---- 调色板贴图 ----
from PIL import Image

palette = [
    (96, 32, 28, 255),     # 0 杆深红
    (216, 175, 105, 255),  # 1 金饰
    (226, 60, 60, 255),    # 2 刃红
    (140, 30, 32, 255),    # 3 刃缘暗红
    (40, 24, 24, 255),     # 4 宝石深色
    (244, 150, 130, 255),  # 5 宝石粉
    (60, 20, 18, 255),     # 6 尾箍暗红
]
img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
for i, c in enumerate(palette):
    img.putpixel((i, 0), c)
img.save(os.path.join(RES, "textures/item/homa_staff.png"))
print("palette texture saved")
