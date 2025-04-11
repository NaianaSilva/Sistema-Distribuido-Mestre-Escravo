
import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class EscravoLetras {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8001), 0);
        server.createContext("/letras", new LetrasHandler());

    
        server.createContext("/ping", exchange -> {
            String resposta = "OK";
            exchange.sendResponseHeaders(200, resposta.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(resposta.getBytes());
            }
        });

        server.setExecutor(Executors.newFixedThreadPool(2));
        server.start();
        System.out.println("Escravo de letras rodando na porta 8001...");
    }

    static class LetrasHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String texto;
                try (BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))) {
                    texto = br.readLine();
                }

                long count = texto.chars().filter(Character::isLetter).count();
                String resposta = Long.toString(count);

                exchange.sendResponseHeaders(200, resposta.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(resposta.getBytes());
                }
            }
        }
    }
}
