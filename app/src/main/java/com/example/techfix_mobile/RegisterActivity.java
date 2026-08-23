package com.example.techfix_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText nameInput, emailInput, phoneInput, passwordInput;
    private Button registerBtn;
    private TextView goToLogin;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        dbHelper = new DatabaseHelper(this);

        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        passwordInput = findViewById(R.id.passwordInput);
        registerBtn = findViewById(R.id.registerBtn);
        goToLogin = findViewById(R.id.goToLogin);

        registerBtn.setOnClickListener(v -> registerUser());

        goToLogin.setOnClickListener(v -> {
            finish();
        });
    }

    private void registerUser() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Name, email and password are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            phoneInput.setError("Phone is required");
            return;
        }

        if (password.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters");
            return;
        }

        registerBtn.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        saveUserToFirestore(mAuth.getCurrentUser().getUid(), name, email, phone);
                    } else {
                        registerBtn.setEnabled(true);
                        Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToFirestore(String uid, String name, String email, String phone) {
        Map<String, Object> user = new HashMap<>();
        user.put("uid", uid);
        user.put("name", name);
        user.put("email", email);
        user.put("phone", phone);
        user.put("role", "customer"); // Default role

        db.collection("users").document(uid).set(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Save to Local SQLite for offline-first experience
                        dbHelper.saveUser(uid, name, email, phone, "customer");

                        Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, com.example.techfix_mobile.ui.home.HomeActivity.class));
                        finish();
                    } else {
                        registerBtn.setEnabled(true);
                        Toast.makeText(RegisterActivity.this, "Error saving user data.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}