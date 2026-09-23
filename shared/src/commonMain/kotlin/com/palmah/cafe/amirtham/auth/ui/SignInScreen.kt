package com.palmah.cafe.amirtham.auth.ui

import amirtham.shared.generated.resources.Res
import amirtham.shared.generated.resources.app_name
import amirtham.shared.generated.resources.app_tagline
import amirtham.shared.generated.resources.chef_logo
import amirtham.shared.generated.resources.continue_label
import amirtham.shared.generated.resources.google_logo
import amirtham.shared.generated.resources.hide_password
import amirtham.shared.generated.resources.ic_visibility
import amirtham.shared.generated.resources.ic_visibility_off
import amirtham.shared.generated.resources.no_account_yet
import amirtham.shared.generated.resources.or_divider
import amirtham.shared.generated.resources.password_label
import amirtham.shared.generated.resources.show_password
import amirtham.shared.generated.resources.sign_in_hero
import amirtham.shared.generated.resources.sign_in_subtitle
import amirtham.shared.generated.resources.sign_in_with_google
import amirtham.shared.generated.resources.username_label
import amirtham.shared.generated.resources.welcome_back
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.palmah.cafe.amirtham.auth.rememberGoogleSignInHandler
import com.palmah.cafe.amirtham.auth.viewmodel.AuthViewModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SignInScreen(
    viewModel: AuthViewModel = koinViewModel<AuthViewModel>(),
    onNavigateToCreateAccount: () -> Unit = {},
) {
    val googleSignInHandler = rememberGoogleSignInHandler()
    val screenState by viewModel.signInScreenUiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        /*Image(
            painter = painterResource(Res.drawable.chef_logo),
            contentDescription = null,
            modifier = Modifier.size(96.dp),
        )*/

        Text(
            text = stringResource(Res.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 16.dp),
        )

        Text(
            text = stringResource(Res.string.app_tagline),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )

        Image(
            painter = painterResource(Res.drawable.sign_in_hero),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .padding(top = 24.dp)
                .fillMaxWidth()
                .aspectRatio(540f / 346f)
                .clip(RoundedCornerShape(16.dp)),
        )

        Text(
            text = stringResource(Res.string.welcome_back),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 40.dp),
        )

        Text(
            text = stringResource(Res.string.sign_in_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(),
        )

        OutlinedTextField(
            value = screenState.email,
            onValueChange = viewModel::onEmailChange,
            label = { Text(stringResource(Res.string.username_label)) },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
        )

        OutlinedTextField(
            value = screenState.password,
            onValueChange = viewModel::onPasswordChange,
            label = { Text(stringResource(Res.string.password_label)) },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        painter = painterResource(
                            if (passwordVisible) Res.drawable.ic_visibility_off else Res.drawable.ic_visibility,
                        ),
                        contentDescription = stringResource(
                            if (passwordVisible) Res.string.hide_password else Res.string.show_password,
                        ),
                    )
                }
            },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
        )

        screenState.errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 12.dp),
            )
        }

        Button(
            onClick = viewModel::submit,
            enabled = !screenState.isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
        ) {
            if (screenState.isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.padding(4.dp))
            } else {
                Text(stringResource(Res.string.continue_label))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }

        TextButton(onClick = onNavigateToCreateAccount, modifier = Modifier.padding(top = 4.dp)) {
            Text(stringResource(Res.string.no_account_yet))
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        ) {
            HorizontalDivider(modifier = Modifier.padding(end = 12.dp).weight(1f))
            Text(
                text = stringResource(Res.string.or_divider),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            HorizontalDivider(modifier = Modifier.padding(start = 12.dp).weight(1f))
        }

        OutlinedButton(
            onClick = { viewModel.signInWithGoogle(googleSignInHandler) },
            enabled = !screenState.isSubmitting,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                painter = painterResource(Res.drawable.google_logo),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = stringResource(Res.string.sign_in_with_google),
                modifier = Modifier.padding(start = 12.dp),
            )
        }
    }
}
