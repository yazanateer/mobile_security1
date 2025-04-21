# 🔐 Yazecurity

**Yazecurity** is an advanced Android security-based login demo application developed for a Mobile Security course. It enforces dynamic, real-world physical conditions for login authentication — going beyond just a password.

This project demonstrates how contextual data from sensors and environment can be used to build secure mobile apps.

---

## 🚀 Features

✅ **Dynamic Login Authentication** based on real-time device context  
✅ **Custom Password** must match current battery percentage  
✅ **Screen Brightness** must match battery level  
✅ **Device Orientation** must face **South** (Azimuth ~180°)  
✅ **User Location** must be within **10km radius of Tel Aviv**  
✅ **Noise Detection** from microphone must exceed a threshold (unless emulator)  
✅ **Ambient Light** must be below a defined value (for "dark mode" condition)  
✅ **Stylized success dialog** (not new activity) when access is granted

---

## 🧠 Conditions Required for Login

To pass authentication, all of the following **must be true**:

| Condition                  | Validation Method                     |
|---------------------------|----------------------------------------|
| 📱 Password               | Must equal battery level (e.g. `87`)   |
| 🔋 Battery Level          | Fetched from `BatteryManager`          |
| 🌞 Screen Brightness      | Must match battery level (in %)        |
| 🧭 Facing South           | Compass must show Azimuth ≈ `180°`     |
| 🌍 Location               | Must be within 10km of Tel Aviv        |
| 🔊 Microphone Loudness    | Noise > 50 dB (simulated in emulator)  |
| 🌑 Darkness               | Ambient light sensor < 50 lux          |


---

## 🛠 Tech Stack

- Android SDK (Java)
- SensorManager
- MediaRecorder
- Google Location Services
- AlertDialog (custom layout)
- EdgeToEdge APIs
- Emulator/real device compatibility

---

## 🧪 Emulator Mode Support

If the app detects it’s running on an emulator, it will:
- Auto-return simulated mic data (65 dB)
- Allow testing of login conditions without physical sensors

> Emulator detection: Based on `Build.MODEL`, `PRODUCT`, `FINGERPRINT`, and `HARDWARE`.

