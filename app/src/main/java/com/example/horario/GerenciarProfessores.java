package com.example.horario;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
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
        db.collection("grade")
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Erro ao carregar professores: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (queryDocumentSnapshots == null) return;

                    listaProfessores.clear();

                    HashMap<String, Map<String, Object>> professoresUnicos = new HashMap<>();

                    for (QueryDocumentSnapshot documento : queryDocumentSnapshots) {
                        String idDocumento = documento.getId();
                        String nome = documento.getString("Professor");

                        if (nome == null || nome.trim().isEmpty()) {
                            continue;
                        }

                        String chaveProfessor = nome.trim().toLowerCase();

                        Map<String, String> vinculo = new HashMap<>();
                        vinculo.put("idDoc", idDocumento);
                        vinculo.put("curso", valorSeguro(documento.getString("Curso")));
                        vinculo.put("disciplina", valorSeguro(documento.getString("Disciplina")));
                        vinculo.put("turma", valorSeguro(documento.getString("Turma")));
                        vinculo.put("turno", valorSeguro(documento.getString("Turno")));
                        vinculo.put("dia", valorSeguro(documento.getString("Dia")));
                        vinculo.put("horario", valorSeguro(documento.getString("Horario")));

                        if (!professoresUnicos.containsKey(chaveProfessor)) {
                            Map<String, Object> professor = new HashMap<>();
                            ArrayList<Map<String, String>> vinculos = new ArrayList<>();
                            vinculos.add(vinculo);

                            professor.put("nome", nome);
                            professor.put("expandido", false);
                            professor.put("vinculos", vinculos);

                            professoresUnicos.put(chaveProfessor, professor);
                        } else {
                            Map<String, Object> professor = professoresUnicos.get(chaveProfessor);

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
                });
    }

    private void mostrarProfessoresNaTela() {
        layoutProfessores.removeAllViews();

        int total = listaProfessores.size();
        textTotalProfessores.setText(total == 1 ? "1 professor" : total + " professores");

        if (listaProfessores.isEmpty()) {
            TextView vazio = new TextView(this);
            vazio.setText("Nenhum professor cadastrado na grade.");
            vazio.setTextColor(Color.parseColor("#8F9BBC"));
            vazio.setGravity(Gravity.CENTER);
            vazio.setTextSize(14);
            vazio.setPadding(dp(30), dp(60), dp(30), dp(60));
            layoutProfessores.addView(vazio);
            return;
        }

        for (Map<String, Object> professor : listaProfessores) {
            String nome = String.valueOf(professor.get("nome"));
            boolean expandido = Boolean.TRUE.equals(professor.get("expandido"));

            @SuppressWarnings("unchecked")
            ArrayList<Map<String, String>> vinculos =
                    (ArrayList<Map<String, String>>) professor.get("vinculos");

            if (vinculos == null) vinculos = new ArrayList<>();

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(18), dp(18), dp(18), dp(14));
            card.setBackground(criarFundo("#0B1536", "#1E3A8A", 28));

            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 0, 0, dp(16));
            card.setLayoutParams(cardParams);

            TextView txtNome = new TextView(this);
            txtNome.setText("👨‍🏫 " + nome);
            txtNome.setTextColor(Color.WHITE);
            txtNome.setTextSize(18);
            txtNome.setTypeface(null, Typeface.BOLD);

            TextView txtQuantidade = new TextView(this);
            txtQuantidade.setText(vinculos.size() == 1 ? "1 vínculo ativo" : vinculos.size() + " vínculos ativos");
            txtQuantidade.setTextColor(Color.parseColor("#16E0C4"));
            txtQuantidade.setTextSize(12);
            txtQuantidade.setTypeface(null, Typeface.BOLD);
            txtQuantidade.setPadding(0, dp(6), 0, dp(10));

            Button btnVerVinculos = criarBotao(
                    expandido ? "▲ OCULTAR VÍNCULOS" : "▼ VER VÍNCULOS",
                    "#081B55"
            );

            btnVerVinculos.setOnClickListener(v -> {
                professor.put("expandido", !expandido);
                mostrarProfessoresNaTela();
            });

            Button btnEditar = criarBotao("✏ EDITAR PROFESSOR", "#7B61FF");
            btnEditar.setOnClickListener(v -> abrirDialogEditarProfessor(nome));

            Button btnAdicionar = criarBotao("➕ ADICIONAR VÍNCULO", "#09C7B0");
            btnAdicionar.setOnClickListener(v -> abrirDialogAdicionarVinculo(nome));

            Button btnExcluir = criarBotao("🗑 EXCLUIR PROFESSOR", "#E84142");
            btnExcluir.setOnClickListener(v -> confirmarExclusao(nome));

            card.addView(txtNome);
            card.addView(txtQuantidade);
            card.addView(btnVerVinculos);

            if (expandido) {
                TextView titulo = new TextView(this);
                titulo.setText("Horários e vínculos cadastrados:");
                titulo.setTextColor(Color.WHITE);
                titulo.setTextSize(15);
                titulo.setTypeface(null, Typeface.BOLD);
                titulo.setPadding(0, dp(18), 0, dp(10));
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
                    txtVinculo.setPadding(dp(16), dp(12), dp(16), dp(12));
                    txtVinculo.setBackground(criarFundo("#020A22", "#1E3A8A", 18));

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(0, 0, 0, dp(10));
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
                dp(48)
        );
        params.setMargins(0, dp(8), 0, 0);
        botao.setLayoutParams(params);

        return botao;
    }

    private void abrirDialogEditarProfessor(String nomeAtual) {
        LinearLayout layout = criarLayoutDialog();

        EditText editNome = criarCampo("Nome do professor", nomeAtual);
        layout.addView(editNome);

        new AlertDialog.Builder(this)
                .setTitle("Editar professor")
                .setView(layout)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String novoNome = editNome.getText().toString().trim();

                    if (novoNome.isEmpty()) {
                        Toast.makeText(this, "Digite o nome do professor.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    atualizarProfessor(nomeAtual, novoNome);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void atualizarProfessor(String nomeAtual, String novoNome) {
        db.collection("grade")
                .whereEqualTo("Professor", nomeAtual)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    WriteBatch batch = db.batch();

                    for (QueryDocumentSnapshot documento : queryDocumentSnapshots) {
                        batch.update(documento.getReference(), "Professor", novoNome);
                    }

                    batch.commit()
                            .addOnSuccessListener(unused ->
                                    Toast.makeText(this, "Professor atualizado!", Toast.LENGTH_SHORT).show()
                            )
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Erro ao atualizar: " + e.getMessage(), Toast.LENGTH_LONG).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao buscar professor: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void abrirDialogAdicionarVinculo(String nomeProfessor) {
        LinearLayout layout = criarLayoutDialog();

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

                    adicionarVinculo(nomeProfessor, curso, disciplina, turma, turno, dia, horario);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void adicionarVinculo(
            String nomeProfessor,
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
        dados.put("Curso", curso);
        dados.put("Disciplina", disciplina);
        dados.put("Turma", turma);
        dados.put("Turno", turno);
        dados.put("Dia", dia);
        dados.put("Horario", horario);

        ref.set(dados)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Vínculo adicionado!", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao adicionar: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void confirmarExclusao(String nome) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir professor")
                .setMessage("Tem certeza que deseja excluir o professor " + nome + "?\n\nTodos os vínculos dele serão apagados da coleção grade.")
                .setPositiveButton("Sim, excluir", (dialog, which) -> deletarProfessor(nome))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void deletarProfessor(String nome) {
        db.collection("grade")
                .whereEqualTo("Professor", nome)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    WriteBatch batch = db.batch();

                    for (QueryDocumentSnapshot documento : queryDocumentSnapshots) {
                        batch.delete(documento.getReference());
                    }

                    batch.commit()
                            .addOnSuccessListener(unused ->
                                    Toast.makeText(this, "Professor excluído com sucesso!", Toast.LENGTH_SHORT).show()
                            )
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Erro ao excluir: " + e.getMessage(), Toast.LENGTH_LONG).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao buscar professor: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private LinearLayout criarLayoutDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(18), dp(12), dp(18), dp(8));
        return layout;
    }

    private EditText criarCampo(String hint, String textoInicial) {
        EditText campo = new EditText(this);
        campo.setHint(hint);
        campo.setText(textoInicial);
        campo.setSingleLine(true);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(55)
        );
        params.setMargins(0, 0, 0, dp(10));
        campo.setLayoutParams(params);

        return campo;
    }

    private String valorSeguro(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return "Não informado";
        }

        return texto;
    }

    private GradientDrawable criarFundo(String corFundo, String corBorda, int raio) {
        GradientDrawable fundo = new GradientDrawable();
        fundo.setColor(Color.parseColor(corFundo));
        fundo.setCornerRadius(dp(raio));
        fundo.setStroke(dp(1), Color.parseColor(corBorda));
        return fundo;
    }

    private int dp(int valor) {
        return (int) (valor * getResources().getDisplayMetrics().density);
    }
}