package no.steria.quizzical.admin;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class LoginServletTest {

	private HttpServletRequest req = mock(HttpServletRequest.class);
	private HttpServletResponse resp = mock(HttpServletResponse.class);
	private LoginServlet servlet = new LoginServlet();
	private DateTime currentTime = new DateTime(2013, 7, 25, 16, 5, 0);
	private MongoUserDao mongoUserDao = mock(MongoUserDao.class);

	@Before
	public void setup() throws Exception {
		PasswordUtil passwordUtil = new PasswordUtil();
		byte[] salt = passwordUtil.generateSalt();
		byte[] encryptedPassword = passwordUtil.getEncryptedPassword("password", salt);

		when(mongoUserDao.getUser("admin")).thenReturn(new User(1, "admin", salt, encryptedPassword, null));

		servlet.setMongoUserDao(mongoUserDao);
		DateTimeUtils.setCurrentMillisFixed(currentTime.getMillis());

		when(mongoUserDao.validatePassword("admin", "password")).thenReturn(true);
	}


	@After
	public void resetClock() {
		DateTimeUtils.setCurrentMillisSystem();
	}

	@Test
	public void shouldLogin() throws Exception {
		when(req.getMethod()).thenReturn("POST");
		when(req.getParameter("user")).thenReturn("admin");
		when(req.getParameter("password")).thenReturn("password");
		HttpSession mockSession = mock(HttpSession.class);
		when(req.getSession()).thenReturn(mockSession);

		servlet.service(req, resp);

		verify(resp).sendRedirect("#/admin");
		verify(mockSession).setAttribute("username", "admin");
		verify(mockSession).setAttribute("valid", currentTime.plusMinutes(30));
	}

	@Test
	public void shouldDenyMissingUsername() throws Exception {
		when(req.getMethod()).thenReturn("POST");
		when(req.getParameter("password")).thenReturn("password");
		HttpSession mockSession = mock(HttpSession.class);
		when(req.getSession()).thenReturn(mockSession);

		servlet.service(req, resp);

		verify(resp).sendRedirect("#/?loginFailed=true");
		verify(mockSession, never()).setAttribute(anyString(), any(Object.class));
	}

	@Test
	public void shouldDenyUnknownUser() throws Exception {
		when(req.getMethod()).thenReturn("POST");
		when(req.getParameter("user")).thenReturn("unknown");
		when(req.getParameter("password")).thenReturn("password");
		HttpSession mockSession = mock(HttpSession.class);
		when(req.getSession()).thenReturn(mockSession);

		servlet.service(req, resp);

		verify(resp).sendRedirect("#/?loginFailed=true");
		verify(mockSession,never()).setAttribute(anyString(), any(Object.class));
	}

	@Test
	public void shouldDenyIllegalPassword() throws Exception {
		when(req.getMethod()).thenReturn("POST");
		when(req.getParameter("user")).thenReturn("admin");
		when(req.getParameter("password")).thenReturn("pasx");
		HttpSession mockSession = mock(HttpSession.class);
		when(req.getSession()).thenReturn(mockSession);

		servlet.service(req, resp);

		verify(resp).sendRedirect("#/?loginFailed=true");
		verify(mockSession, never()).setAttribute(anyString(), any(Object.class));
	}
}
