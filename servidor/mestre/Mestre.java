
import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class Mestre {

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", 8000), 0);
        server.createContext("/processar", new MestreHandler());
        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();
        System.out.println("Servidor mestre rodando na porta 8000...");
    }

    static class MestreHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {

                String texto;
                try (BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))) {
                    texto = br.readLine();
                }

                String letrasUrl = "http://escravo-letras:8001/letras";
                String numerosUrl = "http://escravo-numeros:8002/numeros";

                boolean letrasDisponivel = escravoDisponivel("http://escravo-letras:8001/ping");
                boolean numerosDisponivel = escravoDisponivel("http://escravo-numeros:8002/ping");

                if (!letrasDisponivel || !numerosDisponivel) {
                    String indisponivel = "Um ou mais escravos estão indisponíveis.";
                    System.out.println(indisponivel);
                    byte[] erroBytes = indisponivel.getBytes();
                    exchange.sendResponseHeaders(503, erroBytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(erroBytes);
                    }
                    return;
                }

                System.out.println("Todos os escravos estão disponíveis. Processando...");

                ExecutorService executor = Executors.newFixedThreadPool(2);
                Future<String> letrasFuture = executor.submit(() -> chamaEscravo(letrasUrl, texto));
                Future<String> numerosFuture = executor.submit(() -> chamaEscravo(numerosUrl, texto));

                try {
                    String letras = letrasFuture.get();
                    String numeros = numerosFuture.get();

                    String resultadoFinal = "Letras: " + letras + " | Números: " + numeros;
                    System.out.println("Resultado final: " + resultadoFinal);

                    byte[] respostaBytes = resultadoFinal.getBytes();
                    exchange.sendResponseHeaders(200, respostaBytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(respostaBytes);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    String erro = "Erro ao processar dados.";
                    byte[] erroBytes = erro.getBytes();
                    exchange.sendResponseHeaders(500, erroBytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(erroBytes);
                    }
                } finally {
                    executor.shutdown();
                }
            } else {
                String erroMetodo = "Método não suportado.";
                byte[] erroBytes = erroMetodo.getBytes();
                exchange.sendResponseHeaders(405, erroBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(erroBytes);
                }
            }
        }

        private static boolean escravoDisponivel(String url) {
            try {
                System.out.println("Verificando disponibilidade de: " + url);
                HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(2000);
                conn.setReadTimeout(2000);
                int status = conn.getResponseCode();
                boolean disponivel = (status == 200);
                System.out.println("Resposta de " + url + ": " + status + (disponivel ? " (Disponível)" : " (Indisponível)"));
                return disponivel;
            } catch (Exception e) {
                System.out.println("Erro ao verificar " + url + ": " + e.getMessage());
                return false;
            }
        }

        private String chamaEscravo(String url, String texto) throws IOException {
            HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "text/plain");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(texto.getBytes());
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                return br.readLine();
            }
        }
    }
}
