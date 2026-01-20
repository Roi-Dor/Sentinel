package com.example.sentinelapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.sentinel_sdk.SecurityBadgeView
import com.example.sentinel.SecurityScanner
import com.example.sentinel_sdk.SentinelNetwork
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                SentinelScreen()
            }
        }
    }

    @Composable
    fun SentinelScreen() {
        // STATE: Holds the current score and button status
        var score by remember { mutableIntStateOf(100) } // Default to 100
        var isTransactionAllowed by remember { mutableStateOf(true) }
        var statusText by remember { mutableStateOf("Ready to Scan") }

        // FUNCTION: The logic to run the scan
        fun runScan() {
            // 1. Run SDK Logic
            val report = SecurityScanner.scan(this@MainActivity)

            // 2. Update State (Compose will re-draw automatically)
            score = report.score
            statusText = "Security Score: ${report.score}/100"
            isTransactionAllowed = (report.score >= 80)

            // 3. Send to Cloud
            SentinelNetwork.api.sendReport(report).enqueue(object : Callback<Void?> {
                override fun onResponse(call: Call<Void?>, response: Response<Void?>) {
                    Toast.makeText(this@MainActivity, "Report Sent!", Toast.LENGTH_SHORT).show()
                }
                override fun onFailure(call: Call<Void?>, t: Throwable) {
                    // Handle error
                }
            })
        }

        // Run scan once when the app opens
        LaunchedEffect(Unit) {
            runScan()
        }

        // UI LAYOUT
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // --- THE INTEROP BRIDGE ---
            // This displays your legacy View inside Compose
            AndroidView(
                modifier = Modifier.size(150.dp),
                factory = { context ->
                    SecurityBadgeView(context)
                },
                update = { view ->
                    // This block runs whenever 'score' changes
                    view.setScore(score)
                }
            )
            // --------------------------

            Spacer(modifier = Modifier.height(32.dp))

            Text(text = statusText, style = MaterialTheme.typography.headlineSmall)

            Spacer(modifier = Modifier.height(24.dp))

            // TRANSFER BUTTON
            Button(
                onClick = {
                    Toast.makeText(this@MainActivity, "Transfer Successful!", Toast.LENGTH_SHORT).show()
                },
                enabled = isTransactionAllowed,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isTransactionAllowed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            ) {
                Text(text = if (isTransactionAllowed) "Transfer $1,000" else "Transaction Blocked")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // REFRESH BUTTON
            OutlinedButton(
                onClick = { runScan() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Re-Scan System")
            }
        }
    }
}