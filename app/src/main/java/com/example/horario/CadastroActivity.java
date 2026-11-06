package com.example.horario;

import android.os.Bundle;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.material.textfield.TextInputEditText;

public class CadastroActivity extends AppCompatActivity {

    private TextInputEditText edtNome;
    private TextInputEditText edtEmail;
    private TextInputEditText edtSenha;
    private TextInputEditText edtConfirmarSenha;

    private AppCompatButton btnCadastrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        edtNome = findViewById(R.id.edtNome);
        edtEmail = findViewById(R.id.edtEmail);
        edtSenha = findViewById(R.id.edtSenha);
        edtConfirmarSenha =
                findViewById(R.id.edtConfirmarSenha);

        btnCadastrar = findViewById(R.id.btnCadastrar);

        ImageView btnVoltar =
                findViewById(R.id.btnVoltarCadastro);

        TextView txtEntrar =
                findViewById(R.id.txtEntrar);

        btnVoltar.setOnClickListener(v -> finish());

        btnCadastrar.setOnClickListener(v -> {
            validarCadastro();
        });

        txtEntrar.setOnClickListener(v -> {
            Toast.makeText(
                    this,
                    "Tela de login será implementada.",
                    Toast.LENGTH_SHORT
            ).show();
        });
    }

    private void validarCadastro() {

        String nome = edtNome.getText() == null
                ? ""
                : edtNome.getText().toString().trim();

        String email = edtEmail.getText() == null
                ? ""
                : edtEmail.getText().toString().trim();

        String senha = edtSenha.getText() == null
                ? ""
                : edtSenha.getText().toString();

        String confirmar = edtConfirmarSenha.getText() == null
                ? ""
                : edtConfirmarSenha.getText().toString();

        if (nome.isEmpty()) {
            edtNome.setError("Informe seu nome.");
            edtNome.requestFocus();
            return;
        }

        if (email.isEmpty() ||
                !android.util.Patterns.EMAIL_ADDRESS
                        .matcher(email).matches()) {

            edtEmail.setError("Informe um e-mail válido.");
            edtEmail.requestFocus();
            return;
        }

        if (senha.length() < 6) {
            edtSenha.setError(
                    "A senha deve ter pelo menos 6 caracteres."
            );
            edtSenha.requestFocus();
            return;
        }

        if (!senha.equals(confirmar)) {
            edtConfirmarSenha.setError(
                    "As senhas não coincidem."
            );
            edtConfirmarSenha.requestFocus();
            return;
        }

        Toast.makeText(
                this,
                "Dados validados! Cadastro pronto para integrar ao Firebase.",
                Toast.LENGTH_LONG
        ).show();
    }
}