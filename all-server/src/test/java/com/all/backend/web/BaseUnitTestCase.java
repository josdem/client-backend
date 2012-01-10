package com.all.backend.web;

import java.util.Date;

import org.junit.runner.RunWith;

import com.all.shared.model.Gender;
import com.all.shared.model.User;
import com.all.testing.MockInyectRunner;

@RunWith(MockInyectRunner.class)
public abstract class BaseUnitTestCase {
	public static User createUser() {
		User user = new User();
		user.setFirstName("Unit");
		user.setLastName("Last name");
		user.setEmail("test@all.com");
		user.setBirthday(new Date());
		user.setPassword("1234567890");
		user.setGender(Gender.MALE);
		user.setIdLocation("12345");

		return user;
	}
}
