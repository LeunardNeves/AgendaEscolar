package com.example.horario;

import android.os.Bundle;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;

public class SelecaoPerfilActivity extends AppCompatActivity {

    private ImageView btnVoltar;
    private MaterialCardView cardAluno;
    private MaterialCardView cardAdministrativo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_selecao_perfil);

        // Inicializar os componentes
        btnVoltar = findViewById(R.id.btnVoltar);
        cardAluno = findViewById(R.id.cardAluno);
        cardAdministrativo = findViewById(R.id.cardAdministrativo);

        // Botão voltar
        btnVoltar.setOnClickListener(v -> {
            finish();
        });

        // Perfil Aluno
        cardAluno.setOnClickListener(v -> {

            Toast.makeText(
                    SelecaoPerfilActivity.this,
                    "Perfil Aluno selecionado!",
                    Toast.LENGTH_SHORT
            ).show();

            // Futuramente abrir a tela do aluno
        });

        // Perfil Administrativo
        cardAdministrativo.setOnClickListener(v -> {

            Toast.makeText(
                    SelecaoPerfilActivity.this,
                    "Perfil Administrativo selecionado!",
                    Toast.LENGTH_SHORT
            ).show();

            // Futuramente abrir a tela de cadastro/login
        });
    }
}