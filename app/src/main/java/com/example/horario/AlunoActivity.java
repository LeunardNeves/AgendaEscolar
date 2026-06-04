package com.example.horario;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class AlunoActivity extends AppCompatActivity {

    private String curso, ano, turno, cursoCompleto;

    private TextView txtInfoAluno, txtTituloAviso, txtDescricaoAviso, txtDataAviso;
    private ImageView warn;
    private CardView cardSegunda, cardTerca, cardQuarta, cardQuinta, cardSexta, cardAviso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aluno);

        receberDados();
        inicializarComponentes();
        configurarTela();
        configurarCliques();
        carregarAviso();
    }

    private void receberDados() {
        curso = getIntent().getStringExtra("curso");
        ano = getIntent().getStringExtra("ano");
        turno = getIntent().getStringExtra("turno");

        if (curso == null) curso = "";
        if (ano == null) ano = "";
        if (turno == null) turno = "";

        cursoCompleto = curso + " " + ano;
    }

    private void inicializarComponentes() {
        txtInfoAluno = findViewById(R.id.txtInfoAluno);
        txtTituloAviso = findViewById(R.id.txtTituloAviso);
        txtDescricaoAviso = findViewById(R.id.txtDescricaoAviso);
        txtDataAviso = findViewById(R.id.txtDataAviso);
        warn = findViewById(R.id.warn);

        cardSegunda = findViewById(R.id.cardSegunda);
        cardTerca = findViewById(R.id.cardTerca);
        cardQuarta = findViewById(R.id.cardQuarta);
        cardQuinta = findViewById(R.id.cardQuinta);
        cardSexta = findViewById(R.id.cardSexta);
        cardAviso = findViewById(R.id.cardAviso);
    }

    private void configurarTela() {
        txtInfoAluno.setText(turno + " • " + curso + " • " + ano);
    }

    private void configurarCliques() {
        cardSegunda.setOnClickListener(v -> buscarHorarioPorDia("Segunda"));
        cardTerca.setOnClickListener(v -> buscarHorarioPorDia("Terça"));
        cardQuarta.setOnClickListener(v -> buscarHorarioPorDia("Quarta"));
        cardQuinta.setOnClickListener(v -> buscarHorarioPorDia("Quinta"));
        cardSexta.setOnClickListener(v -> buscarHorarioPorDia("Sexta"));

        cardAviso.setOnClickListener(v -> carregarTodosAvisos());
    }

    private void carregarAviso() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("avisos")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        txtTituloAviso.setText("Nenhum aviso no momento");
                        txtDescricaoAviso.setText("Quando a secretaria publicar um aviso, ele aparecerá aqui.");
                        txtDataAviso.setText("Hoje");
                        aplicarEstiloAviso(false);
                        return;
                    }

                    QueryDocumentSnapshot avisoPrioritario = null;

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Boolean urgente = doc.getBoolean("urgente");

                        if (Boolean.TRUE.equals(urgente)) {
                            avisoPrioritario = doc;
                            break;
                        }

                        avisoPrioritario = doc;
                    }

                    if (avisoPrioritario != null) {
                        String titulo = avisoPrioritario.getString("titulo");
                        String descricao = avisoPrioritario.getString("descricao");
                        String data = avisoPrioritario.getString("data");
                        Boolean urgente = avisoPrioritario.getBoolean("urgente");

                        String tipo = Boolean.TRUE.equals(urgente) ? "🔴 URGENTE" : "🔵 NORMAL";

                        txtTituloAviso.setText(tipo + " - " + valorSeguro(titulo));
                        txtDescricaoAviso.setText(valorSeguro(descricao));
                        txtDataAviso.setText(valorSeguro(data));

                        aplicarEstiloAviso(Boolean.TRUE.equals(urgente));
                    }
                })
                .addOnFailureListener(e -> {
                    txtTituloAviso.setText("Erro ao carregar avisos");
                    txtDescricaoAviso.setText("");
                    txtDataAviso.setText("");
                    aplicarEstiloAviso(false);
                });
    }

    private void aplicarEstiloAviso(boolean urgente) {
        if (urgente) {
            cardAviso.setCardBackgroundColor(Color.parseColor("#4A0E0E"));
            txtTituloAviso.setTextColor(Color.parseColor("#FF6B6B"));
            txtDescricaoAviso.setTextColor(Color.WHITE);
            txtDataAviso.setTextColor(Color.parseColor("#FFB4B4"));
            warn.setImageResource(android.R.drawable.ic_dialog_alert);
            warn.setColorFilter(Color.parseColor("#FF4444"));
        } else {
            cardAviso.setCardBackgroundColor(Color.parseColor("#081B55"));
            txtTituloAviso.setTextColor(Color.parseColor("#16E0C4"));
            txtDescricaoAviso.setTextColor(Color.WHITE);
            txtDataAviso.setTextColor(Color.parseColor("#AEB9D8"));
            warn.setImageResource(android.R.drawable.ic_dialog_info);
            warn.setColorFilter(Color.parseColor("#16E0C4"));
        }
    }

    private void carregarTodosAvisos() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("avisos")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<String> avisos = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String titulo = document.getString("titulo");
                        String descricao = document.getString("descricao");
                        String data = document.getString("data");
                        Boolean urgente = document.getBoolean("urgente");

                        String tipo = Boolean.TRUE.equals(urgente) ? "🔴 URGENTE" : "🔵 NORMAL";

                        avisos.add(
                                tipo + "\n\n" +
                                        "Título: " + valorSeguro(titulo) + "\n\n" +
                                        "Descrição: " + valorSeguro(descricao) + "\n\n" +
                                        "Data: " + valorSeguro(data)
                        );
                    }

                    if (avisos.isEmpty()) {
                        Toast.makeText(this, "Nenhum aviso publicado.", Toast.LENGTH_SHORT).show();
                    } else {
                        mostrarDialogoAvisos(avisos);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void buscarHorarioPorDia(String dia) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("grade")
                .whereEqualTo("Curso", cursoCompleto)
                .whereEqualTo("Turno", turno)
                .whereEqualTo("Dia", dia)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<String> horarios = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String horario = document.getString("Horario");
                        String disciplina = document.getString("Disciplina");
                        String professor = document.getString("Professor");

                        horarios.add(
                                "Horário: " + valorSeguro(horario) + "\n\n" +
                                        "Matéria: " + valorSeguro(disciplina) + "\n\n" +
                                        "Professor: " + valorSeguro(professor)
                        );
                    }

                    if (horarios.isEmpty()) {
                        Toast.makeText(this, "Nenhum horário encontrado para " + dia, Toast.LENGTH_SHORT).show();
                    } else {
                        mostrarDialogoHorario(dia, horarios);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void mostrarDialogoAvisos(ArrayList<String> avisos) {
        mostrarDialogoPersonalizado("📢 Avisos", avisos);
    }

    private void mostrarDialogoHorario(String dia, ArrayList<String> horarios) {
        mostrarDialogoPersonalizado("📅 Horário de " + dia, horarios);
    }

    private void mostrarDialogoPersonalizado(String tituloTexto, ArrayList<String> itens) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        ScrollView scrollView = new ScrollView(this);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(45, 40, 45, 40);

        GradientDrawable fundo = new GradientDrawable();
        fundo.setColor(Color.parseColor("#07163D"));
        fundo.setCornerRadius(38);
        fundo.setStroke(2, Color.parseColor("#263B7A"));
        container.setBackground(fundo);

        TextView titulo = new TextView(this);
        titulo.setText(tituloTexto);
        titulo.setTextColor(Color.WHITE);
        titulo.setTextSize(24);
        titulo.setTypeface(null, Typeface.BOLD);
        titulo.setPadding(0, 0, 0, 28);
        container.addView(titulo);

        for (String item : itens) {
            TextView card = new TextView(this);
            card.setText(item);
            card.setTextColor(Color.WHITE);
            card.setTextSize(16);
            card.setPadding(30, 25, 30, 25);

            boolean urgente = item.startsWith("🔴");

            GradientDrawable bgCard;

            if (urgente) {
                bgCard = new GradientDrawable(
                        GradientDrawable.Orientation.LEFT_RIGHT,
                        new int[]{
                                Color.parseColor("#7A1010"),
                                Color.parseColor("#0B1B4D")
                        }
                );
                bgCard.setStroke(2, Color.parseColor("#FF4444"));
            } else {
                bgCard = new GradientDrawable(
                        GradientDrawable.Orientation.LEFT_RIGHT,
                        new int[]{
                                Color.parseColor("#5B2EFF"),
                                Color.parseColor("#0B1B4D")
                        }
                );
                bgCard.setStroke(1, Color.parseColor("#314D9B"));
            }

            bgCard.setCornerRadius(26);
            card.setBackground(bgCard);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 18);
            card.setLayoutParams(params);

            container.addView(card);
        }

        Button btnFechar = new Button(this);
        btnFechar.setText("✓ Fechar");
        btnFechar.setTextSize(18);
        btnFechar.setTextColor(Color.WHITE);
        btnFechar.setAllCaps(false);

        GradientDrawable bgBotao = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{
                        Color.parseColor("#6C35FF"),
                        Color.parseColor("#332DCC")
                }
        );

        bgBotao.setCornerRadius(60);
        btnFechar.setBackground(bgBotao);

        LinearLayout.LayoutParams paramsBotao = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                110
        );
        paramsBotao.setMargins(0, 15, 0, 0);
        btnFechar.setLayoutParams(paramsBotao);

        btnFechar.setOnClickListener(v -> dialog.dismiss());

        container.addView(btnFechar);
        scrollView.addView(container);

        dialog.setContentView(scrollView);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialog.show();
    }

    private String valorSeguro(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return "Não informado";
        }
        return texto;
    }
}