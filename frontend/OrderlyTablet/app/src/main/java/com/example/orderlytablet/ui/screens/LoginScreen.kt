package com.example.orderlytablet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Tablet design tokens — matches OrdersScreen palette
private val BgPage   = Color(0xFFF3F6FB)
private val BgCard   = Color.White
private val Accent   = Color(0xFFFFA100)
private val TextPri  = Color(0xFF1A1A2E)
private val TextSec  = Color(0xFF6B7280)
private val FieldBg  = Color(0xFFF9FAFB)
private val FieldBorder = Color(0xFFE5E7EB)
private val ErrorRed = Color(0xFFDC2626)

@Composable
fun LoginScreen(viewModel: LoginViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val isLoading = uiState is LoginUiState.Loading
    val errorMsg = (uiState as? LoginUiState.LoginError)?.message ?: ""

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPage),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.width(420.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = BgCard)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 40.dp, vertical = 44.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // ─── Logo / Title ────────────────────────────────────────────
                Text(
                    text = "Orderly",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPri
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Pantalla de cocina",
                    fontSize = 15.sp,
                    color = TextSec
                )

                Spacer(Modifier.height(36.dp))

                // ─── Email ───────────────────────────────────────────────────
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Correo electrónico", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPri)
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("admin@orderly.com", color = TextSec, fontSize = 14.sp) },
                        singleLine = true,
                        enabled = !isLoading,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Accent,
                            unfocusedBorderColor = FieldBorder,
                            focusedContainerColor = FieldBg,
                            unfocusedContainerColor = FieldBg
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(16.dp))

                // ─── Password ────────────────────────────────────────────────
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Contraseña", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPri)
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        singleLine = true,
                        enabled = !isLoading,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = TextSec
                                )
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Accent,
                            unfocusedBorderColor = FieldBorder,
                            focusedContainerColor = FieldBg,
                            unfocusedContainerColor = FieldBg
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // ─── Error ───────────────────────────────────────────────────
                if (errorMsg.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Text(errorMsg, color = ErrorRed, fontSize = 13.sp, modifier = Modifier.fillMaxWidth())
                } else {
                    Spacer(Modifier.height(10.dp))
                }

                Spacer(Modifier.height(14.dp))

                // ─── Button ──────────────────────────────────────────────────
                if (isLoading) {
                    CircularProgressIndicator(color = Accent, modifier = Modifier.size(40.dp))
                } else {
                    Button(
                        onClick = { viewModel.login(email, password) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Accent)
                    ) {
                        Text("Iniciar sesión", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }
            }
        }
    }
}

