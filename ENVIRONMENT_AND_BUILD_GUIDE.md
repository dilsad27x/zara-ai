# ZARA AI — Environment Inspection & Build Guide

## 1. On-Device Environment Inspection Report

An inspection of the local Android / Termux execution environment was conducted with the following results:

| Component | Status | Details |
| :--- | :--- | :--- |
| **Operating System** | Android (Linux Kernel 4.19.191+) | `aarch64` architecture, non-root Termux user `u0_a288` |
| **System Memory** | 3.7 GiB Total | ~940 MiB Available RAM, 2.5 GiB Swap |
| **Java Development Kit (JDK)** | **Not Available** | `java` / `javac` not installed; `JAVA_HOME` is unset |
| **Gradle** | **Not Available** | Local CLI `gradle` not installed |
| **Android SDK** | **Not Available** | `ANDROID_HOME` & `ANDROID_SDK_ROOT` unset; no SDK path exists |
| **Android Build Tools** | **Not Available** | `aapt2`, `d8`, `dx`, `zipalign`, `apksigner` not in `$PATH` |

---

## 2. On-Device Compilation Analysis & Limitations

### Why On-Device AGP Compilation Cannot Run Out-of-the-Box
1. **Missing Toolchain**: Neither OpenJDK 17 nor Android SDK command-line tools are pre-installed in this Termux environment.
2. **Android Gradle Plugin (AGP) Binary Architecture**:
   - The standard Android Gradle Plugin (AGP) automatically downloads pre-compiled x86_64 binaries (such as `aapt2`) from Google Maven repository.
   - On an `aarch64` Android phone running Termux, standard AGP fails when attempting to execute x86_64 ELF binaries unless Termux-specific patched `aapt2` or proot/qemu emulation layers are configured.
3. **Memory Footprint**:
   - Compiling Kotlin 2.0 with Jetpack Compose, Dagger Hilt code generation (KSP), and Room code generation requires significant RAM (~2.5 GiB JVM heap minimum). On a device with ~940 MiB available RAM, the Android system LMK (Low Memory Killer) frequently terminates the Gradle daemon during KSP/Kotlin compilation.

---

## 3. Recommended Build Pathways

### Pathway A: Automated Cloud / CI Build (Zero Setup Required on Phone)
A ready-to-run GitHub Actions workflow has been generated at [`.github/workflows/build.yml`](.github/workflows/build.yml).
- **Steps**:
  1. Push this repository to GitHub (`git remote add origin <repo-url> && git push -u origin master`).
  2. The workflow will automatically launch on Ubuntu with JDK 17, run unit tests, compile `app-debug.apk`, and attach it as an artifact.
  3. Download and install the APK directly on your phone.

### Pathway B: On-Device Compilation Setup (Termux / Proot)
If you wish to configure on-device compilation directly on this device, the following packages would need to be installed:
1. **Install OpenJDK 17**:
   ```bash
   pkg install openjdk-17
   ```
2. **Setup Android SDK Command-line Tools**:
   Download Android SDK command-line tools for Linux `aarch64` and set:
   ```bash
   export ANDROID_HOME=$HOME/Android/Sdk
   export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools
   ```
3. **Use aarch64 aapt2 override**:
   Add to `app/build.gradle.kts`:
   ```kotlin
   configurations.matching { it.name.contains("aapt2") }.all {
       resolutionStrategy.eachDependency {
           // Direct to aarch64 native binary if packaged in Termux
       }
   }
   ```

### Pathway C: Local Machine / Remote Server
You can clone or copy this repository to any computer or server with JDK 17:
```bash
./gradlew assembleDebug
```
The output APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`
