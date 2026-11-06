"""Check G/H on an already-installed debug APK; never clears either app's data."""
import argparse
import json
import pathlib
import re
import subprocess
import time
import xml.etree.ElementTree as ET

parser = argparse.ArgumentParser()
parser.add_argument("--adb", default="adb")
parser.add_argument("--output", default="user-screenshots")
args = parser.parse_args()
output = pathlib.Path(args.output)
output.mkdir(parents=True, exist_ok=True)
PACKAGE = "com.comp1786.eventa.user"
EVENT = "Microsoft Ignite 2026 — AI cho doanh nghiệp"


def adb(*command):
    return subprocess.run([args.adb, *command], check=True, capture_output=True, timeout=40).stdout


def screenshot(name):
    (output / name).write_bytes(adb("exec-out", "screencap", "-p"))


def tree():
    adb("shell", "uiautomator", "dump", "/sdcard/eventa-user-smoke.xml")
    raw = adb("shell", "cat", "/sdcard/eventa-user-smoke.xml")
    (output / "latest-view.xml").write_bytes(raw)
    return ET.fromstring(raw)


def label(node):
    return " ".join((node.get("content-desc") or node.get("text", "")).split())


def bounds(node):
    return list(map(int, re.findall(r"\d+", node.get("bounds", ""))))


def find(name=None, *, kind=None, scroll=False):
    deadline = time.monotonic() + 35
    while time.monotonic() < deadline:
        try:
            root = tree()
        except subprocess.CalledProcessError:
            time.sleep(0.3)
            continue
        for node in root.iter("node"):
            text = label(node)
            matches = (text == name or (name == "Yêu thích" and re.fullmatch(r"\d+\s*Yêu thích", text))) if name else node.get("class") == kind
            box = bounds(node)
            if matches and len(box) == 4 and box[2] > box[0] and box[3] > box[1]:
                return node
        if scroll:
            width, height = size
            adb("shell", "input", "swipe", str(width // 2), str(int(height * .75)),
                str(width // 2), str(int(height * .28)), "300")
        else:
            time.sleep(0.3)
    raise AssertionError("Visible control missing: " + str(name or kind))


def tap(name=None, **options):
    node = find(name, **options)
    x1, y1, x2, y2 = bounds(node)
    adb("shell", "input", "tap", str((x1 + x2) // 2), str((y1 + y2) // 2))


def start():
    component = adb("shell", "cmd", "package", "resolve-activity", "--brief", PACKAGE).decode().strip().splitlines()[-1]
    assert "/" in component, "App must be installed"
    adb("shell", "am", "start", "-n", component)
    find("Khám phá sự kiện")


def saved_ids():
    try:
        names = adb("shell", "run-as", PACKAGE, "ls", "shared_prefs").decode().splitlines()
    except subprocess.CalledProcessError:
        return set()
    for name in names:
        if not name.endswith(".xml"):
            continue
        raw = adb("exec-out", "run-as", PACKAGE, "cat", "shared_prefs/" + name)
        for item in ET.fromstring(raw):
            if item.get("name") == "eventa.favourites.v1":
                return set(json.loads(item.text))
    return set()


size = tuple(map(int, re.findall(r"\d+", adb("shell", "wm", "size").decode())[-2:]))
try:
    adb("shell", "am", "force-stop", PACKAGE)
    start()
    find("5 sự kiện")
    screenshot("01-explore.png")
    print("Explore loaded.", flush=True)

    tap("Lọc theo chủ đề")
    find("Khám phá theo chủ đề")
    tap("Công nghệ")
    find("2 sự kiện")
    screenshot("02-category-and-no-cover.png")
    print("Category filter passed.", flush=True)
    tap("Lọc theo chủ đề")
    tap("Tất cả chủ đề")
    find("5 sự kiện")

    tap(kind="android.widget.EditText")
    adb("shell", "input", "text", "NVIDIA")
    find("2 sự kiện")
    assert label(find(kind="android.widget.EditText")) == "NVIDIA", "Fast typing preserves every character"
    adb("shell", "input", "keyevent", "4")
    screenshot("03-name-search.png")
    print("Name search passed.", flush=True)
    tap("Xóa từ khóa")
    find("5 sự kiện")

    tap("Chọn ngày")
    find("Bạn muốn đi ngày nào?")
    date_input = next(node for node in tree().iter("node") if node.get("resource-id") == "exact-date")
    x1, y1, x2, y2 = bounds(date_input)
    adb("shell", "input", "tap", str((x1 + x2) // 2), str((y1 + y2) // 2))
    root = tree()
    assert any("DatePicker" in node.get("class", "") for node in root.iter("node")), "Native date picker opens"
    adb("shell", "input", "keyevent", "4")
    adb("shell", "input", "keyevent", "4")
    find("5 sự kiện")
    print("Native date picker passed.", flush=True)

    tap("Xem sự kiện: " + EVENT)
    find("Về sự kiện")
    if "ignite-2026" not in saved_ids():
        tap("Lưu hoặc bỏ yêu thích sự kiện")
    assert "ignite-2026" in saved_ids(), "Favourite written to native storage"
    screenshot("04-event-detail.png")
    tap("Đóng thông báo")
    tap("Xem phiên: Demo: AI agents trong doanh nghiệp", scroll=True)
    find("Bạn sẽ khám phá điều gì?")
    screenshot("05-session-detail.png")
    adb("shell", "input", "keyevent", "4")
    find("Về sự kiện", scroll=True)
    tap("Yêu thích")
    find("Điều bạn quan tâm")
    find("Xem sự kiện: " + EVENT)
    screenshot("06-favourites.png")

    adb("shell", "am", "force-stop", PACKAGE)
    start()
    tap("Yêu thích")
    find("Xem sự kiện: " + EVENT)
    assert "ignite-2026" in saved_ids(), "Favourite survives process restart"
    tap("Bỏ yêu thích: " + EVENT)
    find("Dành một chỗ cho điều bạn thích")
    tap("Khám phá")
    tap("Xem sự kiện: " + EVENT)
    tap("Lưu hoặc bỏ yêu thích sự kiện")
    assert "ignite-2026" in saved_ids(), "Favourite can be added again"
    tap("Khám phá")
    screenshot("07-final-explore.png")
    print("Android G/H passed: category, search, date picker, details/session, favourites add/remove/restart.")
except Exception:
    screenshot("failure.png")
    raise
