package com.example.horario;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GerenciarProfessores extends AppCompatActivity {

    private MaterialButton btnVoltar, btnFechar;
    private EditText editPesquisarProfessor;
    private LinearLayout layoutProfessores;
    private TextView textTotalProfessores;
    private FirebaseFirestore db;

    private final ArrayList<Map<String, Object>> listaProfessores = new ArrayList<>();
    private final ArrayList<Map<String, Object>> listaFiltradaProfessores = new ArrayList<>();

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
        btnFechar = findViewById(R.id.btnFechar);
        editPesquisarProfessor = findViewById(R.id.editPesquisarProfessor);
        layoutProfessores = findViewById(R.id.layoutProfessores);
        textTotalProfessores = findViewById(R.id.textTotalProfessores);

        btnVoltar.setOnClickListener(v -> finish());
        btnFechar.setOnClickListener(v -> finish());

        editPesquisarProfessor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarProfessores(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

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
                    filtrarProfessores(editPesquisarProfessor.getText().toString());
                });
    }

    private void filtrarProfessores(String texto) {
        listaFiltradaProfessores.clear();

        String busca = texto.toLowerCase().trim();

        if (busca.isEmpty()) {
            listaFiltradaProfessores.addAll(listaProfessores);
        } else {
            for (Map<String, Object> professor : listaProfessores) {
                String nome = String.valueOf(professor.get("nome")).toLowerCase();

                if (nome.contains(busca)) {
                    listaFiltradaProfessores.add(professor);
                }
            }
        }

        mostrarProfessoresNaTela();
    }

    private void mostrarProfessoresNaTela() {
        layoutProfessores.removeAllViews();

        int total = listaFiltradaProfessores.size();
        textTotalProfessores.setText(total == 1 ? "1 professor" : total + " professores");

        if (listaFiltradaProfessores.isEmpty()) {
            TextView vazio = new TextView(this);
            vazio.setText("Nenhum professor encontrado.");
            vazio.setTextColor(Color.parseColor("#8F9BBC"));
            vazio.setGravity(Gravity.CENTER);
            vazio.setTextSize(14);
            vazio.setPadding(dp(30), dp(60), dp(30), dp(60));
            vazio.setBackground(criarFundo("#0B1536", "#1E3A8A", 24));
            layoutProfessores.addView(vazio);
            return;
        }

        for (Map<String, Object> professor : listaFiltradaProfessores) {
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

            LinearLayout areaNome = new LinearLayout(this);
            areaNome.setOrientation(LinearLayout.HORIZONTAL);
            areaNome.setGravity(Gravity.CENTER_VERTICAL);

            RelativeLayout.LayoutParams areaNomeParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            areaNomeParams.addRule(RelativeLayout.LEFT_OF, ID_BOTAO_MENU);
            areaNome.setLayoutParams(areaNomeParams);

            ImageView icProfessor = new ImageView(this);
            icProfessor.setImageResource(R.drawable.ic_professor_custom);
            icProfessor.setColorFilter(Color.parseColor("#16E0C4"));
            icProfessor.setBackground(criarFundo("#111F4D", "#16E0C4", 50));
            icProfessor.setPadding(dp(9), dp(9), dp(9), dp(9));

            LinearLayout.LayoutParams icParams = new LinearLayout.LayoutParams(dp(46), dp(46));
            icParams.setMargins(0, 0, dp(14), 0);
            icProfessor.setLayoutParams(icParams);

            TextView txtNome = new TextView(this);
            txtNome.setText(nome);
            txtNome.setTextColor(Color.WHITE);
            txtNome.setTextSize(18);
            txtNome.setTypeface(null, Typeface.BOLD);
            txtNome.setSingleLine(false);
            txtNome.setMaxLines(2);
            txtNome.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1
            ));

            ImageButton btnMenu = new ImageButton(this);
            btnMenu.setId(ID_BOTAO_MENU);
            btnMenu.setImageResource(R.drawable.ic_more_vertical_custom);
            btnMenu.setBackground(criarFundo("#111F4D", "#2962FF", 50));
            btnMenu.setColorFilter(Color.WHITE);
            btnMenu.setPadding(dp(10), dp(10), dp(10), dp(10));

            RelativeLayout.LayoutParams menuParams = new RelativeLayout.LayoutParams(dp(42), dp(42));
            menuParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            menuParams.addRule(RelativeLayout.CENTER_VERTICAL);
            btnMenu.setLayoutParams(menuParams);

            ArrayList<Map<String, String>> finalVinculos = vinculos;

            btnMenu.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(GerenciarProfessores.this, btnMenu);
                popup.getMenu().add("Editar professor");
                popup.getMenu().add("Adicionar vínculo");
                popup.getMenu().add("Excluir professor");

                popup.setOnMenuItemClickListener(item -> {
                    String opcao = item.getTitle().toString();

                    if (opcao.equals("Editar professor")) {
                        abrirDialogEditarProfessor(nome);
                    } else if (opcao.equals("Adicionar vínculo")) {
                        abrirDialogAdicionarVinculo(nome);
                    } else if (opcao.equals("Excluir professor")) {
                        confirmarExclusaoProfessor(nome);
                    }

                    return true;
                });

                popup.show();
            });

            areaNome.addView(icProfessor);
            areaNome.addView(txtNome);

            header.addView(areaNome);
            header.addView(btnMenu);
            card.addView(header);

            if (expandido) {
                View divisor = new View(this);
                divisor.setBackgroundColor(Color.parseColor("#1F2E5B"));

                LinearLayout.LayoutParams divisorParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(1)
                );
                divisorParams.setMargins(0, dp(16), 0, dp(16));
                divisor.setLayoutParams(divisorParams);

                card.addView(divisor);

                TextView txtTitulo = new TextView(this);
                txtTitulo.setText("Vínculos cadastrados");
                txtTitulo.setTextColor(Color.parseColor("#7B61FF"));
                txtTitulo.setTextSize(14);
                txtTitulo.setTypeface(null, Typeface.BOLD);
                txtTitulo.setPadding(0, 0, 0, dp(12));
                card.addView(txtTitulo);

                for (Map<String, String> vinculo : finalVinculos) {
                    card.addView(criarLinhaVinculo(vinculo));
                }
            }

            MaterialButton btnExpandir = new MaterialButton(this);
            btnExpandir.setText(expandido ? "Ocultar vínculos" : "Ver vínculos");
            btnExpandir.setTextColor(Color.WHITE);
            btnExpandir.setTextSize(12);
            btnExpandir.setTypeface(null, Typeface.BOLD);
            btnExpandir.setAllCaps(false);
            btnExpandir.setIconResource(expandido ? R.drawable.ic_expand_less_custom : R.drawable.ic_expand_more_custom);
            btnExpandir.setIconTintResource(android.R.color.white);
            btnExpandir.setIconPadding(dp(8));
            btnExpandir.setBackgroundColor(Color.TRANSPARENT);

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
        linha.setPadding(dp(14), dp(14), dp(14), dp(14));
        linha.setBackground(criarFundo("#020A22", "#1E3A8A", 20));

        LinearLayout.LayoutParams linhaParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        linhaParams.setMargins(0, 0, 0, dp(12));
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
        txtDisciplina.setText(valorSeguro(vinculo.get("disciplina")));
        txtDisciplina.setTextColor(Color.WHITE);
        txtDisciplina.setTextSize(15);
        txtDisciplina.setTypeface(null, Typeface.BOLD);

        textos.addView(txtDisciplina);
        textos.addView(criarLinhaInfo("Curso", valorSeguro(vinculo.get("curso"))));
        textos.addView(criarLinhaInfo("Turno", valorSeguro(vinculo.get("turno"))));
        textos.addView(criarLinhaInfo("Dia", valorSeguro(vinculo.get("dia"))));
        textos.addView(criarLinhaInfo("Horário", valorSeguro(vinculo.get("horario"))));

        ImageButton btnEditar = new ImageButton(this);
        btnEditar.setId(ID_BOTAO_EDITAR_VINCULO);
        btnEditar.setImageResource(R.drawable.ic_edit_custom);
        btnEditar.setBackground(criarFundo("#1B1644", "#7B61FF", 50));
        btnEditar.setColorFilter(Color.WHITE);
        btnEditar.setPadding(dp(9), dp(9), dp(9), dp(9));

        RelativeLayout.LayoutParams editarParams = new RelativeLayout.LayoutParams(dp(40), dp(40));
        editarParams.addRule(RelativeLayout.LEFT_OF, ID_BOTAO_EXCLUIR_VINCULO);
        editarParams.addRule(RelativeLayout.CENTER_VERTICAL);
        editarParams.setMargins(0, 0, dp(8), 0);
        btnEditar.setLayoutParams(editarParams);

        ImageButton btnExcluir = new ImageButton(this);
        btnExcluir.setId(ID_BOTAO_EXCLUIR_VINCULO);
        btnExcluir.setImageResource(R.drawable.ic_delete_custom);
        btnExcluir.setBackgroundResource(R.drawable.bg_delete_button);
        btnExcluir.setColorFilter(Color.parseColor("#FF5C5C"));
        btnExcluir.setPadding(dp(9), dp(9), dp(9), dp(9));

        RelativeLayout.LayoutParams excluirParams = new RelativeLayout.LayoutParams(dp(40), dp(40));
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

    private TextView criarLinhaInfo(String titulo, String valor) {
        TextView texto = new TextView(this);
        texto.setText(titulo + ": " + valor);
        texto.setTextColor(Color.parseColor("#16E0C4"));
        texto.setTextSize(13);
        texto.setPadding(0, dp(5), 0, 0);
        texto.setSingleLine(false);
        return texto;
    }

    private void abrirDialogEditarProfessor(String nomeAtual) {
        LinearLayout layoutDialog = criarLayoutDialog();

        EditText editNome = criarCampoDialog("Nome do professor", nomeAtual);
        layoutDialog.addView(editNome);

        new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                .setTitle("Editar professor")
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
        EditText editDia = criarCampoDialog("Dia", "");
        EditText editHorario = criarCampoDialog("Horário", "");

        layoutDialog.addView(editCurso);
        layoutDialog.addView(editDisciplina);
        layoutDialog.addView(editTurno);
        layoutDialog.addView(editDia);
        layoutDialog.addView(editHorario);

        new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                .setTitle("Adicionar vínculo")
                .setView(layoutDialog)
                .setPositiveButton("Adicionar", (dialog, which) -> {
                    String curso = editCurso.getText().toString().trim();
                    String disciplina = editDisciplina.getText().toString().trim();
                    String turno = editTurno.getText().toString().trim();
                    String dia = editDia.getText().toString().trim();
                    String horario = editHorario.getText().toString().trim();

                    if (curso.isEmpty() || disciplina.isEmpty() || turno.isEmpty()
                            || dia.isEmpty() || horario.isEmpty()) {
                        Toast.makeText(this, "Preencha todos os campos.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    adicionarVinculo(nomeProfessor, curso, disciplina, turno, dia, horario);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void adicionarVinculo(String nomeProfessor, String curso, String disciplina,
                                  String turno, String dia, String horario) {
        DocumentReference ref = db.collection("grade").document();

        Map<String, Object> dados = new HashMap<>();
        dados.put("Professor", nomeProfessor);
        dados.put("Curso", curso);
        dados.put("Disciplina", disciplina);
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

    private void abrirDialogEditarVinculo(Map<String, String> vinculo) {
        LinearLayout layoutDialog = criarLayoutDialog();

        EditText editCurso = criarCampoDialog("Curso", valorSeguro(vinculo.get("curso")));
        EditText editDisciplina = criarCampoDialog("Disciplina", valorSeguro(vinculo.get("disciplina")));
        EditText editTurno = criarCampoDialog("Turno", valorSeguro(vinculo.get("turno")));
        EditText editDia = criarCampoDialog("Dia", valorSeguro(vinculo.get("dia")));
        EditText editHorario = criarCampoDialog("Horário", valorSeguro(vinculo.get("horario")));

        layoutDialog.addView(editCurso);
        layoutDialog.addView(editDisciplina);
        layoutDialog.addView(editTurno);
        layoutDialog.addView(editDia);
        layoutDialog.addView(editHorario);

        new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                .setTitle("Editar vínculo")
                .setView(layoutDialog)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String curso = editCurso.getText().toString().trim();
                    String disciplina = editDisciplina.getText().toString().trim();
                    String turno = editTurno.getText().toString().trim();
                    String dia = editDia.getText().toString().trim();
                    String horario = editHorario.getText().toString().trim();

                    if (curso.isEmpty() || disciplina.isEmpty() || turno.isEmpty()
                            || dia.isEmpty() || horario.isEmpty()) {
                        Toast.makeText(this, "Preencha todos os campos.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    editarVinculo(vinculo.get("idDoc"), curso, disciplina, turno, dia, horario);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void editarVinculo(String idDocumento, String curso, String disciplina,
                               String turno, String dia, String horario) {
        db.collection("grade")
                .document(idDocumento)
                .update(
                        "Curso", curso,
                        "Disciplina", disciplina,
                        "Turno", turno,
                        "Dia", dia,
                        "Horario", horario
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
                .setTitle("Excluir vínculo")
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
                .setTitle("Excluir professor")
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
        campo.setText(textoInicial.equals("Não informado") ? "" : textoInicial);
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