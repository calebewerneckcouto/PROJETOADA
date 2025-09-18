package br.com.adacommerce.ecommerce.service;

import java.io.FileOutputStream;
import java.util.List;

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
import br.com.adacommerce.ecommerce.model.ItemPedido;
import br.com.adacommerce.ecommerce.model.Pedido;
import br.com.adacommerce.ecommerce.model.Produto;

public class RelatorioPDFService {

    private ClienteService clienteService;
    private ProdutoService produtoService;
    private VendaService vendaService;
    
    private final String SENHA_GERENTE = System.getenv("GERENTE_SENHA");

    // 🎨 Paleta de cores corporativas
    private static final BaseColor VERDE_ESCURO = new BaseColor(0, 102, 77);
    private static final BaseColor CINZA_CLARO = new BaseColor(245, 245, 245);
    private static final BaseColor CINZA_MEDIO = new BaseColor(200, 200, 200);
    private static final BaseColor BRANCO = BaseColor.WHITE;

    public RelatorioPDFService(ClienteService clienteService, ProdutoService produtoService, VendaService vendaService) {
        this.clienteService = clienteService;
        this.produtoService = produtoService;
        this.vendaService = vendaService;
    }

    public String gerarRelatorio(String caminhoArquivo,String senhaFuncionario) {
        try {
        	
        	
        	if (!SENHA_GERENTE.equals(senhaFuncionario)) {
        	    return "❌ Senha incorreta! Acesso negado.";
        	}
            Document document = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter.getInstance(document, new FileOutputStream(caminhoArquivo));
            document.open();

            // =================== CABEÇALHO ===================
            Font tituloFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, BRANCO);
            PdfPTable headerTable = new PdfPTable(1);
            PdfPCell cell = new PdfPCell(new Phrase("📊 Relatório ADA Commerce", tituloFont));
            cell.setBackgroundColor(VERDE_ESCURO);
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setPadding(12);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerTable.setWidthPercentage(100);
            headerTable.addCell(cell);
            document.add(headerTable);

            document.add(new Paragraph("\n"));

            // =================== CLIENTES ===================
            Font secaoFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, VERDE_ESCURO);
            document.add(new Paragraph("👥 Clientes Cadastrados", secaoFont));
            document.add(new Paragraph("\n"));

            PdfPTable tabelaClientes = criarTabela(new String[]{"ID", "Nome", "Email", "Documento"});
            List<Cliente> clientes = clienteService.listarClientes();
            for (Cliente c : clientes) {
                tabelaClientes.addCell(c.getId().toString());
                tabelaClientes.addCell(c.getNome());
                tabelaClientes.addCell(c.getEmail());
                tabelaClientes.addCell(c.getDocumento());
            }
            document.add(tabelaClientes);

            document.add(new Paragraph("\n"));

            // =================== PRODUTOS ===================
            document.add(new Paragraph("📦 Produtos em Estoque", secaoFont));
            document.add(new Paragraph("\n"));

            PdfPTable tabelaProdutos = criarTabela(new String[]{"ID", "Nome", "Descrição", "Preço", "Estoque"});
            List<Produto> produtos = produtoService.listarProdutos();
            for (Produto p : produtos) {
                tabelaProdutos.addCell(p.getId().toString());
                tabelaProdutos.addCell(p.getNome());
                tabelaProdutos.addCell(p.getDescricao());
                tabelaProdutos.addCell(String.format("R$ %.2f", p.getPreco()));
                tabelaProdutos.addCell(String.valueOf(p.getQuantidadeEstoque()));
            }
            document.add(tabelaProdutos);

            document.add(new Paragraph("\n"));

            // =================== PEDIDOS ===================
            document.add(new Paragraph("🧾 Pedidos Finalizados", secaoFont));
            document.add(new Paragraph("\n"));

            List<Pedido> pedidos = vendaService.listarPedidosFinalizados();
            for (Pedido ped : pedidos) {
                Paragraph pedidoInfo = new Paragraph(
                        String.format("Pedido #%d | Cliente: %s | Status: %s | Total: R$ %.2f | Data: %s",
                                ped.getId(), ped.getCliente().getNome(), ped.getStatus().name(),
                                ped.getValorTotal(), ped.getDataCriacao()
                        ),
                        new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, VERDE_ESCURO)
                );
                document.add(pedidoInfo);

                PdfPTable tabelaItens = criarTabela(new String[]{"Produto", "Quantidade", "Preço Unitário", "Subtotal"});
                for (ItemPedido item : ped.getItens()) {
                    tabelaItens.addCell(item.getProduto().getNome());
                    tabelaItens.addCell(String.valueOf(item.getQuantidade()));
                    tabelaItens.addCell(String.format("R$ %.2f", item.getPrecoVenda()));
                    tabelaItens.addCell(String.format("R$ %.2f", item.getPrecoVenda() * item.getQuantidade()));
                }
                document.add(tabelaItens);

                document.add(new Paragraph("\n"));
            }

            // =================== RODAPÉ ===================
            document.add(new Paragraph("\n"));
            Font rodapeFont = new Font(Font.FontFamily.HELVETICA, 9, Font.ITALIC, BaseColor.GRAY);
            Paragraph footer = new Paragraph("Relatório confidencial - ADA Commerce © 2025", rodapeFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();

           

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "✅ Relatório PDF gerado em: " + caminhoArquivo;
    }

    // 🔹 Criação de tabelas padronizadas com cabeçalho estilizado
    private PdfPTable criarTabela(String[] colunas) {
        PdfPTable tabela = new PdfPTable(colunas.length);
        tabela.setWidthPercentage(100);

        Font cabecalhoFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BRANCO);

        for (String coluna : colunas) {
            PdfPCell header = new PdfPCell(new Phrase(coluna, cabecalhoFont));
            header.setBackgroundColor(VERDE_ESCURO);
            header.setHorizontalAlignment(Element.ALIGN_CENTER);
            header.setPadding(6);
            tabela.addCell(header);
        }

        tabela.getDefaultCell().setBackgroundColor(CINZA_CLARO);
        tabela.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);

        return tabela;
    }
}
