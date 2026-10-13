package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AttendanceViewModel
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AttendanceScreen
import com.example.ui.screens.BarcodeGeneratorScreen
import com.example.ui.screens.ReportScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen()
            }
        }
    }
}

@Composable
fun MainAppScreen(
    viewModel: AttendanceViewModel = viewModel()
) {
    var selectedScreenIndex by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("main_navigation_bar")
            ) {
                NavigationBarItem(
                    selected = selectedScreenIndex == 0,
                    onClick = { selectedScreenIndex = 0 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = stringResource(R.string.nav_attendance),
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(R.string.nav_attendance),
                            fontSize = 11.sp,
                            fontWeight = if (selectedScreenIndex == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_item_attendance")
                )

                NavigationBarItem(
                    selected = selectedScreenIndex == 1,
                    onClick = { selectedScreenIndex = 1 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Dashboard,
                            contentDescription = stringResource(R.string.nav_admin),
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(R.string.nav_admin),
                            fontSize = 11.sp,
                            fontWeight = if (selectedScreenIndex == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_item_admin")
                )

                NavigationBarItem(
                    selected = selectedScreenIndex == 2,
                    onClick = { selectedScreenIndex = 2 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = stringResource(R.string.nav_barcode_gen),
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(R.string.nav_barcode_gen),
                            fontSize = 11.sp,
                            fontWeight = if (selectedScreenIndex == 2) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_item_barcode_gen")
                )

                NavigationBarItem(
                    selected = selectedScreenIndex == 3,
                    onClick = { selectedScreenIndex = 3 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = stringResource(R.string.nav_report),
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(R.string.nav_report),
                            fontSize = 11.sp,
                            fontWeight = if (selectedScreenIndex == 3) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_item_report")
                )
            }
        }
    ) { innerPadding ->
        when (selectedScreenIndex) {
            0 -> AttendanceScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            1 -> AdminDashboardScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            2 -> BarcodeGeneratorScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            3 -> ReportScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
