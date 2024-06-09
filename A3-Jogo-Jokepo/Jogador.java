import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Jogador {
    public static void main(String[] args) {
        int port = 3000; // Porta do servidor

        Scanner scanner = new Scanner(System.in);
        System.out.println("Digite o IP do servidor:");
        String hostname = scanner.nextLine();

        System.out.println("Digite seu nome:");
        String nomeJogador = scanner.nextLine();

        try (Socket socket = new Socket(hostname, port)) { // Conectando ao servidor
            BufferedReader leitor = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8")); // Leitor para receber mensagens do servidor
            PrintWriter escritor = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true); // Escritor para enviar mensagens ao servidor
            BufferedReader entradaUsuario = new BufferedReader(new InputStreamReader(System.in, "UTF-8")); // Leitor para receber entrada do usuário

            escritor.println(nomeJogador); // Enviando o nome do jogador para o servidor

            String mensagemServidor;
            while ((mensagemServidor = leitor.readLine()) != null) { // Lendo mensagens do servidor
                System.out.println("Servidor: " + mensagemServidor);

                if (mensagemServidor.contains("Escolha entre pedra, papel ou tesoura") || mensagemServidor.contains("Proxima rodada")) {
                    String escolha = "";
                    boolean escolhaValida = false;
                    while (!escolhaValida) {
                        System.out.print("Sua escolha: ");
                        escolha = entradaUsuario.readLine().toLowerCase(); // Lendo a escolha do usuário
                        if (escolha.equals("pedra") || escolha.equals("papel") || escolha.equals("tesoura")) {
                            escolhaValida = true;
                        } else {
                            System.out.println("Escolha invalida! Por favor, escolha entre pedra, papel ou tesoura.");
                        }
                    }
                    escritor.println(escolha); // Enviando a escolha ao servidor
                } else if (mensagemServidor.contains("2 - Outro Jogador")) {
                    String escolha = entradaUsuario.readLine();
                    escritor.println(escolha);
                }
            }
        } catch (UnknownHostException ex) {
            System.out.println("Servidor não encontrado: " + ex.getMessage()); // Tratando erro de host desconhecido
        } catch (IOException ex) {
            System.out.println("Erro de I/O: " + ex.getMessage()); // Tratando erro de I/O
        }
    }
}
