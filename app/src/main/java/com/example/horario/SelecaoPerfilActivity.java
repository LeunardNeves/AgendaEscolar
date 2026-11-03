package com.example.horario;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class SelecaoPerfilActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selecao_perfil);

        ImageView btnVoltar = findViewById(R.id.btnVoltar);
        CardView cardAluno = findViewById(R.id.cardAluno);
        CardView cardAdministrativo = findViewById(R.id.cardAdministrativo);

        // Botão para voltar à tela anterior
        btnVoltar.setOnClickListener(v -> finish());

        // Ação ao selecionar o perfil de Aluno
        cardAluno.setOnClickListener(v -> {
            // Podes direcionar para a activity principal do aluno
            // Intent intent = new Intent(SelecaoPerfilActivity.this, AlunoActivity.class);
            // startActivity(intent);
        });

        // Ação ao selecionar o perfil Administrativo (Direciona para o cadastro de escolas)
        cardAdministrativo.setOnClickListener(v -> {
            Intent intent = new Intent(SelecaoPerfilActivity.this, CadastroActivity.class);
            startActivity(intent);
        });
    }
}