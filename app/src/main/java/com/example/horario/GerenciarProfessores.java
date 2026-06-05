package com.example.horario;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
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

    private final int ID_BOTAO_MENU = 1001;
    private final int ID_BOTAO_EDITAR_VINCULO = 1002;
    private final int ID_BOTAO_EXCLUIR_VINCULO = 1003;

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
                        vinculo.put("turno", valorSeguro(documento.getString("Turno")));

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
            vazio.setPadding(30, 60, 30, 60);
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

            RelativeLayout header = new RelativeLayout(this);
            header.setLayoutParams(new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            ));

            LinearLayout textos = new LinearLayout(this);
            textos.setOrientation(LinearLayout.VERTICAL);

            RelativeLayout.LayoutParams textosParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            textosParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            textosParams.addRule(RelativeLayout.LEFT_OF, ID_BOTAO_MENU);
            textos.setLayoutParams(textosParams);

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
            txtQuantidade.setPadding(0, dp(6), 0, 0);

            textos.addView(txtNome);
            textos.addView(txtQuantidade);

            ImageButton btnMenu = new ImageButton(this);
            btnMenu.setId(ID_BOTAO_MENU);
            btnMenu.setImageResource(android.R.drawable.ic_menu_more);
            btnMenu.setBackgroundColor(Color.TRANSPARENT);
            btnMenu.setColorFilter(Color.parseColor("#16E0C4"));

            RelativeLayout.LayoutParams menuParams = new RelativeLayout.LayoutParams(dp(42), dp(42));
            menuParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            menuParams.addRule(RelativeLayout.CENTER_VERTICAL);
            btnMenu.setLayoutParams(menuParams);

            ArrayList<Map<String, String>> finalVinculos = vinculos;

            btnMenu.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(GerenciarProfessores.this, btnMenu);
                popup.getMenu().add("✏ Editar professor");
                popup.getMenu().add("➕ Adicionar vínculo");
                popup.getMenu().add("🗑 Excluir professor");

                popup.setOnMenuItemClickListener(item -> {
                    String opcao = item.getTitle().toString();

                    if (opcao.equals("✏ Editar professor")) {
                        abrirDialogEditarProfessor(nome);
                    } else if (opcao.equals("➕ Adicionar vínculo")) {
                        abrirDialogAdicionarVinculo(nome);
                    } else if (opcao.equals("🗑 Excluir professor")) {
                        confirmarExclusaoProfessor(nome);
                    }

                    return true;
                });

                popup.show();
            });

            header.addView(textos);
            header.addView(btnMenu);
            card.addView(header);

            if (expandido) {
                View divisor = new View(this);
                divisor.setBackgroundColor(Color.parseColor("#1F2E5B"));

                LinearLayout.LayoutParams divisorParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(1)
                );
                divisorParams.setMargins(0, dp(14), 0, dp(14));
                divisor.setLayoutParams(divisorParams);

                card.addView(divisor);

                TextView txtTitulo = new TextView(this);
                txtTitulo.setText("Vínculos cadastrados:");
                txtTitulo.setTextColor(Color.parseColor("#7B61FF"));
                txtTitulo.setTextSize(14);
                txtTitulo.setTypeface(null, Typeface.BOLD);
                txtTitulo.setPadding(0, 0, 0, dp(10));
                card.addView(txtTitulo);

                for (Map<String, String> vinculo : finalVinculos) {
                    card.addView(criarLinhaVinculo(vinculo));
                }
            }

            Button btnExpandir = new Button(this, null, android.R.attr.borderlessButtonStyle);
            btnExpandir.setText(expandido ? "▲ OCULTAR VÍNCULOS" : "▼ VER VÍNCULOS");
            btnExpandir.setTextColor(Color.parseColor("#7B61FF"));
            btnExpandir.setTextSize(12);
            btnExpandir.setTypeface(null, Typeface.BOLD);
            btnExpandir.setAllCaps(false);

            btnExpandir.setOnClickListener(v -> {
                professor.put("expandido", !expandido);
                mostrarProfessoresNaTela();
            });

            card.addView(btnExpandir);
            layoutProfessores.addView(card);
        }
    }

    private View criarLinhaVinculo(Map<String, String> vinculo) {
        RelativeLayout linha = new RelativeLayout(this);
        linha.setPadding(dp(12), dp(12), dp(12), dp(12));
        linha.setBackground(criarFundo("#020A22", "#1E3A8A", 20));

        LinearLayout.LayoutParams linhaParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        linhaParams.setMargins(0, 0, 0, dp(10));
        linha.setLayoutParams(linhaParams);

        LinearLayout textos = new LinearLayout(this);
        textos.setOrientation(LinearLayout.VERTICAL);

        RelativeLayout.LayoutParams textosParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        textosParams.addRule(RelativeLayout.LEFT_OF, ID_BOTAO_EDITAR_VINCULO);
        textos.setLayoutParams(textosParams);

        TextView txtDisciplina = new TextView(this);
        txtDisciplina.setText("📚 " + valorSeguro(vinculo.get("disciplina")));
        txtDisciplina.setTextColor(Color.WHITE);
        txtDisciplina.setTextSize(14);
        txtDisciplina.setTypeface(null, Typeface.BOLD);

        TextView txtCurso = new TextView(this);
        txtCurso.setText("Curso: " + valorSeguro(vinculo.get("curso")));
        txtCurso.setTextColor(Color.parseColor("#AEB9D8"));
        txtCurso.setTextSize(12);
        txtCurso.setPadding(0, dp(4), 0, 0);

        TextView txtTurno = new TextView(this);
        txtTurno.setText("Turno: " + valorSeguro(vinculo.get("turno")));
        txtTurno.setTextColor(Color.parseColor("#16E0C4"));
        txtTurno.setTextSize(12);
        txtTurno.setPadding(0, dp(4), 0, 0);

        textos.addView(txtDisciplina);
        textos.addView(txtCurso);
        textos.addView(txtTurno);

        ImageButton btnEditar = new ImageButton(this);
        btnEditar.setId(ID_BOTAO_EDITAR_VINCULO);
        btnEditar.setImageResource(android.R.drawable.ic_menu_edit);
        btnEditar.setBackgroundColor(Color.TRANSPARENT);
        btnEditar.setColorFilter(Color.parseColor("#7B61FF"));

        RelativeLayout.LayoutParams editarParams = new RelativeLayout.LayoutParams(dp(38), dp(38));
        editarParams.addRule(RelativeLayout.LEFT_OF, ID_BOTAO_EXCLUIR_VINCULO);
        editarParams.addRule(RelativeLayout.CENTER_VERTICAL);
        editarParams.setMargins(0, 0, dp(6), 0);
        btnEditar.setLayoutParams(editarParams);

        ImageButton btnExcluir = new ImageButton(this);
        btnExcluir.setId(ID_BOTAO_EXCLUIR_VINCULO);
        btnExcluir.setImageResource(android.R.drawable.ic_menu_delete);
        btnExcluir.setBackgroundColor(Color.TRANSPARENT);
        btnExcluir.setColorFilter(Color.parseColor("#E84142"));

        RelativeLayout.LayoutParams excluirParams = new RelativeLayout.LayoutParams(dp(38), dp(38));
        excluirParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        excluirParams.addRule(RelativeLayout.CENTER_VERTICAL);
        btnExcluir.setLayoutParams(excluirParams);

        btnEditar.setOnClickListener(v -> abrirDialogEditarVinculo(vinculo));
        btnExcluir.setOnClickListener(v -> confirmarExclusaoVinculo(vinculo));

        linha.addView(textos);
        linha.addView(btnEditar);
        linha.addView(btnExcluir);

        return linha;
    }

    private void abrirDialogEditarProfessor(String nomeAtual) {
        LinearLayout layoutDialog = criarLayoutDialog();

        EditText editNome = criarCampoDialog("Nome do professor", nomeAtual);
        layoutDialog.addView(editNome);

        new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                .setTitle("✏ Editar professor")
                .setView(layoutDialog)
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
                });
    }

    private void abrirDialogAdicionarVinculo(String nomeProfessor) {
        LinearLayout layoutDialog = criarLayoutDialog();

        EditText editCurso = criarCampoDialog("Curso", "");
        EditText editDisciplina = criarCampoDialog("Disciplina", "");
        EditText editTurno = criarCampoDialog("Turno", "");

        layoutDialog.addView(editCurso);
        layoutDialog.addView(editDisciplina);
        layoutDialog.addView(editTurno);

        new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                .setTitle("➕ Adicionar vínculo")
                .setView(layoutDialog)
                .setPositiveButton("Adicionar", (dialog, which) -> {
                    String curso = editCurso.getText().toString().trim();
                    String disciplina = editDisciplina.getText().toString().trim();
                    String turno = editTurno.getText().toString().trim();

                    if (curso.isEmpty() || disciplina.isEmpty() || turno.isEmpty()) {
                        Toast.makeText(this, "Preencha todos os campos.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    adicionarVinculo(nomeProfessor, curso, disciplina, turno);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void adicionarVinculo(String nomeProfessor, String curso, String disciplina, String turno) {
        DocumentReference ref = db.collection("grade").document();

        Map<String, Object> dados = new HashMap<>();
        dados.put("Professor", nomeProfessor);
        dados.put("Curso", curso);
        dados.put("Disciplina", disciplina);
        dados.put("Turno", turno);

        ref.set(dados)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Vínculo adicionado!", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao adicionar: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void abrirDialogEditarVinculo(Map<String, String> vinculo) {
        LinearLayout layoutDialog = criarLayoutDialog();

        EditText editCurso = criarCampoDialog("Curso", valorSeguro(vinculo.get("curso")));
        EditText editDisciplina = criarCampoDialog("Disciplina", valorSeguro(vinculo.get("disciplina")));
        EditText editTurno = criarCampoDialog("Turno", valorSeguro(vinculo.get("turno")));

        layoutDialog.addView(editCurso);
        layoutDialog.addView(editDisciplina);
        layoutDialog.addView(editTurno);

        new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                .setTitle("✏ Editar vínculo")
                .setView(layoutDialog)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String curso = editCurso.getText().toString().trim();
                    String disciplina = editDisciplina.getText().toString().trim();
                    String turno = editTurno.getText().toString().trim();

                    if (curso.isEmpty() || disciplina.isEmpty() || turno.isEmpty()) {
                        Toast.makeText(this, "Preencha todos os campos.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    editarVinculo(vinculo.get("idDoc"), curso, disciplina, turno);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void editarVinculo(String idDocumento, String curso, String disciplina, String turno) {
        db.collection("grade")
                .document(idDocumento)
                .update(
                        "Curso", curso,
                        "Disciplina", disciplina,
                        "Turno", turno
                )
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Vínculo atualizado!", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao atualizar vínculo: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void confirmarExclusaoVinculo(Map<String, String> vinculo) {
        new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                .setTitle("🗑 Excluir vínculo")
                .setMessage("Deseja excluir o vínculo de " + valorSeguro(vinculo.get("disciplina")) + "?")
                .setPositiveButton("Excluir", (dialog, which) -> excluirVinculo(vinculo.get("idDoc")))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void excluirVinculo(String idDocumento) {
        db.collection("grade")
                .document(idDocumento)
                .delete()
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Vínculo excluído!", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao excluir vínculo: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void confirmarExclusaoProfessor(String nomeProfessor) {
        new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                .setTitle("🗑 Excluir professor")
                .setMessage("Isso vai apagar " + nomeProfessor + " e todos os vínculos dele. Deseja continuar?")
                .setPositiveButton("Excluir tudo", (dialog, which) -> excluirProfessorCompleto(nomeProfessor))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void excluirProfessorCompleto(String nomeProfessor) {
        db.collection("grade")
                .whereEqualTo("Professor", nomeProfessor)
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
                                    Toast.makeText(this, "Erro ao excluir professor: " + e.getMessage(), Toast.LENGTH_LONG).show()
                            );
                });
    }

    private LinearLayout criarLayoutDialog() {
        LinearLayout layoutDialog = new LinearLayout(this);
        layoutDialog.setOrientation(LinearLayout.VERTICAL);
        layoutDialog.setBackgroundColor(Color.parseColor("#0B1536"));
        layoutDialog.setPadding(dp(20), dp(20), dp(20), dp(10));
        return layoutDialog;
    }

    private EditText criarCampoDialog(String hint, String textoInicial) {
        EditText campo = new EditText(this);
        campo.setHint(hint);

        if (textoInicial.equals("Não informado")) {
            campo.setText("");
        } else {
            campo.setText(textoInicial);
        }

        campo.setTextColor(Color.WHITE);
        campo.setHintTextColor(Color.parseColor("#8F9BBC"));
        campo.setSingleLine(true);
        campo.setBackgroundColor(Color.parseColor("#020A22"));
        campo.setPadding(dp(14), dp(10), dp(14), dp(10));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(52)
        );
        params.setMargins(0, 0, 0, dp(12));
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