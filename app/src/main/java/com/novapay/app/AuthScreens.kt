
package com.novapay.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val AuthInk = Color(0xFF080B12)
private val AuthPanel = Color(0xFF151C29)
private val AuthGold = Color(0xFFE5B94F)
private val AuthText = Color(0xFFF4F5F7)
private val AuthMuted = Color(0xFF9BA5B5)

@Composable
fun AuthScreen(
    authStorage: AuthStorage,
    onAuthenticated: (AuthUser) -> Unit
) {
    var mode by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    BackHandler(enabled = mode != 0) {
        message = ""
        mode = 0
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AuthInk
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(35.dp))

            Text(
                text = "NOVA PAY",
                color = AuthGold,
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "YOUR WORLD. ONE PAYMENT.",
                color = AuthMuted,
                fontSize = 11.sp,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(35.dp))

            when (mode) {
                0 -> {
                    Text(
                        text = "Bienvenue",
                        color = AuthText,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Accédez à votre espace NOVA PAY.",
                        color = AuthMuted,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    AuthButton(
                        text = "Créer un compte",
                        onClick = {
                            message = ""
                            mode = 1
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            message = ""
                            mode = 2
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Se connecter", color = AuthText)
                    }
                }

                1 -> {
                    Text(
                        text = "Créer un compte",
                        color = AuthText,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    AuthField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Nom complet"
                    )

                    AuthField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Adresse e-mail",
                        keyboardType = KeyboardType.Email
                    )

                    AuthField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = "Numéro de téléphone",
                        keyboardType = KeyboardType.Phone
                    )

                    AuthField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Mot de passe",
                        keyboardType = KeyboardType.Password,
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        onTogglePasswordVisibility = { passwordVisible = !passwordVisible }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Utilise au moins 10 caractères pour ton mot de passe.",
                        color = AuthMuted,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    if (message.isNotBlank()) {
                        Text(
                            text = message,
                            color = Color(0xFFFF7777),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    AuthButton(
                        text = "Créer mon compte",
                        onClick = {
                            if (name.isBlank() || !email.contains("@") || phone.isBlank() || password.length < 10) {
                                message = "Vérifie les informations. Le mot de passe doit contenir au moins 10 caractères."
                            } else {
                                loading = true
                                scope.launch {
                                    try {
                                        NovaSupabase.client.auth.signUpWith(Email, redirectUrl = NovaSupabase.AUTH_REDIRECT_URL) {
                                            this.email = email.trim()
                                            this.password = password
                                            data = buildJsonObject {
                                                put("display_name", name.trim())
                                                put("phone", phone.trim())
                                            }
                                        }
                                        val user = NovaSupabase.currentUser()
                                        if (user != null) {
                                            onAuthenticated(user)
                                        } else {
                                            message = "Compte créé. Vérifie ta boîte e-mail pour confirmer ton adresse, puis connecte-toi."
                                            mode = 2
                                        }
                                    } catch (error: Exception) {
                                        message = error.message?.take(180) ?: "Inscription impossible. Vérifie ta connexion et réessaie."
                                    } finally {
                                        loading = false
                                    }
                                }
                            }
                        }
                    )

                    TextButton(onClick = {
                        message = ""
                        mode = 0
                    }) {
                        Text("Retour", color = AuthMuted)
                    }
                }

                2 -> {
                    Text(
                        text = "Connexion",
                        color = AuthText,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    AuthField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Adresse e-mail",
                        keyboardType = KeyboardType.Email
                    )

                    AuthField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Mot de passe",
                        keyboardType = KeyboardType.Password,
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        onTogglePasswordVisibility = { passwordVisible = !passwordVisible }
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    if (message.isNotBlank()) {
                        Text(
                            text = message,
                            color = Color(0xFFFF7777),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    AuthButton(
                        text = "Se connecter",
                        onClick = {
                            if (!email.contains("@") || password.isBlank()) {
                                message = "Saisis une adresse e-mail et un mot de passe valides."
                            } else {
                                loading = true
                                scope.launch {
                                    try {
                                        NovaSupabase.client.auth.signInWith(Email) {
                                            this.email = email.trim()
                                            this.password = password
                                        }
                                        val user = NovaSupabase.currentUser()
                                        if (user != null) onAuthenticated(user)
                                        else message = "Connexion reçue, mais la session n'est pas encore disponible. Réessaie."
                                    } catch (error: Exception) {
                                        message = error.message?.take(180) ?: "E-mail ou mot de passe incorrect."
                                    } finally {
                                        loading = false
                                    }
                                }
                            }
                        }
                    )

                    TextButton(onClick = {
                        message = ""
                        mode = 0
                    }) {
                        Text("Retour", color = AuthMuted)
                    }
                }
            }

            Spacer(modifier = Modifier.height(35.dp))

            Text(
                text = "VERSION DE DÉMONSTRATION — AUCUN PAIEMENT RÉEL",
                color = AuthMuted,
                fontSize = 9.sp
            )
        }
    }
}

@Composable
private fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePasswordVisibility: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isPassword && !passwordVisible) {
            PasswordVisualTransformation()
        } else {
            androidx.compose.ui.text.input.VisualTransformation.None
        },
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { onTogglePasswordVisibility?.invoke() }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                        contentDescription = if (passwordVisible) "Masquer le mot de passe" else "Afficher le mot de passe",
                        tint = AuthMuted
                    )
                }
            }
        } else null,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = AuthText,
            unfocusedTextColor = AuthText,
            focusedBorderColor = AuthGold,
            unfocusedBorderColor = AuthMuted,
            focusedLabelColor = AuthGold,
            unfocusedLabelColor = AuthMuted,
            cursorColor = AuthGold
        )
    )
}

@Composable
private fun AuthButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AuthGold,
            contentColor = AuthInk
        )
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold
        )
    }
}
