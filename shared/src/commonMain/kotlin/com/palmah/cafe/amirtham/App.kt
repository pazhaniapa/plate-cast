package com.palmah.cafe.amirtham

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.palmah.cafe.amirtham.auth.viewmodel.AuthUiState
import com.palmah.cafe.amirtham.auth.viewmodel.AuthViewModel
import com.palmah.cafe.amirtham.auth.ui.CreateAccountScreen
import com.palmah.cafe.amirtham.auth.ui.SignInScreen
import com.palmah.cafe.amirtham.home.HomeScreen
import com.palmah.cafe.amirtham.navigation.Route
import com.palmah.cafe.amirtham.ui.theme.AmirthamTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun App() {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components { add(KtorNetworkFetcherFactory()) }
            .build()
    }

    AmirthamTheme {
        val authViewModel = koinViewModel<AuthViewModel>()
        val authState by authViewModel.uiState.collectAsState()

        if (authState is AuthUiState.Loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val navController = rememberNavController()

            LaunchedEffect(authState) {
                val destination = if (authState is AuthUiState.SignedIn) Route.Home else Route.SignIn
                navController.navigate(destination) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }

            NavHost(navController = navController, startDestination = Route.SignIn) {
                composable<Route.SignIn> {
                    SignInScreen(
                        viewModel = authViewModel,
                        onNavigateToCreateAccount = { navController.navigate(Route.CreateAccount) },
                    )
                }
                composable<Route.CreateAccount> {
                    CreateAccountScreen(
                        viewModel = authViewModel,
                        onNavigateToSignIn = { navController.popBackStack() },
                    )
                }
                composable<Route.Home> {
                    val signedInState = authState as? AuthUiState.SignedIn
                    if (signedInState != null) {
                        HomeScreen(signedInState.user, onSignOut = authViewModel::signOut)
                    }
                }
            }
        }
    }
}
