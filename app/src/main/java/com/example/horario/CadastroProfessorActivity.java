package com.example.horario;

import android.os.Bundle;
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

    private FirebaseFirestore db;

    private ArrayList<String> listaCursos = new ArrayList<>();
    private ArrayList<String> listaMaterias = new ArrayList<>();
    private ArrayList<Map<String, String>> dadosMaterias = new ArrayList<>();
    private ArrayList<Map<String, String>> vinculosAdicionados = new ArrayList<>();

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
        btnSalvar.setOnClickListener(v -> salvarProfessor());
    }

    private void carregarCursosFirebase() {
        db.collection("grade")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    listaCursos.clear();

                    for (QueryDocumentSnapshot documento : queryDocumentSnapshots) {
                        String curso = documento.getString("Curso");

                        if (curso != null && !listaCursos.contains(curso)) {
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

        for (int i = 0; i < vinculosAdicionados.size(); i++) {
            Map<String, String> vinculo = vinculosAdicionados.get(i);

            TextView textView = new TextView(this);
            textView.setText((i + 1) + ". " + vinculo.get("disciplina") + " | " + vinculo.get("curso") + " | " + vinculo.get("turno"));
            textView.setTextColor(getResources().getColor(android.R.color.white));
            textView.setTextSize(14);
            textView.setPadding(20, 15, 20, 15);
            textView.setBackgroundResource(R.drawable.bg_item);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            params.setMargins(0, 0, 0, 10);
            textView.setLayoutParams(params);

            layoutVinculos.addView(textView);
        }
    }

    private void salvarProfessor() {
        String nome = editNomeProfessor.getText().toString().trim();
        String email = editEmailProfessor.getText().toString().trim();

        if (nome.isEmpty()) {
            editNomeProfessor.setError("Digite o nome do professor");
            return;
        }

        if (email.isEmpty()) {
            editEmailProfessor.setError("Digite o e-mail/login");
            return;
        }

        if (vinculosAdicionados.isEmpty()) {
            Toast.makeText(this, "Adicione pelo menos um vínculo", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> professor = new HashMap<>();
        professor.put("nome", nome);
        professor.put("email", email);
        professor.put("tipo", "professor");

        db.collection("professores")
                .add(professor)
                .addOnSuccessListener(documentReference -> salvarVinculosProfessor(documentReference, nome))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao salvar professor: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void salvarVinculosProfessor(DocumentReference professorRef, String nomeProfessor) {
        WriteBatch batch = db.batch();

        for (Map<String, String> vinculo : vinculosAdicionados) {
            DocumentReference vinculoRef = db.collection("vinculos_professor").document();

            Map<String, Object> dadosVinculo = new HashMap<>();
            dadosVinculo.put("professorId", professorRef.getId());
            dadosVinculo.put("professorNome", nomeProfessor);
            dadosVinculo.put("curso", vinculo.get("curso"));
            dadosVinculo.put("disciplina", vinculo.get("disciplina"));
            dadosVinculo.put("turno", vinculo.get("turno"));

            batch.set(vinculoRef, dadosVinculo);
        }

        batch.commit()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Professor cadastrado com vínculos!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao salvar vínculos: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}