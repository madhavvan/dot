package com.example.Autply

import android.annotation.TargetApi
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private const val REQUEST_CODE_OVERLAY_PERMISSION = 1001

class MainActivity : ComponentActivity() {
    private lateinit var overlayPermissionLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        overlayPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(
                    this,
                    "Overlay permission is required for the app to function",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            requestOverlayPermission()
        }

        setContent {
            MainScreen(packageManager)
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        Toast.makeText(this, "Please grant overlay permission", Toast.LENGTH_LONG).show()
        overlayPermissionLauncher.launch(intent)
    }
}

@Composable
fun MainScreen(packageManager: PackageManager) {
    var showAppList by remember { mutableStateOf(false) }
    var incomingMessage by remember { mutableStateOf("") }
    var suggestedReply by remember { mutableStateOf("") }
    val context = LocalContext.current

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (!showAppList) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add App",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        TextField(
                            value = incomingMessage,
                            onValueChange = { incomingMessage = it },
                            label = { Text("Enter Incoming Message") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        )
                        Button(
                            onClick = {
                                val responseMap = mapOf(
                                    "hi" to "Hi, how are you?",
                                    "hello" to "Hello! How's your day?",
                                    "bye" to "Goodbye! Take care!"
                                )
                                suggestedReply = responseMap[incomingMessage.lowercase()] ?: "Sorry, I don't understand."
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text("Generate Reply")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Suggested Reply: $suggestedReply",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("https://wa.me/?text=${Uri.encode(suggestedReply)}")
                                }
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary
                            )
                        ) {
                            Text("Send Reply")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showAppList = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text("Add Social App", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                } else {
                    AppListScreen(packageManager = packageManager, onClose = { showAppList = false })
                }
            }
        }
    }
}

@Composable
fun AppListScreen(packageManager: PackageManager, onClose: () -> Unit) {
    val context = LocalContext.current
    val socialAppPackages = listOf(
        "com.whatsapp",
        "com.whatsapp.w4b",
        "com.instagram.android",
        "com.facebook.katana",
        "com.twitter.android",
        "com.facebook.orca"
    )

    val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        .filter { it.packageName in socialAppPackages }
        .map { appInfo ->
            packageManager.getApplicationLabel(appInfo).toString() to appInfo.packageName
        }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(onClick = { onClose() }, modifier = Modifier.padding(8.dp)) {
            Text("Back")
        }

        Text(
            text = "Available Apps",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        LazyColumn {
            items(installedApps) { (appName, packageName) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable {
                            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
                            launchIntent?.let {
                                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(it)
                            }
                        },
                    colors = CardDefaults.cardColors(containerColor = Color.LightGray),
                    elevation = CardDefaults.cardElevation(5.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(text = appName, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}
