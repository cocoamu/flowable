package com.cocoamu.flowable.conver;


import com.cocoamu.flowable.domain.User;

public class UserConverter {

    public static org.flowable.idm.api.User userTokenConverterToUser() {
//        AssertUtils.assertNotNull(userToken, "userToken is not null!");
//        User user = new User();
//        user.setId(userToken.getAccountId());
//        user.setFirstName(userToken.getAccountId());
//        AssertUtils.assertNotNull(userToken, "userToken is not null!");
        User user = new User();
        user.setId("1");
        user.setFirstName("1");
        return user;
    }
}
