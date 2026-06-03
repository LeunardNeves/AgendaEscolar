package com.example.horario;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Vincular os campos do XML
        EditText editUsuario = findViewById(R.id.editUsuario);
        EditText editSenha = findViewById(R.id.editSenha);
        Button btnEntrar = findViewById(R.id.btnEntrar);

        btnEntrar.setOnClickListener(v -> {
            String usuario = editUsuario.getText().toString();
            String senha = editSenha.getText().toString();

            // Validação com os dados que você definiu
            if (usuario.equals("secretaria123") && senha.equals("1234")) {
                //  Vincula e abre a SecretariaActivity
                Intent intent = new Intent(LoginActivity.this, SecretariaActivity.class);
                startActivity(intent);

                // Fecha a tela de login para não voltar nela ao clicar em "voltar"
                finish();

                Toast.makeText(this, "Bem-vindo!", Toast.LENGTH_SHORT).show();
            } else {
                // ERRO: Aviso ao usuário
                Toast.makeText(this, "Usuário ou senha inválidos!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}