
import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class EscravoNumeros {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8002), 0);
        server.createContext("/numeros", new NumerosHandler());

        
        server.createContext("/ping", exchange -> {
            String resposta = "OK";
            exchange.sendResponseHeaders(200, resposta.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(resposta.getBytes());
            }
        });

        server.setExecutor(Executors.newFixedThreadPool(2));
        server.start();
        System.out.println("Escravo de números rodando na porta 8002...");
    }

    static class NumerosHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String texto;
                try (BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))) {
                    texto = br.readLine();
                }

                long count = texto.chars().filter(Character::isDigit).count();
                String resposta = Long.toString(count);

                exchange.sendResponseHeaders(200, resposta.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(resposta.getBytes());
                }
            }
        }
    }
}
