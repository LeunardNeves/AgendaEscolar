package com.example.horario;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.button.MaterialButton;
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

    private MaterialButton btnPublicar, btnVoltar, btnFechar, btnIrPainel;

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

        btnPublicar = findViewById(R.id.btnPublicar);
        btnVoltar = findViewById(R.id.btnVoltar);
        btnFechar = findViewById(R.id.btnFechar);
        btnIrPainel = findViewById(R.id.btnIrPainel);

        layoutListaAvisos = findViewById(R.id.layoutListaAvisos);
        textTotalAvisos = findViewById(R.id.textTotalAvisos);

        configurarRadioButtons();

        btnPublicar.setOnClickListener(v -> publicarAviso());
        btnVoltar.setOnClickListener(v -> finish());
        btnFechar.setOnClickListener(v -> finish());

        btnIrPainel.setOnClickListener(v -> {
            Intent intent = new Intent(GestaoAvisosActivity.this, SecretariaActivity.class);
            startActivity(intent);
            finish();
        });

        editBuscarAviso.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                carregarAvisosPublicados(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        carregarAvisosPublicados("");
    }

    private void configurarRadioButtons() {
        ColorStateList roxoSelecionado = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{}
                },
                new int[]{
                        Color.parseColor("#7B61FF"),
                        Color.parseColor("#FFFFFF")
                }
        );

        radioNormal.setButtonTintList(roxoSelecionado);
        radioUrgente.setButtonTintList(roxoSelecionado);
        radioNormal.setChecked(true);
    }

    private void publicarAviso() {
        String titulo = editTitulo.getText().toString().trim();
        String descricao = editAviso.getText().toString().trim();

        if (titulo.isEmpty()) {
            editTitulo.setError("Digite o título");
            editTitulo.requestFocus();
            return;
        }

        if (descricao.isEmpty()) {
            editAviso.setError("Digite a descrição");
            editAviso.requestFocus();
            return;
        }

        boolean urgente = radioUrgente.isChecked();

        String dataAtual = new SimpleDateFormat(
                "dd/MM/yyyy HH:mm",
                Locale.getDefault()
        ).format(new Date());

        HashMap<String, Object> aviso = new HashMap<>();
        aviso.put("titulo", titulo);
        aviso.put("descricao", descricao);
        aviso.put("data", dataAtual);
        aviso.put("urgente", urgente);
        aviso.put("timestamp", System.currentTimeMillis());

        btnPublicar.setEnabled(false);
        btnPublicar.setText("Publicando...");

        FirebaseFirestore.getInstance()
                .collection("avisos")
                .add(aviso)
                .addOnSuccessListener(documentReference -> {
                    btnPublicar.setEnabled(true);
                    btnPublicar.setText("PUBLICAR AVISO");

                    Toast.makeText(this, "Aviso publicado com sucesso!", Toast.LENGTH_SHORT).show();

                    editTitulo.setText("");
                    editAviso.setText("");
                    radioNormal.setChecked(true);

                    carregarAvisosPublicados(editBuscarAviso.getText().toString());
                })
                .addOnFailureListener(e -> {
                    btnPublicar.setEnabled(true);
                    btnPublicar.setText("PUBLICAR AVISO");

                    Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
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

                        if (!busca.isEmpty()) {
                            String tituloBusca = titulo == null ? "" : titulo.toLowerCase();

                            if (!tituloBusca.contains(busca)) {
                                continue;
                            }
                        }

                        encontrados++;
                        criarCardAviso(
                                id,
                                valorSeguro(titulo),
                                valorSeguro(descricao),
                                valorSeguro(data),
                                Boolean.TRUE.equals(urgente)
                        );
                    }

                    if (encontrados == 0) {
                        TextView vazio = new TextView(this);
                        vazio.setText(busca.isEmpty()
                                ? "Nenhum aviso publicado ainda."
                                : "Nenhum aviso encontrado.");
                        vazio.setTextColor(Color.parseColor("#AEB9D8"));
                        vazio.setTextSize(14);
                        vazio.setGravity(Gravity.CENTER);
                        vazio.setPadding(dp(20), dp(35), dp(20), dp(35));
                        vazio.setBackground(criarFundo("#07163D", "#1E3A8A", 24));

                        layoutListaAvisos.addView(vazio);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Erro ao carregar avisos: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
    }

    private void criarCardAviso(String id, String titulo, String descricao, String data, boolean urgente) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(16), dp(18), dp(16));
        card.setBackground(criarFundo("#07163D", "#1E3A8A", 24));

        LinearLayout.LayoutParams paramsCard = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        paramsCard.setMargins(0, 0, 0, dp(14));
        card.setLayoutParams(paramsCard);

        LinearLayout linhaTopo = new LinearLayout(this);
        linhaTopo.setOrientation(LinearLayout.HORIZONTAL);
        linhaTopo.setGravity(Gravity.CENTER_VERTICAL);

        TextView tipo = new TextView(this);
        tipo.setText(urgente ? "URGENTE" : "NORMAL");
        tipo.setTextColor(urgente ? Color.parseColor("#FF5C5C") : Color.parseColor("#7B61FF"));
        tipo.setTextSize(12);
        tipo.setTypeface(null, Typeface.BOLD);
        tipo.setBackground(criarFundo(
                urgente ? "#2A1118" : "#151036",
                urgente ? "#FF5C5C" : "#7B61FF",
                18
        ));
        tipo.setPadding(dp(12), dp(5), dp(12), dp(5));

        linhaTopo.addView(tipo);

        TextView txtTitulo = new TextView(this);
        txtTitulo.setText(titulo);
        txtTitulo.setTextColor(Color.WHITE);
        txtTitulo.setTextSize(16);
        txtTitulo.setTypeface(null, Typeface.BOLD);
        txtTitulo.setPadding(0, dp(14), 0, dp(8));

        TextView txtDescricao = new TextView(this);
        txtDescricao.setText(descricao);
        txtDescricao.setTextColor(Color.parseColor("#DCE4FF"));
        txtDescricao.setTextSize(14);

        TextView txtData = new TextView(this);
        txtData.setText("Publicado em: " + data);
        txtData.setTextColor(Color.parseColor("#AEB9D8"));
        txtData.setTextSize(12);
        txtData.setPadding(0, dp(12), 0, 0);

        LinearLayout linhaBotoes = new LinearLayout(this);
        linhaBotoes.setOrientation(LinearLayout.HORIZONTAL);
        linhaBotoes.setGravity(Gravity.END);
        linhaBotoes.setPadding(0, dp(14), 0, 0);

        ImageButton btnEditar = criarBotaoIcone(
                R.drawable.ic_edit_custom,
                "#7B61FF"
        );

        ImageButton btnExcluir = criarBotaoIcone(
                R.drawable.ic_delete_custom,
                "#EF4444"
        );

        btnEditar.setOnClickListener(v -> abrirDialogEditar(id, titulo, descricao, urgente));
        btnExcluir.setOnClickListener(v -> confirmarExclusao(id));

        linhaBotoes.addView(btnEditar);
        linhaBotoes.addView(btnExcluir);

        card.addView(linhaTopo);
        card.addView(txtTitulo);
        card.addView(txtDescricao);
        card.addView(txtData);
        card.addView(linhaBotoes);

        layoutListaAvisos.addView(card);
    }

    private ImageButton criarBotaoIcone(int icone, String cor) {
        ImageButton botao = new ImageButton(this);

        botao.setImageResource(icone);
        botao.setColorFilter(Color.WHITE);
        botao.setBackground(criarFundo(cor, cor, 50));
        botao.setPadding(dp(10), dp(10), dp(10), dp(10));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(42), dp(42));
        params.setMargins(dp(8), 0, 0, 0);
        botao.setLayoutParams(params);

        return botao;
    }

    private void abrirDialogEditar(String id, String tituloAtual, String descricaoAtual, boolean urgenteAtual) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(24), dp(20), dp(24), dp(10));

        EditText editTituloDialog = criarCampoDialog("Título", tituloAtual, false);
        EditText editDescricaoDialog = criarCampoDialog("Descrição", descricaoAtual, true);

        RadioButton radioNormalDialog = new RadioButton(this);
        radioNormalDialog.setText("Normal");
        radioNormalDialog.setTextColor(Color.WHITE);
        radioNormalDialog.setChecked(!urgenteAtual);

        RadioButton radioUrgenteDialog = new RadioButton(this);
        radioUrgenteDialog.setText("Urgente");
        radioUrgenteDialog.setTextColor(Color.WHITE);
        radioUrgenteDialog.setChecked(urgenteAtual);

        ColorStateList roxoSelecionado = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{}
                },
                new int[]{
                        Color.parseColor("#7B61FF"),
                        Color.parseColor("#FFFFFF")
                }
        );

        radioNormalDialog.setButtonTintList(roxoSelecionado);
        radioUrgenteDialog.setButtonTintList(roxoSelecionado);

        radioNormalDialog.setOnClickListener(v -> radioUrgenteDialog.setChecked(false));
        radioUrgenteDialog.setOnClickListener(v -> radioNormalDialog.setChecked(false));

        layout.addView(editTituloDialog);
        layout.addView(editDescricaoDialog);
        layout.addView(radioNormalDialog);
        layout.addView(radioUrgenteDialog);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Editar aviso")
                .setView(layout)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Salvar", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String novoTitulo = editTituloDialog.getText().toString().trim();
                String novaDescricao = editDescricaoDialog.getText().toString().trim();
                boolean novoUrgente = radioUrgenteDialog.isChecked();

                if (novoTitulo.isEmpty()) {
                    editTituloDialog.setError("Digite o título");
                    editTituloDialog.requestFocus();
                    return;
                }

                if (novaDescricao.isEmpty()) {
                    editDescricaoDialog.setError("Digite a descrição");
                    editDescricaoDialog.requestFocus();
                    return;
                }

                editarAviso(id, novoTitulo, novaDescricao, novoUrgente);
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private EditText criarCampoDialog(String hint, String texto, boolean multiline) {
        EditText editText = new EditText(this);
        editText.setHint(hint);
        editText.setText(texto);
        editText.setTextColor(Color.WHITE);
        editText.setHintTextColor(Color.parseColor("#8F9BBC"));
        editText.setBackgroundColor(Color.parseColor("#020A22"));
        editText.setPadding(dp(14), dp(10), dp(14), dp(10));

        if (multiline) {
            editText.setMinLines(3);
            editText.setGravity(Gravity.TOP);
        } else {
            editText.setSingleLine(true);
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                multiline ? dp(110) : dp(52)
        );
        params.setMargins(0, 0, 0, dp(12));
        editText.setLayoutParams(params);

        return editText;
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
                        Toast.makeText(
                                this,
                                "Erro ao editar: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
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
                        Toast.makeText(
                                this,
                                "Erro ao excluir: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
    }

    private GradientDrawable criarFundo(String corFundo, String corBorda, int raio) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.parseColor(corFundo));
        drawable.setCornerRadius(dp(raio));
        drawable.setStroke(dp(1), Color.parseColor(corBorda));
        return drawable;
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
}