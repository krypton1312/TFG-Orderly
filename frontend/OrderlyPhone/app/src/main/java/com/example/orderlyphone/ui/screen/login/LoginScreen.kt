package com.example.orderlyphone.ui.screen.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    vm: LoginViewModel,
    onSuccess: () -> Unit
) {
    val state by vm.state.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(state) {
        if (state is LoginState.Success) onSuccess()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Staff Login", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { vm.login(email.trim(), password) },
            modifier = Modifier.fillMaxWidth(),
            enabled = state !is LoginState.Loading
        ) {
            if (state is LoginState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text("LOG IN")
        }

        if (state is LoginState.Error) {
            Spacer(Modifier.height(12.dp))
            Text(
                (state as LoginState.Error).message,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
