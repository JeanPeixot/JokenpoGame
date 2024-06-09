import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {
    private final int porta;

    public Servidor(int porta) {
        this.porta = porta;
    }

    public void iniciar() {
        try (ServerSocket serverSocket = new ServerSocket(porta)) {
            System.out.println("Servidor rodando na porta " + porta);

            while (true) {
                try {
                    Socket socket = serverSocket.accept(); // Aceitando novas conexões
                    System.out.println("Novo jogador conectado");

                    Jogo clienteThread = new Jogo(socket);
                    clienteThread.start(); // Iniciando uma nova thread para o cliente conectado
                } catch (IOException e) {
                    System.out.println("Erro ao aceitar conexão: " + e.getMessage());
                }
            }
        } catch (IOException ex) {
            System.out.println("Erro ao iniciar o servidor: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        int porta = 3000; // Porta do servidor
        Servidor servidor = new Servidor(porta);
        servidor.iniciar(); // Iniciando o servidor
    }
}
