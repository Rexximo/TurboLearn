package com.example.turbolearn;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import es.dmoral.toasty.Toasty;

public class RegisterActivity extends AppCompatActivity {
    EditText etName, etEmail, etPassword;
    Button btnRegister;
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnRegister.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString();

            // Validasi Nama
            if (name.isEmpty()) {
                Toasty.info(this, "Field Kosong", Toasty.LENGTH_SHORT, true).show();
                return;
            }

            // Validasi Email
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toasty.info(this, "Email tidak valid", Toasty.LENGTH_SHORT, true).show();
                return;
            }

            // Validasi Password (dipisahkan per syarat)
            if (password.length() < 8) {
                Toasty.info(this, "Password harus minimal 8 karakter", Toasty.LENGTH_SHORT, true).show();
                return;
            }
            if (password.length() > 15) {
                Toasty.info(this, "Password maksimal 15 karakter", Toasty.LENGTH_SHORT, true).show();
                return;
            }
            if (!password.matches(".*[A-Z].*")) {
                Toasty.info(this, "Password harus mengandung minimal 1 huruf kapital", Toasty.LENGTH_SHORT, true).show();
                return;
            }
            if (!password.matches(".*[0-9].*")) {
                Toasty.info(this, "Password harus mengandung minimal 1 angka", Toasty.LENGTH_SHORT, true).show();
                return;
            }


            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // Simpan data user ke Firestore menggunakan User class
                                User userData = new User(name, email);

                                Log.d("RegisterActivity", "Saving user data: " + name + ", " + email);

                                db.collection("users").document(user.getUid())
                                        .set(userData)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("RegisterActivity", "User data saved successfully");
                                            Toasty.success(this, "Register Berhasil", Toasty.LENGTH_SHORT, true).show();
                                            startActivity(new Intent(this, LoginActivity.class));
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("RegisterActivity", "Error saving user data", e);
                                            Toasty.error(this, "Gagal menyimpan data: " + e.getMessage(), Toasty.LENGTH_LONG, true).show();
                                            user.delete();
                                        });
                            }
                        } else {
                            String errorMessage = task.getException() != null ? task.getException().getMessage() : "Register Gagal";
                            Log.e("RegisterActivity", "Registration failed", task.getException());
                            Toasty.error(this, errorMessage, Toasty.LENGTH_LONG, true).show();
                        }
                    });
        });

        TextView tvRegister = findViewById(R.id.tvRegister);
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
}