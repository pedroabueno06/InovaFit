package com.example;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

//Testes de integração do projeto InovaFit executados contra os bancos de dados PostegreSQL e Oracle.
public class InovafitTest {
    private record Banco(String nome, String url, String usuario, String senha, boolean oracle) {}

    //1- Testa a conexão com os dois bancos de dados
    @Test
    void conexoesComOsDoisBancosDeDados() throws Exception {
        BancoTest.testarEmAmbosOsBancosDeDados(((banco, connection) -> {
            assertTrue(connection.isValid(5), "Conexão inválida: " + banco.nome());
        }));
    }

    //2- Testa se a classe Inovafit.java inicia e encerra pelo seu menu()
    @Test
    void InovafitIniciaEEncerraPeloMenu() {
        InputStream entradaOriginal = System.in;
        PrintStream saidaOriginal = System.out;
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        try {
            System.setIn(new ByteArrayInputStream("0\n".getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(saida, true, StandardCharsets.UTF_8));
            Inovafit.main(new String[0]);
            assertTrue(saida.toString(StandardCharsets.UTF_8).contains("Sistema encerrado."));
        } finally {
            System.setIn(entradaOriginal);
            System.setOut(saidaOriginal);
        }
    }
}
