package com.example.horario;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText editUsuario, editSenha;
    private Button btnEntrar;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = FirebaseFirestore.getInstance();

        editUsuario = findViewById(R.id.editUsuario);
        editSenha = findViewById(R.id.editSenha);
        btnEntrar = findViewById(R.id.btnEntrar);

        btnEntrar.setOnClickListener(v -> validarLoginFirebase());
    }

    private void validarLoginFirebase() {
        String usuarioDigitado = editUsuario.getText().toString().trim();
        String senhaDigitada = editSenha.getText().toString().trim();

        if (usuarioDigitado.isEmpty()) {
            editUsuario.setError("Digite o usuário/e-mail");
            return;
        }

        if (senhaDigitada.isEmpty()) {
            editSenha.setError("Digite a senha");
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
                        Toast.makeText(this, "Login não configurado no Firebase.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    String usuarioFirebase = documentSnapshot.getString("email");
                    String senhaFirebase = documentSnapshot.getString("senha");

                    if (usuarioDigitado.equals(usuarioFirebase) && senhaDigitada.equals(senhaFirebase)) {
                        Toast.makeText(this, "Bem-vindo!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(LoginActivity.this, SecretariaActivity.class);
                        startActivity(intent);
                        finish();

                    } else {
                        Toast.makeText(this, "Usuário ou senha inválidos!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    btnEntrar.setEnabled(true);
                    btnEntrar.setText("ENTRAR");

                    Toast.makeText(this, "Erro ao acessar Firebase: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}