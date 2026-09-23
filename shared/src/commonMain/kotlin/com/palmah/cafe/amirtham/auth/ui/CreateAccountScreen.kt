package com.palmah.cafe.amirtham.auth.ui

import amirtham.shared.generated.resources.Res
import amirtham.shared.generated.resources.already_have_account
import amirtham.shared.generated.resources.brand_label
import amirtham.shared.generated.resources.create_account
import amirtham.shared.generated.resources.display_name_label
import amirtham.shared.generated.resources.email_label
import amirtham.shared.generated.resources.outlet_label
import amirtham.shared.generated.resources.password_label
import amirtham.shared.generated.resources.role_label
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.palmah.cafe.amirtham.auth.usecase.UserRole
import com.palmah.cafe.amirtham.auth.viewmodel.AuthViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CreateAccountScreen(
    viewModel: AuthViewModel = koinViewModel<AuthViewModel>(),
    onNavigateToSignIn: () -> Unit = {},
) {
    val screenState by viewModel.createAccountUiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(Res.string.create_account),
            style = MaterialTheme.typography.headlineSmall,
        )

        OutlinedTextField(
            value = screenState.brand,
            onValueChange = viewModel::onCreateAccountBrandChange,
            label = { Text(stringResource(Res.string.brand_label)) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
        )

        OutlinedTextField(
            value = screenState.outlet,
            onValueChange = viewModel::onCreateAccountOutletChange,
            label = { Text(stringResource(Res.string.outlet_label)) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
        )

        OutlinedTextField(
            value = screenState.displayName,
            onValueChange = viewModel::onCreateAccountDisplayNameChange,
            label = { Text(stringResource(Res.string.display_name_label)) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
        )

        OutlinedTextField(
            value = screenState.email,
            onValueChange = viewModel::onCreateAccountEmailChange,
            label = { Text(stringResource(Res.string.email_label)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
        )

        OutlinedTextField(
            value = screenState.password,
            onValueChange = viewModel::onCreateAccountPasswordChange,
            label = { Text(stringResource(Res.string.password_label)) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
        )

        var roleMenuExpanded by remember { mutableStateOf(false) }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
        ) {
            OutlinedButton(
                onClick = { roleMenuExpanded = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(Res.string.role_label, screenState.role.name))
            }
            DropdownMenu(
                expanded = roleMenuExpanded,
                onDismissRequest = { roleMenuExpanded = false },
            ) {
                UserRole.entries.forEach { role ->
                    DropdownMenuItem(
                        text = { Text(role.name) },
                        onClick = {
                            viewModel.onCreateAccountRoleChange(role)
                            roleMenuExpanded = false
                        },
                    )
                }
            }
        }

        screenState.errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 12.dp),
            )
        }

        Button(
            onClick = viewModel::createAccount,
            enabled = !screenState.isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
        ) {
            if (screenState.isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.padding(4.dp))
            } else {
                Text(stringResource(Res.string.create_account))
            }
        }

        TextButton(onClick = onNavigateToSignIn) {
            Text(stringResource(Res.string.already_have_account))
        }
    }
}
