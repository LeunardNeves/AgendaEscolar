package com.example.horario;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Arrays;
import java.util.LinkedHashSet;

public class MainActivity extends AppCompatActivity {

    private String[] cursos = {};
    private String[] anos = {};
    private String[] turnos = {};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayout layoutCurso = findViewById(R.id.layoutCurso);
        LinearLayout layoutAno = findViewById(R.id.layoutAno);
        LinearLayout layoutTurno = findViewById(R.id.layoutTurno);

        TextView txtCurso = findViewById(R.id.txtCurso);
        TextView txtAno = findViewById(R.id.txtAno);
        TextView txtTurno = findViewById(R.id.txtTurno);

        carregarDadosDoFirestore();

        layoutCurso.setOnClickListener(v ->
                mostrarDialogo(txtCurso, "Selecione seu Curso", cursos)
        );

        layoutAno.setOnClickListener(v ->
                mostrarDialogo(txtAno, "Selecione seu Ano", anos)
        );

        layoutTurno.setOnClickListener(v ->
                mostrarDialogo(txtTurno, "Selecione seu Turno", turnos)
        );

        Button btnContinuar = findViewById(R.id.btnContinuar);
        btnContinuar.setOnClickListener(v -> {
            String cursoSelecionado = txtCurso.getText().toString();
            String anoSelecionado = txtAno.getText().toString();
            String turnoSelecionado = txtTurno.getText().toString();

            if (cursoSelecionado.equals("Selecione o Curso") ||
                    anoSelecionado.equals("Selecione o Ano/Módulo") ||
                    turnoSelecionado.equals("Selecione o Turno")) {

                Toast.makeText(this, "Selecione todas as opções!", Toast.LENGTH_SHORT).show();

            } else {
                Intent intent = new Intent(MainActivity.this, AlunoActivity.class);

                intent.putExtra("curso", cursoSelecionado);
                intent.putExtra("ano", anoSelecionado);
                intent.putExtra("turno", turnoSelecionado);

                startActivity(intent);
            }
        });

        Button btnPainelAdmin = findViewById(R.id.btnPainelAdmin);
        btnPainelAdmin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void carregarDadosDoFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("grade")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    LinkedHashSet<String> listaCursos = new LinkedHashSet<>();
                    LinkedHashSet<String> listaAnos = new LinkedHashSet<>();
                    LinkedHashSet<String> listaTurnos = new LinkedHashSet<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String cursoCompleto = document.getString("Curso");
                        String turno = document.getString("Turno");

                        if (cursoCompleto != null && !cursoCompleto.trim().isEmpty()) {
                            String ano = extrairAno(cursoCompleto);
                            String nomeCurso = removerAno(cursoCompleto);

                            if (!nomeCurso.trim().isEmpty()) {
                                listaCursos.add(nomeCurso);
                            }

                            if (!ano.trim().isEmpty()) {
                                listaAnos.add(ano);
                            }
                        }

                        if (turno != null && !turno.trim().isEmpty()) {
                            listaTurnos.add(turno);
                        }
                    }

                    cursos = listaCursos.toArray(new String[0]);
                    anos = listaAnos.toArray(new String[0]);
                    turnos = listaTurnos.toArray(new String[0]);

                    Arrays.sort(cursos);
                    Arrays.sort(anos);
                    Arrays.sort(turnos);

                    if (cursos.length > 0 && anos.length > 0 && turnos.length > 0) {
                        Toast.makeText(MainActivity.this, "Dados carregados com sucesso!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Verifique se a coleção grade possui Curso e Turno.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(
                            MainActivity.this,
                            "Erro Firestore: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private String extrairAno(String cursoCompleto) {
        String[] partes = cursoCompleto.trim().split(" ");

        if (partes.length > 0) {
            String ultimaParte = partes[partes.length - 1];

            if (ultimaParte.matches("\\d{4}")) {
                return ultimaParte;
            }
        }

        return "";
    }

    private String removerAno(String cursoCompleto) {
        return cursoCompleto.trim().replaceAll("\\s\\d{4}$", "");
    }

    private void mostrarDialogo(TextView campoTexto, String titulo, String[] itens) {
        if (itens == null || itens.length == 0) {
            Toast.makeText(this, "Aguarde, os dados ainda estão carregando.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(titulo);

        builder.setItems(itens, (dialog, which) -> {
            campoTexto.setText(itens[which]);
            campoTexto.setTextColor(0xFFFFFFFF);
        });

        builder.show();
    }
}