package com.example.horario;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class GestaoAvisosActivity extends AppCompatActivity {

    private EditText editTitulo, editAviso, editBuscarAviso;
    private RadioButton radioNormal, radioUrgente;
    private LinearLayout layoutListaAvisos;
    private TextView textTotalAvisos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestao_avisos);

        editTitulo = findViewById(R.id.editTitulo);
        editAviso = findViewById(R.id.editAviso);
        editBuscarAviso = findViewById(R.id.editBuscarAviso);

        radioNormal = findViewById(R.id.radioNormal);
        radioUrgente = findViewById(R.id.radioUrgente);

        Button btnPublicar = findViewById(R.id.btnPublicar);
        Button btnVoltar = findViewById(R.id.btnVoltar);
        Button btnIrPainel = findViewById(R.id.btnIrPainel);

        layoutListaAvisos = findViewById(R.id.layoutListaAvisos);
        textTotalAvisos = findViewById(R.id.textTotalAvisos);

        btnPublicar.setOnClickListener(v -> publicarAviso());
        btnVoltar.setOnClickListener(v -> finish());

        btnIrPainel.setOnClickListener(v -> {
            Intent intent = new Intent(GestaoAvisosActivity.this, SecretariaActivity.class);
            startActivity(intent);
            finish();
        });

        editBuscarAviso.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                carregarAvisosPublicados(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        carregarAvisosPublicados("");
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

        boolean urgente = radioUrgente.isChecked();

        String dataAtual = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

        HashMap<String, Object> aviso = new HashMap<>();
        aviso.put("titulo", titulo);
        aviso.put("descricao", descricao);
        aviso.put("data", dataAtual);
        aviso.put("urgente", urgente);
        aviso.put("timestamp", System.currentTimeMillis());

        FirebaseFirestore.getInstance()
                .collection("avisos")
                .add(aviso)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Aviso publicado com sucesso!", Toast.LENGTH_SHORT).show();

                    editTitulo.setText("");
                    editAviso.setText("");
                    radioNormal.setChecked(true);

                    carregarAvisosPublicados(editBuscarAviso.getText().toString());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void carregarAvisosPublicados(String filtro) {
        FirebaseFirestore.getInstance()
                .collection("avisos")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    layoutListaAvisos.removeAllViews();

                    int totalGeral = queryDocumentSnapshots.size();
                    textTotalAvisos.setText(totalGeral == 1 ? "1 aviso" : totalGeral + " avisos");

                    int encontrados = 0;
                    String busca = filtro == null ? "" : filtro.toLowerCase().trim();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String id = document.getId();
                        String titulo = document.getString("titulo");
                        String descricao = document.getString("descricao");
                        String data = document.getString("data");
                        Boolean urgente = document.getBoolean("urgente");

                        if (!busca.isEmpty() && titulo != null && !titulo.toLowerCase().contains(busca)) {
                            continue;
                        }

                        encontrados++;
                        criarCardAviso(id, titulo, descricao, data, Boolean.TRUE.equals(urgente));
                    }

                    if (encontrados == 0) {
                        TextView vazio = new TextView(this);
                        vazio.setText(busca.isEmpty() ? "Nenhum aviso publicado ainda." : "Nenhum aviso encontrado.");
                        vazio.setTextColor(Color.WHITE);
                        vazio.setTextSize(14);
                        vazio.setPadding(0, 10, 0, 10);
                        layoutListaAvisos.addView(vazio);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao carregar avisos: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void criarCardAviso(String id, String titulo, String descricao, String data, boolean urgente) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(20, 18, 20, 18);

        GradientDrawable fundo = new GradientDrawable();
        fundo.setColor(Color.parseColor("#07163D"));
        fundo.setCornerRadius(28);

        if (urgente) {
            fundo.setStroke(3, Color.parseColor("#E84142"));
        } else {
            fundo.setStroke(3, Color.parseColor("#3158C9"));
        }

        card.setBackground(fundo);

        LinearLayout.LayoutParams paramsCard = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        paramsCard.setMargins(0, 0, 0, 16);
        card.setLayoutParams(paramsCard);

        TextView tipo = new TextView(this);
        tipo.setText(urgente ? "🔴 URGENTE" : "🔵 NORMAL");
        tipo.setTextColor(urgente ? Color.parseColor("#FF6B6B") : Color.parseColor("#16E0C4"));
        tipo.setTextSize(13);
        tipo.setTypeface(null, Typeface.BOLD);

        TextView info = new TextView(this);
        info.setText(
                valorSeguro(titulo) + "\n\n" +
                        valorSeguro(descricao) + "\n\n" +
                        "Publicado em: " + valorSeguro(data)
        );
        info.setTextColor(Color.WHITE);
        info.setTextSize(14);
        info.setPadding(0, 10, 0, 14);

        LinearLayout botoes = new LinearLayout(this);
        botoes.setOrientation(LinearLayout.HORIZONTAL);
        botoes.setGravity(Gravity.END);

        Button btnEditar = criarBotaoPequeno("Editar", "#7B61FF");
        Button btnExcluir = criarBotaoPequeno("Excluir", "#D32F2F");

        btnEditar.setOnClickListener(v -> abrirDialogEditar(id, titulo, descricao, urgente));
        btnExcluir.setOnClickListener(v -> confirmarExclusao(id));

        botoes.addView(btnEditar);
        botoes.addView(btnExcluir);

        card.addView(tipo);
        card.addView(info);
        card.addView(botoes);

        layoutListaAvisos.addView(card);
    }

    private Button criarBotaoPequeno(String texto, String cor) {
        Button botao = new Button(this);
        botao.setText(texto);
        botao.setTextColor(Color.WHITE);
        botao.setTextSize(12);
        botao.setAllCaps(false);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor(cor));
        bg.setCornerRadius(20);
        botao.setBackground(bg);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, 48, 1);
        params.setMargins(6, 0, 0, 0);
        botao.setLayoutParams(params);

        return botao;
    }

    private void abrirDialogEditar(String id, String tituloAtual, String descricaoAtual, boolean urgenteAtual) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(30, 20, 30, 10);

        EditText editTituloDialog = new EditText(this);
        editTituloDialog.setHint("Título");
        editTituloDialog.setText(tituloAtual);
        editTituloDialog.setSingleLine(true);

        EditText editDescricaoDialog = new EditText(this);
        editDescricaoDialog.setHint("Descrição");
        editDescricaoDialog.setText(descricaoAtual);
        editDescricaoDialog.setMinLines(3);

        RadioButton radioNormalDialog = new RadioButton(this);
        radioNormalDialog.setText("Normal");
        radioNormalDialog.setChecked(!urgenteAtual);

        RadioButton radioUrgenteDialog = new RadioButton(this);
        radioUrgenteDialog.setText("Urgente");
        radioUrgenteDialog.setChecked(urgenteAtual);

        radioNormalDialog.setOnClickListener(v -> radioUrgenteDialog.setChecked(false));
        radioUrgenteDialog.setOnClickListener(v -> radioNormalDialog.setChecked(false));

        layout.addView(editTituloDialog);
        layout.addView(editDescricaoDialog);
        layout.addView(radioNormalDialog);
        layout.addView(radioUrgenteDialog);

        new AlertDialog.Builder(this)
                .setTitle("Editar aviso")
                .setView(layout)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String novoTitulo = editTituloDialog.getText().toString().trim();
                    String novaDescricao = editDescricaoDialog.getText().toString().trim();
                    boolean novoUrgente = radioUrgenteDialog.isChecked();

                    if (novoTitulo.isEmpty() || novaDescricao.isEmpty()) {
                        Toast.makeText(this, "Preencha todos os campos.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    editarAviso(id, novoTitulo, novaDescricao, novoUrgente);
                })
                .show();
    }

    private void editarAviso(String id, String titulo, String descricao, boolean urgente) {
        FirebaseFirestore.getInstance()
                .collection("avisos")
                .document(id)
                .update(
                        "titulo", titulo,
                        "descricao", descricao,
                        "urgente", urgente
                )
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Aviso atualizado!", Toast.LENGTH_SHORT).show();
                    carregarAvisosPublicados(editBuscarAviso.getText().toString());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao editar: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private String valorSeguro(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return "Não informado";
        }
        return texto;
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
                    carregarAvisosPublicados(editBuscarAviso.getText().toString());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao excluir: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}