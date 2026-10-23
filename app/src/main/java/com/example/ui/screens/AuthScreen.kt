package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoPrimary
import com.example.ui.viewmodel.HabitViewModel

@Composable
fun AuthScreen(viewModel: HabitViewModel) {
    var selectedAuthTab by remember { mutableStateOf(0) }
    val authMethods = listOf("Email", "Google", "Phone")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Logo & Brand Header
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text("🔥", fontSize = 36.sp)
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Welcome to HabitFlow",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Log in to sync your habits, streaks, and calendar",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Auth Method Selector TabRow
        TabRow(
            selectedTabIndex = selectedAuthTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .fillMaxWidth()
                .testTag("auth_tab_row")
        ) {
            authMethods.forEachIndexed { index, title ->
                Tab(
                    selected = selectedAuthTab == index,
                    onClick = { selectedAuthTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedAuthTab == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Selected Method Content Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (selectedAuthTab) {
                    0 -> EmailAuthForm(viewModel)
                    1 -> GoogleAuthForm(viewModel)
                    2 -> PhoneAuthForm(viewModel)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Admin Access Hint
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Administrator account pklaw9855@gmail.com or admin@ accounts automatically gain full Control Panel access.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 1. Email Auth Form
// -------------------------------------------------------------
@Composable
private fun EmailAuthForm(viewModel: HabitViewModel) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("pklaw9855@gmail.com") }
    var password by remember { mutableStateOf("admin123") }
    var displayName by remember { mutableStateOf("Admin User") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = if (isRegisterMode) "Create New Account" else "Sign in with Email",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )

        if (isRegisterMode) {
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Display Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                errorMessage = null
            },
            label = { Text("Email Address") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("auth_email_input")
        )

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = null
            },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle password visibility"
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("auth_password_input")
        )

        errorMessage?.let { err ->
            Text(err, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }

        Button(
            onClick = {
                if (email.isBlank() || !email.contains("@")) {
                    errorMessage = "Please enter a valid email address."
                    return@Button
                }
                if (password.length < 4) {
                    errorMessage = "Password must be at least 4 characters."
                    return@Button
                }

                if (isRegisterMode) {
                    viewModel.registerWithEmail(email, password, displayName) { err ->
                        errorMessage = err
                    }
                } else {
                    viewModel.loginWithEmail(email, password) { err ->
                        errorMessage = err
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("auth_submit_button")
        ) {
            Text(if (isRegisterMode) "Register & Start" else "Sign In")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isRegisterMode) "Already have an account?" else "Don't have an account yet?",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isRegisterMode) "Sign In" else "Register",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    isRegisterMode = !isRegisterMode
                    errorMessage = null
                }
            )
        }
    }
}

// -------------------------------------------------------------
// 2. Google Auth Form
// -------------------------------------------------------------
@Composable
private fun GoogleAuthForm(viewModel: HabitViewModel) {
    var googleEmail by remember { mutableStateOf("pklaw9855@gmail.com") }
    var googleName by remember { mutableStateOf("P.K. Law") }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Continue with Google", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(
            "Instant single sign-on with your Google profile.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = googleEmail,
            onValueChange = { googleEmail = it },
            label = { Text("Google Account Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = googleName,
            onValueChange = { googleName = it },
            label = { Text("Display Name") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                viewModel.loginWithGoogle(googleEmail, googleName)
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("google_signin_button")
        ) {
            Text("Sign In with Google")
        }
    }
}

// -------------------------------------------------------------
// 3. Phone Number & OTP Auth Form
// -------------------------------------------------------------
@Composable
private fun PhoneAuthForm(viewModel: HabitViewModel) {
    var phoneNumber by remember { mutableStateOf("+1 555-123-4567") }
    var otpCode by remember { mutableStateOf("123456") }
    var isCodeSent by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Sign in with Phone Number", fontWeight = FontWeight.Bold, fontSize = 16.sp)

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = {
                phoneNumber = it
                phoneError = null
            },
            label = { Text("Phone Number") },
            placeholder = { Text("+1 555-123-4567") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (!isCodeSent) {
            Button(
                onClick = {
                    if (phoneNumber.trim().length >= 8) {
                        isCodeSent = true
                        phoneError = null
                    } else {
                        phoneError = "Please enter a valid phone number"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("send_otp_button")
            ) {
                Text("Send 6-Digit OTP")
            }
        } else {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = EmeraldSuccess.copy(alpha = 0.1f)
            ) {
                Text(
                    text = "Verification code sent! (Use code 123456 for instant login)",
                    fontSize = 11.sp,
                    color = EmeraldSuccess,
                    modifier = Modifier.padding(8.dp)
                )
            }

            OutlinedTextField(
                value = otpCode,
                onValueChange = {
                    otpCode = it
                    phoneError = null
                },
                label = { Text("6-Digit Code") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            phoneError?.let { err ->
                Text(err, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }

            Button(
                onClick = {
                    viewModel.verifyPhoneOtp(phoneNumber, otpCode) { err ->
                        phoneError = err
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("verify_otp_button")
            ) {
                Text("Verify & Sign In")
            }

            OutlinedButton(
                onClick = { isCodeSent = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Change Phone Number")
            }
        }
    }
}
