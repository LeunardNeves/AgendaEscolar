package com.example.horario;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class GradeHorarioActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    private LinearLayout layoutSegunda;
    private LinearLayout layoutTerca;
    private LinearLayout layoutQuarta;
    private LinearLayout layoutQuinta;
    private LinearLayout layoutSexta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grade_horario);

        db = FirebaseFirestore.getInstance();

        Button btnVoltar = findViewById(R.id.btnVoltar);
        Button btnAtualizar = findViewById(R.id.btnAtualizarGrade);

        Button btnAdicionarSegunda = findViewById(R.id.btnAdicionarSegunda);
        Button btnAdicionarTerca = findViewById(R.id.btnAdicionarTerca);
        Button btnAdicionarQuarta = findViewById(R.id.btnAdicionarQuarta);
        Button btnAdicionarQuinta = findViewById(R.id.btnAdicionarQuinta);
        Button btnAdicionarSexta = findViewById(R.id.btnAdicionarSexta);

        layoutSegunda = findViewById(R.id.layoutSegunda);
        layoutTerca = findViewById(R.id.layoutTerca);
        layoutQuarta = findViewById(R.id.layoutQuarta);
        layoutQuinta = findViewById(R.id.layoutQuinta);
        layoutSexta = findViewById(R.id.layoutSexta);

        btnVoltar.setOnClickListener(v -> finish());

        btnAtualizar.setOnClickListener(v -> carregarGradeFirebase());

        btnAdicionarSegunda.setOnClickListener(v -> abrirNovoHorario("Segunda-feira"));
        btnAdicionarTerca.setOnClickListener(v -> abrirNovoHorario("Terça-feira"));
        btnAdicionarQuarta.setOnClickListener(v -> abrirNovoHorario("Quarta-feira"));
        btnAdicionarQuinta.setOnClickListener(v -> abrirNovoHorario("Quinta-feira"));
        btnAdicionarSexta.setOnClickListener(v -> abrirNovoHorario("Sexta-feira"));

        carregarGradeFirebase();
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarGradeFirebase();
    }

    private void abrirNovoHorario(String dia) {
        Intent intent = new Intent(GradeHorarioActivity.this, novo_horario.class);
        intent.putExtra("dia", dia);
        startActivity(intent);
    }

    private void carregarGradeFirebase() {
        limparLayouts();

        db.collection("grade")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(
                                this,
                                "Nenhum horário encontrado na coleção grade",
                                Toast.LENGTH_LONG
                        ).show();
                        return;
                    }

                    for (QueryDocumentSnapshot documento : queryDocumentSnapshots) {

                        String curso = documento.getString("Curso");
                        String dia = documento.getString("Dia");
                        String disciplina = documento.getString("Disciplina");

                        String horario = documento.getString("Horario");

                        if (horario == null) {
                            horario = documento.getString("Horário");
                        }

                        String professor = documento.getString("Professor");
                        String turno = documento.getString("Turno");

                        if (dia == null) {
                            Toast.makeText(
                                    this,
                                    "Um documento está sem o campo Dia",
                                    Toast.LENGTH_LONG
                            ).show();
                            continue;
                        }

                        if (disciplina == null) {
                            Toast.makeText(
                                    this,
                                    "Um documento está sem o campo Disciplina",
                                    Toast.LENGTH_LONG
                            ).show();
                            continue;
                        }

                        if (horario == null) {
                            Toast.makeText(
                                    this,
                                    "Um documento está sem o campo Horario",
                                    Toast.LENGTH_LONG
                            ).show();
                            continue;
                        }

                        adicionarHorarioNaTela(
                                dia,
                                horario,
                                disciplina,
                                professor,
                                curso,
                                turno
                        );
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(
                            this,
                            "Erro ao carregar grade: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void limparLayouts() {
        layoutSegunda.removeAllViews();
        layoutTerca.removeAllViews();
        layoutQuarta.removeAllViews();
        layoutQuinta.removeAllViews();
        layoutSexta.removeAllViews();
    }

    private void adicionarHorarioNaTela(
            String dia,
            String horario,
            String disciplina,
            String professor,
            String curso,
            String turno
    ) {
        LinearLayout destino = pegarLayoutDoDia(dia);

        if (destino == null) {
            Toast.makeText(this, "Dia não reconhecido: " + dia, Toast.LENGTH_SHORT).show();
            return;
        }

        if (professor == null || professor.isEmpty()) professor = "Não informado";
        if (curso == null || curso.isEmpty()) curso = "Curso não informado";
        if (turno == null || turno.isEmpty()) turno = "Turno não informado";

        LinearLayout cardHorario = new LinearLayout(this);
        cardHorario.setOrientation(LinearLayout.VERTICAL);
        cardHorario.setPadding(22, 18, 22, 18);
        cardHorario.setBackgroundResource(R.drawable.bg_item);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 14);
        cardHorario.setLayoutParams(cardParams);

        TextView txtHorario = new TextView(this);
        txtHorario.setText("⏰ " + horario);
        txtHorario.setTextColor(Color.parseColor("#7B61FF"));
        txtHorario.setTextSize(13);
        txtHorario.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView txtDisciplina = new TextView(this);
        txtDisciplina.setText(disciplina);
        txtDisciplina.setTextColor(Color.parseColor("#FFFFFF"));
        txtDisciplina.setTextSize(16);
        txtDisciplina.setTypeface(null, android.graphics.Typeface.BOLD);
        txtDisciplina.setPadding(0, 8, 0, 4);

        TextView txtProfessor = new TextView(this);
        txtProfessor.setText("Professor(a): " + professor);
        txtProfessor.setTextColor(Color.parseColor("#C9D3F5"));
        txtProfessor.setTextSize(13);
        txtProfessor.setPadding(0, 4, 0, 2);

        TextView txtCurso = new TextView(this);
        txtCurso.setText(curso + " • " + turno);
        txtCurso.setTextColor(Color.parseColor("#9FAAD0"));
        txtCurso.setTextSize(12);
        txtCurso.setPadding(0, 2, 0, 0);

        cardHorario.addView(txtHorario);
        cardHorario.addView(txtDisciplina);
        cardHorario.addView(txtProfessor);
        cardHorario.addView(txtCurso);

        destino.addView(cardHorario);
    }
    private LinearLayout pegarLayoutDoDia(String dia) {

        if (dia == null) {
            return null;
        }

        String d = dia
                .toLowerCase()
                .trim()
                .replace("á", "a")
                .replace("ã", "a")
                .replace("â", "a")
                .replace("é", "e")
                .replace("ê", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ô", "o")
                .replace("ú", "u")
                .replace("ç", "c")
                .replace("-", " ");

        if (d.contains("segunda")) {
            return layoutSegunda;
        }

        if (d.contains("terca") || d.contains("terça")) {
            return layoutTerca;
        }

        if (d.contains("quarta")) {
            return layoutQuarta;
        }

        if (d.contains("quinta")) {
            return layoutQuinta;
        }

        if (d.contains("sexta")) {
            return layoutSexta;
        }

        return null;
    }
}