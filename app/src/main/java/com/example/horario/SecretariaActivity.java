package com.example.horario;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import android.widget.Button;

public class SecretariaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secretaria);

        // Vincular os componentes do XML
        CardView cardAvisos = findViewById(R.id.cardAvisos);
        CardView cardHorarios = findViewById(R.id.cardHorarios);
        CardView cardProfessores = findViewById(R.id.cardProfessores);
        Button btnSair = findViewById(R.id.btnSair);

        // Ação: Gestão de Avisos
        cardAvisos.setOnClickListener(v -> {
            Intent intent = new Intent(SecretariaActivity.this, GestaoAvisosActivity.class);
            startActivity(intent);
        });

        // Ação: Horários e Turmas
        cardHorarios.setOnClickListener(v -> {
            Intent intent = new Intent(SecretariaActivity.this, GradeHorarioActivity.class);
            startActivity(intent);
        });

        // Ação: Professores e Matérias (VINCULADO AGORA)
        cardProfessores.setOnClickListener(v -> {
            Intent intent = new Intent(SecretariaActivity.this, CadastroProfessorActivity.class);
            startActivity(intent);
        });

        // Botão Sair
        btnSair.setOnClickListener(v -> finish());
    }
}