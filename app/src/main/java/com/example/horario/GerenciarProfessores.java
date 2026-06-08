package com.example.horario;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GerenciarProfessores extends AppCompatActivity {

    private Button btnVoltar;
    private LinearLayout layoutProfessores;
    private TextView textTotalProfessores;

    private FirebaseFirestore db;

    private final ArrayList<Map<String, Object>> listaProfessores = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gerenciar_professores);

        db = FirebaseFirestore.getInstance();

        btnVoltar = findViewById(R.id.btnVoltar);
        layoutProfessores = findViewById(R.id.layoutProfessores);
        textTotalProfessores = findViewById(R.id.textTotalProfessores);

        btnVoltar.setOnClickListener(v -> finish());

        carregarProfessores();
    }

    private void carregarProfessores() {
        layoutProfessores.removeAllViews();
        listaProfessores.clear();

        db.collection("grade")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    HashMap<String, Map<String, Object>> professoresUnicos = new HashMap<>();

                    for (QueryDocumentSnapshot documento : queryDocumentSnapshots) {
                        String nome = documento.getString("Professor");
                        String email = documento.getString("EmailProfessor");

                        if (nome == null || nome.trim().isEmpty()
                                || email == null || email.trim().isEmpty()) {
                            continue;
                        }

                        Map<String, String> vinculo = new HashMap<>();
                        vinculo.put("curso", valorSeguro(documento.getString("Curso")));
                        vinculo.put("disciplina", valorSeguro(documento.getString("Disciplina")));
                        vinculo.put("turma", valorSeguro(documento.getString("Turma")));
                        vinculo.put("turno", valorSeguro(documento.getString("Turno")));
                        vinculo.put("dia", valorSeguro(documento.getString("Dia")));
                        vinculo.put("horario", valorSeguro(documento.getString("Horario")));

                        if (!professoresUnicos.containsKey(email)) {
                            Map<String, Object> professor = new HashMap<>();

                            ArrayList<Map<String, String>> vinculos = new ArrayList<>();
                            vinculos.add(vinculo);

                            professor.put("nome", nome);
                            professor.put("email", email);
                            professor.put("expandido", false);
                            professor.put("vinculos", vinculos);

                            professoresUnicos.put(email, professor);
                        } else {
                            Map<String, Object> professor = professoresUnicos.get(email);

                            if (professor != null) {
                                @SuppressWarnings("unchecked")
                                ArrayList<Map<String, String>> vinculos =
                                        (ArrayList<Map<String, String>>) professor.get("vinculos");

                                if (vinculos != null) {
                                    vinculos.add(vinculo);
                                }
                            }
                        }
                    }

                    listaProfessores.addAll(professoresUnicos.values());
                    mostrarProfessoresNaTela();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao carregar professores: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void mostrarProfessoresNaTela() {
        layoutProfessores.removeAllViews();

        int total = listaProfessores.size();
        textTotalProfessores.setText(total == 1 ? "1 professor" : total + " professores");

        if (listaProfessores.isEmpty()) {
            TextView vazio = new TextView(this);
            vazio.setText("Nenhum professor cadastrado na grade.");
            vazio.setTextColor(Color.parseColor("#8F9BBC"));
            vazio.setTextSize(15);
            vazio.setGravity(Gravity.CENTER);
            vazio.setPadding(30, 40, 30, 40);
            vazio.setBackgroundResource(R.drawable.bg_vinculo_vazio);
            layoutProfessores.addView(vazio);
            return;
        }

        for (Map<String, Object> professor : listaProfessores) {
            String nome = String.valueOf(professor.get("nome"));
            String email = String.valueOf(professor.get("email"));
            boolean expandido = Boolean.TRUE.equals(professor.get("expandido"));

            @SuppressWarnings("unchecked")
            ArrayList<Map<String, String>> vinculos =
                    (ArrayList<Map<String, String>>) professor.get("vinculos");

            if (vinculos == null) {
                vinculos = new ArrayList<>();
            }

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(20, 20, 20, 20);
            card.setBackgroundResource(R.drawable.bg_card_vinculos_moderno);

            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 0, 0, 18);
            card.setLayoutParams(cardParams);

            TextView txtNome = new TextView(this);
            txtNome.setText("👨‍🏫 " + nome);
            txtNome.setTextColor(Color.WHITE);
            txtNome.setTextSize(18);
            txtNome.setTypeface(null, Typeface.BOLD);

            TextView txtEmail = new TextView(this);
            txtEmail.setText("📧 " + email);
            txtEmail.setTextColor(Color.parseColor("#AEB9D8"));
            txtEmail.setTextSize(13);
            txtEmail.setPadding(0, 6, 0, 0);

            TextView txtQuantidade = new TextView(this);
            txtQuantidade.setText(vinculos.size() == 1 ? "1 vínculo" : vinculos.size() + " vínculos");
            txtQuantidade.setTextColor(Color.parseColor("#16E0C4"));
            txtQuantidade.setTextSize(14);
            txtQuantidade.setTypeface(null, Typeface.BOLD);
            txtQuantidade.setPadding(0, 10, 0, 0);

            Button btnVerVinculos = criarBotao(
                    expandido ? "▲ OCULTAR VÍNCULOS" : "▼ VER VÍNCULOS",
                    "#081B55"
            );

            btnVerVinculos.setOnClickListener(v -> {
                professor.put("expandido", !expandido);
                mostrarProfessoresNaTela();
            });

            Button btnEditar = criarBotao("✏ EDITAR PROFESSOR", "#7B61FF");
            btnEditar.setOnClickListener(v -> abrirDialogEditarProfessor(nome, email));

            Button btnAdicionar = criarBotao("➕ ADICIONAR VÍNCULO", "#09C7B0");
            btnAdicionar.setOnClickListener(v -> abrirDialogAdicionarVinculo(nome, email));

            Button btnExcluir = criarBotao("🗑 EXCLUIR PROFESSOR", "#E84142");
            btnExcluir.setOnClickListener(v -> confirmarExclusao(nome, email));

            card.addView(txtNome);
            card.addView(txtEmail);
            card.addView(txtQuantidade);
            card.addView(btnVerVinculos);

            if (expandido) {
                TextView titulo = new TextView(this);
                titulo.setText("Horários e vínculos cadastrados:");
                titulo.setTextColor(Color.WHITE);
                titulo.setTextSize(15);
                titulo.setTypeface(null, Typeface.BOLD);
                titulo.setPadding(0, 18, 0, 10);
                card.addView(titulo);

                for (int i = 0; i < vinculos.size(); i++) {
                    Map<String, String> vinculo = vinculos.get(i);

                    TextView txtVinculo = new TextView(this);
                    txtVinculo.setText(
                            (i + 1) + ". Curso: " + vinculo.get("curso") +
                                    "\nMatéria: " + vinculo.get("disciplina") +
                                    "\nTurma: " + vinculo.get("turma") +
                                    "\nTurno: " + vinculo.get("turno") +
                                    "\nDia: " + vinculo.get("dia") +
                                    "\nHorário: " + vinculo.get("horario")
                    );

                    txtVinculo.setTextColor(Color.parseColor("#16E0C4"));
                    txtVinculo.setTextSize(13);
                    txtVinculo.setPadding(16, 12, 16, 12);
                    txtVinculo.setBackgroundResource(R.drawable.bg_item_vinculo_moderno);

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(0, 0, 0, 10);
                    txtVinculo.setLayoutParams(params);

                    card.addView(txtVinculo);
                }
            }

            card.addView(btnEditar);
            card.addView(btnAdicionar);
            card.addView(btnExcluir);

            layoutProfessores.addView(card);
        }
    }

    private Button criarBotao(String texto, String cor) {
        Button botao = new Button(this);
        botao.setText(texto);
        botao.setTextColor(Color.WHITE);
        botao.setTextSize(13);
        botao.setTypeface(null, Typeface.BOLD);
        botao.setAllCaps(false);
        botao.setBackgroundColor(Color.parseColor(cor));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                56
        );
        params.setMargins(0, 14, 0, 0);
        botao.setLayoutParams(params);

        return botao;
    }

    private EditText criarCampo(String hint, String texto) {
        EditText campo = new EditText(this);
        campo.setHint(hint);
        campo.setText(texto);
        campo.setTextColor(Color.WHITE);
        campo.setHintTextColor(Color.parseColor("#8F9BBC"));
        campo.setSingleLine(true);
        campo.setPadding(20, 10, 20, 10);
        campo.setBackgroundResource(R.drawable.bg_input_neon);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                58
        );
        params.setMargins(0, 10, 0, 10);
        campo.setLayoutParams(params);

        return campo;
    }

    private void abrirDialogEditarProfessor(String nomeAtual, String emailAtual) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(30, 20, 30, 10);

        EditText editNome = criarCampo("Nome do professor", nomeAtual);
        EditText editEmail = criarCampo("E-mail do professor", emailAtual);
        editEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        layout.addView(editNome);
        layout.addView(editEmail);

        new AlertDialog.Builder(this)
                .setTitle("Editar professor")
                .setView(layout)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String novoNome = editNome.getText().toString().trim();
                    String novoEmail = editEmail.getText().toString().trim();

                    if (novoNome.isEmpty() || novoEmail.isEmpty()) {
                        Toast.makeText(this, "Preencha nome e e-mail.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    atualizarProfessor(emailAtual, novoNome, novoEmail);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void atualizarProfessor(String emailAtual, String novoNome, String novoEmail) {
        db.collection("grade")
                .whereEqualTo("EmailProfessor", emailAtual)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    WriteBatch batch = db.batch();

                    for (QueryDocumentSnapshot documento : queryDocumentSnapshots) {
                        batch.update(documento.getReference(),
                                "Professor", novoNome,
                                "EmailProfessor", novoEmail
                        );
                    }

                    batch.commit()
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Professor atualizado!", Toast.LENGTH_SHORT).show();
                                carregarProfessores();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Erro ao atualizar: " + e.getMessage(), Toast.LENGTH_LONG).show()
                            );
                });
    }

    private void abrirDialogAdicionarVinculo(String nomeProfessor, String emailProfessor) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(30, 20, 30, 10);

        EditText editCurso = criarCampo("Curso", "");
        EditText editDisciplina = criarCampo("Matéria", "");
        EditText editTurma = criarCampo("Turma", "");
        EditText editTurno = criarCampo("Turno", "");
        EditText editDia = criarCampo("Dia", "");
        EditText editHorario = criarCampo("Horário", "");

        layout.addView(editCurso);
        layout.addView(editDisciplina);
        layout.addView(editTurma);
        layout.addView(editTurno);
        layout.addView(editDia);
        layout.addView(editHorario);

        new AlertDialog.Builder(this)
                .setTitle("Adicionar vínculo")
                .setView(layout)
                .setPositiveButton("Adicionar", (dialog, which) -> {
                    String curso = editCurso.getText().toString().trim();
                    String disciplina = editDisciplina.getText().toString().trim();
                    String turma = editTurma.getText().toString().trim();
                    String turno = editTurno.getText().toString().trim();
                    String dia = editDia.getText().toString().trim();
                    String horario = editHorario.getText().toString().trim();

                    if (curso.isEmpty() || disciplina.isEmpty() || turma.isEmpty()
                            || turno.isEmpty() || dia.isEmpty() || horario.isEmpty()) {
                        Toast.makeText(this, "Preencha todos os campos.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    adicionarVinculo(nomeProfessor, emailProfessor, curso, disciplina, turma, turno, dia, horario);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void adicionarVinculo(
            String nomeProfessor,
            String emailProfessor,
            String curso,
            String disciplina,
            String turma,
            String turno,
            String dia,
            String horario
    ) {
        DocumentReference ref = db.collection("grade").document();

        Map<String, Object> dados = new HashMap<>();
        dados.put("Professor", nomeProfessor);
        dados.put("EmailProfessor", emailProfessor);
        dados.put("Curso", curso);
        dados.put("Disciplina", disciplina);
        dados.put("Turma", turma);
        dados.put("Turno", turno);
        dados.put("Dia", dia);
        dados.put("Horario", horario);

        ref.set(dados)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Vínculo adicionado!", Toast.LENGTH_SHORT).show();
                    carregarProfessores();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao adicionar vínculo: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void confirmarExclusao(String nome, String email) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir professor")
                .setMessage("Tem certeza que deseja excluir o professor " + nome + "?\n\nTodos os vínculos dele serão apagados da coleção grade.")
                .setPositiveButton("Sim, excluir", (dialog, which) -> deletarProfessor(email))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void deletarProfessor(String emailProfessor) {
        db.collection("grade")
                .whereEqualTo("EmailProfessor", emailProfessor)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(this, "Nenhum vínculo encontrado para excluir.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    WriteBatch batch = db.batch();

                    for (QueryDocumentSnapshot documento : queryDocumentSnapshots) {
                        batch.delete(documento.getReference());
                    }

                    batch.commit()
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Professor excluído com sucesso!", Toast.LENGTH_SHORT).show();
                                carregarProfessores();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Erro ao excluir: " + e.getMessage(), Toast.LENGTH_LONG).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao buscar professor: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private String valorSeguro(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return "Não informado";
        }
        return texto;
    }
}