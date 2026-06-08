package com.example.horario;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CadastroProfessorActivity extends AppCompatActivity {

    private EditText editNomeProfessor;
    private MaterialAutoCompleteTextView dropCurso, dropMateria, dropTurno, dropDia, dropHorario;
    private MaterialButton btnVoltar;
    private Button btnAdicionarVinculo, btnSalvar;
    private LinearLayout layoutVinculos;
    private TextView textTotalVinculos;

    private FirebaseFirestore db;

    private final ArrayList<String> listaCursos = new ArrayList<>();
    private final ArrayList<String> listaMaterias = new ArrayList<>();
    private final ArrayList<String> listaTurnos = new ArrayList<>();
    private final ArrayList<String> listaDias = new ArrayList<>();
    private final ArrayList<String> listaHorarios = new ArrayList<>();
    private final ArrayList<Map<String, String>> vinculosAdicionados = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_professor);

        db = FirebaseFirestore.getInstance();

        editNomeProfessor = findViewById(R.id.editNomeProfessor);
        dropCurso = findViewById(R.id.dropCurso);
        dropMateria = findViewById(R.id.dropMateria);
        dropTurno = findViewById(R.id.dropTurno);
        dropDia = findViewById(R.id.dropDia);
        dropHorario = findViewById(R.id.dropHorario);

        btnVoltar = findViewById(R.id.btnVoltar);
        btnAdicionarVinculo = findViewById(R.id.btnAdicionarVinculo);
        btnSalvar = findViewById(R.id.btnSalvarProfessor);

        layoutVinculos = findViewById(R.id.layoutVinculos);
        textTotalVinculos = findViewById(R.id.textTotalVinculos);

        btnVoltar.setOnClickListener(v -> finish());
        btnAdicionarVinculo.setOnClickListener(v -> adicionarVinculo());
        btnSalvar.setOnClickListener(v -> salvarNaGrade());

        carregarDadosFirebase();
        mostrarVinculosNaTela();
    }

    private void carregarDadosFirebase() {
        db.collection("grade")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    listaCursos.clear();
                    listaMaterias.clear();
                    listaTurnos.clear();
                    listaDias.clear();
                    listaHorarios.clear();

                    for (QueryDocumentSnapshot documento : queryDocumentSnapshots) {
                        adicionarSeNaoExiste(listaCursos, documento.getString("Curso"));
                        adicionarSeNaoExiste(listaMaterias, documento.getString("Disciplina"));
                        adicionarSeNaoExiste(listaTurnos, documento.getString("Turno"));
                        adicionarSeNaoExiste(listaDias, documento.getString("Dia"));
                        adicionarSeNaoExiste(listaHorarios, documento.getString("Horario"));
                    }

                    Collections.sort(listaCursos);
                    Collections.sort(listaMaterias);
                    ordenarTurnos();
                    ordenarDias();
                    Collections.sort(listaHorarios);

                    configurarDropdown(dropCurso, listaCursos);
                    configurarDropdown(dropMateria, listaMaterias);
                    configurarDropdown(dropTurno, listaTurnos);
                    configurarDropdown(dropDia, listaDias);
                    configurarDropdown(dropHorario, listaHorarios);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao carregar dados: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void configurarDropdown(MaterialAutoCompleteTextView campo, ArrayList<String> lista) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                lista
        );

        campo.setAdapter(adapter);
        campo.setThreshold(0);

        campo.setOnClickListener(v -> campo.showDropDown());

        campo.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                campo.showDropDown();
            }
        });
    }

    private void adicionarVinculo() {
        String curso = dropCurso.getText().toString().trim();
        String materia = dropMateria.getText().toString().trim();
        String turno = dropTurno.getText().toString().trim();
        String dia = dropDia.getText().toString().trim();
        String horario = dropHorario.getText().toString().trim();

        if (curso.isEmpty()) {
            Toast.makeText(this, "Selecione ou digite o curso/ano.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (materia.isEmpty()) {
            Toast.makeText(this, "Selecione ou digite a matéria.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (turno.isEmpty()) {
            Toast.makeText(this, "Selecione ou digite o turno.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dia.isEmpty()) {
            Toast.makeText(this, "Selecione ou digite o dia.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (horario.isEmpty()) {
            Toast.makeText(this, "Selecione ou digite o horário.", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Map<String, String> vinculo : vinculosAdicionados) {
            if (vinculo.get("curso").equalsIgnoreCase(curso)
                    && vinculo.get("materia").equalsIgnoreCase(materia)
                    && vinculo.get("turno").equalsIgnoreCase(turno)
                    && vinculo.get("dia").equalsIgnoreCase(dia)
                    && vinculo.get("horario").equalsIgnoreCase(horario)) {

                Toast.makeText(this, "Esse vínculo já foi adicionado.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Map<String, String> vinculo = new HashMap<>();
        vinculo.put("curso", curso);
        vinculo.put("materia", materia);
        vinculo.put("turno", turno);
        vinculo.put("dia", dia);
        vinculo.put("horario", horario);

        vinculosAdicionados.add(vinculo);

        limparCamposVinculo();
        mostrarVinculosNaTela();
    }

    private void mostrarVinculosNaTela() {
        layoutVinculos.removeAllViews();

        int total = vinculosAdicionados.size();

        if (total == 0) {
            textTotalVinculos.setText("0 vínculo");

            TextView vazio = new TextView(this);
            vazio.setText("Nenhum vínculo adicionado ainda.");
            vazio.setTextColor(Color.parseColor("#8F9BBC"));
            vazio.setTextSize(14);
            vazio.setGravity(Gravity.CENTER);
            vazio.setPadding(dp(22), dp(22), dp(22), dp(22));
            vazio.setBackgroundResource(R.drawable.bg_vinculo_vazio);

            layoutVinculos.addView(vazio);
            return;
        }

        textTotalVinculos.setText(total == 1 ? "1 vínculo" : total + " vínculos");

        for (int i = 0; i < vinculosAdicionados.size(); i++) {
            final int posicao = i;
            Map<String, String> vinculo = vinculosAdicionados.get(i);

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(18), dp(18), dp(18), dp(18));
            card.setBackgroundResource(R.drawable.bg_item_vinculo_moderno);

            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 0, 0, dp(16));
            card.setLayoutParams(cardParams);

            LinearLayout topo = new LinearLayout(this);
            topo.setOrientation(LinearLayout.HORIZONTAL);
            topo.setGravity(Gravity.CENTER_VERTICAL);

            TextView numero = new TextView(this);
            numero.setText(String.valueOf(i + 1));
            numero.setTextColor(Color.WHITE);
            numero.setTextSize(17);
            numero.setTypeface(null, Typeface.BOLD);
            numero.setGravity(Gravity.CENTER);
            numero.setBackgroundResource(R.drawable.bg_numero_vinculo);

            LinearLayout.LayoutParams numeroParams = new LinearLayout.LayoutParams(dp(46), dp(46));
            numeroParams.setMargins(0, 0, 0, 0);
            numero.setLayoutParams(numeroParams);

            TextView tituloVinculo = new TextView(this);
            tituloVinculo.setText("Vínculo adicionado");
            tituloVinculo.setTextColor(Color.WHITE);
            tituloVinculo.setTextSize(16);
            tituloVinculo.setTypeface(null, Typeface.BOLD);

            LinearLayout.LayoutParams tituloParams = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1
            );
            tituloParams.setMargins(dp(14), 0, dp(14), 0);
            tituloVinculo.setLayoutParams(tituloParams);

            ImageView btnExcluir = new ImageView(this);
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(42), dp(42));
            btnExcluir.setLayoutParams(iconParams);
            btnExcluir.setImageResource(R.drawable.ic_delete_custom);
            btnExcluir.setColorFilter(Color.parseColor("#FF5C5C"));
            btnExcluir.setPadding(dp(9), dp(9), dp(9), dp(9));
            btnExcluir.setBackgroundResource(R.drawable.bg_delete_button);
            btnExcluir.setContentDescription("Excluir vínculo");

            btnExcluir.setOnClickListener(v -> {
                vinculosAdicionados.remove(posicao);
                Toast.makeText(this, "Vínculo removido.", Toast.LENGTH_SHORT).show();
                mostrarVinculosNaTela();
            });

            topo.addView(numero);
            topo.addView(tituloVinculo);
            topo.addView(btnExcluir);

            TextView disciplina = criarLinhaInfo("Disciplina", vinculo.get("materia"));
            TextView curso = criarLinhaInfo("Curso", vinculo.get("curso"));
            TextView turno = criarLinhaInfo("Turno", vinculo.get("turno"));
            TextView dia = criarLinhaInfo("Dia", vinculo.get("dia"));
            TextView horario = criarLinhaInfo("Horário", vinculo.get("horario"));

            card.addView(topo);
            card.addView(disciplina);
            card.addView(curso);
            card.addView(turno);
            card.addView(dia);
            card.addView(horario);

            layoutVinculos.addView(card);
        }
    }

    private TextView criarLinhaInfo(String titulo, String valor) {
        TextView texto = new TextView(this);

        if (valor == null || valor.trim().isEmpty()) {
            valor = "Não informado";
        }

        texto.setText(titulo + ": " + valor);
        texto.setTextColor(Color.parseColor("#16E0C4"));
        texto.setTextSize(14);
        texto.setPadding(0, dp(8), 0, 0);
        texto.setSingleLine(false);

        return texto;
    }

    private void salvarNaGrade() {
        String nomeProfessor = editNomeProfessor.getText().toString().trim();

        if (nomeProfessor.isEmpty()) {
            editNomeProfessor.setError("Digite o nome do professor");
            return;
        }

        if (vinculosAdicionados.isEmpty()) {
            Toast.makeText(this, "Adicione pelo menos um vínculo.", Toast.LENGTH_SHORT).show();
            return;
        }

        WriteBatch batch = db.batch();

        for (Map<String, String> vinculo : vinculosAdicionados) {
            DocumentReference gradeRef = db.collection("grade").document();

            Map<String, Object> dadosGrade = new HashMap<>();
            dadosGrade.put("Curso", vinculo.get("curso"));
            dadosGrade.put("Disciplina", vinculo.get("materia"));
            dadosGrade.put("Turno", vinculo.get("turno"));
            dadosGrade.put("Dia", vinculo.get("dia"));
            dadosGrade.put("Horario", vinculo.get("horario"));
            dadosGrade.put("Professor", nomeProfessor);

            batch.set(gradeRef, dadosGrade);
        }

        batch.commit()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Professor salvo na grade com sucesso!", Toast.LENGTH_SHORT).show();
                    limparTudo();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao salvar: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void limparCamposVinculo() {
        dropCurso.setText("");
        dropMateria.setText("");
        dropTurno.setText("");
        dropDia.setText("");
        dropHorario.setText("");
    }

    private void limparTudo() {
        editNomeProfessor.setText("");
        limparCamposVinculo();
        vinculosAdicionados.clear();
        mostrarVinculosNaTela();
    }

    private void adicionarSeNaoExiste(ArrayList<String> lista, String valor) {
        if (valor == null) return;

        String valorLimpo = valor.trim();

        if (valorLimpo.isEmpty()) return;

        for (String item : lista) {
            if (item.equalsIgnoreCase(valorLimpo)) {
                return;
            }
        }

        lista.add(valorLimpo);
    }

    private void ordenarTurnos() {
        Collections.sort(listaTurnos, (a, b) -> {
            int ordemA = ordemTurno(a);
            int ordemB = ordemTurno(b);

            if (ordemA != ordemB) {
                return Integer.compare(ordemA, ordemB);
            }

            return a.compareToIgnoreCase(b);
        });
    }

    private int ordemTurno(String turno) {
        String t = turno.toLowerCase().trim();

        if (t.contains("manhã") || t.contains("manha")) return 1;
        if (t.contains("tarde")) return 2;
        if (t.contains("noite")) return 3;

        return 4;
    }

    private void ordenarDias() {
        Collections.sort(listaDias, (a, b) -> {
            int ordemA = ordemDia(a);
            int ordemB = ordemDia(b);

            if (ordemA != ordemB) {
                return Integer.compare(ordemA, ordemB);
            }

            return a.compareToIgnoreCase(b);
        });
    }

    private int ordemDia(String dia) {
        String d = dia.toLowerCase().trim();

        if (d.contains("segunda")) return 1;
        if (d.contains("terça") || d.contains("terca")) return 2;
        if (d.contains("quarta")) return 3;
        if (d.contains("quinta")) return 4;
        if (d.contains("sexta")) return 5;
        if (d.contains("sábado") || d.contains("sabado")) return 6;
        if (d.contains("domingo")) return 7;

        return 8;
    }

    private int dp(int valor) {
        return (int) (valor * getResources().getDisplayMetrics().density);
    }
}