"""搬运 Genshin Nature 中带模型的武器资产到本 mod。

复制：custom 模型 JSON（含升阶版）+ 贴图；生成物品包装模型（含 ascended 谓词 override）。
资产为 ARR（The Genshin Mineverse team），仅私人游玩，发布前须替换。
"""
import json
import os
import shutil

REF = ".tools/reference/genshin_nature/assets/genshin_nature"
DST = "genshin-mod/src/main/resources/assets/teyvat_elements"

WEAPONS = [
    "apprentices_notes", "beginners_protector", "blackcliff_pole", "dull_blade",
    "festering_desire", "hunters_bow", "iron_point", "kitain_cross_spear",
    "old_mercs_pal", "pocket_grimoire", "prototype_rancour", "serpent_spine",
    "silver_sword", "skyward_atlas", "skyward_harp", "vortex_vanquisher",
    "waster_greatsword",
]

os.makedirs(f"{DST}/models/custom", exist_ok=True)

for name in WEAPONS:
    # 模型 JSON（base + ascended）
    for suffix in ("", "_ascended"):
        src = f"{REF}/models/custom/{name}{suffix}.json"
        if os.path.exists(src):
            shutil.copy(src, f"{DST}/models/custom/{name}{suffix}.json")
        else:
            print("MISSING MODEL", src)
    # 贴图（textures/block/<name>.png）
    for suffix in ("", "_ascended"):
        src = f"{REF}/textures/block/{name}{suffix}.png"
        if os.path.exists(src):
            shutil.copy(src, f"{DST}/textures/item/{name}{suffix}.png")
        else:
            print("MISSING TEXTURE", src)

    # 升阶版包装模型
    asc_wrapper = {
        "credit": "引自 Genshin Nature 1.5.0（ARR，仅限私人游玩）",
        "parent": f"teyvat_elements:custom/{name}_ascended",
        "textures": {
            "0": f"teyvat_elements:item/{name}_ascended",
            "particle": f"teyvat_elements:item/{name}_ascended",
        },
    }
    with open(f"{DST}/models/item/{name}_ascended.json", "w", encoding="utf-8") as f:
        json.dump(asc_wrapper, f, ensure_ascii=False, indent=2)

    # 基础包装模型（带升阶 override）
    wrapper = {
        "credit": "引自 Genshin Nature 1.5.0（ARR，仅限私人游玩）",
        "parent": f"teyvat_elements:custom/{name}",
        "textures": {
            "0": f"teyvat_elements:item/{name}",
            "particle": f"teyvat_elements:item/{name}",
        },
        "overrides": [
            {
                "predicate": {"teyvat_elements:ascended": 1.0},
                "model": f"teyvat_elements:item/{name}_ascended",
            }
        ],
    }
    with open(f"{DST}/models/item/{name}.json", "w", encoding="utf-8") as f:
        json.dump(wrapper, f, ensure_ascii=False, indent=2)
    print("ported", name)

print("total:", len(WEAPONS))
