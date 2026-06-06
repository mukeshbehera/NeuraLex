# NeuraLex

AI-powered modern dictionary with Gen Z translations and contextual explanations.

---

## 📱 Android APK Build

This repository is equipped with a **GitHub Actions CI/CD workflow** that automatically builds and packages the application into installable APK files. This ensures rapid testing cycles and high code reliability.

### ⚡ Automatic APK Generation
The pipeline runs automatically on every:
* **Push** to the `main` branch
* **Pull Request** targeting the `main` branch
* Handled manually via the Github environment using **Workflow Dispatch**

### 📦 Artifact Download Process
To download the generated APK:
1. Go to the **Actions** tab on your GitHub repository page.
2. Click on the latest workflow run styled under **Android APK Build**.
3. Scroll down to the **Artifacts** section at the bottom of the page.
4. Click **NeuraLex-apk** to download the compiled `.apk` file.

### ⚙️ Manual Workflow Execution
If you need to trigger a clean build on-demand:
1. Click on the **Actions** tab.
2. Select the **Android APK Build** workflow from the panel on the left.
3. Click the **Run workflow** button, choose your branch (e.g., `main`), and confirm.

---

### 🔑 GitHub Secrets Required

If incorporating Expo or external hybrid EAS dependencies, configure the following secrets under your repository's actions secret environment:

```env
EXPO_TOKEN=<your_expo_access_token>
```
Additionally, `GEMINI_API_KEY` may be configured to supply API authentication tokens during compilation.
