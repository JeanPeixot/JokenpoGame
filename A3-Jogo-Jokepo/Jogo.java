import java.io.*;
import java.net.*;
import java.util.Random;

public class Jogo extends Thread {
    private Socket socket;
    private PrintWriter escritor;
    private BufferedReader leitor;
    private static final Object lock = new Object();
    private static Jogo PrimeiroJogador = null;
    private static Jogo SegundoJogador = null;
    private static int rodada = 0;
    private static final int totalRodadas = 5;
    private static String[] escolhas = new String[2];
    private static int placarJogador1 = 0;
    private static int placarJogador2 = 0;
    private String nomeJogador;

    public Jogo(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            InputStream input = socket.getInputStream();
            leitor = new BufferedReader(new InputStreamReader(input, "UTF-8"));

            OutputStream output = socket.getOutputStream();
            escritor = new PrintWriter(new OutputStreamWriter(output, "UTF-8"), true);

            escritor.println("Bem vindo ao Jokenpo!");
            nomeJogador = leitor.readLine();
            System.out.println("Nome do jogador: " + nomeJogador);

            escritor.println("Ola, " + nomeJogador + "! Por favor escolha se deseja jogar contra um Computador ou contra outro jogador: \n 1 - Computador  \n 2 - Outro Jogador");

            String escolhaModo = leitor.readLine();
            System.out.println("Escolha do jogador: " + escolhaModo);

            if ("1".equals(escolhaModo)) {
                JogarContraComputador();
            } else if ("2".equals(escolhaModo)) {
                jogarContraOutroJogador();
            } else {
                escritor.println("Escolha invalida. Encerrando conexao.");
            }

        } catch (IOException | InterruptedException ex) {
            System.out.println("Erro: " + ex.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException ex) {
                System.out.println("Erro ao fechar socket: " + ex.getMessage());
            }
        }
    }

    private void JogarContraComputador() throws IOException {
        Random random = new Random();
        String[] opcoes = {"pedra", "papel", "tesoura"};
        int rodada = 0;

        escritor.println("O jogo começou, " + nomeJogador + "! Escolha entre pedra, papel ou tesoura");

        while (rodada < totalRodadas) {
            String escolhaJogador = leitor.readLine();
            String escolhaRobo = opcoes[random.nextInt(opcoes.length)];

            escritor.println("O Computador escolheu: " + escolhaRobo);

            if (!escolhaJogador.equals("pedra") && !escolhaJogador.equals("papel") && !escolhaJogador.equals("tesoura")) {
                escritor.println("Escolha invalida! Por favor, escolha entre pedra, papel ou tesoura");
                continue; // Solicitar nova escolha do jogador
            }

            if (escolhaJogador.equals(escolhaRobo)) {
                escritor.println("Empate!");
            } else if ((escolhaJogador.equals("pedra") && escolhaRobo.equals("tesoura")) ||
                    (escolhaJogador.equals("papel") && escolhaRobo.equals("pedra")) ||
                    (escolhaJogador.equals("tesoura") && escolhaRobo.equals("papel"))) {
                escritor.println("Você ganhou a rodada!");
                placarJogador1++;
            } else {
                escritor.println("Computador ganhou a rodada!");
                placarJogador2++;
            }

            rodada++;
            if (rodada < totalRodadas) {
                escritor.println("Proxima rodada. Escolha entre pedra, papel ou tesoura");
            }
        }

        escritor.println("Fim do jogo. Placar Final - " + nomeJogador + ": " + placarJogador1 + " | Computador: " + placarJogador2);
    }

    private void jogarContraOutroJogador() throws IOException, InterruptedException {
        synchronized (lock) {
            if (PrimeiroJogador == null) {
                PrimeiroJogador = this;
                escritor.println("Esperando outro jogador se conectar!");
                while (SegundoJogador == null) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                iniciarJogo();
            } else {
                SegundoJogador = this;
                lock.notifyAll();
            }
        }

        respostaJogadores();

        if (rodada == totalRodadas) {
            finalizarJogo();
        }
    }

    private void iniciarJogo() {
        PrimeiroJogador.escritor.println("O jogo começou! Escolha entre pedra, papel ou tesoura");
        SegundoJogador.escritor.println("O jogo começou! Escolha entre pedra, papel ou tesoura");
    }

    private void determinarVencedorRodada() throws InterruptedException, IOException {
        rodada++;
        String resultado;
        if (escolhas[0].equals(escolhas[1])) {
            resultado = "Empate!";
        } else if ((escolhas[0].equals("pedra") && escolhas[1].equals("tesoura")) ||
                (escolhas[0].equals("papel") && escolhas[1].equals("pedra")) ||
                (escolhas[0].equals("tesoura") && escolhas[1].equals("papel"))) {
            resultado = PrimeiroJogador.nomeJogador + " ganhou a rodada!";
            placarJogador1++;
        } else {
            resultado = SegundoJogador.nomeJogador + " ganhou a rodada!";
            placarJogador2++;
        }

        PrimeiroJogador.escritor.println(resultado);
        SegundoJogador.escritor.println(resultado);

        if (rodada < totalRodadas) {
            PrimeiroJogador.escritor.println("Proxima rodada. Escolha entre pedra, papel ou tesoura");
            SegundoJogador.escritor.println("Proxima rodada. Escolha entre pedra, papel ou tesoura");
        }
    }

    private void finalizarJogo() {
        PrimeiroJogador.escritor.println("Fim do jogo. Placar Final - " + nomeJogador + ": " + placarJogador1 + " | " + SegundoJogador.nomeJogador + ": " + placarJogador2);
        SegundoJogador.escritor.println("Fim do jogo. Placar Final - " + nomeJogador + ": " + placarJogador1 + " | " + SegundoJogador.nomeJogador + ": " + placarJogador2);
    }

    private void respostaJogadores() throws InterruptedException, IOException {
        while (rodada < totalRodadas) {
            String escolha = leitor.readLine();
            synchronized (lock) {
                if (this == PrimeiroJogador) {
                    if (!escolha.equals("pedra") && !escolha.equals("papel") && !escolha.equals("tesoura")) {
                        escritor.println("Escolha invalida! Por favor, escolha entre pedra, papel ou tesoura");
                        continue; // Solicitar nova escolha do jogador
                    }
                    escolhas[0] = escolha;
                    SegundoJogador.escritor.println("Esperando resposta do outro jogador!");
                } else {
                    if (!escolha.equals("pedra") && !escolha.equals("papel") && !escolha.equals("tesoura")) {
                        escritor.println("Escolha invalida! Por favor, escolha entre pedra, papel ou tesoura");
                        continue; // Solicitar nova escolha do jogador
                    }
                    escolhas[1] = escolha;
                    PrimeiroJogador.escritor.println("Esperando resposta do outro jogador!");
                }

                if (escolhas[0] != null && escolhas[1] != null) {
                    determinarVencedorRodada();
                    escolhas[0] = null;
                    escolhas[1] = null;
                    lock.notifyAll();
                } else {
                    while (escolhas[0] == null && escolhas[1] == null) {
                        lock.wait();
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        int porta = 3000; // Porta do servidor
        Servidor servidor = new Servidor(porta);
        servidor.iniciar(); // Iniciando o servidor
    }
}
