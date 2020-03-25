package com.stories.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.stories.domain.StoryDomain;
import com.stories.exception.EntityNotFoundException;
import com.stories.model.StoryModel;
import com.stories.repository.StoriesRepository;

import ma.glasnost.orika.MapperFacade;

@Service
public class StoriesServiceImpl implements StoriesService {

	@Autowired
	StoriesRepository storiesRepository;

	@Autowired
	private MapperFacade mapperFacade;

	@Override
	public StoryDomain getStoryById(String id) throws Exception {
		StoryDomain story = new StoryDomain();
		if (!storiesRepository.existsById(id))
			throw new EntityNotFoundException("Story not found", StoryDomain.class);
		StoryModel storyModel = storiesRepository.findById(id).get();
		story = mapperFacade.map(storyModel, StoryDomain.class);
		return story;
	}
	
	@Override
	public List<StoryDomain> getAllStories() throws Exception {
		List<StoryModel> storiesModel = new ArrayList<StoryModel>();
		storiesModel = storiesRepository.findAll();
		List<StoryDomain> storiesDomain = new ArrayList<>();
		if (storiesModel == null)
			throw new EntityNotFoundException("Story not found", StoryDomain.class);
		for (int i = 0; i < storiesModel.size(); i++) {
			storiesDomain.add(mapperFacade.map(storiesModel.get(i), StoryDomain.class));
		}
		return storiesDomain;
	}
	
	@Override
	public String createStory(StoryDomain request) throws Exception {
		StoryModel storyModel = new StoryModel();
		storyModel = mapperFacade.map(request, StoryModel.class);
		String storystatus = storyModel.getStatus();
		String[] statusArray = { "Ready to Work", "Working", "Testing", "Ready to Accept", "Accepted" };
		boolean test = Arrays.asList(statusArray).contains(storystatus);

		if (test) {
			try {
				System.err.println("Creating story with the status indicated....");
				return storiesRepository.save(storyModel).get_id().toString();
			} catch (Exception e) {
				throw new EntityNotFoundException("There is a story with this name already", e.getMessage(),
						StoryDomain.class);
			}
		} else {
			throw new EntityNotFoundException("Status json state is invalid", "The status should be: Ready to Work, Working, Testing, Ready to Accept or Accepted." ,StoryDomain.class);
		}
	}

	@Override
	public void deleteStory(String id) throws Exception {
		if (!storiesRepository.existsById(id)) 
			throw new EntityNotFoundException("Story with the given id was not found", StoryModel.class);
		storiesRepository.deleteById(id);
	}

	public StoryDomain updateStory(StoryDomain storyDomain, String id) throws Exception {
		StoryModel storyModel = mapperFacade.map(storyDomain, StoryModel.class);
		if (!storiesRepository.existsById(id))
			throw new EntityNotFoundException("Story not found", StoryDomain.class);
		storyModel.set_id(id);
		storiesRepository.save(storyModel);
		storyDomain = mapperFacade.map(storyModel, StoryDomain.class);
		System.err.println(storyDomain);
		return storyDomain;
	}
}