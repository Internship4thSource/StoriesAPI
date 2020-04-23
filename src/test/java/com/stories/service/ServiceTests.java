package com.stories.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.stories.constants.StoriesApiTestsConstants;
import com.stories.domain.StoryDomain;
import com.stories.domain.TasksDomain;
import com.stories.exception.EntityNotFoundException;
import com.stories.model.StoryModel;
import com.stories.model.TaskModel;
import com.stories.repository.StoriesCustomRepository;
import com.stories.repository.StoriesRepository;
import com.stories.repository.UsersRepository;
import com.stories.sprintsclient.SprintsClient;
import com.stories.utils.TestUtils;
import com.stories.utils.UnitTestProperties;

import ma.glasnost.orika.MapperFacade;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@EnableAutoConfiguration(exclude = { MongoAutoConfiguration.class, MongoDataAutoConfiguration.class })
public class ServiceTests {

	@MockBean
	StoriesRepository storiesRepository;

	@MockBean
	UsersRepository usersRepository;
	
	@MockBean
	StoriesCustomRepository storiesCustomRepository;

	@MockBean
	private MapperFacade mapperFacade;

	@Autowired
	UnitTestProperties unitTestProperties;

	@MockBean
	SprintsClient sprintsClient;

	@InjectMocks
	StoriesServiceImpl storiesServiceImpl;

	private TestUtils testUtils;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		testUtils = new TestUtils();
	}

    MongoTemplate mongoTemplate = Mockito.mock(MongoTemplate.class);
    
	@Test
	public void getById() throws Exception {
		when(storiesRepository.existsById(unitTestProperties.getUrlId())).thenReturn(StoriesApiTestsConstants.booleanTrue);
		when(storiesRepository.findById(unitTestProperties.getUrlId()))
				.thenReturn(java.util.Optional.of(TestUtils.getDummyStoryModel()));
		when(mapperFacade.map(testUtils.getStoryModel(), StoryDomain.class))
				.thenReturn(testUtils.getDummyStoryDomain());
		when(storiesServiceImpl.getStoryById(unitTestProperties.getUrlId()))
				.thenReturn(testUtils.getDummyStoryDomain());
		assertEquals(testUtils.getDummyStoryDomain(), storiesServiceImpl.getStoryById(unitTestProperties.getUrlId()));
	}

	@Test(expected = EntityNotFoundException.class)
	public void getByIdException() throws Exception {
		when(storiesRepository.existsById(unitTestProperties.getUrlId())).thenReturn(StoriesApiTestsConstants.booleanFalse);
		Mockito.when(storiesServiceImpl.getStoryById(unitTestProperties.getUrlId()))
				.thenThrow(new EntityNotFoundException(StoriesApiTestsConstants.messageStory, StoriesApiTestsConstants.path));
	}

	@Test
	public void getAllStories() throws Exception {
		when(storiesRepository.findAll()).thenReturn(storiesServiceImpl.storiesModel);
		assertEquals(storiesServiceImpl.storiesDomain, storiesServiceImpl.getAllStories());
	}

	@Test(expected = EntityNotFoundException.class)
	public void getAllStoriesException() throws Exception {
		when(storiesRepository.findAll()).thenReturn(TestUtils.listStoriesModelNull());
		Mockito.when(storiesServiceImpl.getAllStories())
				.thenThrow(new EntityNotFoundException(StoriesApiTestsConstants.messageStories,StoriesApiTestsConstants.path));
	}

	@Test
	public void updateStory() throws Exception {
		when(usersRepository.existsById(unitTestProperties.getModelAssigneeId())).thenReturn(StoriesApiTestsConstants.booleanTrue);
		when(storiesRepository.findById(unitTestProperties.getModelId())).thenReturn(Optional.of(TestUtils.getStoryModel()));
		when(sprintsClient.existsSprintById(unitTestProperties.getSprintClientId())).thenReturn(StoriesApiTestsConstants.booleanTrue);
		when(storiesRepository.existsById(unitTestProperties.getModelId())).thenReturn(StoriesApiTestsConstants.booleanTrue);
		when(mapperFacade.map(testUtils.getStoryDomain(), StoryModel.class)).thenReturn(testUtils.getStoryModel());
		storiesServiceImpl.updateStory(testUtils.getStoryDomain(), unitTestProperties.getModelId());
	}

	@Test(expected = EntityNotFoundException.class)
	public void updateUserException() throws Exception {
		when(usersRepository.existsById(unitTestProperties.getModelAssigneeId())).thenReturn(StoriesApiTestsConstants.booleanFalse);
		storiesServiceImpl.updateStory(testUtils.getStoryDomain(), unitTestProperties.getUrlId());
	}
	
	@Test(expected = EntityNotFoundException.class)
	public void updateStorykSpecialChar() throws Exception {
		when(storiesServiceImpl.updateStory(testUtils.getStoryDomainSpecialChar(), unitTestProperties.getDomainId())).thenThrow(new EntityNotFoundException("The next field have special characters: Status", "", "/stories/"));
		storiesServiceImpl.updateStory(testUtils.getStoryDomainSpecialChar(), unitTestProperties.getDomainId());
	}
	
	@Test(expected = EntityNotFoundException.class)
	public void updateStorySpecialsChars() throws Exception {
		when(storiesServiceImpl.updateStory(testUtils.getStoryDomainSpecialsChars(), unitTestProperties.getDomainId())).thenThrow(new EntityNotFoundException("The next field have special characters: Status, assignee", "", "/stories/"));
		storiesServiceImpl.updateStory(testUtils.getStoryDomainSpecialsChars(), unitTestProperties.getDomainId());
	}
	
	@Test(expected = EntityNotFoundException.class)
	public void updateStorySprintException() throws Exception {
		when(usersRepository.existsById(unitTestProperties.getModelAssigneeId())).thenReturn(StoriesApiTestsConstants.booleanTrue);
		when(sprintsClient.existsSprintById(unitTestProperties.getSprintClientId())).thenReturn(StoriesApiTestsConstants.booleanFalse);
		storiesServiceImpl.updateStory(testUtils.getStoryDomain(), unitTestProperties.getUrlId());
	}

	@Test(expected = EntityNotFoundException.class)
	public void updateStoryIdException() throws Exception {
		when(usersRepository.existsById(unitTestProperties.getModelAssigneeId())).thenReturn(StoriesApiTestsConstants.booleanTrue);
		when(sprintsClient.existsSprintById(unitTestProperties.getSprintClientId())).thenReturn(StoriesApiTestsConstants.booleanTrue);
		when(storiesRepository.existsById(unitTestProperties.getModelId())).thenReturn(StoriesApiTestsConstants.booleanFalse);
		storiesServiceImpl.updateStory(testUtils.getStoryDomain(), unitTestProperties.getUrlId());
	}

	@Test(expected = EntityNotFoundException.class)
	public void updateStoryStatusException() throws Exception {
		storiesServiceImpl.storyDomain = TestUtils.getStoryDomain();
		storiesServiceImpl.storyDomain.setStatus("incorrect");
		storiesServiceImpl.storyModel = TestUtils.getStoryModel();
		storiesServiceImpl.storyModel.setStatus("incorrect");
		when(usersRepository.existsById(storiesServiceImpl.storyDomain.getAssignee_id())).thenReturn(StoriesApiTestsConstants.booleanTrue);
		when(sprintsClient.existsSprintById(storiesServiceImpl.storyDomain.getSprint_id())).thenReturn(StoriesApiTestsConstants.booleanTrue);
		when(storiesRepository.existsById(unitTestProperties.getModelId())).thenReturn(StoriesApiTestsConstants.booleanTrue);
		when(mapperFacade.map(storiesServiceImpl.storyDomain, StoryModel.class))
				.thenReturn(storiesServiceImpl.storyModel);
		when(storiesServiceImpl.updateStory(storiesServiceImpl.storyDomain, storiesServiceImpl.storyModel.get_id()))
				.thenThrow(new EntityNotFoundException(
						StoriesApiTestsConstants.messageStatusInvalid,
						StoriesApiTestsConstants.varEmpty, 
						StoriesApiTestsConstants.path));
		storiesServiceImpl.createStory(storiesServiceImpl.storyDomain);
	}

	@Test(expected = EntityNotFoundException.class)
	public void updateException() throws Exception {
		when(storiesServiceImpl.updateStory(storiesServiceImpl.storyDomain, testUtils.getStoryModel().get_id()))
				.thenThrow(new EntityNotFoundException(StoriesApiTestsConstants.messageStory, StoriesApiTestsConstants.path));
	}

	@Test
	public void deleteStory() throws Exception {
		when(storiesRepository.existsById(unitTestProperties.getUrlId())).thenReturn(StoriesApiTestsConstants.booleanTrue);
		Mockito.doNothing().when(storiesRepository).deleteById(unitTestProperties.getUrlId());
		storiesServiceImpl.deleteStory(unitTestProperties.getUrlId());
	}

	@Test(expected = EntityNotFoundException.class)
	public void deleteStoryException() throws Exception {
		when(storiesRepository.existsById(unitTestProperties.getUrlId())).thenReturn(StoriesApiTestsConstants.booleanFalse);
		storiesServiceImpl.deleteStory(unitTestProperties.getUrlId());
	}
	
	@Test
	public void deleteTask() throws Exception {
		storiesServiceImpl.storyModel = TestUtils.getStoryTaskModel();
		Mockito.when(storiesRepository.existsById(unitTestProperties.getModelId())).thenReturn(StoriesApiTestsConstants.booleanTrue);
		when(storiesRepository.findById(unitTestProperties.getModelId())).thenReturn(Optional.of(storiesServiceImpl.storyModel));
		storiesServiceImpl.deleteTask(storiesServiceImpl.storyModel.get_id(), storiesServiceImpl.storyModel.getTasks().get(0).get_id());
	}
	
	@Test(expected = EntityNotFoundException.class)
	public void deleteTaskNotFoundException() throws Exception {
		storiesServiceImpl.storyModel = TestUtils.getStoryTaskModel();
		Mockito.when(storiesRepository.existsById(unitTestProperties.getModelId())).thenReturn(StoriesApiTestsConstants.booleanTrue);
		when(storiesRepository.findById(unitTestProperties.getModelId())).thenReturn(Optional.of(storiesServiceImpl.storyModel));
		storiesServiceImpl.deleteTask(storiesServiceImpl.storyModel.get_id(), storiesServiceImpl.storyModel.getTasks().get(0).get_id()+StoriesApiTestsConstants.plusError);
	}
	
	@Test(expected = EntityNotFoundException.class)
	public void deleteTaskException() throws Exception {
		when(storiesRepository.existsById(unitTestProperties.getUrlId())).thenReturn(StoriesApiTestsConstants.booleanFalse);
		storiesServiceImpl.deleteTask(unitTestProperties.getUrlId(), storiesServiceImpl.storyModel.get_id());
	}
	
	@Test(expected = EntityNotFoundException.class)
	public void deleteTaskNullException() throws Exception {
		storiesServiceImpl.storyModel = TestUtils.getStoryTaskModel();
		List<TaskModel> task = new ArrayList<>();
		storiesServiceImpl.storyModel.setTasks(task);
		Mockito.when(storiesRepository.existsById(unitTestProperties.getModelId())).thenReturn(StoriesApiTestsConstants.booleanTrue);
		when(storiesRepository.findById(unitTestProperties.getModelId())).thenReturn(Optional.of(storiesServiceImpl.storyModel));
		storiesServiceImpl.deleteTask(storiesServiceImpl.storyModel.get_id(), StoriesApiTestsConstants.varEmpty);
	}
	
	@Test
	public void createTask() throws Exception{
		TasksDomain taskDomain = TestUtils.getTasksDomain();
		taskDomain.setName("Taks");
		when(storiesRepository.existsById(unitTestProperties.getModelId())).thenReturn(StoriesApiTestsConstants.booleanTrue);
		when(usersRepository.existsById(taskDomain.getAssignee())).thenReturn(StoriesApiTestsConstants.booleanTrue);
		when(storiesRepository.findById(unitTestProperties.getModelId()))
		.thenReturn(java.util.Optional.of(TestUtils.getStoryModel()));
		when(mapperFacade.map(taskDomain, TaskModel.class)).thenReturn(TestUtils.getTasksModel());
		when(storiesRepository.save(TestUtils.getStoryModel())).thenReturn(TestUtils.getStoryModel());
		storiesServiceImpl.createTask(taskDomain, unitTestProperties.getModelId());
	}
	
	@Test(expected = EntityNotFoundException.class)
	public void createTaskSpecialChar() throws Exception {
		when(storiesServiceImpl.createTask(testUtils.getTaskDomainSpecialChar(), unitTestProperties.getModelId())).thenThrow(new EntityNotFoundException("The next field have special characters: Status", "", "/stories/"));
		storiesServiceImpl.createTask(testUtils.getTaskDomainSpecialChar(), unitTestProperties.getModelId());
	}
	
	@Test(expected = EntityNotFoundException.class)
	public void createTaskSpecialsChars() throws Exception {
		when(storiesServiceImpl.createTask(testUtils.getTaskDomainSpecialsChars(), unitTestProperties.getModelId())).thenThrow(new EntityNotFoundException("The next field have special characters: Status, assignee", "", "/stories/"));
		storiesServiceImpl.createTask(testUtils.getTaskDomainSpecialsChars(), unitTestProperties.getModelId());
	}
	
	@Test(expected = EntityNotFoundException.class)
	public void createTaskStoryExistException() throws Exception{
		when(storiesRepository.existsById(unitTestProperties.getModelId())).thenReturn(StoriesApiTestsConstants.booleanFalse);
		when(storiesServiceImpl.createTask(TestUtils.getTasksDomain(), unitTestProperties.getModelId())).
			thenThrow(new EntityNotFoundException(StoriesApiTestsConstants.messageStory, HttpStatus.CONFLICT,StoriesApiTestsConstants.path));
	}
	
	@Test(expected = EntityNotFoundException.class)
	public void createTaskNameException() throws Exception{
		when(storiesRepository.existsById(unitTestProperties.getModelId())).thenReturn(StoriesApiTestsConstants.booleanTrue);
		when(storiesServiceImpl.createTask(TestUtils.getTasksDomain(), unitTestProperties.getModelId())).
		thenThrow(new EntityNotFoundException(StoriesApiTestsConstants.messageName,StoriesApiTestsConstants.numbreError,StoriesApiTestsConstants.path));
	}
	
	@Test(expected = EntityNotFoundException.class)
	public void createTaskUserExistException() throws Exception{
		TasksDomain taskDomain = TestUtils.getTasksDomain();
		taskDomain.setName("Taks");
		taskDomain.setAssignee("ss");
		when(storiesRepository.existsById(unitTestProperties.getModelId())).thenReturn(StoriesApiTestsConstants.booleanTrue);
		when(usersRepository.existsById(TestUtils.getDummyTasksDomain().getAssignee())).thenReturn(StoriesApiTestsConstants.booleanFalse);
		when(storiesServiceImpl.createTask(taskDomain, unitTestProperties.getModelId())).
			thenThrow(new EntityNotFoundException(StoriesApiTestsConstants.messageStory, HttpStatus.CONFLICT,StoriesApiTestsConstants.path));
	}
	
	@Test(expected = EntityNotFoundException.class)
	public void createTaskStatusException() throws Exception{
		TasksDomain taskDomain = TestUtils.getTasksDomain();
		taskDomain.setName("Taks");
		taskDomain.setStatus("error");
		when(storiesRepository.existsById(unitTestProperties.getModelId())).thenReturn(StoriesApiTestsConstants.booleanTrue);
		when(usersRepository.existsById(TestUtils.getDummyTasksDomain().getAssignee())).thenReturn(StoriesApiTestsConstants.booleanTrue);
		when(storiesServiceImpl.createTask(taskDomain, unitTestProperties.getModelId())).
			thenThrow(new EntityNotFoundException(
					StoriesApiTestsConstants.messageStatusInvalid,
					StoriesApiTestsConstants.numbreError,
					StoriesApiTestsConstants.path));
	}

	@Test
	public void createStory() throws Exception {
		when(mapperFacade.map(TestUtils.getStoryDomain(), StoryModel.class)).thenReturn(testUtils.getStoryModel());
		when(usersRepository.existsById(unitTestProperties.getModelAssigneeId())).thenReturn(StoriesApiTestsConstants.booleanTrue);
		when(sprintsClient.existsSprintById(unitTestProperties.getSprintClientId())).thenReturn(StoriesApiTestsConstants.booleanTrue);
		when(storiesRepository.save(TestUtils.getStoryModel())).thenReturn(testUtils.getStoryModel());
		assertEquals(unitTestProperties.getUrlId(), storiesServiceImpl.createStory(TestUtils.getStoryDomain()));
	}

	@Test(expected = EntityNotFoundException.class)
	public void createStoryException() throws Exception {
		storiesServiceImpl.storyDomain = TestUtils.getStoryDomain();
		storiesServiceImpl.storyDomain.setStatus("incorrect");
		storiesServiceImpl.storyModel = TestUtils.getStoryModel();
		storiesServiceImpl.storyModel.setStatus("incorrect");
		when(usersRepository.existsById(storiesServiceImpl.storyDomain.getAssignee_id())).thenReturn(StoriesApiTestsConstants.booleanTrue);
		when(sprintsClient.existsSprintById(storiesServiceImpl.storyDomain.getSprint_id())).thenReturn(StoriesApiTestsConstants.booleanTrue);
		when(mapperFacade.map(storiesServiceImpl.storyDomain, StoryModel.class))
				.thenReturn(storiesServiceImpl.storyModel);
		when(storiesServiceImpl.createStory(storiesServiceImpl.storyDomain)).thenThrow(new EntityNotFoundException(
				StoriesApiTestsConstants.messageStatusInvalid,
				StoriesApiTestsConstants.varEmpty, 
				StoriesApiTestsConstants.path));
		storiesServiceImpl.createStory(storiesServiceImpl.storyDomain);
	}

	@Test(expected = EntityNotFoundException.class)
	public void createStorySprintIdException() throws Exception {
		storiesServiceImpl.storyDomain = TestUtils.getStoryDomain();
		storiesServiceImpl.storyDomain.setSprint_id("incorrect");
		storiesServiceImpl.storyModel = TestUtils.getStoryModel();
		storiesServiceImpl.storyModel.setSprint_id("incorrect");
		when(usersRepository.existsById(storiesServiceImpl.storyDomain.getAssignee_id())).thenReturn(StoriesApiTestsConstants.booleanTrue);
		when(mapperFacade.map(storiesServiceImpl.storyDomain, StoryModel.class))
				.thenReturn(storiesServiceImpl.storyModel);
		when(storiesServiceImpl.createStory(storiesServiceImpl.storyDomain))
				.thenThrow(new EntityNotFoundException(StoriesApiTestsConstants.messageSprintId, StoriesApiTestsConstants.path));
		storiesServiceImpl.createStory(storiesServiceImpl.storyDomain);
	}

	@Test(expected = EntityNotFoundException.class)
	public void createStoryNameInvalid() throws Exception {
		storiesServiceImpl.storyDomain = TestUtils.getStoryDomain();
		storiesServiceImpl.storyDomain.setName("");
		storiesServiceImpl.storyModel = TestUtils.getStoryModel();
		storiesServiceImpl.storyModel.setName("");
		when(usersRepository.existsById(storiesServiceImpl.storyDomain.getAssignee_id())).thenReturn(StoriesApiTestsConstants.booleanTrue);
		when(mapperFacade.map(storiesServiceImpl.storyDomain, StoryModel.class))
				.thenReturn(storiesServiceImpl.storyModel);
		when(storiesServiceImpl.createStory(storiesServiceImpl.storyDomain)).thenThrow(new EntityNotFoundException(
				StoriesApiTestsConstants.messageName,
				StoriesApiTestsConstants.varEmpty,
				StoriesApiTestsConstants.path));
	}

	@Test(expected = EntityNotFoundException.class)
	public void createStoryStatusInvalid() throws Exception {
		storiesServiceImpl.storyDomain = TestUtils.getStoryDomain();
		storiesServiceImpl.storyDomain.setStatus("");
		storiesServiceImpl.storyModel = TestUtils.getStoryModel();
		storiesServiceImpl.storyModel.setStatus("");
		when(usersRepository.existsById(storiesServiceImpl.storyDomain.getAssignee_id())).thenReturn(StoriesApiTestsConstants.booleanTrue);
		when(mapperFacade.map(storiesServiceImpl.storyDomain, StoryModel.class))
				.thenReturn(storiesServiceImpl.storyModel);
		when(storiesServiceImpl.createStory(storiesServiceImpl.storyDomain)).thenThrow(new EntityNotFoundException(
				StoriesApiTestsConstants.messageName, 
				StoriesApiTestsConstants.varEmpty, 
				StoriesApiTestsConstants.path));
	}

	@Test(expected = EntityNotFoundException.class)
	public void createStartDateNull() throws Exception {
		storiesServiceImpl.storyDomain = TestUtils.getStoryDomain();
		storiesServiceImpl.storyDomain.setStart_date(null);
		storiesServiceImpl.storyModel = TestUtils.getStoryModel();
		storiesServiceImpl.storyModel.setStart_date(null);
		when(usersRepository.existsById(storiesServiceImpl.storyDomain.getAssignee_id())).thenReturn(StoriesApiTestsConstants.booleanTrue);
		when(mapperFacade.map(storiesServiceImpl.storyDomain, StoryModel.class))
				.thenReturn(storiesServiceImpl.storyModel);
		when(storiesServiceImpl.createStory(storiesServiceImpl.storyDomain)).thenThrow(new EntityNotFoundException(
				StoriesApiTestsConstants.messageName, 
				StoriesApiTestsConstants.varEmpty, 
				StoriesApiTestsConstants.path));
	}

	@Test(expected = EntityNotFoundException.class)
	public void createPointsProggresNegative() throws Exception {
		storiesServiceImpl.storyDomain = TestUtils.getStoryDomain();
		storiesServiceImpl.storyDomain.setPoints(-1);
		storiesServiceImpl.storyDomain.setProgress(-1);
		storiesServiceImpl.storyModel = TestUtils.getStoryModel();
		storiesServiceImpl.storyModel.setPoints(-1);
		storiesServiceImpl.storyModel.setProgress(-1);
		when(usersRepository.existsById(storiesServiceImpl.storyDomain.getAssignee_id())).thenReturn(StoriesApiTestsConstants.booleanTrue);
		when(sprintsClient.existsSprintById(storiesServiceImpl.storyDomain.getSprint_id())).thenReturn(StoriesApiTestsConstants.booleanTrue);
		when(mapperFacade.map(storiesServiceImpl.storyDomain, StoryModel.class))
				.thenReturn(storiesServiceImpl.storyModel);
		when(storiesServiceImpl.createStory(storiesServiceImpl.storyDomain)).thenThrow(new EntityNotFoundException(
				StoriesApiTestsConstants.messageName, 
				StoriesApiTestsConstants.varEmpty, 
				StoriesApiTestsConstants.path));
	}

	@Test(expected = EntityNotFoundException.class)
	public void createPointsProgressInvalid() throws Exception {
		storiesServiceImpl.storyDomain = TestUtils.getStoryDomain();
		storiesServiceImpl.storyDomain.setPoints(123);
		storiesServiceImpl.storyDomain.setProgress(123);
		storiesServiceImpl.storyModel = TestUtils.getStoryModel();
		storiesServiceImpl.storyModel.setPoints(123);
		storiesServiceImpl.storyModel.setProgress(123);
		when(usersRepository.existsById(storiesServiceImpl.storyDomain.getAssignee_id())).thenReturn(StoriesApiTestsConstants.booleanTrue);
		when(sprintsClient.existsSprintById(storiesServiceImpl.storyDomain.getSprint_id())).thenReturn(StoriesApiTestsConstants.booleanTrue);
		when(mapperFacade.map(storiesServiceImpl.storyDomain, StoryModel.class))
				.thenReturn(storiesServiceImpl.storyModel);
		when(storiesServiceImpl.createStory(storiesServiceImpl.storyDomain)).thenThrow(new EntityNotFoundException(
				StoriesApiTestsConstants.messageName, 
				StoriesApiTestsConstants.varEmpty, 
				StoriesApiTestsConstants.path));
	}
	
	@Test 
	public void getTasksByStory() throws Exception {
		AggregationResults <TasksDomain> aggregationResultsMock = Mockito.mock(AggregationResults.class);
        Aggregation aggregateMock = Mockito.mock(Aggregation.class);
        
        when(storiesRepository.existsById(unitTestProperties.getUrlId())).thenReturn(StoriesApiTestsConstants.booleanTrue);
        Mockito.doReturn(aggregationResultsMock).when(mongoTemplate)
        .aggregate(Mockito.any(Aggregation.class), Mockito.eq("stories"), Mockito.eq(TasksDomain.class));        
        Mockito.doReturn(testUtils.getTasksDomainList()).when(aggregationResultsMock).getMappedResults();        
        when(storiesCustomRepository.getTasksByStory(unitTestProperties.getUrlId())).thenReturn(aggregationResultsMock);        
       when(storiesServiceImpl.getTasksByStory(unitTestProperties.getUrlId()))
       .thenReturn(testUtils.getTasksDomainList());
        
    assertEquals(testUtils.getTasksDomainList(), storiesServiceImpl.getTasksByStory(unitTestProperties.getUrlId()));
	}
	
	@Test(expected = EntityNotFoundException.class)
	public void getTasksByStoryFail() throws Exception {
		when(storiesRepository.existsById(unitTestProperties.getUrlId()))
        .thenReturn(StoriesApiTestsConstants.booleanFalse);
		
		Mockito.when(storiesServiceImpl.getTasksByStory(unitTestProperties.getUrlId()))
        .thenThrow(new EntityNotFoundException(StoriesApiTestsConstants.messageStory, StoriesApiTestsConstants.path + unitTestProperties.getUrlId()));
		
	}
	
	@Test
	public void getTaskById() throws Exception {
		AggregationResults<TaskModel> aggregationResultsMock = Mockito.mock(AggregationResults.class);
		when(storiesRepository.existsById(unitTestProperties.getUrlId()))
			.thenReturn(StoriesApiTestsConstants.booleanTrue);
		Mockito.doReturn(aggregationResultsMock).when(mongoTemplate)
			.aggregate(Mockito.any(Aggregation.class), Mockito.eq("stories"), Mockito.eq(TaskModel.class));
		Mockito.doReturn(testUtils.getTaskModelId()).when(aggregationResultsMock).getUniqueMappedResult();
		when(storiesCustomRepository.getTaskById(unitTestProperties.getUrlId(), unitTestProperties.getUrlId()))
			.thenReturn(aggregationResultsMock);	
		when(mapperFacade.map(testUtils.getTaskModelId(), TasksDomain.class))
			.thenReturn(testUtils.getDummyTasksDomain());
		when(storiesRepository.findByTasks__id(unitTestProperties.getUrlId()))
			.thenReturn(testUtils.getTaskModelId());
		assertEquals(testUtils.getDummyTasksDomain(), storiesServiceImpl.getTaskById(unitTestProperties.getUrlId(), unitTestProperties.getUrlId()));
	}
	
	@Test(expected = EntityNotFoundException.class)
	public void getTaskByIdNoStory() throws Exception {
		when(storiesRepository.existsById(unitTestProperties.getUrlId()))
		.thenReturn(StoriesApiTestsConstants.booleanFalse);
		Mockito.when(storiesServiceImpl.getTaskById(unitTestProperties.getUrlId(), unitTestProperties.getUrlId()))
			.thenThrow(new EntityNotFoundException(StoriesApiTestsConstants.messageStory, StoriesApiTestsConstants.path + unitTestProperties.getUrlId()));
	}
	
	@Test(expected = EntityNotFoundException.class)
	public void getTaskByIdNoEquals() throws Exception {
		AggregationResults<TaskModel> aggregationResultsMock = Mockito.mock(AggregationResults.class);
		when(storiesRepository.existsById(unitTestProperties.getUrlId()))
			.thenReturn(StoriesApiTestsConstants.booleanTrue);
		Mockito.doReturn(aggregationResultsMock).when(mongoTemplate)
			.aggregate(Mockito.any(Aggregation.class), Mockito.eq("stories"), Mockito.eq(TaskModel.class));
		Mockito.doReturn(testUtils.getTaskModelId()).when(aggregationResultsMock).getUniqueMappedResult();
		when(storiesCustomRepository.getTaskById(unitTestProperties.getUrlId(), unitTestProperties.getUrlId()))
			.thenReturn(aggregationResultsMock);	
		when(mapperFacade.map(testUtils.getDummyTaskModel(), TasksDomain.class))
			.thenReturn(testUtils.getDummyTasksDomain());
		when(storiesRepository.findByTasks__id(unitTestProperties.getUrlId()))
			.thenReturn(testUtils.getDummyTaskModel());
		assertEquals(StoriesApiTestsConstants.specificId, testUtils.getDummyTaskModel().get_id());
		assertEquals(testUtils.getDummyTasksDomain(), storiesServiceImpl.getTaskById(unitTestProperties.getUrlId(), unitTestProperties.getUrlId()));
	}
	
	@Test(expected = EntityNotFoundException.class)
	public void getTaskByIdTry() throws Exception {
		AggregationResults<TaskModel> aggregationResultsMock = Mockito.mock(AggregationResults.class);
		when(storiesRepository.existsById(unitTestProperties.getUrlId()))
			.thenReturn(StoriesApiTestsConstants.booleanTrue);
		Mockito.doReturn(aggregationResultsMock).when(mongoTemplate)
			.aggregate(Mockito.any(Aggregation.class), Mockito.eq("stories"), Mockito.eq(TaskModel.class));
		Mockito.doReturn(testUtils.getTaskModelNull()).when(aggregationResultsMock).getUniqueMappedResult();
		when(storiesCustomRepository.getTaskById(unitTestProperties.getUrlId(), unitTestProperties.getUrlId()))
			.thenReturn(aggregationResultsMock);
		when(mapperFacade.map(testUtils.getDummyTaskModel(), TasksDomain.class))
			.thenReturn(testUtils.getDummyTasksDomain());
		assertEquals(testUtils.getDummyTasksDomain(), storiesServiceImpl.getTaskById(unitTestProperties.getUrlId(), unitTestProperties.getUrlId()));
	}
	
	@Test
	public void updateTaskbyIdHappyPath() throws Exception{
		when(storiesRepository.existsById(unitTestProperties.getModelId())).thenReturn(StoriesApiTestsConstants.booleanTrue);
		when(storiesRepository.findById(unitTestProperties.getModelId())).thenReturn(java.util.Optional.of(testUtils.getStoryTaskModel()));
		storiesServiceImpl.updateTaskById(testUtils.getUpdateTaskDomain(), unitTestProperties.getModelId(), StoriesApiTestsConstants.ValidPutTaskId);
	}
	
	@Test(expected = EntityNotFoundException.class)
	public void updateStorySpecialChar() throws Exception {
		when(storiesServiceImpl.updateTaskById(testUtils.getTaskDomainSpecialChar(), unitTestProperties.getModelId(), testUtils.getTaskModelId().get_id())).thenThrow(new EntityNotFoundException("The next field have special characters: Status", "", "/stories/"));
		storiesServiceImpl.updateTaskById(testUtils.getTaskDomainSpecialChar(), unitTestProperties.getModelId(), testUtils.getTaskModelId().get_id());
	}
	
	@Test(expected = EntityNotFoundException.class)
	public void updateTaskSpecialsChars() throws Exception {
		when(storiesServiceImpl.updateTaskById(testUtils.getTaskDomainSpecialsChars(), unitTestProperties.getModelId(), testUtils.getTaskModelId().get_id())).thenThrow(new EntityNotFoundException("The next field have special characters: Status, assignee", "", "/stories/"));
		storiesServiceImpl.updateTaskById(testUtils.getTaskDomainSpecialsChars(), unitTestProperties.getModelId(), testUtils.getTaskModelId().get_id());
	}
	
	@Test(expected = EntityNotFoundException.class)
	public void updateTaskByIdStoryNotFound() throws Exception{
		when(storiesRepository.existsById(unitTestProperties.getModelId())).thenReturn(StoriesApiTestsConstants.booleanFalse);
		storiesServiceImpl.updateTaskById(testUtils.getDummyTasksDomain(), unitTestProperties.getModelId(), StoriesApiTestsConstants.ValidPutTaskId);
	}
	
	@Test(expected = EntityNotFoundException.class)
	public void updateTaskByIdStatusValidation() throws Exception{
		when(storiesRepository.existsById(unitTestProperties.getModelId())).thenReturn(StoriesApiTestsConstants.booleanTrue);
		storiesServiceImpl.updateTaskById(testUtils.getUpdateTaskDomainWrongstatus(), unitTestProperties.getModelId(), StoriesApiTestsConstants.ValidPutTaskId);
	}
	
	@Test(expected = EntityNotFoundException.class)
	public void updateTaskbyIdInvalidAsignee() throws Exception{
		when(storiesRepository.existsById(unitTestProperties.getModelId())).thenReturn(StoriesApiTestsConstants.booleanTrue);
		when(usersRepository.existsById(testUtils.getUpdateTaskDomainAssignee().getAssignee())).thenReturn(StoriesApiTestsConstants.booleanFalse);
		storiesServiceImpl.updateTaskById(testUtils.getUpdateTaskDomainAssignee(), unitTestProperties.getModelId(), StoriesApiTestsConstants.ValidPutTaskId);
	}
	
	@Test(expected = EntityNotFoundException.class)
	public void updateTaskbyIdNameEmpty() throws Exception{
		when(storiesRepository.existsById(unitTestProperties.getModelId())).thenReturn(StoriesApiTestsConstants.booleanTrue);
		when(usersRepository.existsById(testUtils.getUpdateTaskDomainNameEmpty().getAssignee())).thenReturn(StoriesApiTestsConstants.booleanTrue);
		storiesServiceImpl.updateTaskById(testUtils.getUpdateTaskDomainNameEmpty(), unitTestProperties.getModelId(), StoriesApiTestsConstants.ValidPutTaskId);
	}
	
	@Test(expected = EntityNotFoundException.class)
	public void updateTaskbyIdInvalidTask() throws Exception{
		when(storiesRepository.existsById(unitTestProperties.getModelId())).thenReturn(StoriesApiTestsConstants.booleanTrue);
		when(storiesRepository.findById(unitTestProperties.getModelId())).thenReturn(java.util.Optional.of(testUtils.getStoryTaskModel()));
		storiesServiceImpl.updateTaskById(testUtils.getUpdateTaskDomain(), unitTestProperties.getModelId(), StoriesApiTestsConstants.InvalidId);
	}
}