package com.example.horario;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

public class SecretariaActivity extends AppCompatActivity {

    private CardView cardAvisos;
    private CardView cardHorarios;
    private CardView cardProfessores;
    private CardView cardGerenciarProfessores;
    private Button btnSair;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secretaria);

        cardAvisos = findViewById(R.id.cardAvisos);
        cardHorarios = findViewById(R.id.cardHorarios);
        cardProfessores = findViewById(R.id.cardProfessores);
        cardGerenciarProfessores = findViewById(R.id.cardGerenciarProfessores);
        btnSair = findViewById(R.id.btnSair);

        cardAvisos.setOnClickListener(v -> {
            Intent intent = new Intent(SecretariaActivity.this, GestaoAvisosActivity.class);
            startActivity(intent);
        });

        cardProfessores.setOnClickListener(v -> {
            Intent intent = new Intent(SecretariaActivity.this, CadastroProfessorActivity.class);
            startActivity(intent);
        });

        cardGerenciarProfessores.setOnClickListener(v -> {
            Intent intent = new Intent(SecretariaActivity.this, GerenciarProfessores.class);
            startActivity(intent);
        });

        cardHorarios.setOnClickListener(v -> {
            Intent intent = new Intent(SecretariaActivity.this, GradeHorarioActivity.class);
            startActivity(intent);
        });

        btnSair.setOnClickListener(v -> finish());
    }
}