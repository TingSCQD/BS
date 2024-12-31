package org.bs.service;

import org.bs.entities.*;
import org.bs.mapper.UserMapper;
import org.bs.utils.ApiResult;

import java.lang.reflect.Array;
import java.time.Duration;

import org.checkerframework.checker.units.qual.A;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.*;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.convert.DurationFormat;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.bs.utils.HashUtils;


import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ExecutionException;
// import java.util.Map;


@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private final UserMapper userMapper;


    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public ApiResult openAccount(User user) {
        try {
            String email = user.getEmail();
            int count = userMapper.checkEmail(email);
            if (count > 0) {
                return ApiResult.failure("Email already in use");
                //throw new RuntimeException("Email already registered");
            }
            String userName = user.getUserName();
            System.out.println(userName);
            int count2 = userMapper.checkUserName(userName);
            System.out.println(count2);
            if (count2 > 0) {
                return ApiResult.failure("Username already in use");
                //throw new RuntimeException("User name already exists");
            }
            //System.out.println("count2: " + count2);
            userMapper.openAccount(userName, HashUtils.sha256Hash(user.getPassword()), email);
            User newUser = userMapper.getUserByEmail(email);
            return ApiResult.success(newUser);
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ApiResult.failure("Error opening account");
        }
        //return null;
    }

    @Override
    public ApiResult deleteAccount(Integer userId) {
        try {
            userMapper.deleteUser(userId);
            return ApiResult.success(null);
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ApiResult.failure("Error deleting account");
        }
    }

    @Override
    public ApiResult modifyPassword(Integer userId, String oldPassword, String newPassword) {
        try {
            int count = userMapper.judgePassword(userId, HashUtils.sha256Hash(oldPassword));
            if (count != 1) {
                return ApiResult.failure("密码错误");
            }
            userMapper.updatePassword(HashUtils.sha256Hash(newPassword), userId);
            return ApiResult.success(null);
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ApiResult.failure("Error modify password");
        }
    }

    @Override
    @Transactional
    public ApiResult modifyUserName(Integer userId, String newUserName) {
        try {
            int count = userMapper.checkUserName(newUserName);
            if (count > 0) {
                return ApiResult.failure("Username already in use");
            }
            userMapper.updateUserName(userId, newUserName);
            return ApiResult.success(null);
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ApiResult.failure("Error modify user name");
        }
    }

    @Override
    public ApiResult userLogin(String email, String password) {
        try {
            Integer userId = userMapper.getUserId(email);
            if (userId == null) {
                return ApiResult.failure("账户名或密码错误！");
            }
            int count = userMapper.judgePassword(userId, HashUtils.sha256Hash(password));
            if (count != 1) {
                return ApiResult.failure("账户名或密码错误！");
            } else {
                return ApiResult.success(userId);
            }
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ApiResult.failure("Error judging user login");
        }
    }

    @Override
    public ApiResult discountRemind(Discount discount) {
        try {
            if(userMapper.checkDiscount(discount.getUser_id(),discount.getGood_id()) == 1)
            {
                userMapper.updateDiscount(discount.getUser_id(),discount.getGood_id(),discount.getRemind_price());
            }else {
                userMapper.insertDiscount(discount);
            }
            return ApiResult.success();
        } catch (Exception e) {
            return ApiResult.failure("Error discount");
        }
    }
    @Override
    public ApiResult getHistory(int goodId)
    {
        try{
            ArrayList<History> histories = new ArrayList<>();
            histories = userMapper.getGoodHistory(goodId);
            return ApiResult.success(histories);

        }catch (Exception e){
            return ApiResult.failure("Error getting history");
        }
    }

    @Override
    public ApiResult userSearch(String query) {


        ArrayList<Good> SNgoods = new ArrayList<>();
        ArrayList<Good> JDgoods = new ArrayList<>();
        JDgoods = crawlJD(query);
        SNgoods = crawlSN(query);
        ArrayList<Good> goodsList = new ArrayList<>();
        goodsList.addAll(JDgoods);
        goodsList.addAll(SNgoods);
        LocalDate currentDate = LocalDate.now();
        System.out.println(currentDate);
        for (Good good : goodsList) {
            try{
                userMapper.insertDatePrice(String.valueOf(currentDate),good.getId(),good.getPrice());
            }catch (Exception e){
                continue;
            }
        }


        if (!goodsList.isEmpty()) {
            return ApiResult.success(goodsList);
        }
        else {
            return ApiResult.failure("wrong");
        }

    }

    public ArrayList<Good> crawlSN(String query) {
        ArrayList<Good> goodsList = new ArrayList<Good>();
        WebDriver driver = new EdgeDriver();
        try {
            // 初始化WebDriver
            // 设置目标网址
            String SN_url = "https://search.suning.com/" + query + "/";

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

                // 使用Selenium提取动态加载的价格信息
                WebElement priceElement = driver.findElement(By.xpath("//li[contains(@class, 'item') and .//img[@src='" + pict + "']]//span[@class='def-price']"));
                String price = priceElement.getText();
                Good good = new Good();
                good.setImage(pict);
                good.setTitle(title);
                good.setPrice(price);
                good.setSource("苏宁");
                good.setShopName(shopName);
                if (userMapper.checkGoods(title, shopName) == 0) {
                    userMapper.insertGoods(good);
                }
                good.setId(userMapper.getGoodsId(title, shopName));
                goodsList.add(good);
                System.out.println(title);
                System.out.println(pict);
                System.out.println(price);
                System.out.println(shopName);
            }
            // 关闭浏览器
            driver.quit();
            return goodsList;
        } catch (Exception e) {
            driver.quit();
            System.out.println(e.getMessage());
            return goodsList;
        }
    }


    public ArrayList<Good> crawlJD(String query) {


        ArrayList<Good> goodsList = new ArrayList<Good>();
        try {
            // 初始化WebDriver
            // 设置目标网址
            String url =  "https://search.jd.com/Search?keyword=" + query;
            Map<String, String> cookies = new HashMap<String, String>();
           cookies.put("thor", "43FA03C88CED023CC23AE9DC314E7ECAFDDFAB35405FFECE1E7D474C1E3FAE05639FDAB0C078A0F4D5B41CA660AF12E7D2507805F9BCD7DC0882B40BFFE29E00B24045BFCA6C727B5121E338972855143CFAB87F983DAD6C671667EEFA781FD6211453F216FD2E26E8C3B91BBD7DD4C6C0FA4A4AADAB809F6E42E4CC8C1F9F2F97975301F3F0032DBA00D10916A720AEE096EB8086D3874F70286FC835059694");

            Document doc = Jsoup.connect(url).cookies(cookies).get();
            Elements ul = doc.getElementsByClass("gl-warp clearfix");
            int times =0;
            while (ul.isEmpty()) {
                times++;
                if (times>5)
                    break;
                doc = Jsoup.connect(url).cookies(cookies).get();
                ul = doc.getElementsByClass("gl-warp clearfix");
            }
            // 获取ul标签下的所有li标签
            Elements liList = ul.select("li");
            for (Element element : liList) {
                System.out.println("------------------");
                if ("ps-item".equals(element.attr("class"))) {
                    continue;
                }
                String pict = element.getElementsByTag("img").first().attr("data-lazy-img");
                String title = element.getElementsByClass("p-name").first().text();
                String price = element.getElementsByClass("p-price").first().text();
                String shopName = element.getElementsByClass("p-shop").first().text();
                Good good = new Good();
                good.setTitle(title);
                good.setImage(pict);
                good.setPrice(price);
                good.setShopName(shopName);
                good.setSource("京东");
                if (userMapper.checkGoods(title, shopName) == 0) {
                    userMapper.insertGoods(good);
                }
                good.setId(userMapper.getGoodsId(title, shopName));
                System.out.println(title);
                System.out.println(pict);
                System.out.println(price);
                System.out.println(shopName);
                goodsList.add(good);
            }
            // 关闭浏览器

            return goodsList;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return goodsList;
        }

    }

}

