package com.travio.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.travio.app.ui.TripViewModel
import com.travio.app.ui.screens.BudgetScreen
import com.travio.app.ui.screens.HomeMapScreen
import com.travio.app.ui.screens.MoreScreen
import com.travio.app.ui.screens.PlanScreen
import com.travio.app.ui.theme.TravioTheme
import org.osmdroid.config.Configuration

sealed class Dest(val route: String, val label: String) {
    data object Home : Dest("home", "Start")
    data object Plan : Dest("plan", "Plan")
    data object Budget : Dest("budget", "Budżet")
    data object More : Dest("more", "Więcej")
}

class MainActivity : ComponentActivity() {

    private val viewModel: TripViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = packageName
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            TravioTheme(mode = themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TravioApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun TravioApp(viewModel: TripViewModel) {
    val navController = rememberNavController()
    val items = listOf(Dest.Home, Dest.Plan, Dest.Budget, Dest.More)

    // Gest/przycisk "wstecz" z dowolnej zakładki wraca na start, zamiast od razu zamykać
    // aplikację — standardowe zachowanie dolnej nawigacji.
    val backStackEntry by navController.currentBackStackEntryAsState()
    val onHome = backStackEntry?.destination?.hierarchy?.any { it.route == Dest.Home.route } == true
    BackHandler(enabled = !onHome) {
        navController.navigate(Dest.Home.route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                val currentRoute = backStackEntry?.destination
                items.forEach { dest ->
                    NavigationBarItem(
                        selected = currentRoute?.hierarchy?.any { it.route == dest.route } == true,
                        onClick = {
                            navController.navigate(dest.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(iconForDest(dest), contentDescription = dest.label) },
                        label = { Text(dest.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Dest.Home.route,
            modifier = Modifier.padding(padding),
            enterTransition = { fadeIn(animationSpec = androidx.compose.animation.core.tween(220)) + slideInHorizontally(animationSpec = androidx.compose.animation.core.tween(220)) { it / 8 } },
            exitTransition = { fadeOut(animationSpec = androidx.compose.animation.core.tween(180)) },
            popEnterTransition = { fadeIn(animationSpec = androidx.compose.animation.core.tween(220)) },
            popExitTransition = { fadeOut(animationSpec = androidx.compose.animation.core.tween(180)) + slideOutHorizontally(animationSpec = androidx.compose.animation.core.tween(180)) { it / 8 } }
        ) {
            composable(Dest.Home.route) { HomeMapScreen(viewModel) }
            composable(Dest.Plan.route) { PlanScreen(viewModel) }
            composable(Dest.Budget.route) { BudgetScreen(viewModel) }
            composable(Dest.More.route) { MoreScreen(viewModel) }
        }
    }
}

private fun iconForDest(dest: Dest) = when (dest) {
    Dest.Home -> Icons.Filled.Map
    Dest.Plan -> Icons.Filled.CalendarMonth
    Dest.Budget -> Icons.Filled.AccountBalanceWallet
    Dest.More -> Icons.Filled.MoreHoriz
}
