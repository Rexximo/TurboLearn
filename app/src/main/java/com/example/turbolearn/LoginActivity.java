package com.example.turbolearn;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;


import es.dmoral.toasty.Toasty;

public class LoginActivity extends AppCompatActivity {
    EditText etEmail, etPassword;
    Button btnLogin;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        mAuth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // Validasi input kosong
            if (email.isEmpty() && password.isEmpty()) {
                Toasty.warning(this, "Email dan password tidak boleh kosong", Toasty.LENGTH_SHORT, true).show();
                return;
            }

            if (email.isEmpty()) {
                Toasty.warning(this, "Email tidak boleh kosong", Toasty.LENGTH_SHORT, true).show();
                etEmail.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                Toasty.warning(this, "Password tidak boleh kosong", Toasty.LENGTH_SHORT, true).show();
                etPassword.requestFocus();
                return;
            }

            // Proses login jika validasi berhasil
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toasty.success(this, "Login Berhasil", Toasty.LENGTH_SHORT, true).show();
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        } else {
                            Toasty.error(this, "Email atau password salah", Toasty.LENGTH_SHORT, true).show();
                        }
                    });
        });

        TextView tvRegister = findViewById(R.id.tvRegister);

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }
}