#!/usr/bin/env python3
"""Kleine Hilfe für den UI-Test im Emulator: tippen nach ID/Text, Screenshots, UI-Dumps."""
import re, subprocess, sys, time, xml.etree.ElementTree as ET

OUT = "shots"

def adb(*a, check=False):
    r = subprocess.run(["adb", *a], capture_output=True, text=True)
    return r.stdout + r.stderr

def dump(name=None):
    adb("shell", "uiautomator", "dump", "/sdcard/ui.xml")
    xml = adb("shell", "cat", "/sdcard/ui.xml")
    xml = xml[xml.find("<?xml"):] if "<?xml" in xml else xml
    if name:
        open(f"{OUT}/{name}.xml.txt", "w").write(xml)
    try:
        return ET.fromstring(xml)
    except Exception:
        return None

def center(node):
    x1, y1, x2, y2 = map(int, re.findall(r"\d+", node.get("bounds")))
    return (x1 + x2) // 2, (y1 + y2) // 2

def find(pred, idx=0):
    root = dump()
    if root is None:
        return None
    hits = [n for n in root.iter("node") if pred(n)]
    return hits[idx] if len(hits) > idx else None

def tap_node(n, label):
    if n is None:
        print(f"NICHT GEFUNDEN: {label}")
        return False
    x, y = center(n)
    adb("shell", "input", "tap", str(x), str(y))
    print(f"getippt: {label} @ {x},{y}")
    return True

def tap_id(rid, idx=0):
    return tap_node(find(lambda n: n.get("resource-id", "").endswith(rid), idx), f"id {rid}[{idx}]")

def tap_text(t, idx=0):
    return tap_node(find(lambda n: n.get("text", "") == t, idx), f"text '{t}'[{idx}]")

def longpress_id(rid, idx=0):
    n = find(lambda n: n.get("resource-id", "").endswith(rid), idx)
    if n is None:
        print(f"NICHT GEFUNDEN: {rid}"); return
    x, y = center(n)
    adb("shell", "input", "swipe", str(x), str(y), str(x), str(y), "1200")
    print(f"lang gedrückt: {rid}")

def shot(name):
    with open(f"{OUT}/{name}.png", "wb") as f:
        f.write(subprocess.run(["adb", "exec-out", "screencap", "-p"], capture_output=True).stdout)
    print(f"Screenshot: {name}")

def texts(name):
    root = dump(name)
    if root is None:
        return []
    t = [n.get("text") for n in root.iter("node") if n.get("text")]
    print(f"[{name}] Texte: {t}")
    return t

if __name__ == "__main__":
    cmd, *args = sys.argv[1:]
    {"tap_id": lambda: tap_id(args[0], int(args[1]) if len(args) > 1 else 0),
     "tap_text": lambda: tap_text(args[0], int(args[1]) if len(args) > 1 else 0),
     "longpress_id": lambda: longpress_id(args[0], int(args[1]) if len(args) > 1 else 0),
     "shot": lambda: shot(args[0]),
     "texts": lambda: texts(args[0])}[cmd]()
