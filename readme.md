# 🛡️ Sentinel: Zero Trust Mobile Security SDK

**Sentinel** is a full-stack security platform designed to protect mobile applications from compromised environments. It allows Android developers to integrate "Zero Trust" checks (Root detection, Emulator detection, USB debugging) into their apps with a single line of code, while providing a centralized Cloud Dashboard for security teams to monitor threats in real-time.

---

## 🚀 Key Features

### 📱 Android SDK (Client)
* **Root Detection:** Detects `su` binaries and test-keys to identify compromised OS kernels.
* **Environment Integrity:** Identifies if the app is running on an Emulator or a real device.
* **Interface Security:** Detects active USB Debugging (ADB) sessions which allow external tampering.
* **Sideload Detection:** Flags apps installed via unofficial sources (not Google Play).
* **Real-time Scoring:** Generates a security score (0-100) and enables/disables sensitive UI features automatically.

### ☁️ Cloud Backend (Server)
* **REST API:** Python Flask server deployed on AWS EC2.
* **Database:** MongoDB Atlas for persistent storage of security reports.
* **Admin Portal:** A web-based dashboard for security officers to view live threats and manage/delete incident reports (CRUD).

---

## 🏗️ Architecture

1.  **The App:** Uses the `Sentinel SDK` to scan the device.
2.  **The Bridge:** Sends a JSON report (`device_id`, `score`, `risk_factors`) to the AWS Cloud.
3.  **The Core:** AWS EC2 Server processes the data and timestamps it.
4.  **The Vault:** MongoDB Atlas stores the record.
5.  **The View:** The Admin Portal fetches data from AWS to display threat analytics.

---

## 🛠️ Configuration & Setup

### Prerequisites
* Android Studio (Ladybug or newer)
* Python 3.9+
* AWS EC2 Instance (Ubuntu)
* MongoDB Atlas Account

### 1. Backend Setup (AWS)
1.  SSH into your EC2 instance.
2.  Clone the repository and navigate to `backend`.
3.  Create a `.env` file with your MongoDB Connection String:
    ```bash
    MONGO_URI=mongodb+srv://<user>:<password>@cluster0.mongodb.net/?retryWrites=true&w=majority
    ```
4.  Activate the virtual environment and install dependencies:
    ```bash
    source venv/bin/activate
    pip install flask pymongo flask-cors python-dotenv
    ```
5.  Start the server in the background:
    ```bash
    nohup flask run --host=0.0.0.0 > server.log 2>&1 &
    ```

### 2. Android Setup
1.  Open the project in Android Studio.
2.  Open `SentinelNetwork.kt` in the SDK module.
3.  Update the `BASE_URL` to your AWS Public IP:
    ```kotlin
    private const val BASE_URL = "http://YOUR_AWS_IP:5000/"
    ```
4.  Ensure `android:usesCleartextTraffic="true"` is added to the App's `AndroidManifest.xml` (required for HTTP communication).

### 3. Admin Portal Setup
1.  Open `admin.html` locally.
2.  Update the `API_URL` const variable to match your AWS IP.
3.  Open the file in any web browser to view the dashboard.

---

## 💻 Usage

To use the scanner in your Android App:

```kotlin
// 1. Run the scan
val report = SecurityScanner.scan(context)

// 2. Use the data
if (report.score < 50) {
    // Block sensitive transaction
    transferButton.isEnabled = false
}

// 3. Send to Cloud
SentinelNetwork.api.sendReport(report).enqueue(...)