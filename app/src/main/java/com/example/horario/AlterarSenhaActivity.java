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
    private static final String DOCUMENTO_ACESSO = "acesso";

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

        if (senhaAtual.equals(novaSenha)) {
            editNovaSenha.setError("A nova senha precisa ser diferente da senha atual");
            editNovaSenha.requestFocus();
            return;
        }

        btnSalvarSenha.setEnabled(false);
        btnSalvarSenha.setText("Salvando...");

        db.collection(COLECAO_LOGIN)
                .document(DOCUMENTO_ACESSO)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        restaurarBotao();
                        Toast.makeText(
                                AlterarSenhaActivity.this,
                                "Documento de acesso não encontrado.",
                                Toast.LENGTH_LONG
                        ).show();
                        return;
                    }

                    String senhaBanco = documentSnapshot.getString("senha");

                    if (senhaBanco == null || senhaBanco.trim().isEmpty()) {
                        restaurarBotao();
                        Toast.makeText(
                                AlterarSenhaActivity.this,
                                "Campo senha não encontrado no banco.",
                                Toast.LENGTH_LONG
                        ).show();
                        return;
                    }

                    if (!senhaAtual.equals(senhaBanco.trim())) {
                        restaurarBotao();
                        Toast.makeText(
                                AlterarSenhaActivity.this,
                                "Senha atual incorreta.",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    db.collection(COLECAO_LOGIN)
                            .document(DOCUMENTO_ACESSO)
                            .update("senha", novaSenha)
                            .addOnSuccessListener(unused -> {
                                restaurarBotao();
                                Toast.makeText(
                                        AlterarSenhaActivity.this,
                                        "Senha alterada com sucesso!",
                                        Toast.LENGTH_SHORT
                                ).show();

                                limparCampos();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                restaurarBotao();
                                Toast.makeText(
                                        AlterarSenhaActivity.this,
                                        "Erro ao alterar senha: " + e.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            });
                })
                .addOnFailureListener(e -> {
                    restaurarBotao();
                    Toast.makeText(
                            AlterarSenhaActivity.this,
                            "Erro ao buscar senha: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void restaurarBotao() {
        btnSalvarSenha.setEnabled(true);
        btnSalvarSenha.setText("SALVAR NOVA SENHA");
    }

    private void limparCampos() {
        editSenhaAtual.setText("");
        editNovaSenha.setText("");
        editConfirmarSenha.setText("");
    }
}