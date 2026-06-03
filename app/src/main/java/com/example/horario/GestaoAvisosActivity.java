package com.example.horario;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class GestaoAvisosActivity extends AppCompatActivity {

    private EditText editTitulo, editAviso;
    private CheckBox checkNormal, checkUrgente;
    private LinearLayout layoutListaAvisos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestao_avisos);

        editTitulo = findViewById(R.id.editTitulo);
        editAviso = findViewById(R.id.editAviso);

        checkNormal = findViewById(R.id.checkNormal);
        checkUrgente = findViewById(R.id.checkUrgente);

        Button btnPublicar = findViewById(R.id.btnPublicar);
        Button btnVoltar = findViewById(R.id.btnVoltar);
        Button btnIrPainel = findViewById(R.id.btnIrPainel);

        layoutListaAvisos = findViewById(R.id.layoutListaAvisos);

        checkNormal.setOnClickListener(v -> {
            if (checkNormal.isChecked()) {
                checkUrgente.setChecked(false);
            }
        });

        checkUrgente.setOnClickListener(v -> {
            if (checkUrgente.isChecked()) {
                checkNormal.setChecked(false);
            }
        });

        btnPublicar.setOnClickListener(v -> publicarAviso());

        btnVoltar.setOnClickListener(v -> finish());

        btnIrPainel.setOnClickListener(v -> {
            Intent intent = new Intent(GestaoAvisosActivity.this, SecretariaActivity.class);
            startActivity(intent);
            finish();
        });

        carregarAvisosPublicados();
    }

    private void publicarAviso() {
        String titulo = editTitulo.getText().toString().trim();
        String descricao = editAviso.getText().toString().trim();

        if (titulo.isEmpty()) {
            editTitulo.setError("Digite o título");
            return;
        }

        if (descricao.isEmpty()) {
            editAviso.setError("Digite a descrição");
            return;
        }

        boolean urgente = checkUrgente.isChecked();

        String dataAtual = new SimpleDateFormat(
                "dd/MM/yyyy HH:mm",
                Locale.getDefault()
        ).format(new Date());

        HashMap<String, Object> aviso = new HashMap<>();
        aviso.put("titulo", titulo);
        aviso.put("descricao", descricao);
        aviso.put("data", dataAtual);
        aviso.put("urgente", urgente);

        FirebaseFirestore.getInstance()
                .collection("avisos")
                .add(aviso)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Aviso publicado com sucesso!", Toast.LENGTH_SHORT).show();

                    editTitulo.setText("");
                    editAviso.setText("");
                    checkNormal.setChecked(false);
                    checkUrgente.setChecked(false);

                    carregarAvisosPublicados();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void carregarAvisosPublicados() {
        FirebaseFirestore.getInstance()
                .collection("avisos")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    layoutListaAvisos.removeAllViews();

                    if (queryDocumentSnapshots.isEmpty()) {
                        TextView vazio = new TextView(this);
                        vazio.setText("Nenhum aviso publicado ainda.");
                        vazio.setTextColor(Color.WHITE);
                        vazio.setTextSize(14);
                        vazio.setPadding(0, 10, 0, 10);
                        layoutListaAvisos.addView(vazio);
                        return;
                    }

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String id = document.getId();
                        String titulo = document.getString("titulo");
                        String descricao = document.getString("descricao");
                        String data = document.getString("data");
                        Boolean urgente = document.getBoolean("urgente");

                        criarCardAviso(id, titulo, descricao, data, Boolean.TRUE.equals(urgente));
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao carregar avisos: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void criarCardAviso(String id, String titulo, String descricao, String data, boolean urgente) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setPadding(18, 18, 18, 18);
        card.setGravity(Gravity.CENTER_VERTICAL);

        GradientDrawable fundo = new GradientDrawable();
        fundo.setColor(Color.parseColor("#07163D"));
        fundo.setCornerRadius(25);
        fundo.setStroke(2, Color.parseColor("#263B7A"));
        card.setBackground(fundo);

        LinearLayout.LayoutParams paramsCard = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        paramsCard.setMargins(0, 0, 0, 15);
        card.setLayoutParams(paramsCard);

        TextView info = new TextView(this);
        info.setText(
                (urgente ? "🔴 URGENTE\n" : "🔵 NORMAL\n") +
                        titulo + "\n" +
                        descricao + "\n" +
                        data
        );
        info.setTextColor(Color.WHITE);
        info.setTextSize(14);
        info.setTypeface(null, Typeface.BOLD);

        LinearLayout.LayoutParams paramsInfo = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        info.setLayoutParams(paramsInfo);

        Button btnExcluir = new Button(this);
        btnExcluir.setText("Excluir");
        btnExcluir.setTextColor(Color.WHITE);
        btnExcluir.setTextSize(12);
        btnExcluir.setAllCaps(false);

        GradientDrawable bgExcluir = new GradientDrawable();
        bgExcluir.setColor(Color.parseColor("#D32F2F"));
        bgExcluir.setCornerRadius(18);
        btnExcluir.setBackground(bgExcluir);

        btnExcluir.setOnClickListener(v -> confirmarExclusao(id));

        card.addView(info);
        card.addView(btnExcluir);

        layoutListaAvisos.addView(card);
    }

    private void confirmarExclusao(String id) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir aviso")
                .setMessage("Tem certeza que deseja excluir este aviso?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Excluir", (dialog, which) -> excluirAviso(id))
                .show();
    }

    private void excluirAviso(String id) {
        FirebaseFirestore.getInstance()
                .collection("avisos")
                .document(id)
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Aviso excluído!", Toast.LENGTH_SHORT).show();
                    carregarAvisosPublicados();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao excluir: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}