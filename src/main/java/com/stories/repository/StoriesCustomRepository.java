package com.stories.repository;

import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import com.stories.domain.TasksDomain;

public interface StoriesCustomRepository {
	
	AggregationResults<TasksDomain> getTasksByStory(String id);
}
