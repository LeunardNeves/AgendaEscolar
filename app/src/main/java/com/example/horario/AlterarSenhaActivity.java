package com.example.horario;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

public class AlterarSenhaActivity extends AppCompatActivity {

    private MaterialButton btnVoltar, btnSalvarSenha;
    private EditText editSenhaAtual, editNovaSenha, editConfirmarSenha;
    private FirebaseFirestore db;

    private static final String COLECAO_LOGIN = "loginSecretaria";
    private static final String DOCUMENTO_LOGIN = "acesso";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alterar_senha);

        db = FirebaseFirestore.getInstance();

        btnVoltar = findViewById(R.id.btnVoltar);
        btnSalvarSenha = findViewById(R.id.btnSalvarSenha);

        editSenhaAtual = findViewById(R.id.editSenhaAtual);
        editNovaSenha = findViewById(R.id.editNovaSenha);
        editConfirmarSenha = findViewById(R.id.editConfirmarSenha);

        btnVoltar.setOnClickListener(v -> finish());
        btnSalvarSenha.setOnClickListener(v -> alterarSenha());
    }

    private void alterarSenha() {
        String senhaAtual = editSenhaAtual.getText().toString().trim();
        String novaSenha = editNovaSenha.getText().toString().trim();
        String confirmarSenha = editConfirmarSenha.getText().toString().trim();

        if (senhaAtual.isEmpty()) {
            editSenhaAtual.setError("Digite a senha atual");
            editSenhaAtual.requestFocus();
            return;
        }

        if (novaSenha.isEmpty()) {
            editNovaSenha.setError("Digite a nova senha");
            editNovaSenha.requestFocus();
            return;
        }

        if (novaSenha.length() < 6) {
            editNovaSenha.setError("A senha deve ter pelo menos 6 caracteres");
            editNovaSenha.requestFocus();
            return;
        }

        if (!novaSenha.equals(confirmarSenha)) {
            editConfirmarSenha.setError("As senhas não são iguais");
            editConfirmarSenha.requestFocus();
            return;
        }

        btnSalvarSenha.setEnabled(false);
        btnSalvarSenha.setText("Salvando...");

        db.collection(COLECAO_LOGIN)
                .document(DOCUMENTO_LOGIN)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        restaurarBotao();
                        Toast.makeText(this, "Documento de login não encontrado.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    String senhaBanco = documentSnapshot.getString("senha");

                    if (senhaBanco == null) {
                        restaurarBotao();
                        Toast.makeText(this, "Campo senha não encontrado no Firebase.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (!senhaAtual.equals(senhaBanco.trim())) {
                        restaurarBotao();
                        Toast.makeText(this, "Senha atual incorreta.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    db.collection(COLECAO_LOGIN)
                            .document(DOCUMENTO_LOGIN)
                            .update("senha", novaSenha)
                            .addOnSuccessListener(unused -> {
                                restaurarBotao();
                                Toast.makeText(this, "Senha alterada no Firebase com sucesso!", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                restaurarBotao();
                                Toast.makeText(this, "Erro ao atualizar senha: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    restaurarBotao();
                    Toast.makeText(this, "Erro ao buscar senha: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void restaurarBotao() {
        btnSalvarSenha.setEnabled(true);
        btnSalvarSenha.setText("SALVAR NOVA SENHA");
    }
}