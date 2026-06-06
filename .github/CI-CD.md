# NeuraLex CI/CD Pipeline

Welcome to the **NeuraLex** CI/CD pipeline documentation. This project uses **GitHub Actions** to automatically build and package the native Android app whenever changes are made.

---

## 🛠️ How the Build Pipeline Works

The build pipeline automates testing, quality checks (linting), and APK generation. When triggered, the runner performs the following phases:

1. **Environment Setup**:
   - Spins up a hosted `ubuntu-latest` running container.
   - Installs **JDK 17 (Temurin)**.
   - Installs **Node.js LTS** and configures automated caching for fast subsequent builds.

2. **Quality Assurance & Verification**:
   - Scans for JS-level configuration and checks if a `package.json` file is present to conditionally run JS Lint and Type-Checks.
   - Runs Android specific static code analysis (Linting) using the Gradle wrapper daemon.

3. **Compilation & Packaging**:
   - Grabs latest dependencies and compiles code.
   - Assembles the APK using `./gradlew assembleDebug` to build a clean debug package containing compiled classes, visual assets, and dynamic local resources.

4. **Artifact Archival**:
   - Saves the compiled `.apk` binary as a downloadable run artifact named `NeuraLex-apk`.

---

## 🔒 Required GitHub Secrets

To successfully build the application with advanced features or EAS credentials (if any hybrid modules are added or run within corresponding EAS frameworks), you can configure the following secrets in your repository settings under **Settings > Secrets and variables > Actions**:

| Secret Name | Purpose | Example / Requirements |
| --- | --- | --- |
| `GEMINI_API_KEY` | Injected into build properties to power real-time dictionary AI translations. | _Your Gemini API Key_ |
| `EXPO_TOKEN` | Required only if implementing EAS-based compilation (e.g., in hybrid modules). | `expo_access_token` |

---

## 📥 How to Download APK Artifacts

Every successful pipeline run produces a downloadable version of the application:

1. Navigate to the **Actions** tab at the top of your GitHub repository.
2. Select the latest run listed under **Android APK Build**.
3. Scroll down to the bottom of the run dashboard page to locate the **Artifacts** section.
4. Click on **NeuraLex-apk** to download the archive file containing the installable `app-debug.apk`.

---

## 🚀 How to Trigger Manual Builds

You can manually trigger a full build configuration at any time:

1. Navigate to the **Actions** tab on your GitHub repository.
2. Under the list on the left side, select the **Android APK Build** pipeline.
3. Locate the **Run workflow** dropdown menus on the right.
4. Select the target branch (typically `main`) and press the green **Run workflow** button.
5. The build will initiate immediately!
