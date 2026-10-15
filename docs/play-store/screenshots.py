"""Takes Google Play screenshots of Goshiki on a device or emulator, with private details hidden.

1. The app starts in screenshot mode (debug builds only). It then shows sample account
   details (username, email, server, avatar, currency and time zone) and colored placeholders
   instead of the images from the server.
2. Before each screenshot, the script reads the text on the screen. If any --hide word still
   shows, the script blurs that area and reports it.

The script only opens screens and goes back. It never taps buttons that change data.

Example, from the repository root, with a debug build installed and logged in:
    python docs/play-store/screenshots.py --device emulator-5554 --collection Movies --item "Wolf Children" --hide humboshot

The screenshots go to build/play-screenshots/. Check each one before you upload it.
"""
import argparse
import os
import re
import shutil
import struct
import subprocess
import sys
import time
import xml.etree.ElementTree as ET
import zlib

PACKAGE = "com.humboshot.koillection"
ACTIVITY = f"{PACKAGE}/.MainActivity"
# Taps on these labels could change data or end the session, so the script refuses them.
UNSAFE = {"save", "delete", "log out", "lend", "mark returned", "upload", "sign in", "unlock", "done", "change image"}


def find_adb():
    for sdk in (os.environ.get("ANDROID_HOME"), os.environ.get("ANDROID_SDK_ROOT"),
                os.path.join(os.environ.get("LOCALAPPDATA", ""), "Android", "Sdk")):
        if sdk:
            path = os.path.join(sdk, "platform-tools", "adb.exe" if os.name == "nt" else "adb")
            if os.path.exists(path):
                return path
    return shutil.which("adb") or sys.exit("adb not found. Set ANDROID_HOME or put adb on the PATH.")


class Device:
    def __init__(self, serial):
        self.serial = serial
        self.adb_path = find_adb()

    def adb(self, *args, binary=False):
        result = subprocess.run([self.adb_path, "-s", self.serial, *args], capture_output=True, check=True)
        return result.stdout if binary else result.stdout.decode("utf-8", "replace")

    def nodes(self):
        """The text, content description and bounds of each element on the screen."""
        # The dump fails while the screen still animates, so try a few times.
        for _ in range(10):
            self.adb("shell", "rm", "-f", "/sdcard/goshiki-ui.xml")
            self.adb("shell", "uiautomator", "dump", "/sdcard/goshiki-ui.xml")
            try:
                root = ET.fromstring(self.adb("exec-out", "cat", "/sdcard/goshiki-ui.xml"))
                break
            except ET.ParseError:
                time.sleep(0.5)
        else:
            sys.exit("Could not read the screen contents.")
        found = []
        for node in root.iter("node"):
            label = node.get("text") or node.get("content-desc") or ""
            x0, y0, x1, y1 = map(int, re.findall(r"\d+", node.get("bounds", "[0,0][0,0]")))
            found.append((label, (x0, y0, x1, y1)))
        return found

    def find(self, label, timeout=10.0):
        end = time.time() + timeout
        while time.time() < end:
            for text, bounds in self.nodes():
                if text == label:
                    return bounds
            time.sleep(0.5)
        return None

    def tap(self, label, timeout=10.0):
        if label.strip().lower() in UNSAFE:
            sys.exit(f"Refusing to tap '{label}': it could change data.")
        bounds = self.find(label, timeout)
        if bounds is None:
            sys.exit(f"Could not find '{label}' on the screen.")
        x0, y0, x1, y1 = bounds
        self.adb("shell", "input", "tap", str((x0 + x1) // 2), str((y0 + y1) // 2))

    def back(self):
        self.adb("shell", "input", "keyevent", "KEYCODE_BACK")

    def screenshot(self):
        return self.adb("exec-out", "screencap", "-p", binary=True)


def read_png(data):
    """Decodes an 8-bit RGB or RGBA PNG, as screencap makes it. Returns (width, height, rows of RGBA bytes)."""
    pos, chunks = 8, {}
    while pos < len(data):
        length, kind = struct.unpack(">I4s", data[pos:pos + 8])
        chunks.setdefault(kind, []).append(data[pos + 8:pos + 8 + length])
        pos += 12 + length
    width, height, depth, color, _, _, interlace = struct.unpack(">IIBBBBB", chunks[b"IHDR"][0])
    if depth != 8 or color not in (2, 6) or interlace:
        sys.exit("Unexpected screenshot format.")
    bpp = 4 if color == 6 else 3
    raw = zlib.decompress(b"".join(chunks[b"IDAT"]))
    stride = width * bpp
    rows, prev, i = [], bytearray(stride), 0
    for _ in range(height):
        kind, line = raw[i], bytearray(raw[i + 1:i + 1 + stride])
        i += 1 + stride
        for x in range(stride):
            a = line[x - bpp] if x >= bpp else 0
            b = prev[x]
            c = prev[x - bpp] if x >= bpp else 0
            if kind == 1:
                line[x] = (line[x] + a) & 0xFF
            elif kind == 2:
                line[x] = (line[x] + b) & 0xFF
            elif kind == 3:
                line[x] = (line[x] + (a + b) // 2) & 0xFF
            elif kind == 4:
                p = a + b - c
                pa, pb, pc = abs(p - a), abs(p - b), abs(p - c)
                line[x] = (line[x] + (a if pa <= pb and pa <= pc else b if pb <= pc else c)) & 0xFF
        prev = line
        if bpp == 3:
            line = bytearray(b"".join(line[k:k + 3] + b"\xff" for k in range(0, stride, 3)))
        rows.append(line)
    return width, height, rows


def write_png(path, width, height, rows):
    raw = b"".join(b"\x00" + bytes(row) for row in rows)

    def chunk(kind, body):
        return struct.pack(">I", len(body)) + kind + body + struct.pack(">I", zlib.crc32(kind + body) & 0xFFFFFFFF)

    with open(path, "wb") as f:
        f.write(b"\x89PNG\r\n\x1a\n")
        f.write(chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0)))
        f.write(chunk(b"IDAT", zlib.compress(raw, 6)))
        f.write(chunk(b"IEND", b""))


def pixelate(rows, box, block=24):
    """Replaces the area with large single-color blocks, so the text cannot be read."""
    x0, y0, x1, y1 = box
    for by in range(y0, y1, block):
        for bx in range(x0, x1, block):
            ys = range(by, min(by + block, y1))
            xs = range(bx, min(bx + block, x1))
            count = len(ys) * len(xs)
            avg = [sum(rows[y][x * 4 + ch] for y in ys for x in xs) // count for ch in range(3)]
            for y in ys:
                for x in xs:
                    rows[y][x * 4:x * 4 + 3] = bytes(avg)


def capture(device, name, hide, out_dir):
    time.sleep(1.5)
    leaks = [(text, box) for text, box in device.nodes() if any(word.lower() in text.lower() for word in hide)]
    png = device.screenshot()
    path = os.path.join(out_dir, f"{name}.png")
    if leaks:
        # Decoding in pure Python is slow, so only do it when something must be blurred.
        width, height, rows = read_png(png)
        for _, box in leaks:
            pixelate(rows, (max(box[0], 0), max(box[1], 0), min(box[2], width), min(box[3], height)))
        write_png(path, width, height, rows)
    else:
        with open(path, "wb") as f:
            f.write(png)
    note = f" - blurred {len(leaks)} area(s): " + ", ".join(repr(text) for text, _ in leaks) if leaks else ""
    print(f"{path}{note}")


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--device", required=True, help="adb serial, for example emulator-5554")
    parser.add_argument("--collection", required=True, help="name of a collection to open")
    parser.add_argument("--item", required=True, help="name of an item in that collection")
    parser.add_argument("--hide", action="append", default=[], help="text to blur if it shows (repeat as needed)")
    parser.add_argument("--out", default=os.path.join("build", "play-screenshots"))
    args = parser.parse_args()

    os.makedirs(args.out, exist_ok=True)
    device = Device(args.device)
    device.adb("shell", "am", "force-stop", PACKAGE)
    device.adb("shell", "am", "start", "-n", ACTIVITY, "--ez", "screenshot_mode", "true")
    if device.find("Collections", timeout=20) is None:
        sys.exit("The app did not show Collections. Log in first, and unlock the app if it asks.")

    capture(device, "1-collections", args.hide, args.out)
    device.tap(args.collection)
    device.find(args.item, timeout=15)
    capture(device, "2-collection", args.hide, args.out)
    device.tap(args.item)
    device.find("Edit item", timeout=15)
    capture(device, "3-item", args.hide, args.out)
    device.back()
    device.back()

    device.tap("Menu")
    capture(device, "4-menu", args.hide, args.out)
    device.tap("Statistics")
    device.find("Statistics", timeout=15)
    capture(device, "5-statistics", args.hide, args.out)

    device.tap("Menu")
    device.tap("collection.example.com")
    device.find("App settings", timeout=15)
    capture(device, "6-profile", args.hide, args.out)

    # Start the app again without screenshot mode.
    device.adb("shell", "am", "force-stop", PACKAGE)
    print("Done. Check each screenshot before you upload it.")


if __name__ == "__main__":
    main()
