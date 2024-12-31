package org.bs.service;

import org.bs.entities.Discount;
import org.bs.entities.Good;
import org.bs.mapper.UserMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Component

public class DiscountRemind {


    @Autowired
    UserMapper userMapper;
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Scheduled(fixedRate = 600000)
    public void discountRemind() {
        ArrayList<Discount> discounts = new ArrayList<>();

        discounts = userMapper.getDiscountList();

        for (Discount discount : discounts) {
            Good good = new Good();
            good = userMapper.getGoodById(discount.getGood_id());
            int now_price = 0;
            if (good.getSource().equals("苏宁")) {
                now_price = getSN_price(good);
            } else {
                now_price = getJD_price(good);
            }
            if (now_price < discount.getRemind_price() && now_price != -1) {
                if(remind_user(good, discount.getUser_id(), now_price))
                {
                    System.out.println("成功向用户发送提醒");
                }
            }

        }
    }

    private int getSN_price(Good good) {
        int result = -1;
        WebDriver driver = new EdgeDriver();
        try {
            // 初始化WebDriver
            // 设置目标网址
            String SN_url = "https://search.suning.com/" + good.getTitle() + "/";

            // 打开目标网址
            driver.get(SN_url);

            // 使用WebDriverWait等待页面加载完成
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".general.clearfix")));
            // 获取页面源代码并使用Jsoup解析
            String pageSource = driver.getPageSource();
            Document doc = Jsoup.parse(pageSource);
            // 提取商品信息
            Elements ul = doc.getElementsByClass("general clearfix");
            Elements liList = ul.select("li");

            for (Element element : liList) {
                String pict = element.getElementsByTag("img").attr("src");
                String title = element.getElementsByClass("title-selling-point").text();
                String shopName = element.getElementsByClass("store-stock").text();
                if (title.equals(good.getTitle()) && shopName.equals(good.getShopName())) {
                    WebElement priceElement = driver.findElement(By.xpath("//li[contains(@class, 'item') and .//img[@src='" + pict + "']]//span[@class='def-price']"));
                    String price = priceElement.getText();
                    String regex = "\\d+";
                    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
                    java.util.regex.Matcher matcher = pattern.matcher(price);

                    if (matcher.find()) { // 将匹配到的整数部分转换为int类型
                        result = Integer.parseInt(matcher.group());
                    }
                }
            }
            // 关闭浏览器
            driver.quit();
            return result;
        } catch (Exception e) {
            driver.quit();
            System.out.println(e.getMessage());
            return result;
        }
    }

    private int getJD_price(Good good) {
        int result = -1;
        try {
            String url = "https://search.jd.com/Search?keyword=" + good.getTitle();
            Map<String, String> cookies = new HashMap<String, String>();
            cookies.put("thor", "43FA03C88CED023CC23AE9DC314E7ECAFDDFAB35405FFECE1E7D474C1E3FAE050D905AD0E9BF7BD6148FACBAF4F223C74E4C5ADE3D03A26CB35F1D7AF0D9BCF9C1AD93E45BB5FDDA285EA297BAB7A98F5D8217337657F9A1B27FE80D6D454B3405F5732AB762370E7F107D14610EBE65EF00A0394276CCB5D1E908E618183B56DBF06492B2ECFF981F60999602CF18A16AA65E734570C769E35CAA636B56ABF8");
//            thor=43FA03C88CED023CC23AE9DC314E7ECAFDDFAB35405FFECE1E7D474C1E3FAE050D905AD0E9BF7BD6148FACBAF4F223C74E4C5ADE3D03A26CB35F1D7AF0D9BCF9C1AD93E45BB5FDDA285EA297BAB7A98F5D8217337657F9A1B27FE80D6D454B3405F5732AB762370E7F107D14610EBE65EF00A0394276CCB5D1E908E618183B56DBF06492B2ECFF981F60999602CF18A16AA65E734570C769E35CAA636B56ABF8;
            Document doc = Jsoup.connect(url).cookies(cookies).get();
            Elements ul = doc.getElementsByClass("gl-warp clearfix");
            while (ul.isEmpty()) {
                doc = Jsoup.connect(url).cookies(cookies).get();
                ul = doc.getElementsByClass("gl-warp clearfix");
            }
            // 获取ul标签下的所有li标签
            Elements liList = ul.select("li");
            for (Element element : liList) {
                if ("ps-item".equals(element.attr("class"))) {
                    continue;
                }
                String title = element.getElementsByClass("p-name").first().text();
                String shopName = element.getElementsByClass("p-shop").first().text();
                if (title.equals(good.getTitle()) && shopName.equals(good.getShopName())) {
                    String price = element.getElementsByClass("p-price").first().text();
                    String regex = "\\d+";
                    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
                    java.util.regex.Matcher matcher = pattern.matcher(price);

                    if (matcher.find()) { // 将匹配到的整数部分转换为int类型
                        result = Integer.parseInt(matcher.group());
                    }
                }
            }
            return result;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return result;
        }
    }

    private boolean remind_user(Good good, int user_id, int now_price) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom(from);
            message.setTo(userMapper.getEmailById(user_id));
            message.setSubject("降价提醒");
            message.setText("降价提醒：您关注的商品:" + good.getTitle() + "\n"
                    + "现在正在打折促销，现价为￥" + now_price + "，快来看看吧");
            mailSender.send(message);
            return true;
        }catch (Exception e)
        {
            System.out.println(e.getMessage());
            return false;
        }
    }

}
