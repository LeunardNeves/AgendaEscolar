package com.example.horario;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GradeHorarioActivity extends AppCompatActivity {

    private Button btnVoltar;
    private TextView btnMenu;
    private LinearLayout layoutDias, layoutHorarios;
    private FirebaseFirestore db;

    private String diaSelecionado = "SEG";
    private final String[] dias = {"SEG", "TER", "QUA", "QUI", "SEX"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grade_horario);

        db = FirebaseFirestore.getInstance();

        btnVoltar = findViewById(R.id.btnVoltar);
        btnMenu = findViewById(R.id.btnMenu);
        layoutDias = findViewById(R.id.layoutDias);
        layoutHorarios = findViewById(R.id.layoutHorarios);

        btnVoltar.setOnClickListener(v -> finish());

        btnMenu.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(this, btnMenu);
            menu.getMenu().add("Novo horário");

            menu.setOnMenuItemClickListener(item -> {
                startActivity(new Intent(this, novo_horario.class));
                return true;
            });

            menu.show();
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
            btnDia.setTextSize(16);
            btnDia.setTypeface(null, Typeface.BOLD);
            btnDia.setPadding(dp(22), dp(16), dp(22), dp(16));

            if (dia.equals(diaSelecionado)) {
                btnDia.setTextColor(Color.WHITE);
                btnDia.setBackground(criarFundo("#21113D", "#B84DFF", 22));
            } else {
                btnDia.setTextColor(Color.parseColor("#D8D8E8"));
                btnDia.setBackground(criarFundo("#07163D", "#13254F", 22));
            }

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(92), dp(72));
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

                        Map<String, String> item = new HashMap<>();
                        item.put("id", doc.getId());
                        item.put("Dia", dia);
                        item.put("Horario", pegarCampo(doc, "Horario"));
                        item.put("Disciplina", pegarCampo(doc, "Disciplina", "disciplina", "materia", "Materia"));
                        item.put("Professor", pegarCampo(doc, "Professor", "professor", "professorNome", "ProfessorNome"));
                        item.put("Sala", pegarCampo(doc, "Sala", "sala"));

                        lista.add(item);
                    }

                    Collections.sort(lista, (a, b) -> a.get("Horario").compareTo(b.get("Horario")));

                    if (lista.isEmpty()) {
                        TextView vazio = new TextView(this);
                        vazio.setText("Nenhum horário cadastrado para esse dia.");
                        vazio.setTextColor(Color.WHITE);
                        vazio.setTextSize(15);
                        vazio.setGravity(Gravity.CENTER);
                        vazio.setPadding(0, dp(30), 0, dp(30));
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
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(12), dp(14), dp(8), dp(14));
        card.setBackground(criarFundo("#07163D", "#13254F", 18));

        LinearLayout.LayoutParams paramsCard = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        paramsCard.setMargins(0, 0, 0, dp(12));
        card.setLayoutParams(paramsCard);

        TextView txtHora = new TextView(this);
        txtHora.setText(item.get("Horario"));
        txtHora.setTextColor(Color.parseColor("#16E0C4"));
        txtHora.setTextSize(14);
        txtHora.setTypeface(null, Typeface.BOLD);
        txtHora.setGravity(Gravity.CENTER);
        txtHora.setLayoutParams(new LinearLayout.LayoutParams(dp(95), LinearLayout.LayoutParams.WRAP_CONTENT));

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setPadding(dp(12), 0, dp(8), 0);
        info.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView txtDisciplina = new TextView(this);
        txtDisciplina.setText(item.get("Disciplina"));
        txtDisciplina.setTextColor(Color.WHITE);
        txtDisciplina.setTextSize(15);
        txtDisciplina.setTypeface(null, Typeface.BOLD);

        TextView txtProfessor = new TextView(this);
        txtProfessor.setText(item.get("Professor"));
        txtProfessor.setTextColor(Color.parseColor("#D8D8E8"));
        txtProfessor.setTextSize(13);

        TextView txtSala = new TextView(this);
        txtSala.setText(item.get("Sala"));
        txtSala.setTextColor(Color.parseColor("#9FA8DA"));
        txtSala.setTextSize(12);

        info.addView(txtDisciplina);
        info.addView(txtProfessor);
        info.addView(txtSala);

        TextView btnOpcoes = new TextView(this);
        btnOpcoes.setText("⋮");
        btnOpcoes.setTextColor(Color.WHITE);
        btnOpcoes.setTextSize(28);
        btnOpcoes.setGravity(Gravity.CENTER);

        btnOpcoes.setOnClickListener(v -> mostrarMenuCard(btnOpcoes, item));

        card.addView(txtHora);
        card.addView(info);
        card.addView(btnOpcoes);

        layoutHorarios.addView(card);
    }

    private void mostrarMenuCard(TextView btn, Map<String, String> item) {
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

        new AlertDialog.Builder(this)
                .setTitle("Editar horário")
                .setView(layout)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    Map<String, Object> dados = new HashMap<>();
                    dados.put("Dia", editDia.getText().toString());
                    dados.put("Horario", editHorario.getText().toString());
                    dados.put("Disciplina", editDisciplina.getText().toString());
                    dados.put("Professor", editProfessor.getText().toString());
                    dados.put("Sala", editSala.getText().toString());

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
        new AlertDialog.Builder(this)
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
        editText.setText(texto);
        editText.setTextColor(Color.BLACK);
        editText.setHintTextColor(Color.GRAY);
        return editText;
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