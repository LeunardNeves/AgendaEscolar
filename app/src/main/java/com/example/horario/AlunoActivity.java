package com.example.horario;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
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

                        String tipo = Boolean.TRUE.equals(urgente) ? "URGENTE" : "NORMAL";

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
            warn.setImageResource(R.drawable.ic_warning_custom);
            warn.setColorFilter(Color.parseColor("#FF5C5C"));
        } else {
            cardAviso.setCardBackgroundColor(Color.parseColor("#081B55"));
            txtTituloAviso.setTextColor(Color.parseColor("#16E0C4"));
            txtDescricaoAviso.setTextColor(Color.WHITE);
            txtDataAviso.setTextColor(Color.parseColor("#AEB9D8"));
            warn.setImageResource(R.drawable.ic_info_custom);
            warn.setColorFilter(Color.parseColor("#16E0C4"));
        }
    }

    private void carregarTodosAvisos() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("avisos")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<AvisoItem> avisos = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String titulo = document.getString("titulo");
                        String descricao = document.getString("descricao");
                        String data = document.getString("data");
                        Boolean urgente = document.getBoolean("urgente");

                        avisos.add(new AvisoItem(
                                valorSeguro(titulo),
                                valorSeguro(descricao),
                                valorSeguro(data),
                                Boolean.TRUE.equals(urgente)
                        ));
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
                    ArrayList<AulaHorario> horarios = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String horario = document.getString("Horario");
                        String disciplina = document.getString("Disciplina");
                        String professor = document.getString("Professor");

                        horarios.add(new AulaHorario(
                                valorSeguro(horario),
                                valorSeguro(disciplina),
                                valorSeguro(professor)
                        ));
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

    private void mostrarDialogoAvisos(ArrayList<AvisoItem> avisos) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        ScrollView scrollView = new ScrollView(this);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(28), dp(26), dp(28), dp(28));
        container.setBackground(criarFundo("#07163D", "#2563EB", 34));

        LinearLayout topo = new LinearLayout(this);
        topo.setOrientation(LinearLayout.HORIZONTAL);
        topo.setGravity(Gravity.CENTER_VERTICAL);

        TextView titulo = new TextView(this);
        titulo.setText("Avisos");
        titulo.setTextColor(Color.WHITE);
        titulo.setTextSize(24);
        titulo.setTypeface(null, Typeface.BOLD);

        topo.addView(titulo, new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1
        ));

        ImageButton btnX = new ImageButton(this);
        btnX.setImageResource(R.drawable.ic_close_custom);
        btnX.setColorFilter(Color.WHITE);
        btnX.setBackgroundColor(Color.TRANSPARENT);
        btnX.setPadding(dp(10), dp(10), dp(10), dp(10));
        btnX.setOnClickListener(v -> dialog.dismiss());

        topo.addView(btnX, new LinearLayout.LayoutParams(dp(48), dp(48)));
        container.addView(topo);

        for (AvisoItem aviso : avisos) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(20), dp(18), dp(20), dp(18));

            card.setBackground(
                    aviso.urgente
                            ? criarFundo("#4A0E0E", "#FF5C5C", 24)
                            : criarFundo("#081B55", "#2563EB", 24)
            );

            TextView tipo = new TextView(this);
            tipo.setText(aviso.urgente ? "URGENTE" : "NORMAL");
            tipo.setTextColor(aviso.urgente ? Color.parseColor("#FF6B6B") : Color.parseColor("#16E0C4"));
            tipo.setTextSize(12);
            tipo.setTypeface(null, Typeface.BOLD);

            TextView tituloAviso = new TextView(this);
            tituloAviso.setText(aviso.titulo);
            tituloAviso.setTextColor(Color.WHITE);
            tituloAviso.setTextSize(17);
            tituloAviso.setTypeface(null, Typeface.BOLD);
            tituloAviso.setPadding(0, dp(6), 0, dp(6));

            TextView descricao = new TextView(this);
            descricao.setText(aviso.descricao);
            descricao.setTextColor(Color.WHITE);
            descricao.setTextSize(14);

            TextView data = new TextView(this);
            data.setText(aviso.data);
            data.setTextColor(Color.parseColor("#AEB9D8"));
            data.setTextSize(12);
            data.setGravity(Gravity.END);
            data.setPadding(0, dp(8), 0, 0);

            card.addView(tipo);
            card.addView(tituloAviso);
            card.addView(descricao);
            card.addView(data);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, dp(16), 0, 0);

            container.addView(card, params);
        }

        scrollView.addView(container);
        dialog.setContentView(scrollView);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialog.show();

        Window janela = dialog.getWindow();
        if (janela != null) {
            janela.setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.92),
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private void mostrarDialogoHorario(String dia, ArrayList<AulaHorario> horarios) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(28), dp(26), dp(28), dp(28));
        container.setBackground(criarFundo("#07163D", "#2563EB", 34));

        LinearLayout topo = new LinearLayout(this);
        topo.setOrientation(LinearLayout.HORIZONTAL);
        topo.setGravity(Gravity.CENTER_VERTICAL);

        TextView titulo = new TextView(this);
        titulo.setText("Horário de " + dia);
        titulo.setTextColor(Color.WHITE);
        titulo.setTextSize(23);
        titulo.setTypeface(null, Typeface.BOLD);

        topo.addView(titulo, new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1
        ));

        ImageButton btnX = new ImageButton(this);
        btnX.setImageResource(R.drawable.ic_close_custom);
        btnX.setColorFilter(Color.WHITE);
        btnX.setBackgroundColor(Color.TRANSPARENT);
        btnX.setPadding(dp(10), dp(10), dp(10), dp(10));
        btnX.setOnClickListener(v -> dialog.dismiss());

        topo.addView(btnX, new LinearLayout.LayoutParams(dp(48), dp(48)));
        container.addView(topo);

        ScrollView scrollView = new ScrollView(this);

        LinearLayout lista = new LinearLayout(this);
        lista.setOrientation(LinearLayout.VERTICAL);
        lista.setPadding(0, dp(22), 0, 0);

        for (AulaHorario aula : horarios) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(22), dp(20), dp(22), dp(20));
            card.setBackground(criarFundo("#081B55", "#2563EB", 24));

            LinearLayout linhaHorario = new LinearLayout(this);
            linhaHorario.setOrientation(LinearLayout.HORIZONTAL);
            linhaHorario.setGravity(Gravity.CENTER_VERTICAL);

            ImageView icRelogio = new ImageView(this);
            icRelogio.setImageResource(R.drawable.ic_clock_custom);
            icRelogio.setColorFilter(Color.parseColor("#16E0C4"));

            linhaHorario.addView(icRelogio, new LinearLayout.LayoutParams(dp(20), dp(20)));

            TextView txtHorario = new TextView(this);
            txtHorario.setText(aula.horario);
            txtHorario.setTextColor(Color.parseColor("#16E0C4"));
            txtHorario.setTextSize(17);
            txtHorario.setTypeface(null, Typeface.BOLD);
            txtHorario.setPadding(dp(8), 0, 0, 0);

            linhaHorario.addView(txtHorario);

            TextView txtDisciplina = new TextView(this);
            txtDisciplina.setText(aula.disciplina);
            txtDisciplina.setTextColor(Color.WHITE);
            txtDisciplina.setTextSize(20);
            txtDisciplina.setTypeface(null, Typeface.BOLD);
            txtDisciplina.setPadding(0, dp(10), 0, dp(6));

            TextView txtProfessor = new TextView(this);
            txtProfessor.setText("Professor(a): " + aula.professor);
            txtProfessor.setTextColor(Color.WHITE);
            txtProfessor.setTextSize(15);

            card.addView(linhaHorario);
            card.addView(txtDisciplina);
            card.addView(txtProfessor);

            LinearLayout.LayoutParams paramsCard = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            paramsCard.setMargins(0, 0, 0, dp(14));

            lista.addView(card, paramsCard);
        }

        scrollView.addView(lista);
        container.addView(scrollView);

        dialog.setContentView(container);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialog.show();

        Window janela = dialog.getWindow();
        if (janela != null) {
            janela.setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.92),
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private GradientDrawable criarFundo(String corFundo, String corBorda, int raio) {
        GradientDrawable fundo = new GradientDrawable();
        fundo.setColor(Color.parseColor(corFundo));
        fundo.setCornerRadius(dp(raio));
        fundo.setStroke(dp(1), Color.parseColor(corBorda));
        return fundo;
    }

    private String valorSeguro(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return "Não informado";
        }
        return texto;
    }

    private int dp(int valor) {
        return (int) (valor * getResources().getDisplayMetrics().density);
    }

    private static class AulaHorario {
        String horario;
        String disciplina;
        String professor;

        AulaHorario(String horario, String disciplina, String professor) {
            this.horario = horario;
            this.disciplina = disciplina;
            this.professor = professor;
        }
    }

    private static class AvisoItem {
        String titulo;
        String descricao;
        String data;
        boolean urgente;

        AvisoItem(String titulo, String descricao, String data, boolean urgente) {
            this.titulo = titulo;
            this.descricao = descricao;
            this.data = data;
            this.urgente = urgente;
        }
    }
}