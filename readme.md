# 🛡️ Sentinel: Zero Trust Mobile Security SDK

**Sentinel** is a full-stack security platform designed to protect mobile applications from compromised environments. It allows Android developers to integrate "Zero Trust" checks (Root detection, Emulator detection, USB debugging) into their apps with a single line of code, while providing a centralized Cloud Dashboard for security teams to monitor threats in real-time.

---

## 🚀 Key Features

### 📱 Android SDK (Client)
* **Advanced Root Detection:** Identifies compromised OS kernels by detecting `su` binaries, `Superuser.apk`, and "test-keys" signatures used in developer images.
* **Environment Integrity:** Distinguishes between real physical devices and emulators (including advanced detection for "Google APIs" developer images).
* **Interface Security:**
    * **ADB Detection:** Flags active USB Debugging sessions that allow external tampering.
    * **Physical USB Detection:** Detects if the device is plugged into a data port (Computer) vs. a power source, mitigating "Juice Jacking" risks.
* **Sideload Detection:** Identifies apps installed via unofficial sources (not Google Play). *Currently configured as a warning (0 score penalty) for development testing.*
* **Real-time Scoring:** Generates a security score (0-100) and enables/disables sensitive UI features automatically.

### ☁️ Cloud Backend (Server)
* **REST API:** Python Flask server deployed on AWS EC2.
* **Database:** MongoDB Atlas for persistent storage of security reports.
* **Admin Portal:** A web-based dashboard for security officers to view live threats and manage/delete incident reports (CRUD).

---

## 🏗️ Architecture

1.  **The App:** Uses the `Sentinel SDK` to scan the device locally.
2.  **The Bridge:** Sends a JSON report (`device_id`, `score`, `risk_factors`) to the AWS Cloud.
3.  **The Core:** AWS EC2 Server processes the data, timestamps it, and sanitizes inputs.
4.  **The Vault:** MongoDB Atlas stores the immutable security record.
5.  **The View:** The Admin Portal fetches live data from AWS to visualize threat vectors in real-time.

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
4.  Ensure `android:usesCleartextTraffic="true"` is added to the App's `AndroidManifest.xml` (required for HTTP communication with the cloud).

### 3. Admin Portal Setup
1.  Open `admin.html` on your local machine.
2.  Update the `API_URL` const variable to match your AWS Public IP.
3.  Open the file in any web browser to view the dashboard.

---

## 💻 Usage

To use the scanner in your Android App:

```kotlin
// 1. Run the scan
val report = SecurityScanner.scan(context)

// 2. Use the data to protect the UI
if (report.score < 50) {
    // Block sensitive transaction
    transferButton.isEnabled = false
    statusText.text = "Environment Unsafe"
}

// 3. Send report to Cloud
SentinelNetwork.api.sendReport(report).enqueue(object : Callback<Void> {
    override fun onResponse(call: Call<Void>, response: Response<Void>) {
        Log.d("Sentinel", "Report uploaded successfully")
    }
    override fun onFailure(call: Call<Void>, t: Throwable) {
        Log.e("Sentinel", "Upload failed")
    }
})