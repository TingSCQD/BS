package org.bs.mapper;

import org.bs.entities.*;
import org.apache.ibatis.annotations.*;

import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

@Mapper
public interface UserMapper {

    // 创建用户
    @Insert("INSERT INTO users(username, password, email) values (#{userName}, #{password}, #{email})")
    public int openAccount(String userName, String password, String email);
    //public int openAccount(User user);

    // 根据邮箱获取用户信息
    @Select("select * from users where email = #{email}")
    public User getUserByEmail(String email);

    // 检查用户名唯一性
    @Select("select count(*) from users where username=#{name}")
    public int checkUserName(String name);

    // 检查邮箱唯一性
    @Select("select count(*) from users where email=#{email}")
    public int checkEmail(String email);

    // 匹配账户密码
    @Select("select count(*) from users where id=#{userId} and password=#{password}")
    public int judgePassword(Integer userId, String password);

    // 获取用户Id
    @Select("select id from users where email=#{email}")
    public int getUserId(String email);

    // 修改密码
    @Update("update users set password=#{newPassword} where id=#{userId}")
    public int updatePassword(String newPassword, Integer userId);

    // 修改用户名
    @Update("update users set username=#{newName} where id=#{userId}")
    public int updateUserName(Integer userId, String newName);

    // 注销账号
    @Delete("delete from users where id=#{userId}")
    public int deleteUser(Integer userId);

    @Select("select count(*) from goods where title=#{title} and shopName=#{shopName}")
    public int checkGoods(String title, String shopName);

    @Insert("insert into goods(title, image, price, shopName, source) values(#{title},#{image},#{price},#{shopName},#{source})")
    public void insertGoods(Good good);

    @Select("select id from goods where title=#{title} and shopName=#{shopName}")
    public int getGoodsId(String title, String shopName);

    @Insert("insert into discount(user_id, good_id, remind_price) values(#{user_id},#{good_id},#{remind_price})")
    public void insertDiscount(Discount discount);

    @Select("select * from goods where id = #{id}")
    public Good getGoodById(Integer id);

    @Select("select email from users where id=#{id}")
    public String getEmailById(Integer id);

    @Select("SELECT * FROM discount")
    @Results({@Result(property = "user_id", column = "user_id"), @Result(property = "good_id", column = "good_id"), @Result(property = "remind_price", column = "remind_price")})
    ArrayList<Discount> getDiscountList();


    @Select("Select count(*) from discount where user_id=#{user_id} and good_id = #{good_id}")
    public int checkDiscount(Integer user_id, Integer good_id);

    @Update("UPDATE discount set remind_price=#{remind_price} where user_id=#{user_id} and good_id=#{good_id} ")
    public void updateDiscount(Integer user_id, Integer good_id,int remind_price) ;

    @Insert("INSERT into dateprice(date,goods_id, price) values(#{date},#{goods_id},#{price})")
    public void insertDatePrice(String date, Integer goods_id, String price);


    @Select("SELECT * from dateprice where goods_id = #{goods_id)}")
    @Results(
            {
                    @Result(property = "date",column = "date"),@Result(property = "price" ,column = "price")
            }
    )
    public ArrayList<History> getGoodHistory(Integer goods_id);

}