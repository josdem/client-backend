package com.all.backend.web;

import java.util.Date;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.all.shared.model.Gender;
import com.all.shared.model.User;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/persistence-context.xml",
		"/test-dataSource.xml", "/test-service-context.xml" })
@TransactionConfiguration
@Transactional
public abstract class BasetestCase {
	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}

	public User createUser() {
		User user = new User();
		user.setFirstName("Unit");
		user.setLastName("Last name");
		user.setEmail("test@all.com");
		user.setNickName("nickname");
		user.setBirthday(new Date());
		user.setPassword("1234567890");
		user.setGender(Gender.MALE);
		user.setIdLocation("12345");

		return user;
	}

}
