package br.com.adacommerce.ecommerce.service;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import br.com.adacommerce.ecommerce.model.Cliente;
import br.com.adacommerce.ecommerce.model.Pedido;
import br.com.adacommerce.ecommerce.model.Produto;

public class GptService {

    private final String CHAT_GPT_API_KEY = System.getenv("OPENAI_KEY");
    private final String MODEL = "gpt-4-turbo";
    private final String SENHA_GERENTE = System.getenv("GERENTE_SENHA");

    private ClienteService clienteService;
    private ProdutoService produtoService;
    private VendaService vendaService;

    private Gson gson = new Gson();

    public GptService(ClienteService clienteService, ProdutoService produtoService, VendaService vendaService) {
        this.clienteService = clienteService;
        this.produtoService = produtoService;
        this.vendaService = vendaService;
    }

    /**
     * Gera relatório via GPT
     */
    public String gerarRelatorioGPT(String pergunta, String senhaFuncionario) {
        if (!SENHA_GERENTE.equals(senhaFuncionario)) {
            return "Senha incorreta! Acesso negado.";
        }

        List<String> chunks = gerarChunksDoBanco();

        if (chunks.isEmpty()) {
            return "Nenhum dado encontrado no banco de dados para análise.";
        }

        StringBuilder dadosCompletos = new StringBuilder();
        dadosCompletos.append(criarPromptComRegras(pergunta));
        dadosCompletos.append("\n\n=== DADOS DO BANCO ADA COMMERCE ===\n\n");

        for (String chunk : chunks) {
            dadosCompletos.append(chunk).append("\n\n");
        }

        dadosCompletos.append("=== FIM DOS DADOS ===\n\n");
        dadosCompletos.append("Baseando-se EXCLUSIVAMENTE nos dados acima, responda: ").append(pergunta);

        try {
            return enviarParaGPT(dadosCompletos.toString());
        } catch (Exception e) {
            return "Erro ao processar dados com GPT: " + e.getMessage();
        }
    }

    private String criarPromptComRegras(String pergunta) {
        return String.format(
                """
                Você é o Gerente ADA, assistente de análise de dados do sistema ADA Commerce.

                INSTRUÇÕES CRÍTICAS:
                1. Os dados do banco de dados serão fornecidos logo abaixo
                2. Responda APENAS com base nos dados fornecidos
                3. Se a pergunta não estiver relacionada aos dados do banco (clientes, produtos, pedidos, vendas), responda:
                   "Estou aqui apenas para ajudar na gestão e análise dos dados do ADA Commerce. Para outras questões, consulte a administração."
                4. Seja preciso e use apenas informações reais dos dados
                5. Forneça insights úteis (estatísticas, análises, recomendações)
                6. Use formato claro e organizado

                PERGUNTA A RESPONDER: %s
                """,
                pergunta);
    }

    private List<String> gerarChunksDoBanco() {
        List<String> chunks = new ArrayList<>();

        
        StringBuilder clientesChunk = new StringBuilder("=== CLIENTES ===\n");
        for (Cliente c : clienteService.listarClientes()) {
            clientesChunk.append(String.format("ID: %d | Nome: %s | Email: %s | Documento: %s\n",
                    c.getId(), c.getNome(), c.getEmail(), c.getDocumento()));
        }
        chunks.add(clientesChunk.toString());

       
        StringBuilder produtosChunk = new StringBuilder("=== PRODUTOS ===\n");
        for (Produto p : produtoService.listarProdutos()) {
            produtosChunk.append(String.format("ID: %d | Nome: %s | Descrição: %s | Preço: %.2f | Estoque: %d\n",
                    p.getId(), p.getNome(), p.getDescricao(), p.getPreco(), p.getQuantidadeEstoque()));
        }
        chunks.add(produtosChunk.toString());

        
        StringBuilder pedidosChunk = new StringBuilder("=== PEDIDOS ===\n");
        for (Pedido ped : vendaService.listarPedidosFinalizados()) {
            pedidosChunk.append(String.format(
                    "\nPEDIDO ID: %d | Cliente: %s | Status: %s | Valor Total: %.2f | Data: %s\n",
                    ped.getId(), ped.getCliente().getNome(), ped.getStatus().name(),
                    ped.getValorTotal(), ped.getDataCriacao()));
        }
        if (!pedidosChunk.isEmpty()) chunks.add(pedidosChunk.toString());

        return chunks;
    }
    
    /**
     * Método para debug - mostra os dados que serão enviados ao GPT
     */
    public void debugDados(String pergunta) {
        List<String> chunks = gerarChunksDoBanco();
        StringBuilder dadosCompletos = new StringBuilder();
        dadosCompletos.append(criarPromptComRegras(pergunta));
        dadosCompletos.append("\n\n=== DADOS DO BANCO ADA COMMERCE ===\n\n");

        for (String chunk : chunks) {
            dadosCompletos.append(chunk).append("\n\n");
        }

        System.out.println("=== DEBUG: DADOS QUE SERÃO ENVIADOS AO GPT ===");
        System.out.println(dadosCompletos.toString());
        System.out.println("=== FIM DO DEBUG ===");
    }

    
    /**
     * Método para debug - mostra quantos chunks foram gerados e preview dos dados
     */
    public void mostrarEstatisticasChunks() {
        List<String> chunks = gerarChunksDoBanco();
        System.out.println("=== ESTATÍSTICAS DOS CHUNKS ===");
        System.out.println("Total de chunks: " + chunks.size());

        for (int i = 0; i < chunks.size(); i++) {
            System.out.printf("Chunk %d: %d caracteres\n", i + 1, chunks.get(i).length());
           
            String[] linhas = chunks.get(i).split("\n");
            System.out.println("Preview:");
            for (int j = 0; j < Math.min(3, linhas.length); j++) {
                System.out.println("  " + linhas[j]);
            }
            if (linhas.length > 3) {
                System.out.println("  ... (+" + (linhas.length - 3) + " linhas)");
            }
            System.out.println();
        }
    }


    private String enviarParaGPT(String conteudo) {
        try {
            URL url = new URL("https://api.openai.com/v1/chat/completions");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization", "Bearer " + CHAT_GPT_API_KEY);
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);

            String body = gson.toJson(Map.of(
                    "model", MODEL,
                    "messages", List.of(Map.of("role", "user", "content", conteudo)),
                    "max_tokens", 2000,
                    "temperature", 0.2
            ));

            try (OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream())) {
                writer.write(body);
                writer.flush();
            }

            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                StringBuilder responseData = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    responseData.append(line);
                }

                JsonObject jsonResponse = JsonParser.parseString(responseData.toString()).getAsJsonObject();
                JsonArray choices = jsonResponse.getAsJsonArray("choices");

                if (choices.size() > 0) {
                    JsonObject choice = choices.get(0).getAsJsonObject();
                    JsonObject messageObj = choice.getAsJsonObject("message");
                    if (messageObj != null && messageObj.has("content")) {
                        return messageObj.get("content").getAsString();
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            return "Erro ao conectar com GPT: " + e.getMessage();
        }

        return "Resposta vazia do GPT";
    }

    /**
     * Envia relatório por email (simples, sem pedido)
     */
    public boolean enviarRelatorioPorEmail(String destinatario, String pergunta, String relatorio) {
        try {
            String assunto = "Relatório ADA Commerce - " + pergunta;
            String corpoHtml = formatarRelatorioParaEmail(pergunta, relatorio);

            EmailService emailService = new EmailService(destinatario, "Sistema ADA Commerce", assunto, corpoHtml);
            emailService.enviarEmail();
            return true;

        } catch (Exception e) {
            System.err.println("Erro ao enviar email: " + e.getMessage());
            return false;
        }
    }

    private String formatarRelatorioParaEmail(String pergunta, String relatorio) {
        String dataHora = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date());

        return String.format(
                "<html><body><h2>Relatório ADA Commerce</h2><p>Gerado em %s</p><p>Pergunta: %s</p><pre>%s</pre></body></html>",
                dataHora,
                pergunta,
                relatorio.replace("<", "&lt;").replace(">", "&gt;")
        );
    }
    
    
    public void gerarRelatorioPdfComPergunta(String caminhoArquivo, String pergunta, String relatorio) {
        try {
            Document document = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter.getInstance(document, new FileOutputStream(caminhoArquivo));
            document.open();

           
            Font tituloFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, BaseColor.WHITE);
            PdfPTable headerTable = new PdfPTable(1);
            PdfPCell cell = new PdfPCell(new Phrase("📊 Relatório do Gerente ADA", tituloFont));
            cell.setBackgroundColor(new BaseColor(30, 70, 32)); // verde escuro
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setPadding(12);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerTable.setWidthPercentage(100);
            headerTable.addCell(cell);
            document.add(headerTable);

            document.add(new Paragraph("\n"));

            
            String dataHora = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date());
            Font dataFont = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC, BaseColor.GRAY);
            Paragraph dataParagrafo = new Paragraph("Gerado em: " + dataHora, dataFont);
            dataParagrafo.setAlignment(Element.ALIGN_RIGHT);
            document.add(dataParagrafo);

            document.add(new Paragraph("\n"));

            
            Font perguntaTituloFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, new BaseColor(0, 123, 255));
            Paragraph perguntaParagrafo = new Paragraph("Pergunta solicitada:", perguntaTituloFont);
            document.add(perguntaParagrafo);

            Font perguntaFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.BLACK);
            Paragraph perguntaConteudo = new Paragraph(pergunta, perguntaFont);
            perguntaConteudo.setIndentationLeft(10);
            perguntaConteudo.setSpacingAfter(15);
            document.add(perguntaConteudo);

          
            Font respostaTituloFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, new BaseColor(30, 70, 32));
            Paragraph respostaTitulo = new Paragraph("Resposta do Gerente ADA:", respostaTituloFont);
            document.add(respostaTitulo);

            Font respostaFont = new Font(Font.FontFamily.COURIER, 12, Font.NORMAL, BaseColor.BLACK);
            Paragraph respostaConteudo = new Paragraph(relatorio, respostaFont);
            respostaConteudo.setIndentationLeft(10);
            respostaConteudo.setSpacingAfter(20);
            document.add(respostaConteudo);

           
            Font rodapeFont = new Font(Font.FontFamily.HELVETICA, 9, Font.ITALIC, BaseColor.GRAY);
            Paragraph rodape = new Paragraph("ADA Commerce - Gestão Inteligente de Dados", rodapeFont);
            rodape.setAlignment(Element.ALIGN_CENTER);
            document.add(rodape);

            document.close();
            System.out.println("✅ PDF gerado com sucesso em: " + caminhoArquivo);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
