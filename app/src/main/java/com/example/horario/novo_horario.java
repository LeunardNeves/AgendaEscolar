package com.example.horario;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class novo_horario extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_novo_horario);

        // 1. Vincular os blocos clicáveis (LinearLayouts)
        LinearLayout layoutDiaSemana = findViewById(R.id.layoutDiaSemana);
        LinearLayout layoutInicio = findViewById(R.id.layoutInicio);
        LinearLayout layoutTermino = findViewById(R.id.layoutTermino);
        LinearLayout layoutDisciplina = findViewById(R.id.layoutDisciplina);
        LinearLayout layoutProfessor = findViewById(R.id.layoutProfessor);
        LinearLayout layoutSala = findViewById(R.id.layoutSala);

        // 2. Vincular os TextViews que vão mudar de texto
        TextView txtDiaSemana = findViewById(R.id.txtDiaSemana);
        TextView txtInicio = findViewById(R.id.txtInicio);
        TextView txtTermino = findViewById(R.id.txtTermino);
        TextView txtDisciplina = findViewById(R.id.txtDisciplina);
        TextView txtProfessor = findViewById(R.id.txtProfessor);
        TextView txtSala = findViewById(R.id.txtSala);

        // 3. Definir as listas de dados
        String[] dias = {"Segunda-feira", "Terça-feira", "Quarta-feira", "Quinta-feira", "Sexta-feira"};
        String[] horariosInicio = {"07:30", "08:20", "09:30", "10:20", "13:30", "14:20"};
        String[] horariosTermino = {"08:20", "09:10", "10:20", "11:10", "14:20", "15:10"};
        String[] disciplinas = {"Lógica de Programação", "Redes de Computadores", "Banco de Dados", "Desenvolvimento Web"};
        String[] professores = {"Prof. Alan Turing", "Profª. Ada Lovelace", "Prof. Grace Hopper"};
        String[] salas = {"Laboratório 01", "Laboratório 02", "Sala 05", "Auditório"};

        // 4. Configurar os cliques para abrir as listas de escolha
        layoutDiaSemana.setOnClickListener(v -> mostrarDialogo(txtDiaSemana, "Selecione o Dia", dias));
        layoutInicio.setOnClickListener(v -> mostrarDialogo(txtInicio, "Horário de Início", horariosInicio));
        layoutTermino.setOnClickListener(v -> mostrarDialogo(txtTermino, "Horário de Término", horariosTermino));
        layoutDisciplina.setOnClickListener(v -> mostrarDialogo(txtDisciplina, "Selecione a Disciplina", disciplinas));
        layoutProfessor.setOnClickListener(v -> mostrarDialogo(txtProfessor, "Selecione o Professor(a)", professores));
        layoutSala.setOnClickListener(v -> mostrarDialogo(txtSala, "Selecione a Sala", salas));

        // 5. Configurar os botões de fechar e voltar
        Button btnVoltar = findViewById(R.id.btnVoltar);
        ImageView btnFechar = findViewById(R.id.btnFechar);

        if (btnVoltar != null) btnVoltar.setOnClickListener(v -> finish());
        if (btnFechar != null) btnFechar.setOnClickListener(v -> finish());

        // 6. Configurar a ação do botão Salvar (Agora dentro do escopo correto!)
        androidx.appcompat.widget.AppCompatButton btnSalvarHorario = findViewById(R.id.btnSalvarHorario);
        if (btnSalvarHorario != null) {
            btnSalvarHorario.setOnClickListener(v -> {
                String dia = txtDiaSemana.getText().toString();
                String disciplina = txtDisciplina.getText().toString();

                if (dia.equals("Selecione o dia da semana") || disciplina.equals("Selecione a disciplina")) {
                    Toast.makeText(this, "Por favor, preencha os campos obrigatórios!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Horário Salvo com Sucesso!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }
    } // Fim do onCreate

    // Função para exibir a lista de seleção moderna e atualizar o texto
    private void mostrarDialogo(TextView campoTexto, String titulo, String[] itens) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(titulo);
        builder.setItems(itens, (dialog, which) -> {
            campoTexto.setText(itens[which]);
        });
        builder.show();
    }
}