package com.example.turbolearn;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import es.dmoral.toasty.Toasty;

public class RegisterActivity extends AppCompatActivity {
    EditText etEmail, etPassword;
    Button btnRegister;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        mAuth = FirebaseAuth.getInstance();

        btnRegister.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString();

            // Validasi Email
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Email tidak valid", Toast.LENGTH_SHORT).show();
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

            // Jika lolos semua validasi, lakukan register
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toasty.success(this, "Register Berhasil", Toasty.LENGTH_SHORT, true).show();
                            startActivity(new Intent(this, LoginActivity.class));
                        } else {
                            Toasty.error(this, "Register Gagal", Toasty.LENGTH_SHORT, true).show();
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
