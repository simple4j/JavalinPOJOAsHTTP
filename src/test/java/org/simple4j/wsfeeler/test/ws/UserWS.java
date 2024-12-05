package org.simple4j.wsfeeler.test.ws;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.javalin.Javalin;


public class UserWS
{

	private static Logger logger = LoggerFactory.getLogger(UserWS.class);
	private static ApplicationContext ac;

	public static void start()
	{
		ac = new ClassPathXmlApplicationContext("ws/main-appCntxt.xml");
		UserDAO userDAO = (UserDAO) ac.getBean("userDAO");
		
		try
		{
			//below line is needed for this sample WS only
			userDAO.createUserTable();
		}
		catch(Throwable t)
		{
			logger.warn("Probably the table exists already", t);
		}
		ObjectMapper om = new ObjectMapper();

		Javalin javalin = Javalin.create();
        javalin.get("/user/{userPK}", ctx -> 
			{
				ctx.header("Content-Type", "application/JSON");
				String userPK = ctx.pathParam("userPK");
				if (userPK == null || userPK.trim().length() < 1)
				{
					ctx.status(412);
					ctx.result( "{\"errorCodes\" : [\"userPK-required\"]}");
					return;
				}
				if (userPK.trim().length() > 40)
				{
					ctx.status(412);
					ctx.result( "{\"errorCodes\" : [\"userPK-maxlength\"]}");
					return;
				}
				UserVO user = userDAO.getUser(userPK);
				ctx.status(200);
				ctx.result(om.writeValueAsString(user));
			});

        javalin.put("/user", ctx -> 
			{
				logger.info("inside user put");
				if (ctx.contentLength() > 2048)
					throw new RuntimeException("content too large");
				ctx.header("Content-Type", "application/JSON");
				String bodyStr = ctx.body();
				UserVO userVO = om.readValue(bodyStr, UserVO.class);

				// input validation start
				StringBuilder sb = validateUserVO(userVO);
				if (sb.length() > 0)
				{
					ctx.status(412);
					ctx.result(sb.toString());
					return;
				}
				// input validation end

				userVO.userPK = UUID.randomUUID().toString();
				userDAO.insertUser(userVO);
				ctx.status(200);
				ctx.result("{\"userPK\" : \""+userVO.userPK+"\"}");
				return;
			});

        javalin.post("/user", ctx -> 
			{
				logger.info("inside user post");
				if (ctx.contentLength() > 2048)
					throw new RuntimeException("content too large");
				ctx.header("Content-Type", "application/JSON");
				String bodyStr = ctx.body();
				UserVO userVO = om.readValue(bodyStr, UserVO.class);

				// input validation start
				StringBuilder sb = validateUserVO(userVO);
				if (sb.length() > 0)
				{
					ctx.status(412);
					ctx.result( sb.toString());
					return;
				}
				// input validation end

				userDAO.updateUser(userVO);
				ctx.status(200);
				ctx.result("{}");
				return ;
			});

        javalin.start(2001);
	}

	private static StringBuilder validateUserVO(UserVO userVO)
	{
		StringBuilder sb = new StringBuilder();
		String delim = "";
		if (userVO.displayName == null || userVO.displayName.trim().length() < 1)
		{
			sb.append(delim).append("displayName-required");
			delim = ",";
		}
		if (userVO.displayName.trim().length() > 20)
		{
			sb.append(delim).append("displayName-maxlength");
			delim = ",";
		}
		if (userVO.gender == null || userVO.gender.trim().length() < 1)
		{
			sb.append(delim).append("gender-required");
			delim = ",";
		}
		if (!userVO.gender.equalsIgnoreCase("F") && !userVO.gender.equalsIgnoreCase("M")
				&& !userVO.gender.equalsIgnoreCase("O"))
		{
			sb.append(delim).append("gender-invalid");
			delim = ",";
		}
		if (userVO.birthMonth != null && (userVO.birthMonth < 1 || userVO.birthMonth > 12))
		{
			sb.append(delim).append("birthMonth-invalid");
			delim = ",";
		}
		return sb;
	}

	public static void stop()
	{

	}
}
