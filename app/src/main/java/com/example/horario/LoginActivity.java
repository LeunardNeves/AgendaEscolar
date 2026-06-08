package com.example.horario;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText editUsuario;
    private EditText editSenha;
    private AppCompatButton btnEntrar;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = FirebaseFirestore.getInstance();

        editUsuario = findViewById(R.id.editUsuario);
        editSenha = findViewById(R.id.editSenha);
        btnEntrar = findViewById(R.id.btnEntrar);

        btnEntrar.setOnClickListener(v -> validarLogin());
    }

    private void validarLogin() {

        String emailDigitado =
                editUsuario.getText().toString().trim();

        String senhaDigitada =
                editSenha.getText().toString().trim();

        if (emailDigitado.isEmpty()) {
            editUsuario.setError("Digite o e-mail");
            editUsuario.requestFocus();
            return;
        }

        if (senhaDigitada.isEmpty()) {
            editSenha.setError("Digite a senha");
            editSenha.requestFocus();
            return;
        }

        btnEntrar.setEnabled(false);
        btnEntrar.setText("ENTRANDO...");

        db.collection("loginSecretaria")
                .document("acesso")
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    btnEntrar.setEnabled(true);
                    btnEntrar.setText("ENTRAR");

                    if (!documentSnapshot.exists()) {

                        Toast.makeText(
                                LoginActivity.this,
                                "Documento de acesso não encontrado.",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    String emailBanco =
                            documentSnapshot.getString("email");

                    String senhaBanco =
                            documentSnapshot.getString("senha");

                    if (emailBanco == null || senhaBanco == null) {

                        Toast.makeText(
                                LoginActivity.this,
                                "Campos email ou senha não encontrados.",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    if (emailDigitado.equalsIgnoreCase(emailBanco.trim())
                            && senhaDigitada.equals(senhaBanco.trim())) {

                        Toast.makeText(
                                LoginActivity.this,
                                "Login realizado com sucesso!",
                                Toast.LENGTH_SHORT
                        ).show();

                        Intent intent =
                                new Intent(
                                        LoginActivity.this,
                                        SecretariaActivity.class
                                );

                        startActivity(intent);
                        finish();

                    } else {

                        Toast.makeText(
                                LoginActivity.this,
                                "E-mail ou senha incorretos.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                })
                .addOnFailureListener(e -> {

                    btnEntrar.setEnabled(true);
                    btnEntrar.setText("ENTRAR");

                    Toast.makeText(
                            LoginActivity.this,
                            "Erro ao acessar o Firestore:\n" + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }
}