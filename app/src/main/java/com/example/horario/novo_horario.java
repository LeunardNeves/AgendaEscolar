package com.example.horario;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class novo_horario extends AppCompatActivity {

    private FirebaseFirestore db;

    private TextView txtDiaSemana;
    private TextView txtInicio;
    private TextView txtTermino;
    private TextView txtCurso;
    private TextView txtDisciplina;
    private TextView txtProfessor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_novo_horario);

        db = FirebaseFirestore.getInstance();

        LinearLayout layoutDiaSemana = findViewById(R.id.layoutDiaSemana);
        LinearLayout layoutInicio = findViewById(R.id.layoutInicio);
        LinearLayout layoutTermino = findViewById(R.id.layoutTermino);
        LinearLayout layoutCurso = findViewById(R.id.layoutCurso);
        LinearLayout layoutDisciplina = findViewById(R.id.layoutDisciplina);
        LinearLayout layoutProfessor = findViewById(R.id.layoutProfessor);

        txtDiaSemana = findViewById(R.id.txtDiaSemana);
        txtInicio = findViewById(R.id.txtInicio);
        txtTermino = findViewById(R.id.txtTermino);
        txtCurso = findViewById(R.id.txtCurso);
        txtDisciplina = findViewById(R.id.txtDisciplina);
        txtProfessor = findViewById(R.id.txtProfessor);

        String[] dias = {
                "Segunda",
                "Terça",
                "Quarta",
                "Quinta",
                "Sexta"
        };

        String[] horariosInicio = {
                "07:30",
                "08:20",
                "09:30",
                "10:20",
                "10:50",
                "13:30",
                "14:20",
                "15:10",
                "16:00",
                "19:00",
                "20:00"
        };

        String[] horariosTermino = {
                "08:20",
                "09:10",
                "10:20",
                "11:10",
                "11:30",
                "14:20",
                "15:10",
                "16:00",
                "17:00",
                "20:00",
                "21:00"
        };

        String[] cursos = {
                "Computação Gráfica 2024",
                "Guia de Turismo",
                "Enfermagem",
                "Administração",
                "Informática"
        };

        String[] disciplinas = {
                "Animação de Computador II",
                "Computação Gráfica",
                "Desenho Técnico",
                "Geografia",
                "Modelagem 3D",
                "Banco de Dados",
                "Desenvolvimento Web",
                "Lógica de Programação",
                "Redes de Computadores"
        };

        String[] professores = {
                "Adrielle Veras",
                "Adrielle",
                "Alessandra",
                "Prof. Alan Turing",
                "Profª. Ada Lovelace",
                "Prof. Grace Hopper"
        };

        layoutDiaSemana.setOnClickListener(v ->
                mostrarDialogo(txtDiaSemana, "Selecione o Dia", dias));

        layoutInicio.setOnClickListener(v ->
                mostrarDialogo(txtInicio, "Horário de Início", horariosInicio));

        layoutTermino.setOnClickListener(v ->
                mostrarDialogo(txtTermino, "Horário de Término", horariosTermino));

        layoutCurso.setOnClickListener(v ->
                mostrarDialogo(txtCurso, "Selecione o Curso", cursos));

        layoutDisciplina.setOnClickListener(v ->
                mostrarDialogo(txtDisciplina, "Selecione a Disciplina", disciplinas));

        layoutProfessor.setOnClickListener(v ->
                mostrarDialogo(txtProfessor, "Selecione o Professor(a)", professores));

        Button btnVoltar = findViewById(R.id.btnVoltar);

        if (btnVoltar != null) {
            btnVoltar.setOnClickListener(v -> finish());
        }


        androidx.appcompat.widget.AppCompatButton btnSalvarHorario = findViewById(R.id.btnSalvarHorario);

        if (btnSalvarHorario != null) {
            btnSalvarHorario.setOnClickListener(v -> salvarHorarioFirebase());
        }
    }

    private void salvarHorarioFirebase() {
        String dia = txtDiaSemana.getText().toString().trim();
        String inicio = txtInicio.getText().toString().trim();
        String termino = txtTermino.getText().toString().trim();
        String curso = txtCurso.getText().toString().trim();
        String disciplina = txtDisciplina.getText().toString().trim();
        String professor = txtProfessor.getText().toString().trim();

        if (dia.equals("Selecione o dia da semana") || dia.isEmpty()) {
            Toast.makeText(this, "Selecione o dia da semana.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (inicio.equals("Selecione o horário de início") || inicio.isEmpty()) {
            Toast.makeText(this, "Selecione o horário de início.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (termino.equals("Selecione o horário de término") || termino.isEmpty()) {
            Toast.makeText(this, "Selecione o horário de término.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (curso.equals("Selecione o curso") || curso.isEmpty()) {
            Toast.makeText(this, "Selecione o curso.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (disciplina.equals("Selecione a disciplina") || disciplina.isEmpty()) {
            Toast.makeText(this, "Selecione a disciplina.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (professor.equals("Selecione o professor(a)") || professor.isEmpty()) {
            Toast.makeText(this, "Selecione o professor.", Toast.LENGTH_SHORT).show();
            return;
        }

        String horarioCompleto = inicio + "-" + termino;
        String turno = descobrirTurno(inicio);

        Map<String, Object> dados = new HashMap<>();
        dados.put("Curso", curso);
        dados.put("Dia", dia);
        dados.put("Disciplina", disciplina);
        dados.put("Horario", horarioCompleto);
        dados.put("Professor", professor);
        dados.put("Turno", turno);

        db.collection("grade")
                .add(dados)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Horário salvo com sucesso!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao salvar: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private String descobrirTurno(String inicio) {
        if (inicio.startsWith("07")
                || inicio.startsWith("08")
                || inicio.startsWith("09")
                || inicio.startsWith("10")
                || inicio.startsWith("11")) {
            return "Manhã";
        }

        if (inicio.startsWith("13")
                || inicio.startsWith("14")
                || inicio.startsWith("15")
                || inicio.startsWith("16")
                || inicio.startsWith("17")) {
            return "Tarde";
        }

        if (inicio.startsWith("18")
                || inicio.startsWith("19")
                || inicio.startsWith("20")
                || inicio.startsWith("21")
                || inicio.startsWith("22")) {
            return "Noite";
        }

        return "Não informado";
    }

    private void mostrarDialogo(TextView campoTexto, String titulo, String[] itens) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(titulo);

        builder.setItems(itens, (dialog, which) -> {
            campoTexto.setText(itens[which]);
        });

        builder.show();
    }
}