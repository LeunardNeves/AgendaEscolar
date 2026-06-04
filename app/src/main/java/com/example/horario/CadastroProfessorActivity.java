package com.example.horario;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CadastroProfessorActivity extends AppCompatActivity {

    private EditText editNomeProfessor, editEmailProfessor;
    private MaterialAutoCompleteTextView dropCurso, dropMateria;
    private Button btnVoltar, btnAdicionarVinculo, btnSalvar;
    private LinearLayout layoutVinculos;
    private TextView textTotalVinculos;

    private FirebaseFirestore db;

    private final ArrayList<String> listaCursos = new ArrayList<>();
    private final ArrayList<String> listaMaterias = new ArrayList<>();
    private final ArrayList<Map<String, String>> dadosMaterias = new ArrayList<>();
    private final ArrayList<Map<String, String>> vinculosAdicionados = new ArrayList<>();

    private String cursoSelecionado = "";
    private String disciplinaSelecionada = "";
    private String turnoSelecionado = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_professor);

        db = FirebaseFirestore.getInstance();

        editNomeProfessor = findViewById(R.id.editNomeProfessor);
        editEmailProfessor = findViewById(R.id.editEmailProfessor);
        dropCurso = findViewById(R.id.dropCurso);
        dropMateria = findViewById(R.id.dropMateria);
        btnVoltar = findViewById(R.id.btnVoltar);
        btnAdicionarVinculo = findViewById(R.id.btnAdicionarVinculo);
        btnSalvar = findViewById(R.id.btnSalvarProfessor);
        layoutVinculos = findViewById(R.id.layoutVinculos);
        textTotalVinculos = findViewById(R.id.textTotalVinculos);

        btnVoltar.setOnClickListener(v -> finish());

        carregarCursosFirebase();

        dropCurso.setOnItemClickListener((parent, view, position, id) -> {
            cursoSelecionado = listaCursos.get(position);

            dropMateria.setText("");
            disciplinaSelecionada = "";
            turnoSelecionado = "";

            carregarMateriasDoCurso(cursoSelecionado);
        });

        dropMateria.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < dadosMaterias.size()) {
                Map<String, String> materia = dadosMaterias.get(position);

                disciplinaSelecionada = materia.get("Disciplina");
                turnoSelecionado = materia.get("Turno");
            }
        });

        btnAdicionarVinculo.setOnClickListener(v -> adicionarVinculo());
        btnSalvar.setOnClickListener(v -> salvarNaGrade());
    }

    private void carregarCursosFirebase() {
        db.collection("grade")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    listaCursos.clear();

                    for (QueryDocumentSnapshot documento : queryDocumentSnapshots) {
                        String curso = documento.getString("Curso");

                        if (curso != null && !curso.trim().isEmpty() && !listaCursos.contains(curso)) {
                            listaCursos.add(curso);
                        }
                    }

                    ArrayAdapter<String> adapterCurso = new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_dropdown_item_1line,
                            listaCursos
                    );

                    dropCurso.setAdapter(adapterCurso);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao carregar cursos: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void carregarMateriasDoCurso(String curso) {
        db.collection("grade")
                .whereEqualTo("Curso", curso)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    listaMaterias.clear();
                    dadosMaterias.clear();

                    for (QueryDocumentSnapshot documento : queryDocumentSnapshots) {
                        String disciplina = documento.getString("Disciplina");
                        String turno = documento.getString("Turno");

                        if (disciplina != null && turno != null) {
                            String textoMateria = disciplina + " - " + turno;

                            if (!listaMaterias.contains(textoMateria)) {
                                listaMaterias.add(textoMateria);

                                Map<String, String> dados = new HashMap<>();
                                dados.put("Disciplina", disciplina);
                                dados.put("Turno", turno);

                                dadosMaterias.add(dados);
                            }
                        }
                    }

                    ArrayAdapter<String> adapterMateria = new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_dropdown_item_1line,
                            listaMaterias
                    );

                    dropMateria.setAdapter(adapterMateria);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao carregar matérias: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void adicionarVinculo() {
        if (cursoSelecionado.isEmpty()) {
            Toast.makeText(this, "Selecione um curso", Toast.LENGTH_SHORT).show();
            return;
        }

        if (disciplinaSelecionada == null || disciplinaSelecionada.isEmpty()) {
            Toast.makeText(this, "Selecione uma matéria", Toast.LENGTH_SHORT).show();
            return;
        }

        if (turnoSelecionado == null || turnoSelecionado.isEmpty()) {
            Toast.makeText(this, "Turno não encontrado", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Map<String, String> vinculo : vinculosAdicionados) {
            if (vinculo.get("curso").equals(cursoSelecionado)
                    && vinculo.get("disciplina").equals(disciplinaSelecionada)
                    && vinculo.get("turno").equals(turnoSelecionado)) {

                Toast.makeText(this, "Esse vínculo já foi adicionado", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Map<String, String> vinculo = new HashMap<>();
        vinculo.put("curso", cursoSelecionado);
        vinculo.put("disciplina", disciplinaSelecionada);
        vinculo.put("turno", turnoSelecionado);

        vinculosAdicionados.add(vinculo);

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
            vazio.setPadding(22, 22, 22, 22);
            vazio.setBackgroundResource(R.drawable.bg_vinculo_vazio);

            layoutVinculos.addView(vazio);
            return;
        }

        if (total == 1) {
            textTotalVinculos.setText("1 vínculo");
        } else {
            textTotalVinculos.setText(total + " vínculos");
        }

        for (int i = 0; i < vinculosAdicionados.size(); i++) {
            final int posicao = i;
            Map<String, String> vinculo = vinculosAdicionados.get(i);

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setGravity(Gravity.CENTER_VERTICAL);
            card.setPadding(14, 14, 14, 14);
            card.setBackgroundResource(R.drawable.bg_item_vinculo_moderno);

            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 0, 0, 14);
            card.setLayoutParams(cardParams);

            TextView numero = new TextView(this);
            numero.setText(String.valueOf(i + 1));
            numero.setTextColor(Color.WHITE);
            numero.setTextSize(16);
            numero.setGravity(Gravity.CENTER);
            numero.setTypeface(null, Typeface.BOLD);
            numero.setBackgroundResource(R.drawable.bg_numero_vinculo);

            LinearLayout.LayoutParams numeroParams = new LinearLayout.LayoutParams(44, 44);
            numeroParams.setMargins(0, 0, 14, 0);
            numero.setLayoutParams(numeroParams);

            LinearLayout textos = new LinearLayout(this);
            textos.setOrientation(LinearLayout.VERTICAL);
            textos.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1
            ));

            TextView disciplina = new TextView(this);
            disciplina.setText(vinculo.get("disciplina"));
            disciplina.setTextColor(Color.WHITE);
            disciplina.setTextSize(15);
            disciplina.setTypeface(null, Typeface.BOLD);

            TextView cursoTurno = new TextView(this);
            cursoTurno.setText(vinculo.get("curso") + "  |  " + vinculo.get("turno"));
            cursoTurno.setTextColor(Color.parseColor("#16E0C4"));
            cursoTurno.setTextSize(13);
            cursoTurno.setPadding(0, 4, 0, 0);

            textos.addView(disciplina);
            textos.addView(cursoTurno);

            TextView btnExcluir = new TextView(this);
            btnExcluir.setText("🗑");
            btnExcluir.setTextSize(22);
            btnExcluir.setGravity(Gravity.CENTER);
            btnExcluir.setTextColor(Color.parseColor("#FF6B6B"));
            btnExcluir.setPadding(14, 8, 8, 8);

            btnExcluir.setOnClickListener(v -> {
                vinculosAdicionados.remove(posicao);
                Toast.makeText(this, "Vínculo removido", Toast.LENGTH_SHORT).show();
                mostrarVinculosNaTela();
            });

            card.addView(numero);
            card.addView(textos);
            card.addView(btnExcluir);

            layoutVinculos.addView(card);
        }
    }

    private void salvarNaGrade() {
        String nomeProfessor = editNomeProfessor.getText().toString().trim();
        String emailProfessor = editEmailProfessor.getText().toString().trim();

        if (nomeProfessor.isEmpty()) {
            editNomeProfessor.setError("Digite o nome do professor");
            return;
        }

        if (emailProfessor.isEmpty()) {
            editEmailProfessor.setError("Digite o e-mail/login");
            return;
        }

        if (vinculosAdicionados.isEmpty()) {
            Toast.makeText(this, "Adicione pelo menos um vínculo", Toast.LENGTH_SHORT).show();
            return;
        }

        WriteBatch batch = db.batch();

        for (Map<String, String> vinculo : vinculosAdicionados) {
            DocumentReference gradeRef = db.collection("grade").document();

            Map<String, Object> dadosGrade = new HashMap<>();
            dadosGrade.put("Curso", vinculo.get("curso"));
            dadosGrade.put("Disciplina", vinculo.get("disciplina"));
            dadosGrade.put("Turno", vinculo.get("turno"));
            dadosGrade.put("Professor", nomeProfessor);
            dadosGrade.put("EmailProfessor", emailProfessor);

            batch.set(gradeRef, dadosGrade);
        }

        batch.commit()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Professor salvo na grade com sucesso!", Toast.LENGTH_SHORT).show();
                    limparCampos();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao salvar na grade: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void limparCampos() {
        editNomeProfessor.setText("");
        editEmailProfessor.setText("");
        dropCurso.setText("");
        dropMateria.setText("");

        cursoSelecionado = "";
        disciplinaSelecionada = "";
        turnoSelecionado = "";

        vinculosAdicionados.clear();
        mostrarVinculosNaTela();
    }
}