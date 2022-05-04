package com.example.graphwebdata;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class GraphWebDataController implements Initializable {
    private Parent root;
    private Stage stage;
    private Scene scene;

    @FXML
    public LineChart lineChart;

    @FXML
    private BorderPane borderPane;

    @FXML
    public DatePicker inputStartDate;

    @FXML
    public DatePicker inputEndDate;

    @FXML
    public ChoiceBox inputInterval;

    @FXML
    public TextField inputTicker;

    @FXML
    public Button btnGraphData;
    //public String[] tickers = {"GOOG", "AAPL", "AMD", "NVDA", "INTC", "BTC-USD", "BBBB"}; // sample tickers
    public String startTime; // current start date
    public String endTime;   // current end date
    public String interval;  // current interval of data points (i.e. daily, weekly, monthly, ...)
    public String ticker;    // current ticker

    public List<List<String>> stockData = new ArrayList<>(); // current stock data

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        inputInterval.getItems().addAll("1d", "1wk", "1mo"); // daily, weekly, monthly
    }

    /**
     * Retrieves ticker choice from choiceBox, calls getStockPrices with ticker to retrieve data, and then updates
     * the chart with new data.
     */
    public void takeInputs() {
        // Local Time to seconds since epoch (i.e. 2015-07-23 -> 1001002020)
        startTime = String.valueOf(inputStartDate.getValue().toEpochSecond(LocalTime.parse("00:00"), ZoneOffset.of("+00:00")));
        endTime = String.valueOf(inputEndDate.getValue().toEpochSecond(LocalTime.parse("00:00"), ZoneOffset.of("+00:00")));
        interval = String.valueOf(inputInterval.getValue());
        ticker = inputTicker.getText();
        this.stockData = getStockPrices(ticker, startTime, endTime, interval); // get the corresponding stock data
        updateLineChart(this.stockData); // update line chart with newly chosen data
    }

    /**
     * Updates lineChart whenever new inputs are chosen
     * @param data
     */
    public void updateLineChart(List<List<String>> data) {
        List<Float> closingData = new ArrayList();
        List<String> dateData = new ArrayList();
        for (List<String> row: data) {
            dateData.add(row.get(0));
            closingData.add(Float.parseFloat(row.get(1)));
        }

        NumberAxis yAxis = new NumberAxis(); // closing price
        yAxis.setLabel("Closing Price ($USD)");

        CategoryAxis xAxis = new CategoryAxis(); // dates
        xAxis.setLabel("Date");

        lineChart = new LineChart(xAxis, yAxis);

        XYChart.Series series = new XYChart.Series();

        for (int i = 0; i < dateData.size(); i++) {
            series.getData().add(new XYChart.Data(dateData.get(i), closingData.get(i)));
        }
        series.setName(ticker.toUpperCase());

        lineChart.getData().add(series);

        borderPane.setCenter(lineChart);
    }

    /**
     * Gets the monthly stock data from 2010-2022 of the specified ticker (e.g. google is GOOG)
     * @return - returns list of stock closing prices
     * @throws IOException
     */
    public static List<List<String>> getStockPrices(String stock, String start, String end, String inter) {
        List<List<String>> data = new ArrayList<>();
        if (stock != null && start != null && end != null && inter != null) {
            try {
                URL url = new URL("https://query1.finance.yahoo.com/v7/finance/download/" + stock +
                        "?period1=" + start + "&period2=" + end + "&interval=" + inter + "&events=history&includeAdjustedClose=true");

                URLConnection conn = url.openConnection();
                conn.setDoOutput(false);
                conn.setDoInput(true);
                try {
                    InputStream is = conn.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader br = new BufferedReader(isr);
                    br.readLine(); // first row is column names, which are not needed.

                    String line = "";
                    while ((line = br.readLine()) != null) {
                        String[] row = line.split(",");
                        data.add(List.of(row[0], row[4])); // date and closing price
                    }
                    br.close();

                } catch (IOException e) {
                    // Exception handling for when ticker is not valid (not a stock listed on finance.yahoo.ca)
                    handleInvalidTicker(stock);

                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return data;
    }

    /**
     * when Invalid ticker is entered, FileNotFoundException is thrown in getStockPrices. This function is to make a
     * pop-up error message and to make the code a bit more cleanly.
     * @param ticker
     */
    public static void handleInvalidTicker(String ticker) {
        Alert tickerAlert = new Alert(Alert.AlertType.ERROR, "Invalid Ticker: " + ticker, ButtonType.CLOSE);
        tickerAlert.setHeaderText("INVALID TICKER");
        tickerAlert.showAndWait();

        if (tickerAlert.getResult() == ButtonType.CLOSE) {
            tickerAlert.close();
        }
    }

    /**
     * handles the event for Graph Data button
     * @param actionEvent - Graph Data button is pressed
     */
    @FXML
    public void handleGraphData(ActionEvent actionEvent) {
        // if null inputs, error pop-up window and don't take inputs
        if (inputStartDate.getValue() == null || inputEndDate.getValue() == null || inputInterval.getValue() == null ||
        inputTicker.getText() == "") {
            handleNullInputs();
        }
        else {
            takeInputs();
        }
    }

    /**
     * error pop-up message for when user inputs null values
     */
    public void handleNullInputs() {
        Alert tickerAlert = new Alert(Alert.AlertType.ERROR, "", ButtonType.CLOSE);
        tickerAlert.setHeaderText("INVALID INPUT");
        tickerAlert.showAndWait();

        if (tickerAlert.getResult() == ButtonType.CLOSE) {
            tickerAlert.close();
        }
    }

    /**
     * handles the event for menu item Exit
     * @param actionEvent - Exit menu item is pressed
     */
    @FXML
    public void handleExit(ActionEvent actionEvent) {
        System.exit(0); //end program
    }
}