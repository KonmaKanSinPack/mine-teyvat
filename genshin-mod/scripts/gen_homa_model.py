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
    cuboid([7.5, -12, 7.5], [8.5, 20.5, 8.5], SHAFT, "枪杆主段"),
    cuboid([7.3, -15, 7.3], [8.7, -12.5, 8.7], GOLD, "枪尾金箍"),
    cuboid([7.6, -16, 7.6], [8.4, -15, 8.4], DARK, "尾锥"),
    # 杆上金环（护摩杆身的分段装饰）
    cuboid([7.3, -4, 7.3], [8.7, -3.2, 8.7], GOLD, "金环1"),
    cuboid([7.3, 4, 7.3], [8.7, 4.8, 8.7], GOLD, "金环2"),
    cuboid([7.3, 12, 7.3], [8.7, 12.8, 8.7], GOLD, "金环3"),
    # ---- 枪头金座 ----
    cuboid([7.2, 20.5, 7.2], [8.8, 21.8, 8.8], GOLD, "枪头金环座"),

    # ---- 焰形刃：按 1 单位高的横切片堆叠（刃沿 Z 展开、X 向薄板）----
    # 结构语言：宽基座 → 浅镂空缺口（嵌宝石）→ 火焰鼓腹 → 裂为双角收尖
    cuboid([7.7, 21.8, 5.0], [8.3, 23.8, 14.0], BLADE, "刃基座（3单位高满宽）"),
    # 外侧暗红倒刺（基座中部向外探出）
    cuboid([7.6, 22.6, 4.4], [8.4, 24.6, 5.0], EDGE, "左倒刺"),
    cuboid([7.6, 22.6, 14.0], [8.4, 24.6, 14.6], EDGE, "右倒刺"),
    # 浅镂空缺口（只 1 单位高，z 7.5-10.5 留空，宝石嵌在缺口里）
    cuboid([7.7, 23.8, 5.0], [8.3, 24.8, 7.5], BLADE, "缺口左翼"),
    cuboid([7.7, 23.8, 10.5], [8.3, 24.8, 14.0], BLADE, "缺口右翼"),
    # 火焰鼓腹（缺口上方重新合拢，保持宽度）
    cuboid([7.7, 24.8, 5.5], [8.3, 25.8, 13.5], BLADE, "鼓腹"),
    cuboid([7.7, 25.8, 6.0], [8.3, 26.8, 13.0], BLADE, "鼓腹收窄"),
    # 双角分离
    cuboid([7.7, 26.8, 6.0], [8.3, 28.0, 8.0], BLADE, "左角根"),
    cuboid([7.7, 26.8, 10.0], [8.3, 28.0, 13.0], BLADE, "右角根"),
    cuboid([7.7, 28.0, 6.3], [8.3, 29.5, 7.6], BLADE, "左角中"),
    cuboid([7.7, 28.0, 10.5], [8.3, 29.5, 11.9], BLADE, "右角中"),
    cuboid([7.75, 29.5, 6.6], [8.25, 31.0, 7.4], EDGE, "左角尖暗缘"),
    cuboid([7.75, 29.5, 10.7], [8.25, 31.0, 11.6], EDGE, "右角尖暗缘"),
    # 角尖细刺（原版坐标上限 y=32）
    cuboid([7.8, 31.0, 6.8], [8.2, 32.0, 7.3], BLADE, "左角尖"),
    cuboid([7.8, 31.0, 10.9], [8.2, 32.0, 11.4], BLADE, "右角尖"),
    # 刃腹宝石（嵌在镂空缺口里，凸出刃面）
    cuboid([7.5, 23.9, 8.0], [8.5, 25.7, 9.4], GEM_DARK, "宝石底座"),
    cuboid([7.45, 24.5, 8.3], [8.55, 25.2, 9.1], GEM_PINK, "宝石粉芯"),
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
