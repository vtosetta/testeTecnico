package com.sincronizacaoReceita;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.sincronizacaoReceita.ReceitaService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;

@SpringBootApplication
public class SincronizacaoReceitaApplication {
	

	public static void main(String[] args) {

		SpringApplication.run(SincronizacaoReceitaApplication.class, args);

		try {
			Reader reader = null;
			reader = Files.newBufferedReader(Paths.get(args[0]));
			CSVParser parser = new CSVParserBuilder().withSeparator(';').build();
			CSVReader csvReader = new CSVReaderBuilder(reader).withCSVParser(parser).build();
			List<String[]> conteudoCsv = csvReader.readAll();
			
			if (Objects.nonNull(conteudoCsv) && conteudoCsv.size() > 0 && conteudoCsv.get(0).length > 1) {
				
				FileWriter writer = new FileWriter(args[0]);
				CSVWriter csvWriter = new CSVWriter(writer, ';', CSVWriter.NO_QUOTE_CHARACTER,
						CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
				
				String[] cabecalho = conteudoCsv.get(0);
				String[] novaColuna = Arrays.copyOf(cabecalho, cabecalho.length + 1);
				novaColuna[novaColuna.length-1] = "resultado";
				csvWriter.flush();
				csvWriter.writeNext(novaColuna);
				conteudoCsv.remove(0);
				
				ReceitaService receitaService = new ReceitaService();
				
				for (String[] linha : conteudoCsv) {
					if (linha.length == 4) {
						boolean retorno = false;
						try {
							retorno = receitaService.atualizarConta(linha[0], linha[1].replaceAll("-", ""),
									Double.valueOf(linha[2].replaceAll(",", ".")), linha[3]);
						} catch (RuntimeException | InterruptedException e) {
							System.out.println("Erro ao comunicar com o serviço da receita");
						}
						novaColuna = Arrays.copyOf(linha, linha.length + 1);
						novaColuna[novaColuna.length-1] = retorno ? "sucesso" : "erro";
						csvWriter.writeNext(novaColuna);
					} else if (linha.length > 4){
						System.out.println("Formato de arquivo inválido");
					} else {
						novaColuna = Arrays.copyOf(linha, linha.length + 1);
						novaColuna[novaColuna.length-1] = "Erro - Favor preencher devidamente todas as colunas.";
						csvWriter.writeNext(novaColuna);
					}
				}
				
				csvWriter.flush();
				writer.close();
			}else {
				System.out.println("Arquivo sem conteúdo, ou com formato inválido");
			}

		} catch (IOException e) {
			System.out.println("Forneça o caminho do arquivo de entrada.");
		} catch (CsvException e) {
			System.out.println("Ocorreu um erro ao realizar a leitura do arquivo.");
		}

	}
	
}
