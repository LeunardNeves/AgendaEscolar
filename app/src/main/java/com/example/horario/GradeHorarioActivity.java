package com.example.horario;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GradeHorarioActivity extends AppCompatActivity {

    private MaterialButton btnVoltar, btnMenu;
    private EditText editPesquisar;
    private LinearLayout layoutDias, layoutHorarios;
    private FirebaseFirestore db;

    private String diaSelecionado = "SEG";
    private String textoPesquisa = "";

    private final String[] dias = {"SEG", "TER", "QUA", "QUI", "SEX"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grade_horario);

        db = FirebaseFirestore.getInstance();

        btnVoltar = findViewById(R.id.btnVoltar);
        btnMenu = findViewById(R.id.btnMenu);
        editPesquisar = findViewById(R.id.editPesquisar);
        layoutDias = findViewById(R.id.layoutDias);
        layoutHorarios = findViewById(R.id.layoutHorarios);

        btnVoltar.setOnClickListener(v -> finish());

        btnMenu.setOnClickListener(v -> {
            Intent intent = new Intent(this, novo_horario.class);
            startActivity(intent);
        });

        editPesquisar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textoPesquisa = s.toString().trim().toLowerCase();
                carregarGrade();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        criarBotoesDias();
        carregarGrade();
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarGrade();
    }

    private void criarBotoesDias() {
        layoutDias.removeAllViews();

        for (String dia : dias) {
            TextView btnDia = new TextView(this);
            btnDia.setText(dia);
            btnDia.setGravity(Gravity.CENTER);
            btnDia.setTextSize(15);
            btnDia.setTypeface(null, Typeface.BOLD);
            btnDia.setPadding(dp(18), dp(14), dp(18), dp(14));

            if (dia.equals(diaSelecionado)) {
                btnDia.setTextColor(Color.WHITE);
                btnDia.setBackground(criarFundo("#21113D", "#7B61FF", 24));
            } else {
                btnDia.setTextColor(Color.parseColor("#D8D8E8"));
                btnDia.setBackground(criarFundo("#07163D", "#1E3A8A", 24));
            }

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(86), dp(60));
            params.setMargins(0, 0, dp(12), 0);
            btnDia.setLayoutParams(params);

            btnDia.setOnClickListener(v -> {
                diaSelecionado = dia;
                criarBotoesDias();
                carregarGrade();
            });

            layoutDias.addView(btnDia);
        }
    }

    private void carregarGrade() {
        layoutHorarios.removeAllViews();

        db.collection("grade")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<Map<String, String>> lista = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String dia = pegarCampo(doc, "Dia", "dia", "diaSemana", "DiaSemana");

                        if (!diaPertenceAoSelecionado(dia)) {
                            continue;
                        }

                        String disciplina = pegarCampo(doc, "Disciplina", "disciplina", "materia", "Materia");
                        String professor = pegarCampo(doc, "Professor", "professor", "professorNome", "ProfessorNome");

                        if (!textoPesquisa.isEmpty()) {
                            String disciplinaLower = disciplina.toLowerCase();
                            String professorLower = professor.toLowerCase();

                            if (!disciplinaLower.contains(textoPesquisa)
                                    && !professorLower.contains(textoPesquisa)) {
                                continue;
                            }
                        }

                        Map<String, String> item = new HashMap<>();
                        item.put("id", doc.getId());
                        item.put("Dia", dia);
                        item.put("Horario", pegarCampo(doc, "Horario", "horario"));
                        item.put("Disciplina", disciplina);
                        item.put("Professor", professor);
                        item.put("Sala", pegarCampo(doc, "Sala", "sala"));

                        lista.add(item);
                    }

                    Collections.sort(lista, (a, b) -> valorSeguro(a.get("Horario")).compareTo(valorSeguro(b.get("Horario"))));

                    if (lista.isEmpty()) {
                        TextView vazio = new TextView(this);

                        if (textoPesquisa.isEmpty()) {
                            vazio.setText("Nenhum horário cadastrado para esse dia.");
                        } else {
                            vazio.setText("Nenhum resultado encontrado.");
                        }

                        vazio.setTextColor(Color.parseColor("#8F9BBC"));
                        vazio.setTextSize(14);
                        vazio.setGravity(Gravity.CENTER);
                        vazio.setPadding(dp(20), dp(40), dp(20), dp(40));
                        vazio.setBackground(criarFundo("#07163D", "#1E3A8A", 20));
                        layoutHorarios.addView(vazio);
                        return;
                    }

                    for (Map<String, String> item : lista) {
                        criarCardHorario(item);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao carregar grade: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void criarCardHorario(Map<String, String> item) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(14));
        card.setBackground(criarFundo("#07163D", "#1E3A8A", 22));

        LinearLayout.LayoutParams paramsCard = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        paramsCard.setMargins(0, 0, 0, dp(14));
        card.setLayoutParams(paramsCard);

        RelativeLayout topo = new RelativeLayout(this);

        LinearLayout areaInfo = new LinearLayout(this);
        areaInfo.setOrientation(LinearLayout.HORIZONTAL);
        areaInfo.setGravity(Gravity.CENTER_VERTICAL);

        RelativeLayout.LayoutParams areaParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        areaParams.addRule(RelativeLayout.LEFT_OF, 2001);
        areaInfo.setLayoutParams(areaParams);

        ImageView icHorario = new ImageView(this);
        icHorario.setImageResource(R.drawable.ic_clock_custom);
        icHorario.setColorFilter(Color.parseColor("#16E0C4"));
        icHorario.setBackground(criarFundo("#111F4D", "#16E0C4", 50));
        icHorario.setPadding(dp(8), dp(8), dp(8), dp(8));

        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(44), dp(44));
        iconParams.setMargins(0, 0, dp(14), 0);
        icHorario.setLayoutParams(iconParams);

        LinearLayout textos = new LinearLayout(this);
        textos.setOrientation(LinearLayout.VERTICAL);
        textos.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));

        TextView txtDisciplina = new TextView(this);
        txtDisciplina.setText(valorSeguro(item.get("Disciplina")));
        txtDisciplina.setTextColor(Color.WHITE);
        txtDisciplina.setTextSize(16);
        txtDisciplina.setTypeface(null, Typeface.BOLD);
        txtDisciplina.setSingleLine(false);
        txtDisciplina.setMaxLines(2);

        TextView txtHorario = new TextView(this);
        txtHorario.setText(valorSeguro(item.get("Horario")));
        txtHorario.setTextColor(Color.parseColor("#16E0C4"));
        txtHorario.setTextSize(14);
        txtHorario.setTypeface(null, Typeface.BOLD);
        txtHorario.setPadding(0, dp(4), 0, 0);

        textos.addView(txtDisciplina);
        textos.addView(txtHorario);

        ImageButton btnOpcoes = new ImageButton(this);
        btnOpcoes.setId(2001);
        btnOpcoes.setImageResource(R.drawable.ic_more_vertical_custom);
        btnOpcoes.setBackground(criarFundo("#111F4D", "#2962FF", 50));
        btnOpcoes.setColorFilter(Color.WHITE);
        btnOpcoes.setPadding(dp(10), dp(10), dp(10), dp(10));

        RelativeLayout.LayoutParams menuParams = new RelativeLayout.LayoutParams(dp(42), dp(42));
        menuParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        menuParams.addRule(RelativeLayout.CENTER_VERTICAL);
        btnOpcoes.setLayoutParams(menuParams);

        btnOpcoes.setOnClickListener(v -> mostrarMenuCard(btnOpcoes, item));

        areaInfo.addView(icHorario);
        areaInfo.addView(textos);

        topo.addView(areaInfo);
        topo.addView(btnOpcoes);

        card.addView(topo);

        TextView txtProfessor = criarLinhaInfo("Professor", valorSeguro(item.get("Professor")));
        TextView txtSala = criarLinhaInfo("Sala", valorSeguro(item.get("Sala")));
        TextView txtDia = criarLinhaInfo("Dia", valorSeguro(item.get("Dia")));

        card.addView(txtProfessor);
        card.addView(txtSala);
        card.addView(txtDia);

        layoutHorarios.addView(card);
    }

    private void mostrarMenuCard(ImageButton btn, Map<String, String> item) {
        PopupMenu menu = new PopupMenu(this, btn);
        menu.getMenu().add("Editar");
        menu.getMenu().add("Excluir");

        menu.setOnMenuItemClickListener(menuItem -> {
            String opcao = menuItem.getTitle().toString();

            if (opcao.equals("Editar")) {
                abrirDialogoEditar(item);
            } else {
                confirmarExclusao(item.get("id"));
            }

            return true;
        });

        menu.show();
    }

    private void abrirDialogoEditar(Map<String, String> item) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(20), dp(10), dp(20), dp(10));
        layout.setBackgroundColor(Color.parseColor("#0B1536"));

        EditText editDia = criarEditText("Dia", item.get("Dia"));
        EditText editHorario = criarEditText("Horário", item.get("Horario"));
        EditText editDisciplina = criarEditText("Disciplina", item.get("Disciplina"));
        EditText editProfessor = criarEditText("Professor", item.get("Professor"));
        EditText editSala = criarEditText("Sala", item.get("Sala"));

        layout.addView(editDia);
        layout.addView(editHorario);
        layout.addView(editDisciplina);
        layout.addView(editProfessor);
        layout.addView(editSala);

        new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                .setTitle("Editar horário")
                .setView(layout)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    Map<String, Object> dados = new HashMap<>();
                    dados.put("Dia", editDia.getText().toString().trim());
                    dados.put("Horario", editHorario.getText().toString().trim());
                    dados.put("Disciplina", editDisciplina.getText().toString().trim());
                    dados.put("Professor", editProfessor.getText().toString().trim());
                    dados.put("Sala", editSala.getText().toString().trim());

                    db.collection("grade").document(item.get("id"))
                            .update(dados)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Horário atualizado!", Toast.LENGTH_SHORT).show();
                                carregarGrade();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Erro ao editar: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void confirmarExclusao(String id) {
        new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                .setTitle("Excluir horário")
                .setMessage("Tem certeza que deseja excluir esse horário?")
                .setPositiveButton("Excluir", (dialog, which) -> {
                    db.collection("grade").document(id)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Horário excluído!", Toast.LENGTH_SHORT).show();
                                carregarGrade();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Erro ao excluir: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private EditText criarEditText(String hint, String texto) {
        EditText editText = new EditText(this);
        editText.setHint(hint);
        editText.setText(texto == null ? "" : texto);
        editText.setTextColor(Color.WHITE);
        editText.setHintTextColor(Color.parseColor("#8F9BBC"));
        editText.setSingleLine(true);
        editText.setBackgroundColor(Color.parseColor("#020A22"));
        editText.setPadding(dp(14), dp(10), dp(14), dp(10));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(52)
        );
        params.setMargins(0, 0, 0, dp(12));
        editText.setLayoutParams(params);

        return editText;
    }

    private TextView criarLinhaInfo(String titulo, String valor) {
        TextView texto = new TextView(this);

        texto.setText(titulo + ": " + valor);
        texto.setTextColor(Color.parseColor("#AEB9D8"));
        texto.setTextSize(13);
        texto.setPadding(dp(58), dp(6), 0, 0);
        texto.setSingleLine(false);

        return texto;
    }

    private String pegarCampo(QueryDocumentSnapshot doc, String... nomes) {
        for (String nome : nomes) {
            Object valor = doc.get(nome);
            if (valor != null) {
                return valor.toString();
            }
        }
        return "";
    }

    private boolean diaPertenceAoSelecionado(String dia) {
        if (dia == null) return false;

        String d = dia.toUpperCase();

        if (diaSelecionado.equals("SEG")) return d.contains("SEG") || d.contains("SEGUNDA");
        if (diaSelecionado.equals("TER")) return d.contains("TER") || d.contains("TERÇA") || d.contains("TERCA");
        if (diaSelecionado.equals("QUA")) return d.contains("QUA") || d.contains("QUARTA");
        if (diaSelecionado.equals("QUI")) return d.contains("QUI") || d.contains("QUINTA");
        if (diaSelecionado.equals("SEX")) return d.contains("SEX") || d.contains("SEXTA");

        return false;
    }

    private String valorSeguro(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return "Não informado";
        }
        return texto;
    }

    private GradientDrawable criarFundo(String corFundo, String corBorda, int raio) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.parseColor(corFundo));
        drawable.setCornerRadius(dp(raio));
        drawable.setStroke(dp(1), Color.parseColor(corBorda));
        return drawable;
    }

    private int dp(int valor) {
        return (int) (valor * getResources().getDisplayMetrics().density);
    }
}