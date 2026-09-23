package com.palmah.cafe.amirtham

import androidx.compose.ui.window.ComposeUIViewController
import com.palmah.cafe.amirtham.auth.GoogleSignInHandler
import com.palmah.cafe.amirtham.auth.setGoogleSignInHandler
import platform.UIKit.UIViewController

fun MainViewController(googleSignInHandler: GoogleSignInHandler): UIViewController {
    setGoogleSignInHandler(googleSignInHandler)
    return ComposeUIViewController { App() }
}