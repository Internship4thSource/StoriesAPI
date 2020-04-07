package com.stories.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.stories.domain.StoryDomain;
import com.stories.exception.ApiError;
import com.stories.exception.EntityNotFoundException;
import com.stories.model.StoryModel;
import com.stories.repository.StoriesRepository;
import com.stories.repository.UsersRepository;
import com.stories.sprintsclient.SprintsClient;

import io.micrometer.core.instrument.util.StringUtils;
import ma.glasnost.orika.MapperFacade;

@Service
public class StoriesServiceImpl implements StoriesService {

	@Autowired
	StoriesRepository storiesRepository;

	@Autowired
	UsersRepository usersRepository;

	private static Logger logger = LogManager.getLogger();

	@Autowired
	private MapperFacade mapperFacade;

	String[] statusArray = { "Refining", "Ready to Work", "Working", "Testing", "Ready to Accept", "Accepted" };
	String[] DomainValidation = { "Sprint_id", "Technology", "Description", "Acceptance_criteria", "Points", "Progress",
			"Notes", "Comments", "Start_date","Due_date","Priority", "Assignee_id", "History" };
	int[] pointsArray = { 0, 1, 2, 3, 5 };
	StoryModel storyModel = new StoryModel();
	List<StoryModel> storiesModel = new ArrayList<StoryModel>();
	StoryDomain storyDomain = new StoryDomain();
	List<StoryDomain> storiesDomain = new ArrayList<>();

	@Autowired
	SprintsClient sprintClient;

	@Override
	public String createStory(StoryDomain storyDomain) throws Exception {
		
		storyDomain.setStart_date(startDateValidation(storyDomain.getStart_date()));
		
//		nameStatusNullValidation(storyDomain.getName(), storyDomain.getStatus());
		String []mensaggeDinamicValidation = dinamicValidation(storyDomain);
		
		if(StringUtils.isEmpty(mensaggeDinamicValidation[0])) {
			storyModel = mapperFacade.map(storyDomain, StoryModel.class);
			logger.debug("Creating story with the json : {}", storyModel);
			String id = nameValidation(storyModel).get_id();
			return id;
		}
		else {
			throw new EntityNotFoundException(mensaggeDinamicValidation[0], mensaggeDinamicValidation[1]);
		}
	}

	@Override
	public void deleteStory(String id) throws Exception {
		if (!storiesRepository.existsById(id)) {
			throw new EntityNotFoundException("Story with the given id was not found", HttpStatus.CONFLICT,
					"/stories/");
		} else
			logger.debug("Deleting story with the id: " + id);
		storiesRepository.deleteById(id);
	}

	@Override
	public StoryDomain updateStory(StoryDomain storyDomain, String id) throws Exception {
//		nameStatusNullValidation(storyDomain.getName(), storyDomain.getStatus());
		storyDomain.setStart_date(startDateValidation(storyDomain.getStart_date()));
		String []mensaggeDinamicValidation = dinamicValidation(storyDomain);
		
		if(StringUtils.isEmpty(mensaggeDinamicValidation[0])) {
			if (storiesRepository.existsById(id)) {
			storyModel = mapperFacade.map(storyDomain, StoryModel.class);
				storyModel.set_id(id);
				nameValidation(storyModel);
				storyDomain = mapperFacade.map(storyModel, StoryDomain.class);
				logger.debug("Updating story with the id: " + id + " - JSON : {}", storyDomain);
				return storyDomain;
			} else {
				throw new EntityNotFoundException("Story not found", "/stories/");
			}
		}
		else {
			throw new EntityNotFoundException(mensaggeDinamicValidation[0], mensaggeDinamicValidation[1]);
		}
	}

	@Override
	public StoryDomain getStoryById(String id) throws Exception {
		if (!storiesRepository.existsById(id))
			throw new EntityNotFoundException("Story not found", "/stories/");
		storyModel = storiesRepository.findById(id).get();
		storyDomain = mapperFacade.map(storyModel, StoryDomain.class);
		logger.debug("Getting story with the id: " + id + " - JSON : {}", storyDomain);
		return storyDomain;
	}

	@Override
	public List<StoryDomain> getAllStories() throws Exception {
		List<StoryModel> allStoriesModel = new ArrayList<>();
		List<StoryDomain> allStoriesDomain = new ArrayList<>();
		allStoriesModel = storiesRepository.findAll();
		if (allStoriesModel == null)
			throw new EntityNotFoundException("Stories not found", "/stories/");
		for (int i = 0; i < allStoriesModel.size(); i++) {
			allStoriesDomain.add(mapperFacade.map(allStoriesModel.get(i), StoryDomain.class));
		}
		logger.debug("Getting all stories - JSON : {}", allStoriesDomain);
		return allStoriesDomain;
	}

	private String statusValidation(String[] statusArray, String storyStatus){
		String validationStatus ="";
		if(!(Arrays.asList(statusArray).contains(storyStatus))) {
			validationStatus = "The Status field should be one of the following options: 'Refining' ,'Ready to Work', 'Working', 'Testing', 'Ready to Accept' or 'Accepted'.";	
		}
		
		if(StringUtils.isEmpty(validationStatus)) {
			return validationStatus;
		}
		else {
			return validationStatus + ", " ;	
		}
	}

	private StoryModel nameValidation(StoryModel storyModel) throws EntityNotFoundException {
		try {
			storiesRepository.save(storyModel);
			return storyModel;
		} catch (Exception e) {
			throw new EntityNotFoundException("There is a story with this name already.", "", "/stories/");
		}
	}

	private String userNullValidation(String assigneeId) {
		String validation = "";
		
		if (!usersRepository.existsById(assigneeId) && !(StringUtils.isEmpty(assigneeId))) {
			validation = "User assignee_id does not exist";
		}
		
		if(StringUtils.isEmpty(validation)) {
			return validation;
		}
		else {
			return validation + ", " ;	
		}
	}

	private String sprintNullValidation(String sprintId){
		String validation = "";
		if (StringUtils.isEmpty(sprintId)) {
		
		} else {
			if (!sprintClient.existsSprintById(sprintId))
				validation = "The id entered in the sprint_id field does not exist";
		}
		
		if(StringUtils.isEmpty(validation)) {
			return validation;
		}
		else {
			return validation + ", " ;	
		}
	}

	private String nameStatusNullValidation(String name, String status) {
		String validationNameStatus = "";
		
		if (StringUtils.isEmpty(name) && StringUtils.isEmpty(status)) {
			validationNameStatus = "The JSON format provided is invalid, please provide the required fields ('Name','Status').";
		} else if (StringUtils.isEmpty(name)) {
			validationNameStatus = "The JSON format provided is invalid, please provide the required field ('Name').";
		} else if (StringUtils.isEmpty(status)) {
			validationNameStatus = "The JSON format provided is invalid, please provide the required field ('Status').";
		}
		
		if(!StringUtils.isEmpty(validationNameStatus)) {
			return validationNameStatus;
		}
		else {
			return validationNameStatus;
		}
	}

	private LocalDate startDateValidation(LocalDate start_date) {
		if ((!(start_date == null || (StringUtils.isEmpty(start_date.toString()))))) {
			return start_date;
		} else {
			return LocalDate.now();
		}
	}
	
	private String proggressValidation(int progress) {
		String progressValidation = "";
		if (progress <0) {
			progressValidation = "The number entered in the progress field is a negative number";
		}

		if (progress > 100) {
			progressValidation = "The number entered in the progress field exceeds 100%";
		} 
		
		if(StringUtils.isEmpty(progressValidation)) {
			return progressValidation;
		}
		else {
			return progressValidation + ", " ;	
		}
	}

	private String pointsValidation(int points, int[] pointsArray) {
		String pointsValidation = "";
//		System.err.println(points);
		if (points <0) {
			pointsValidation = "The number entered in the points field is a negative number";
		}
		else {
			for (int i = 0; i < pointsArray.length; i++) {
				if (points == pointsArray[i]) {
					break;
				} else if (i == pointsArray.length - 1) {
					pointsValidation = "The number entered in the points field does not match a valid story point";
				}
			}
		}
		
		if(StringUtils.isEmpty(pointsValidation)) {
			return pointsValidation;
		}
		else {
			return pointsValidation + ", " ;	
		}
	}
	
	private String []dinamicValidation(StoryDomain storyDomain) {
		String []mensaggeDinamicValidation = {"",""};
		String validationRespons = "";
		String []validationPath = {"/Sprints/", "/StoryDomain/", "/Users/", "/stories/"};
		
		
		
		validationRespons = nameStatusNullValidation(storyDomain.getName(), storyDomain.getStatus());
		if(!StringUtils.isEmpty(validationRespons)) {
			mensaggeDinamicValidation[0] = mensaggeDinamicValidation[0] + validationRespons;
			mensaggeDinamicValidation[1] = mensaggeDinamicValidation[1] + "/stories/";
			return mensaggeDinamicValidation;
		}
		
		validationRespons = sprintNullValidation(storyDomain.getSprint_id());
		if(!StringUtils.isEmpty(validationRespons)) {
			mensaggeDinamicValidation[0] = mensaggeDinamicValidation[0] + validationRespons;
			mensaggeDinamicValidation[1] = mensaggeDinamicValidation[1] + "/stories/";
		}
		
		validationRespons = pointsValidation(storyDomain.getPoints(), pointsArray);
		if(!StringUtils.isEmpty(validationRespons)) {
			mensaggeDinamicValidation[0] = mensaggeDinamicValidation[0] + validationRespons;
			mensaggeDinamicValidation[1] = mensaggeDinamicValidation[1] + "/stories/";
		}
		
		validationRespons = proggressValidation(storyDomain.getProgress());
		if(!StringUtils.isEmpty(validationRespons)) {
			mensaggeDinamicValidation[0] = mensaggeDinamicValidation[0] + validationRespons;
			mensaggeDinamicValidation[1] = mensaggeDinamicValidation[1] + "/stories/";
		}
		
		validationRespons = userNullValidation(storyDomain.getAssignee_id());
		if(!StringUtils.isEmpty(validationRespons)) {
			mensaggeDinamicValidation[0] = mensaggeDinamicValidation[0] + validationRespons;
			mensaggeDinamicValidation[1] = mensaggeDinamicValidation[1] + "/stories/";
		}
		
		validationRespons = statusValidation(statusArray, storyDomain.getStatus());
		if(!StringUtils.isEmpty(validationRespons)) {
			mensaggeDinamicValidation[0] = mensaggeDinamicValidation[0] + validationRespons;
			mensaggeDinamicValidation[1] = mensaggeDinamicValidation[1] + "/stories/";
		}
		
		validationRespons = "";
		for (int i = 0; i < validationPath.length; i++) {
			if (mensaggeDinamicValidation[1].toString().indexOf(validationPath[i]) == -1) {

			} else {
				if (validationPath[i].equals(mensaggeDinamicValidation[1].toString().substring(mensaggeDinamicValidation[1].toString().indexOf(validationPath[i]),
						mensaggeDinamicValidation[1].toString().indexOf(validationPath[i]) + validationPath[i].length()))) {
					
					if(!(mensaggeDinamicValidation[1].toString().indexOf(validationPath[i]) == -1)) {
						if(StringUtils.isEmpty(validationRespons)) {
							validationRespons = validationPath[i];
						}
					}
				}
			}
		}
		
		mensaggeDinamicValidation[1] = validationRespons;
		return mensaggeDinamicValidation;
	}
}