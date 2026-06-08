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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;

public class MainActivity extends AppCompatActivity {

    private String[] cursos = {};
    private String[] anos = {};
    private String[] turnos = {};

    private final ArrayList<String> todosCursosCompletos = new ArrayList<>();
    private final ArrayList<String> todosTurnos = new ArrayList<>();

    private TextView txtCurso, txtAno, txtTurno;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayout layoutCurso = findViewById(R.id.layoutCurso);
        LinearLayout layoutAno = findViewById(R.id.layoutAno);
        LinearLayout layoutTurno = findViewById(R.id.layoutTurno);

        txtCurso = findViewById(R.id.txtCurso);
        txtAno = findViewById(R.id.txtAno);
        txtTurno = findViewById(R.id.txtTurno);

        carregarDadosDoFirestore();

        layoutCurso.setOnClickListener(v -> mostrarDialogoCurso());

        layoutAno.setOnClickListener(v -> {
            if (txtCurso.getText().toString().equals("Selecione o Curso")) {
                Toast.makeText(this, "Selecione o curso primeiro.", Toast.LENGTH_SHORT).show();
                return;
            }

            mostrarDialogoAno();
        });

        layoutTurno.setOnClickListener(v -> {
            if (txtCurso.getText().toString().equals("Selecione o Curso")) {
                Toast.makeText(this, "Selecione o curso primeiro.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (txtAno.getText().toString().equals("Selecione o Ano/Módulo")) {
                Toast.makeText(this, "Selecione o ano primeiro.", Toast.LENGTH_SHORT).show();
                return;
            }

            mostrarDialogoTurno();
        });

        Button btnContinuar = findViewById(R.id.btnContinuar);
        btnContinuar.setOnClickListener(v -> validarEEntrar());

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

                    todosCursosCompletos.clear();
                    todosTurnos.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String cursoCompleto = document.getString("Curso");
                        String turno = document.getString("Turno");

                        if (cursoCompleto != null && !cursoCompleto.trim().isEmpty()) {
                            String cursoLimpo = cursoCompleto.trim();
                            String nomeCurso = removerAno(cursoLimpo);

                            todosCursosCompletos.add(cursoLimpo);
                            todosTurnos.add(turno == null ? "" : turno.trim());

                            if (!nomeCurso.isEmpty()) {
                                listaCursos.add(nomeCurso);
                            }
                        }
                    }

                    ArrayList<String> cursosOrdenados = new ArrayList<>(listaCursos);
                    Collections.sort(cursosOrdenados);

                    cursos = cursosOrdenados.toArray(new String[0]);

                    if (cursos.length > 0) {
                        Toast.makeText(MainActivity.this, "Dados carregados com sucesso!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Nenhum curso encontrado na coleção grade.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                MainActivity.this,
                                "Erro Firestore: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
    }

    private void mostrarDialogoCurso() {
        if (cursos == null || cursos.length == 0) {
            Toast.makeText(this, "Aguarde, os cursos ainda estão carregando.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecione seu Curso");

        builder.setItems(cursos, (dialog, which) -> {
            String cursoSelecionado = cursos[which];

            txtCurso.setText(cursoSelecionado);
            txtCurso.setTextColor(0xFFFFFFFF);

            txtAno.setText("Selecione o Ano/Módulo");
            txtAno.setTextColor(0xFF8A93B8);

            txtTurno.setText("Selecione o Turno");
            txtTurno.setTextColor(0xFF8A93B8);

            filtrarAnosPorCurso(cursoSelecionado);
        });

        builder.show();
    }

    private void mostrarDialogoAno() {
        if (anos == null || anos.length == 0) {
            Toast.makeText(this, "Nenhum ano disponível para esse curso.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecione seu Ano/Módulo");

        builder.setItems(anos, (dialog, which) -> {
            String anoSelecionado = anos[which];

            txtAno.setText(anoSelecionado);
            txtAno.setTextColor(0xFFFFFFFF);

            txtTurno.setText("Selecione o Turno");
            txtTurno.setTextColor(0xFF8A93B8);

            filtrarTurnosPorCursoEAno(
                    txtCurso.getText().toString(),
                    anoSelecionado
            );
        });

        builder.show();
    }

    private void mostrarDialogoTurno() {
        if (turnos == null || turnos.length == 0) {
            Toast.makeText(this, "Nenhum turno disponível para esse curso e ano.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecione seu Turno");

        builder.setItems(turnos, (dialog, which) -> {
            txtTurno.setText(turnos[which]);
            txtTurno.setTextColor(0xFFFFFFFF);
        });

        builder.show();
    }

    private void filtrarAnosPorCurso(String cursoSelecionado) {
        LinkedHashSet<String> listaAnos = new LinkedHashSet<>();

        for (String cursoCompleto : todosCursosCompletos) {
            String nomeCurso = removerAno(cursoCompleto);
            String ano = extrairAno(cursoCompleto);

            if (nomeCurso.equalsIgnoreCase(cursoSelecionado.trim()) && !ano.isEmpty()) {
                listaAnos.add(ano);
            }
        }

        ArrayList<String> anosOrdenados = new ArrayList<>(listaAnos);

        Collections.sort(anosOrdenados, (a, b) -> {
            try {
                return Integer.compare(Integer.parseInt(a), Integer.parseInt(b));
            } catch (NumberFormatException e) {
                return a.compareTo(b);
            }
        });

        anos = anosOrdenados.toArray(new String[0]);
        turnos = new String[]{};
    }

    private void filtrarTurnosPorCursoEAno(String cursoSelecionado, String anoSelecionado) {
        LinkedHashSet<String> listaTurnos = new LinkedHashSet<>();

        String cursoCompletoEsperado = cursoSelecionado.trim() + " " + anoSelecionado.trim();

        for (int i = 0; i < todosCursosCompletos.size(); i++) {
            String cursoCompleto = todosCursosCompletos.get(i);
            String turno = todosTurnos.get(i);

            if (cursoCompleto.equalsIgnoreCase(cursoCompletoEsperado)
                    && turno != null
                    && !turno.trim().isEmpty()) {
                listaTurnos.add(turno.trim());
            }
        }

        ArrayList<String> turnosOrdenados = new ArrayList<>(listaTurnos);

        Collections.sort(turnosOrdenados, (a, b) -> {
            int ordemA = ordemTurno(a);
            int ordemB = ordemTurno(b);

            if (ordemA != ordemB) {
                return Integer.compare(ordemA, ordemB);
            }

            return a.compareToIgnoreCase(b);
        });

        turnos = turnosOrdenados.toArray(new String[0]);
    }

    private void validarEEntrar() {
        String cursoSelecionado = txtCurso.getText().toString();
        String anoSelecionado = txtAno.getText().toString();
        String turnoSelecionado = txtTurno.getText().toString();

        if (cursoSelecionado.equals("Selecione o Curso")) {
            Toast.makeText(this, "Selecione o curso.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (anoSelecionado.equals("Selecione o Ano/Módulo")) {
            Toast.makeText(this, "Selecione o ano/módulo.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (turnoSelecionado.equals("Selecione o Turno")) {
            Toast.makeText(this, "Selecione o turno.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!existeCombinacao(cursoSelecionado, anoSelecionado, turnoSelecionado)) {
            Toast.makeText(this, "Essa combinação de curso, ano e turno não existe.", Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(MainActivity.this, AlunoActivity.class);

        intent.putExtra("curso", cursoSelecionado);
        intent.putExtra("ano", anoSelecionado);
        intent.putExtra("turno", turnoSelecionado);

        startActivity(intent);
    }

    private boolean existeCombinacao(String cursoSelecionado, String anoSelecionado, String turnoSelecionado) {
        String cursoCompletoEsperado = cursoSelecionado.trim() + " " + anoSelecionado.trim();

        for (int i = 0; i < todosCursosCompletos.size(); i++) {
            String cursoCompleto = todosCursosCompletos.get(i);
            String turno = todosTurnos.get(i);

            if (cursoCompleto.equalsIgnoreCase(cursoCompletoEsperado)
                    && turno != null
                    && turno.equalsIgnoreCase(turnoSelecionado.trim())) {
                return true;
            }
        }

        return false;
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
        return cursoCompleto.trim().replaceAll("\\s\\d{4}$", "").trim();
    }

    private int ordemTurno(String turno) {
        String t = turno.toLowerCase().trim();

        if (t.contains("manhã") || t.contains("manha")) {
            return 1;
        }

        if (t.contains("tarde")) {
            return 2;
        }

        if (t.contains("noite")) {
            return 3;
        }

        return 4;
    }
}