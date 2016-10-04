package ru.sbt.bit.ood.hw1;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.net.ftp.FTPClient;

import java.io.*;

public class TradesJob {

    private final TradesDAO tradesDAO;

    public TradesJob(TradesDAO tradesDAO) {
        this.tradesDAO = tradesDAO;
    }

    public void run() {
        String filename = downloadTradesFileFromFTP();
        Iterable<CSVRecord> tradeRecords = parse(filename);
        updateTrades(tradeRecords);
    }

    public String downloadTradesFileFromFTP() {
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect("localhost", 8090);
            ftpClient.login("foo", "password");
            File tempFile = File.createTempFile("trades", "download");
            OutputStream out = new FileOutputStream(tempFile);
            ftpClient.retrieveFile("public/prod/trades.csv", out);
            out.close();
            return tempFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not download the file");
        }
    }

    private void updateTrades(Iterable<CSVRecord> trades) {
        tradesDAO.deleteAll();
        for (CSVRecord tradeRecord : trades) {
            Trade trade = new Trade(tradeRecord.toMap());
            tradesDAO.save(trade);
        }
    }

    private Iterable<CSVRecord> parse(String filename) {
        try {
            Reader in = new FileReader(filename);
            Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(in);
            return records;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("There was an error while parsing CSV file");
        }
    }
}
