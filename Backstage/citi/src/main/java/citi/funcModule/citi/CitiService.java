package citi.funcModule.citi;

import citi.API.Authorize;
import citi.API.Card;
import citi.API.Customer;
import citi.API.PayWithAwards;
import citi.persist.mapper.CitiMapper;
import citi.persist.mapper.MSCardMapper;
import citi.persist.mapper.TokenMapper;
import citi.persist.mapper.UserMapper;
import citi.vo.*;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

/*
 * 构架：刘钟博
 * 作者：曹子轩
 */
@Service
public class CitiService {

    @Autowired
    private TokenMapper tokenMapper;

    @Autowired
    private CitiMapper citiMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private Gson gson;

    @Autowired
    private MSCardMapper msCardMapper;

    /**
     * 绑定操作
     * @param citiCard 花旗卡
     * @return 判断绑定是否成功
     */
    public boolean binding(CitiCard citiCard){
        int flag = citiMapper.insert(citiCard);
        if(flag>0)
            return true;
        return false;
    }

    public void saveRefreshToken(String accessInformation, String userID){
        String refreshAccessToken = Authorize.getRefreshToken(accessInformation);
        refreshAccessToken = Authorize.getTokenByRF(refreshAccessToken);
        RefreshToken refreshToken = new RefreshToken(userID,refreshAccessToken,new Timestamp(new Date().getTime()));
        tokenMapper.update(refreshToken);
    }

    public String getPhoneNum(String accessToken){
        String phoneNum = null;
        String phoneInformation = Customer.getCustomerPhone(accessToken);
        //先转JsonObject
        JsonObject jsonObject = new JsonParser().parse(phoneInformation).getAsJsonObject();
        //再转JsonArray 加上数据头
        JsonArray jsonArray = jsonObject.getAsJsonArray("phones");

        ArrayList<Phone> phones = new ArrayList<>();

        //循环遍历
        for (JsonElement phone : jsonArray) {
            //通过反射 得到UserBean.class
            Phone phoneVO = gson.fromJson(phone, new TypeToken<Phone>() {}.getType());
            phones.add(phoneVO);
        }
        phoneNum = String.valueOf(phones.get(0).getPhoneNum());
        return phoneNum;
    }

    public String getCardNum(String accessToken){
        String creditCardNum=null;
        String cardsInformation = Card.getCardsInformation(accessToken);
        //先转JsonObject
        JsonObject jsonObject = new JsonParser().parse(cardsInformation).getAsJsonObject();
        //再转JsonArray 加上数据头
        JsonArray jsonArray = jsonObject.getAsJsonArray("cardDetails");

        ArrayList<CardDetail> details = new ArrayList<>();

        //循环遍历
        for (JsonElement phone : jsonArray) {
            //通过反射 得到UserBean.class
            CardDetail cardDetail = gson.fromJson(phone, new TypeToken<CardDetail>() {}.getType());
            details.add(cardDetail);
        }
        creditCardNum = String.valueOf(details.get(0).getDisplayCardNumber());
        // 只取最后四位
        creditCardNum = creditCardNum.substring(creditCardNum.length()-4);
        return creditCardNum;
    }



    public String getCardID(String accessToken){
        String creditCardNum=null;
        String cardsInformation = Card.getCardsInformation(accessToken);
        //先转JsonObject
        JsonObject jsonObject = new JsonParser().parse(cardsInformation).getAsJsonObject();
        //再转JsonArray 加上数据头
        JsonArray jsonArray = jsonObject.getAsJsonArray("cardDetails");

        ArrayList<CardDetail> details = new ArrayList<>();

        //循环遍历
        for (JsonElement detail : jsonArray) {
            //通过反射 得到UserBean.class
            CardDetail cardDetail = gson.fromJson(detail, new TypeToken<CardDetail>() {}.getType());
            details.add(cardDetail);
        }
        creditCardNum = String.valueOf(details.get(0).getCardId());
        return creditCardNum;
    }

    public double getPoints(String pointInformation){
        PointInformation jsonObject = gson.fromJson(pointInformation, PointInformation.class);
        return jsonObject.getAvailablePointBalance();
    }

    public CitiCard getCardToBeBind(String code, String state){
        String phoneNum = null;
        String creditCardNum = null;
        String citiCardID = null;
        String accessInformation = Authorize.getAccessTokenWithGrantType(code,"http://193.112.44.141/citi/citi/bindCard");//
        String accessToken = Authorize.getToken(accessInformation);
        //saveRefreshToken(accessInformation, state);
        phoneNum = getPhoneNum(accessToken);
        creditCardNum = getCardNum(accessToken);
        citiCardID = getCardID(accessToken);
        CitiCard citiCard = new CitiCard(citiCardID, creditCardNum,phoneNum,state,0.0);
        String linkCode = PayWithAwards.getLinkCode(creditCardNum,phoneNum,"2608191234111",accessToken);
        PayWithAwards.activateCode(linkCode,accessToken);
        String pointsInformation = PayWithAwards.getInformation(linkCode,accessToken);
        double totalPoints = getPoints(pointsInformation);
        MSCard msCard = new MSCard(state, creditCardNum, (int)totalPoints, "23");
        msCardMapper.insert(msCard);
        return citiCard;
    }


    public String getNewTokenAndSaveFreshToken(String userID){
        String  formerRefreshToken = tokenMapper.select(userID);
        String[] tokens = Authorize.getTokenAndRefreshTokenByFormerRefreshToken(userID,formerRefreshToken);
        String token = tokens[0];
        String refreshToken = tokens[1];
        saveRefreshToken(refreshToken, userID);
        return token;
    }

    public String updatePointsInformation(String userID){
        String token = getNewTokenAndSaveFreshToken(userID);
        User user = userMapper.getInfoByPhone(userID);
        String linkCode = user.getRewardLinkCode();
        String pointsInformation = PayWithAwards.getInformation(linkCode, token);
        return pointsInformation;
    }

    public String payWithPoints(String userID, String orderInformation){
        User user = userMapper.getInfoByUserID(userID);
        String linkCode = user.getRewardLinkCode();
        String finishOrderInformation = PayWithAwards.finishOrder(linkCode, orderInformation);
        return finishOrderInformation;
    }

}
