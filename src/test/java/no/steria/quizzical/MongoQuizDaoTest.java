package no.steria.quizzical;

import static org.fest.assertions.Assertions.assertThat;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

public class MongoQuizDaoTest {

	private MongoQuizDao mongoQuizDao;
	
	@Before
	public void setUp(){
		MongoDatabasePopulation.insertTestQuizzes();
		mongoQuizDao = new MongoQuizDao();
	}
	
	@Test
	public void shouldReadQuizzes() throws Exception {
		ArrayList<Quiz> quizzes = mongoQuizDao.getQuizzes();				
		assertThat(quizzes).hasSize(3);
	}
	
	@Test
	public void shouldGetQuiz() throws Exception {		
		Quiz quiz = MongoDatabasePopulation.testQuiz1();		
		assertThat(mongoQuizDao.getQuiz(1)).isEqualTo(quiz);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldGetExceptionWhenQuizIdNotAvailable() {		
		assertThat(mongoQuizDao.getQuiz(10));
	}
	
	@Test
	public void shouldInsertQuizIntoDB(){		
		MongoDatabasePopulation.main(null);
		Quiz quiz = MongoDatabasePopulation.testQuiz1();
		User user = MongoDatabasePopulation.testUser1();
		
		MongoDatabasePopulation.dropQuizzesInDB();
		mongoQuizDao.insertQuizIntoDB(quiz, user.getUserId());
		assertThat(mongoQuizDao.getQuiz(quiz.getQuizId())).isEqualTo(quiz);
	}

}