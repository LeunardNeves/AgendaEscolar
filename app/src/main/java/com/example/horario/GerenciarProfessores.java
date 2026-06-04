package com.example.horario;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

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
                        String curso = documento.getString("Curso");
                        String disciplina = documento.getString("Disciplina");
                        String turno = documento.getString("Turno");

                        if (nome == null || email == null) {
                            continue;
                        }

                        String vinculo = "Curso: " + valorSeguro(curso)
                                + "\nMatéria: " + valorSeguro(disciplina)
                                + "\nTurno: " + valorSeguro(turno);

                        if (!professoresUnicos.containsKey(email)) {
                            Map<String, Object> professor = new HashMap<>();

                            ArrayList<String> vinculos = new ArrayList<>();
                            vinculos.add(vinculo);

                            professor.put("nome", nome);
                            professor.put("email", email);
                            professor.put("vinculos", vinculos);

                            professoresUnicos.put(email, professor);
                        } else {
                            Map<String, Object> professor = professoresUnicos.get(email);

                            if (professor != null) {
                                @SuppressWarnings("unchecked")
                                ArrayList<String> vinculos = (ArrayList<String>) professor.get("vinculos");
                                vinculos.add(vinculo);
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

    private String valorSeguro(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return "Não informado";
        }
        return texto;
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

            @SuppressWarnings("unchecked")
            ArrayList<String> vinculos = (ArrayList<String>) professor.get("vinculos");

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

            TextView txtTituloVinculos = new TextView(this);
            txtTituloVinculos.setText("Vínculos cadastrados:");
            txtTituloVinculos.setTextColor(Color.WHITE);
            txtTituloVinculos.setTextSize(15);
            txtTituloVinculos.setTypeface(null, Typeface.BOLD);
            txtTituloVinculos.setPadding(0, 18, 0, 10);

            TextView txtQuantidade = new TextView(this);
            txtQuantidade.setText(vinculos.size() == 1 ? "1 vínculo" : vinculos.size() + " vínculos");
            txtQuantidade.setTextColor(Color.parseColor("#16E0C4"));
            txtQuantidade.setTextSize(14);
            txtQuantidade.setTypeface(null, Typeface.BOLD);
            txtQuantidade.setPadding(0, 8, 0, 0);

            card.addView(txtNome);
            card.addView(txtEmail);
            card.addView(txtQuantidade);
            card.addView(txtTituloVinculos);

            for (int i = 0; i < vinculos.size(); i++) {
                TextView txtVinculo = new TextView(this);
                txtVinculo.setText((i + 1) + ". " + vinculos.get(i));
                txtVinculo.setTextColor(Color.parseColor("#16E0C4"));
                txtVinculo.setTextSize(13);
                txtVinculo.setPadding(16, 10, 16, 10);
                txtVinculo.setBackgroundResource(R.drawable.bg_item_vinculo_moderno);

                LinearLayout.LayoutParams vinculoParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                vinculoParams.setMargins(0, 0, 0, 10);
                txtVinculo.setLayoutParams(vinculoParams);

                card.addView(txtVinculo);
            }

            Button btnExcluir = new Button(this);
            btnExcluir.setText("🗑 EXCLUIR PROFESSOR");
            btnExcluir.setTextColor(Color.WHITE);
            btnExcluir.setTextSize(13);
            btnExcluir.setTypeface(null, Typeface.BOLD);
            btnExcluir.setBackgroundResource(R.drawable.bg_btn_excluir);

            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    56
            );
            btnParams.setMargins(0, 16, 0, 0);
            btnExcluir.setLayoutParams(btnParams);

            btnExcluir.setOnClickListener(v -> confirmarExclusao(nome, email));

            card.addView(btnExcluir);
            layoutProfessores.addView(card);
        }
    }

    private void confirmarExclusao(String nome, String email) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir professor")
                .setMessage("Deseja excluir " + nome + " e todos os vínculos dele da grade?")
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
}