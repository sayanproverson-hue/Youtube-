package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.*
import com.example.ui.viewmodel.AuthUiState

private enum class AuthMode {
    STUDENT_LOGIN,
    REGISTER,
    ADMIN_LOGIN,
    FORGOT_PASSWORD
}

@Composable
fun AuthScreen(
    authState: AuthUiState,
    onLogin: (identifier: String, password: String, requireAdmin: Boolean) -> Unit,
    onDemoLogin: (role: String) -> Unit,
    onStartRegister: (fullName: String, mobile: String, email: String, password: String, confirm: String) -> Unit,
    onVerifyOtp: (otp: String) -> Unit,
    onResetPassword: (identifier: String, newPassword: String) -> Unit,
    onClearError: () -> Unit
) {
    var mode by remember { mutableStateOf(AuthMode.STUDENT_LOGIN) }

    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    var fullName by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }
    var resetIdentifier by remember { mutableStateOf("") }
    var resetNewPassword by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Brand Header Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 540.dp),
            shape = RoundedCornerShape(24.dp),
            color = BrandBlueDeep
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TargetGradients.BluePurple)
                    .padding(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_target_ssb_logo),
                        contentDescription = "Target SSB Emblem",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(18.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "TARGET SSB",
                        style = MaterialTheme.typography.headlineMedium,
                        color = PureWhite,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "আপনার সফলতার প্রস্তুতি শুরু হোক Target SSB-এর সাথে",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = BengaliFontFamily),
                        color = PureWhite.copy(alpha = 0.92f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Learn Today, Achieve Tomorrow.",
                        style = MaterialTheme.typography.labelMedium,
                        color = BrandOrangeSoft
                    )
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        // Role / Mode Tabs
        if (authState !is AuthUiState.OtpPending) {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 540.dp)
            ) {
                val tabs = listOf(
                    AuthMode.STUDENT_LOGIN to "Student",
                    AuthMode.REGISTER to "Register",
                    AuthMode.ADMIN_LOGIN to "Admin"
                )
                tabs.forEachIndexed { index, (tabMode, label) ->
                    SegmentedButton(
                        selected = mode == tabMode,
                        onClick = {
                            mode = tabMode
                            onClearError()
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = tabs.size),
                        modifier = Modifier.testTag("auth_tab_${tabMode.name.lowercase()}")
                    ) {
                        Text(label, maxLines = 1)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // Main Form Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 540.dp),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                if (authState is AuthUiState.Error) {
                    Surface(
                        color = BrandRoseSoft,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = BrandRose)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = authState.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = BrandRose
                            )
                        }
                    }
                }

                if (authState is AuthUiState.SuccessMessage) {
                    Surface(
                        color = BrandGreenSoft,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = BrandGreen)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = authState.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = BrandGreen
                            )
                        }
                    }
                }

                AnimatedContent(targetState = authState to mode, label = "auth_content") { (state, currentMode) ->
                    when {
                        state is AuthUiState.OtpPending -> {
                            Column {
                                Text(
                                    text = "OTP Verification",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = "Enter the 6-digit verification code sent to ${state.mobile}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(10.dp))
                                Surface(
                                    color = BrandBlueSoft,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Verification Code (Demo SMS): ${state.generatedOtp}",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = BrandBlueDeep,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                                Spacer(Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = otpInput,
                                    onValueChange = { otpInput = it },
                                    label = { Text("Enter 6-Digit OTP") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("otp_input")
                                )
                                Spacer(Modifier.height(16.dp))
                                Button(
                                    onClick = { onVerifyOtp(otpInput) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("verify_otp_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                                ) {
                                    Text("Verify & Create Account")
                                }
                                TextButton(
                                    onClick = onClearError,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    Text("Back to Registration")
                                }
                            }
                        }

                        currentMode == AuthMode.STUDENT_LOGIN || currentMode == AuthMode.ADMIN_LOGIN -> {
                            val isAdminMode = currentMode == AuthMode.ADMIN_LOGIN
                            Column {
                                Text(
                                    text = if (isAdminMode) "Admin Control Login" else "Student Login",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isAdminMode)
                                        "Manage courses, payments, live classes, tests & students"
                                    else
                                        "Access ANM & GNM, RRB NTPC classes, notes & mock tests",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(14.dp))
                                OutlinedTextField(
                                    value = identifier,
                                    onValueChange = { identifier = it },
                                    label = { Text("Email or Mobile Number") },
                                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("login_identifier_input")
                                )
                                Spacer(Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    label = { Text("Password") },
                                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                                    trailingIcon = {
                                        IconButton(onClick = { showPassword = !showPassword }) {
                                            Icon(
                                                if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                                contentDescription = "Toggle password"
                                            )
                                        }
                                    },
                                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("login_password_input")
                                )
                                if (!isAdminMode) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        TextButton(onClick = { mode = AuthMode.FORGOT_PASSWORD }) {
                                            Text("Forgot Password?", color = BrandPurple)
                                        }
                                    }
                                } else {
                                    Spacer(Modifier.height(14.dp))
                                }

                                Button(
                                    onClick = { onLogin(identifier, password, isAdminMode) },
                                    enabled = state !is AuthUiState.Loading,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("login_submit_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isAdminMode) BrandPurple else BrandOrange
                                    )
                                ) {
                                    if (state is AuthUiState.Loading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(22.dp),
                                            color = PureWhite,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text(if (isAdminMode) "Sign In as Admin" else "Login to Dashboard")
                                    }
                                }

                                Spacer(Modifier.height(12.dp))
                                HorizontalDivider()
                                Spacer(Modifier.height(12.dp))

                                Text(
                                    text = "Instant Quick Access (Preloaded Demo Accounts)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { onDemoLogin("STUDENT") },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("demo_student_button")
                                    ) {
                                        Icon(Icons.Filled.School, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Demo Student", maxLines = 1)
                                    }
                                    OutlinedButton(
                                        onClick = { onDemoLogin("ADMIN") },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("demo_admin_button")
                                    ) {
                                        Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Demo Admin", maxLines = 1)
                                    }
                                }
                            }
                        }

                        currentMode == AuthMode.REGISTER -> {
                            Column {
                                Text(
                                    text = "Create Student Account",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = fullName,
                                    onValueChange = { fullName = it },
                                    label = { Text("Full Name") },
                                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("reg_name_input")
                                )
                                Spacer(Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = mobile,
                                    onValueChange = { mobile = it },
                                    label = { Text("Mobile Number") },
                                    leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("reg_mobile_input")
                                )
                                Spacer(Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = email,
                                    onValueChange = { email = it },
                                    label = { Text("Email Address") },
                                    leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("reg_email_input")
                                )
                                Spacer(Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    label = { Text("Password (min 6 chars)") },
                                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                                    visualTransformation = PasswordVisualTransformation(),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("reg_password_input")
                                )
                                Spacer(Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = confirmPassword,
                                    onValueChange = { confirmPassword = it },
                                    label = { Text("Confirm Password") },
                                    leadingIcon = { Icon(Icons.Filled.LockReset, contentDescription = null) },
                                    visualTransformation = PasswordVisualTransformation(),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("reg_confirm_input")
                                )
                                Spacer(Modifier.height(14.dp))
                                Button(
                                    onClick = { onStartRegister(fullName, mobile, email, password, confirmPassword) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("reg_submit_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                                ) {
                                    Text("Send OTP & Register")
                                }
                            }
                        }

                        currentMode == AuthMode.FORGOT_PASSWORD -> {
                            Column {
                                Text(
                                    text = "Reset Password",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = resetIdentifier,
                                    onValueChange = { resetIdentifier = it },
                                    label = { Text("Registered Email or Mobile") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = resetNewPassword,
                                    onValueChange = { resetNewPassword = it },
                                    label = { Text("New Password") },
                                    visualTransformation = PasswordVisualTransformation(),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(14.dp))
                                Button(
                                    onClick = { onResetPassword(resetIdentifier, resetNewPassword) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandPurple)
                                ) {
                                    Text("Reset Password")
                                }
                                TextButton(
                                    onClick = { mode = AuthMode.STUDENT_LOGIN },
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    Text("Back to Login")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
