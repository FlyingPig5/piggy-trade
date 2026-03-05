# platform_setup.py

import os
import sys
import platform as _platform

# --- Platform Detection ---
IS_ANDROID = bool(
    os.environ.get('ANDROID_ROOT') or
    os.environ.get('ANDROID_DATA') or
    hasattr(sys, 'getandroidapilevel')
)

# PLATFORM: 'android' | 'windows' | 'linux' | 'darwin'
PLATFORM = "android" if IS_ANDROID else _platform.system().lower()

def apply_dbus_patch():
    """Apply the GTK DBus bypass patch to prevent Toga deadlocks on Linux."""
    try:
        import gi
        gi.require_version('Gtk', '3.0')
        from gi.repository import Gtk, Gio

        original_init = Gtk.Application.__init__

        def patched_init(self, *args, **kwargs):
            kwargs['flags'] = Gio.ApplicationFlags.NON_UNIQUE
            original_init(self, *args, **kwargs)

        Gtk.Application.__init__ = patched_init
    except (ImportError, ValueError):
        print("[piggytrade] GTK not found or not supported on this platform, skipping DBus patch.", flush=True)

def initialize_platform():
    """Runs all necessary platform setups."""
    apply_dbus_patch()
    print("[piggytrade] Platform initialized (native mode).", flush=True)
