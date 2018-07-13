package citiMerchant.mapper;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class DBHandler {
    @Autowired
    public  OrderMapper orderMapper;
    @Autowired
    public  ItemMapper itemMapper;
    @Autowired
    public  StrategyMapper strategyMapper;

    static final private String resource = "./src/main/java/citiMerchant/mapper/citiMerchant/mapper.xml";
    static private SqlSessionFactory sqlSessionFactory;

    static private String pathname = "./src/main/java/citiMerchant/mapper/log.txt";
    static private File log_file;
    static private BufferedWriter log_writer;

    static {
        try {

            System.out.println(System.getProperty("user.dir"));

            //init log_file
            log_file = new File(pathname);

            //init sqlsession
            Reader reader = Resources.getResourceAsReader(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);

        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("\nfail to read mapper.xml\n");
        }
    }

    /****************************        初始化完成        ****************************/

    /*
     * 用于日志
     */
    static private void log(String method, long time) {
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
            String current_time = df.format(new Date());// new Date()为获取当前系统时间
            log_writer = new BufferedWriter(new FileWriter(log_file));
            log_writer.write("\"" + method + "\" elapsed time: " +
                    (System.currentTimeMillis() - time) + "ms at" + current_time + "\n");
            log_writer.flush();
            log_writer.close();

        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("\nfail to log \"" + method + "\"\n");
        }
    }

    /****************************       用于商户后台的接口     ****************************/

    /*
     * 返回一个列表：包含该商家的“优惠商品”，“减免策略”，“历史订单”的数量。
     */
     public ArrayList<Integer> getAmountByMerchantID(String merchantID) {
        long time = System.currentTimeMillis();
        int itemAmount = itemMapper.getItemAmountByMerchantID(merchantID);
        int stategyAmount = strategyMapper.getStrategyAmountByMerchantID(merchantID);
        int orderAmount = orderMapper.getOrderAmount(merchantID);
        log("getAmountByMerchantID", time);
        List<Integer> amount = Arrays.asList(itemAmount, stategyAmount, orderAmount);
        return new ArrayList(amount);
    }


    /*
     * 用于统计商家的积分流水信息，包含兑入和兑出。
     */
    static public class Record {

        //针对某一商家的积分流入信息
        static public enum Record_IN {
            //返回coupon的积分兑入总和
            Coupon,
            //返回order的积分使用总和
            Order;
            static Map<String, Record_IN> enumMap1 = new HashMap<>();
            static Map<Record_IN, String> enumMap2 = new HashMap<>();

            static {
                enumMap1.put("coupon_record", Coupon);
                enumMap1.put("order_record", Order);

                enumMap2.put(Coupon, "coupon_record");
                enumMap2.put(Order, "order_record");
            }

            public static Record_IN getRecord_TYPE(String record_type_s) {
                return enumMap1.get(record_type_s);
            }

            public static String getRecord_TYPE_S(Record_IN record_type) {
                return enumMap2.get(record_type);
            }

        }


        /*
         * 调用格式：
         * 返回7天之内商家"00001"的优惠券积分流入
         * long totalPoints = DBHandler.Record.points_in_record("00001", 7, DBHandler.Record.Record_IN.Coupon);
         */
        static public long points_in_record(String IN_MerchantID, int IN_intervalDate, Record_IN record_type) {
            Map<String, Object> map = new HashMap<>();
            long totalPoints = -1;
            map.put("IN_MerchantID", (Object) IN_MerchantID);
            map.put("IN_intervalDate", (Object) IN_intervalDate);
            map.put("totalPoints", (Object) totalPoints);
            long time = System.currentTimeMillis();
            SqlSession session = sqlSessionFactory.openSession();
            final String record_type_s = Record_IN.getRecord_TYPE_S(record_type);
            session.selectOne("citiMerchant.mapper.DBHandler." + record_type_s, map);
            totalPoints = (Integer) map.get("totalPoints");
            session.commit();
            session.close();
            log("STORED PROCEDURE " + record_type_s, time);
            return totalPoints;
        }

    }//end class Record - 统计商家积分流水


}
