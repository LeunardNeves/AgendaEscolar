package com.example.horario;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Button btnComecar = findViewById(R.id.btnComecar);
        TextView txtCadastrar = findViewById(R.id.txtJaTenhoConta);

        // Ao clicar em Entrar, vai para a seleção de perfil (Aluno / Administrativo)
        btnComecar.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, SelecaoPerfilActivity.class);
            startActivity(intent);
        });

        // Ao clicar no cadastro de primeiro acesso, vai direto para o cadastro de escolas/usuário
        txtCadastrar.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, CadastroActivity.class);
            startActivity(intent);
        });
    }
}