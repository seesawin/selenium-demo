package com.example.songrank;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.List;

public class App {

    public static final String BROWSER_DRIVER_KEY = "webdriver.chrome.driver";
    public static final String APP_REPO_KEY = "app.repo";

    public static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("songrank <options>", options);
    }

    public static void configBrowserDriver() {
        String basePath = System.getProperty(APP_REPO_KEY) + File.separator + "drivers" + File.separator;

        if (SystemUtils.IS_OS_WINDOWS) {
            System.setProperty(BROWSER_DRIVER_KEY, basePath + "chromedriver.exe");
        } else if (SystemUtils.IS_OS_MAC) {
            System.setProperty(BROWSER_DRIVER_KEY, basePath + "chromedriver-mac");
        } else if (SystemUtils.IS_OS_LINUX) {
            System.setProperty(BROWSER_DRIVER_KEY, basePath + "chromedriver-linux");
        }

    }

    public static void main(String[] args) {

        // 自動設定 Browser Driver
        if (System.getProperty(APP_REPO_KEY) != null) {
            configBrowserDriver();
        }

        // 定義 CLI 參數
        Options options = new Options();
        options.addOption("y", "year", true, "西元年 (四位數) 預設: 今年");
        options.addOption("m", "month", true, "月 01 ~ 12) 預設: 本月");
        options.addOption("d", "day", true, "日 01 ~ 31 預設: 本日");
        options.addOption("h", "help", false, "印出CLI說明");

        // 透過 Selenium 開始命令 Chrome Driver 開啟一個網頁
        WebDriver browser = new ChromeDriver();
        try {
            DefaultParser parser = new DefaultParser();

            // 解析使用者在指令行輸入的參數
            CommandLine cmdLine = parser.parse(options, args);

            // 如果使用者帶入 -h 參數，則印出操作說明，並結束程式
            if (cmdLine.hasOption('h')) {
                printHelp(options);
                System.exit(0);
            }

            // 解析要抓取的日子
            Calendar today = Calendar.getInstance();

            String year = Integer.toString(today.get(Calendar.YEAR));
            if (cmdLine.hasOption('y')) {
                year = cmdLine.getOptionValue('y');
            }

            String month = Integer.toString(today.get(Calendar.MONTH) + 1);
            if (cmdLine.hasOption('m')) {
                month = cmdLine.getOptionValue('m');
            }
            month = StringUtils.leftPad(month, 2, '0');

            String day = Integer.toString(today.get(Calendar.DAY_OF_MONTH));
            if (cmdLine.hasOption('d')) {
                day = cmdLine.getOptionValue('d');
            }
            day = StringUtils.leftPad(day, 2, '0');

            browser.get("https://kma.kkbox.com/charts/daily/newrelease?date=" + year + "-" + month + "-" + day + "&lang=tc&terr=tw");

            Thread.sleep(5000);

            System.out.println("process...");

            List<WebElement> songElements = browser.findElements(By.cssSelector("li.charts-list-row"));


            for (WebElement songElement : songElements) {
                try {
                    String currRank = songElement.findElement(By.cssSelector("span.charts-list-rank")).getText();
                    String prevRank = songElement.findElement(By.cssSelector("span.charts-list-prev-rank")).getText();
                    String song = songElement.findElement(By.cssSelector("span.charts-list-song")).getText();
                    String artist = songElement.findElement(By.cssSelector("span.charts-list-artist")).getText();
                    System.out.println(currRank + ": " + song + " [" + artist + "]");
                } catch (NoSuchElementException e) {
                    e.printStackTrace();
                    continue;
                }

            }
            System.out.println("抓取完成");

            System.out.println("開始全部播放...按ENTER結束");
            browser.findElement(By.cssSelector("a.btn-preview-all")).click();

            System.in.read();

        } catch (Exception ex) {
            ex.printStackTrace();
            printHelp(options);
        } finally {
            browser.close();
        }
    }
}
